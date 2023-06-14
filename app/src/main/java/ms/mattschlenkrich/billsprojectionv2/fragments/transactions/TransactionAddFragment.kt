package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

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
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
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
    private var mFromAccount: AccountWithType? = null
    private var mToAccount: AccountWithType? = null
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
                args.transaction,
                prepareBudgetRule(),
                REQUEST_FROM_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"

        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment(
                args.transaction,
                prepareBudgetRule(),
                REQUEST_TO_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    private fun prepareBudgetRule(): BudgetRuleDetailed {
        val zToAccount = if (mToAccount != null) {
            mToAccount!!.account
        } else {
            null
        }
        val zFromAccount = if (mFromAccount != null) {
            mFromAccount!!.account
        } else {
            null
        }
        return BudgetRuleDetailed(
            mBudgetRule,
            zToAccount,
            zFromAccount
        )
    }

    private fun fillValues() {
        binding.apply {
            if (args.transaction != null) {
                if (args.transaction != null) {
                    etDescription.setText(
                        args.transaction!!.transaction!!.transName
                    )
                    etNote.setText(
                        args.transaction!!.transaction!!.transNote
                    )
                    etTransDate.setText(
                        args.transaction!!.transaction!!.transDate
                    )
                    etAmount.setText(
                        dollarFormat.format(
                            args.transaction!!.transaction!!.amount
                        )
                    )
                    if (args.transaction!!.budgetRule != null) {
                        mBudgetRule = args.transaction!!.budgetRule!!
                        tvBudgetRule.text =
                            args.transaction!!.budgetRule!!.budgetRuleName
                    }
                    if (args.transaction!!.toAccountWithType != null) {
                        mToAccount = args.transaction!!.toAccountWithType!!
                        tvToAccount.text =
                            args.transaction!!.toAccountWithType!!
                                .account.accountName
                    }
                    if (args.transaction!!.fromAccountWithType != null) {
                        mFromAccount = args.transaction!!.fromAccountWithType
                        tvFromAccount.text =
                            args.transaction!!.fromAccountWithType!!
                                .account.accountName
                    }
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
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
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
            var id =
                Random().nextInt(Int.MAX_VALUE).toLong()
            id = if (Random().nextBoolean()) -id
            else id
            val bRuleId = mBudgetRule!!.ruleId
            val toAccountId = mToAccount!!.account.accountId
            val fromAccountId = mFromAccount!!.account.accountId
            val updateTime = timeFormatter.format(
                Calendar.getInstance().time
            )
            binding.apply {
                val amount =
                    etAmount.text.toString().trim()
                        .replace(",", "")
                        .replace("$", "")
                        .toDouble()
                val mTransaction = Transactions(
                    id,
                    etTransDate.text.toString(),
                    etDescription.text.toString(),
                    etNote.text.toString(),
                    bRuleId,
                    toAccountId,
                    fromAccountId,
                    amount,
                    false,
                    updateTime
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

    private fun checkTransaction(): String {
        binding.apply {
            val errorMes =
                if (etDescription.text.isNullOrBlank()
                ) {
                    "     Error!!\n" +
                            "Please enter a description"
                } else if (mBudgetRule == null
                ) {
                    "     Error!!\n" +
                            "There needs to be a budget rule " +
                            "to add this transaction to budget reports."
                } else if (mToAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (mFromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etAmount.text.isNullOrEmpty()
                ) {
                    "     Error!!\n" +
                            "Please enter a budget amount (including zero)"
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}