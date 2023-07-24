package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

private const val TAG = FRAG_TRANS_ADD

@Suppress("DEPRECATION")
class TransactionAddFragment :
    Fragment(R.layout.fragment_transaction_add) {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel
    private val args: TransactionAddFragmentArgs by navArgs()

    private var mBudgetRule: BudgetRule? = null
    private var mFromAccount: Account? = null
    private var mToAccount: Account? = null
    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val timeFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_DATE, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel =
            mainActivity.transactionViewModel
        mainActivity.title = "Add a new Transaction"
        fillValues()

        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
            }
            etTransDate.setOnLongClickListener {
                chooseDate()
                false
            }
        }
    }

    private fun chooseBudgetRule() {
        val fragmentChain = TAG

        val direction =
            TransactionAddFragmentDirections
                .actionTransactionAddFragmentToBudgetRuleFragment(
                    null,
                    getCurTransaction(),
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun getCurTransaction(): TransactionDetailed {
        binding.apply {
            val curTransaction = Transactions(
                generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule?.ruleId ?: 0L,
                mToAccount?.accountId ?: 0L,
                mFromAccount?.accountId ?: 0L,
                if (etAmount.text.isNotEmpty()) {
                    etAmount.text.toString()
                        .trim()
                        .replace("$", "")
                        .replace(",", "")
                        .toDouble()
                } else {
                    0.0
                },
                transIsPending = false,
                transIsDeleted = false,
                transUpdateTime = timeFormatter.format(
                    Calendar.getInstance().time
                )
            )
            return TransactionDetailed(
                curTransaction,
                mBudgetRule,
                mToAccount,
                mFromAccount
            )
        }
    }

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etTransDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etTransDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the first date")
            datePickerDialog.show()
        }
    }

    private fun chooseFromAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment(
                null,
                getCurTransaction(),
                null,
                REQUEST_FROM_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment(
                null,
                getCurTransaction(),
                null,
                REQUEST_TO_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    /*private fun prepareBudgetRule(): BudgetRuleDetailed {
        val zToAccount = if (mToAccount != null) {
            mToAccount!!
        } else {
            null
        }
        val zFromAccount = if (mFromAccount != null) {
            mFromAccount!!
        } else {
            null
        }
        return BudgetRuleDetailed(
            mBudgetRule,
            zToAccount,
            zFromAccount
        )
    }*/

    private fun fillValues() {
        binding.apply {
            if (args.transaction != null) {
                if (args.transaction!!.transaction != null) {
                    etDescription.setText(
                        if (args.transaction!!.transaction!!.transName.isBlank()) {
                            if (args.transaction!!.budgetRule != null) {
                                args.transaction!!.budgetRule!!.budgetRuleName
                            } else {
                                ""
                            }
                        } else {
                            args.transaction!!.transaction!!.transName
                        }
                    )
                    etNote.setText(
                        args.transaction!!.transaction!!.transNote
                    )
                    etTransDate.setText(
                        args.transaction!!.transaction!!.transDate
                    )
                    etAmount.setText(
                        if (args.transaction!!.transaction!!.transAmount == 0.0) {
                            if (args.transaction!!.budgetRule != null) {
                                dollarFormat.format(
                                    args.transaction!!.budgetRule!!.budgetAmount
                                )
                            } else {
                                dollarFormat.format(
                                    0.0
                                )
                            }
                        } else {
                            dollarFormat.format(
                                args.transaction!!.transaction!!.transAmount
                            )
                        }
                    )
                }
                if (args.transaction!!.budgetRule != null) {
                    mBudgetRule = args.transaction!!.budgetRule!!
                    tvBudgetRule.text =
                        args.transaction!!.budgetRule!!.budgetRuleName
                }
                if (args.transaction!!.toAccount != null) {
                    mToAccount = args.transaction!!.toAccount!!
                    tvToAccount.text =
                        mToAccount!!.accountName
                }
                if (args.transaction!!.fromAccount != null) {
                    mFromAccount = args.transaction!!.fromAccount
                    tvFromAccount.text =
                        mFromAccount!!.accountName
                }

            } else {
                etTransDate.setText(
                    dateFormatter.format(
                        Calendar.getInstance().time
                    )
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
//        menu.add("new")
//        inflater.inflate(R.menu.settings_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveTransaction()
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun saveTransaction() {
        val mes = checkTransaction()
        if (mes == "Ok") {
            binding.apply {
                val amount =
                    etAmount.text.toString().trim()
                        .replace(",", "")
                        .replace("$", "")
                        .toDouble()
                val mTransaction = Transactions(
                    generateId(),
                    etTransDate.text.toString(),
                    etDescription.text.toString(),
                    etNote.text.toString(),
                    mBudgetRule!!.ruleId,
                    mToAccount!!.accountId,
                    mFromAccount!!.accountId,
                    amount,
                    transIsPending = false,
                    transIsDeleted = false,
                    transUpdateTime = timeFormatter.format(
                        Calendar.getInstance().time
                    )
                )
                transactionViewModel.insertTransaction(mTransaction)
                val direction =
                    TransactionAddFragmentDirections
                        .actionTransactionAddFragmentToTransactionViewFragment(
                            null,
                            null
                        )
                mView.findNavController().navigate(direction)
            }
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun generateId(): Long {
        var id =
            Random().nextInt(Int.MAX_VALUE).toLong()
        id = if (Random().nextBoolean()) -id
        else id
        return id
    }

    private fun checkTransaction(): String {
        binding.apply {
            val amount = etAmount.text.toString().trim()
                .replace("$", "")
                .replace(",", "")
                .toDouble()
            val errorMes =
                if (etDescription.text.isNullOrBlank()
                ) {
                    "     Error!!\n" +
                            "Please enter a description"
                } else if (mToAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (mFromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount for this transaction"
                } else if (mBudgetRule == null) {
                    if (saveWithoutBudget()) {
                        "Ok"
                    } else {
                        "Choose a Budget Rule"
                    }
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

    private fun saveWithoutBudget(): Boolean {
        var bool = false
        AlertDialog.Builder(activity).apply {
            setMessage(
                "There is no Budget Rule!" +
                        "Budget Rules are used to update the budget."
            )
            setPositiveButton("Save anyway") { _, _ ->
                bool = true
            }
            setNegativeButton("Retry", null)
        }.create().show()
        return bool
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}