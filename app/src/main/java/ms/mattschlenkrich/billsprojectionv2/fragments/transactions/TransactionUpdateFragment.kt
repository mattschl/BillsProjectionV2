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
import ms.mattschlenkrich.billsprojectionv2.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = FRAG_TRANS_UPDATE

@Suppress("DEPRECATION")
class TransactionUpdateFragment :
    Fragment(R.layout.fragment_transaction_update) {

    private var _binding: FragmentTransactionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var mView: View
    private val args: TransactionUpdateFragmentArgs by navArgs()

    private var mTransaction: Transactions? = null
    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null

    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val timeFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
//    private val dateFormatter: SimpleDateFormat =
//        SimpleDateFormat(SQLITE_DATE, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionUpdateBinding.inflate(
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
        mainActivity.title = "Update this Transaction"
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
            fabUpdateDone.setOnClickListener {
                updateTransaction()
            }
        }
    }

    private fun updateTransaction() {
        val mes = checkTransaction()
        binding.apply {
            if (mes == "Ok") {
                val transaction = Transactions(
                    args.transaction!!.transaction!!.transId,
                    etTransDate.text.toString(),
                    etDescription.text.toString(),
                    etNote.text.toString(),
                    mBudgetRule!!.ruleId,
                    mToAccount!!.accountId,
                    mFromAccount!!.accountId,
                    etAmount.text.toString().trim()
                        .replace("$", "")
                        .replace(",", "")
                        .toDouble(),
                    false,
                    timeFormatter.format(
                        Calendar.getInstance().time
                    )
                )
//                val fragmentChain = "${args.callingFragments}, $TAG"
                transactionViewModel.updateTransaction(
                    transaction
                )
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment(
                            getCurTransDetailed(),
                            TAG
                        )
                mView.findNavController().navigate(direction)
            } else {
                Toast.makeText(
                    mView.context,
                    mes,
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun checkTransaction(): String {
        binding.apply {
            val amount = etAmount.text.toString().trim()
                .replace("$", "")
                .replace(",", "")
                .toDouble()
            val errorMes =
                if (etDescription.text.isNullOrBlank()) {
                    "     ERROR!!\n" +
                            "Please enter a description."
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
                            "Please enter an amount fr this transaction"
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

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etTransDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etTransDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the final date")
            datePickerDialog.show()
        }
    }

    private fun chooseFromAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment(
                    getCurTransDetailed(),
                    null,
                    REQUEST_FROM_ACCOUNT,
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment(
                    getCurTransDetailed(),
                    null,
                    REQUEST_TO_ACCOUNT,
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetRuleFragment(
                    getCurTransDetailed(),
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun getCurTransDetailed(): TransactionDetailed {
        binding.apply {
            val curTransaction = Transactions(
                args.transaction!!.transaction!!.transId,
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule!!.ruleId,
                mToAccount!!.accountId,
                mFromAccount!!.accountId,
                etAmount.text.toString().trim()
                    .replace("$", "")
                    .replace(",", "")
                    .toDouble(),
                false,
                timeFormatter.format(
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

    private fun fillValues() {
        binding.apply {
            if (args.transaction != null) {
                if (args.transaction!!.transaction != null) {
                    mTransaction =
                        args.transaction!!.transaction
                    etTransDate.setText(
                        mTransaction!!.transDate
                    )
                    etAmount.setText(
                        dollarFormat.format(
                            mTransaction!!.transAmount
                        )
                    )
                    etDescription.setText(
                        mTransaction!!.transName
                    )
                    etNote.setText(
                        mTransaction!!.transNote
                    )
                }
                if (args.transaction!!.budgetRule != null) {
                    mBudgetRule =
                        args.transaction!!.budgetRule
                    tvBudgetRule.text =
                        mBudgetRule!!.budgetRuleName
                }
                if (args.transaction!!.toAccount != null) {
                    mToAccount =
                        args.transaction!!.toAccount
                    tvToAccount.text =
                        mToAccount!!.accountName
                }
                if (args.transaction!!.fromAccount != null) {
                    mFromAccount =
                        args.transaction!!.fromAccount
                    tvFromAccount.text =
                        mFromAccount!!.accountName
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.delete_menu, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete -> {
                deleteTransaction()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete this Transaction")
            setMessage("Are you sure you want to delete this Transaction?")
            setPositiveButton("Delete") { _, _ ->
                transactionViewModel.deleteTransaction(
                    mTransaction!!.transId,
                    timeFormatter.format(
                        Calendar.getInstance().time
                    )
                )
                val fragmentChain = args.callingFragments!!.replace(
                    FRAG_TRANS_UPDATE, ""
                )
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment(
                            null, fragmentChain
                        )
                mView.findNavController().navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}