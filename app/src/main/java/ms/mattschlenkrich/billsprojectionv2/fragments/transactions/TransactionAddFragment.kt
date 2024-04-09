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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_ADD

class TransactionAddFragment :
    Fragment(R.layout.fragment_transaction_add) {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel

    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    //    private var mBudgetRule: BudgetRule? = null
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel =
            mainActivity.transactionViewModel
        accountViewModel =
            mainActivity.accountViewModel
        mainActivity.title = "Add a new Transaction"
        createMenu()
        fillValues()
        createActions()
    }

    private fun createActions() {
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
            etAmount.setOnLongClickListener {
                gotoCalc()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b)
                    updateAmountDisplay()
            }
            etDescription.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            etNote.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            etTransDate.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            btnSplit.setOnClickListener {
                splitTransactions()
            }
        }
    }

    private fun splitTransactions() {
        mainViewModel.setSplitTransactionDetailed(null)
        if (mFromAccount != null &&
            nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0
        ) {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments() + ", " + TAG
            )
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            mView.findNavController().navigate(
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToTransactionSplitFragment()
            )
        }
    }

    private fun gotoCalc() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        mView.findNavController().navigate(
            TransactionAddFragmentDirections
                .actionTransactionAddFragmentToCalcFragment()
        )
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        menuItem.isEnabled = false
                        saveTransaction()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + "', " + TAG
        )
        mainViewModel.setTransactionDetailed(
            getTransactionDetailed()
        )
        mView.findNavController().navigate(
            TransactionAddFragmentDirections
                .actionTransactionAddFragmentToBudgetRuleFragment()
        )
    }

    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                nf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mainViewModel.getTransactionDetailed()?.budgetRule?.ruleId ?: 0L,
                mToAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                mFromAccount?.accountId ?: 0L,
                chkFromAccPending.isChecked,
                if (etAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                },
                transIsDeleted = false,
                transUpdateTime = df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateAmountDisplay() {
        binding.apply {
            btnSplit.isEnabled =
                etAmount.text.toString().isNotEmpty() &&
                        nf.getDoubleFromDollars(etAmount.text.toString()) > 0.0 &&
                        mFromAccount != null
            etAmount.setText(
                nf.displayDollars(
                    nf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                )
            )
        }
    }

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurTransaction(),
            mBudgetRule,
            mToAccount,
            mFromAccount
        )
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
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }


    private fun fillValues() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed() != null) {
                if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                    fillFromTransactionDetailed()
                }
                if (mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                    fillFromBudgetRule()
                }
                if (mainViewModel.getTransactionDetailed()!!.toAccount != null) {
                    fillFromToAccount()
                } else {
                    chkToAccPending.visibility = View.GONE
                }
                chkToAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
                if (mainViewModel.getTransactionDetailed()!!.fromAccount != null) {
                    fillFromFromAccount()
                } else {
                    chkFromAccPending.visibility = View.GONE
                }
                chkFromAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending

            } else {
                etTransDate.setText(df.getCurrentDateAsString())
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                updateAmountDisplay()
            }
        }
    }

    private fun fillFromFromAccount() {
        binding.apply {
            mFromAccount =
                mainViewModel.getTransactionDetailed()!!.fromAccount
            tvFromAccount.text =
                mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
                    )
                }
                mFromAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mFromAccountWithType!!.accountType!!.allowPending) {
                    chkFromAccPending.visibility = View.VISIBLE
                } else {
                    chkFromAccPending.visibility = View.GONE
                }
            }
        }
    }

    private fun fillFromToAccount() {
        binding.apply {
            mToAccount =
                mainViewModel.getTransactionDetailed()!!.toAccount
            tvToAccount.text =
                mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                } else {
                    chkToAccPending.visibility = View.GONE
                }
            }
        }
    }

    private fun fillFromBudgetRule() {
        binding.apply {
            mBudgetRule = mainViewModel.getTransactionDetailed()!!.budgetRule
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            if (mainViewModel.getTransactionDetailed()!!.toAccount == null &&
                mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId != 0L
            ) {
                fillToAccountFromBudgetRule()
            }
            if (mainViewModel.getTransactionDetailed()!!.fromAccount == null &&
                mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId != 0L
            ) {
                fillFromAccountFromBudgetRule()
            }
        }
    }

    private fun fillFromAccountFromBudgetRule() {
        binding.apply {
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccount(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId
                    )
                }
                mFromAccount = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                tvFromAccount.text = mFromAccount!!.accountName
                CoroutineScope(Dispatchers.IO).launch {
                    val acc = async {
                        accountViewModel.getAccountWithType(
                            mFromAccount!!.accountName
                        )
                    }
                    mFromAccountWithType = acc.await()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    if (mFromAccountWithType!!.accountType!!.allowPending) {
                        chkFromAccPending.visibility = View.VISIBLE
                    } else {
                        chkFromAccPending.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun fillToAccountFromBudgetRule() {
        binding.apply {
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccount(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                    )
                }
                mToAccount = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                tvToAccount.text = mToAccount!!.accountName
            }

            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                } else {
                    chkToAccPending.visibility = View.GONE
                }
            }
        }
    }

    private fun fillFromTransactionDetailed() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed()!!.budgetRule != null &&
                mainViewModel.getTransactionDetailed()!!.transaction!!.transName.isBlank()
            ) {
                etDescription.setText(
                    mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetRuleName
                )
            } else {
                etDescription.setText(
                    mainViewModel.getTransactionDetailed()!!.transaction?.transName ?: ""
                )
            }
            etNote.setText(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transNote
            )
            etTransDate.setText(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
            )
            etAmount.hint = "Budgeted " +
                    if (mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount == 0.0 &&
                        mainViewModel.getTransactionDetailed()!!.budgetRule != null
                    ) {
                        nf.displayDollars(
                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetAmount
                        )
                    } else {
                        nf.displayDollars(
                            mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                        )
                    }
            etAmount.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                    }
                )
            )
            mainViewModel.setTransferNum(0.0)
        }
    }

    private fun saveTransaction() {
        val mes = checkTransaction()
        if (mes == "Ok") {
            val mTransaction = getCurTransaction()
            transactionViewModel.insertTransaction(
                mTransaction
            )
            updateAccounts(mTransaction)
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun updateAccounts(mTransaction: Transactions): Boolean {
        if (!mTransaction.transToAccountPending) {
            updateToAccountBalanceOrOwing(mTransaction)
        }
        if (!mTransaction.transFromAccountPending) {
            updateFromAccountBalanceOrOwing(mTransaction)
        }
        gotoCallingFragment()
        return true
    }

    private fun updateFromAccountBalanceOrOwing(mTransaction: Transactions) {
        if (mFromAccountWithType!!.accountType!!.keepTotals) {
            transactionViewModel.updateAccountBalance(
                mFromAccountWithType!!.account.accountBalance -
                        mTransaction.transAmount,
                mFromAccount!!.accountId,
                df.getCurrentTimeAsString()
            )
        }
        if (mFromAccountWithType!!.accountType!!.tallyOwing) {
            transactionViewModel.updateAccountOwing(
                mFromAccountWithType!!.account.accountOwing +
                        mTransaction.transAmount,
                mFromAccount!!.accountId,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateToAccountBalanceOrOwing(mTransaction: Transactions) {
        if (mToAccountWithType!!.accountType!!.keepTotals) {
            transactionViewModel.updateAccountBalance(
                mToAccountWithType!!.account.accountBalance +
                        mTransaction.transAmount,
                mToAccount!!.accountId,
                df.getCurrentTimeAsString()
            )
        }
        if (mToAccountWithType!!.accountType!!.tallyOwing) {
            transactionViewModel.updateAccountOwing(
                mToAccountWithType!!.account.accountOwing -
                        mTransaction.transAmount,
                mToAccount!!.accountId,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoCallingFragment() {
        updateAmountDisplay()
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.setBudgetRuleDetailed(null)
        if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANSACTION_VIEW)
        ) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToTransactionViewFragment()
            mView.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_VIEW)) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToBudgetViewFragment()
            mView.findNavController().navigate(direction)
        }
    }

    private fun checkTransaction(): String {
        binding.apply {
            val amount =
                if (etAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                }
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
                    //TODO: Find a way to display a message and suspend until after a choice is made
//                } else if (mainViewModel.getTransactionDetailed()!!.budgetRule == null) {
//                    if (saveWithoutBudget()) {
//                        "Ok"
//                    } else {
//                        "Choose a Budget Rule"
//                    }
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

//    private fun saveWithoutBudget(): Boolean {
//        AlertDialog.Builder(activity).apply {
//            setMessage(
//                "There is no Budget Rule!" +
//                        "Budget Rules are used to update the budget. " +
//                        "If you want to attach this to a budget rule " +
//                        "you will hav to edit it later."
//            )
//            setNegativeButton("Ok", null)
//        }.create().show()
//        return true
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}