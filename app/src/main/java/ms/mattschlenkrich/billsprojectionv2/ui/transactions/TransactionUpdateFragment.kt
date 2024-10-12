package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionFull
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_TRANS_UPDATE

class TransactionUpdateFragment :
    Fragment(R.layout.fragment_transaction_update) {

    private var _binding: FragmentTransactionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var mView: View
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    //    private var mOldTransactionFull: TransactionFull? = null
    private var mTransaction: Transactions? = null
    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        accountViewModel = mainActivity.accountViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.title = "Update this Transaction"
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createMenuActions()
        populateValues()
        createClickActions()
    }

    private fun createClickActions() {
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
            etTransDate.setOnClickListener {
                chooseDate()
            }
            fabUpdateDone.setOnClickListener {
                updateTransaction()
            }
            etAmount.setOnLongClickListener {
                gotoCalculator()
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
                gotoSplitTransactionFragment()
            }
        }
    }

    private fun gotoSplitTransactionFragment() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.setTransferNum(0.0)
        mainViewModel.setUpdatingTransaction(true)
        if (mFromAccount != null &&
            nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0
        ) {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments() + ", " + TAG
            )
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            mView.findNavController().navigate(
                TransactionUpdateFragmentDirections
                    .actionTransactionUpdateFragmentToTransactionSplitFragment()
            )
        }
    }

    private fun updateAmountDisplay() {
        binding.apply {
            btnSplit.isEnabled = etAmount.text.toString().isNotEmpty() &&
                    nf.getDoubleFromDollars(etAmount.text.toString()) > 0.0 &&
                    mFromAccount != null
        }
    }

    private fun createMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteTransaction()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToCalcFragment()
        )
    }

    private fun updateTransaction() {
        val mes = validateTransactionForDave()
        binding.apply {
            if (mes == "Ok") {
                reverseOldTransactionAccountBalances(mainViewModel.getOldTransaction()!!)
                val curTransaction = getCurrentTransactionForSave()
                transactionViewModel.updateTransaction(
                    curTransaction
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val newToAccount = async {
                        accountViewModel.getAccountAndType(
                            curTransaction.transToAccountId
                        )
                    }
                    val newFromAccount = async {
                        accountViewModel.getAccountAndType(
                            curTransaction.transFromAccountId
                        )
                    }
                    val newBudgetRule = async {
                        budgetRuleViewModel.getBudgetRuleDetailed(
                            curTransaction.transRuleId
                        )
                    }
                    val newTrans = TransactionFull(
                        curTransaction,
                        newBudgetRule.await().budgetRule!!,
                        newToAccount.await(),
                        newFromAccount.await()
                    )
                    updateAccountsWithNewValues(newTrans)
                }

                mainViewModel.setCallingFragments(
                    mainViewModel.getCallingFragments()!!
                        .replace(", $TAG", "")
                )
                gotoCallingFragment()
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

    private fun validateTransactionForDave(): String {
        binding.apply {
            val amount =
                nf.getDoubleFromDollars(etAmount.text.toString())
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
                    etTransDate.text = display
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
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun getCurrentTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            mBudgetRule,
            mToAccount,
            mFromAccount
        )

    }

    private fun getCurrentTransactionForSave(): Transactions {
        binding.apply {
            return Transactions(
                mTransaction!!.transId,
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule!!.ruleId,
                mToAccount!!.accountId,
                chkToAccountPending.isChecked,
                mFromAccount!!.accountId,
                chkFromAccountPending.isChecked,
                nf.getDoubleFromDollars(etAmount.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun populateValues() {
        binding.apply {
            if (mainViewModel.getOldTransaction() != null &&
                mainViewModel.getTransactionDetailed() == null
            ) {
                populateValuesFromOldTransaction()
            } else if (mainViewModel.getTransactionDetailed() != null) {
                populateValuesFromCache()
            }
        }
        updateAmountDisplay()
        if (mainViewModel.getUpdatingTransaction()) {
            updateTransaction()
        }
    }

    private fun populateValuesFromCache() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                populateValuesFromTransactionInCache()
            }
            if (mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                populateBudgetRuleFromCache()
            }
            if (mainViewModel.getTransactionDetailed()!!.toAccount != null) {
                populateToAccountFromCache()
            }
            chkToAccountPending.isChecked =
                mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
            if (mainViewModel.getTransactionDetailed()!!.fromAccount != null) {
                populateFromAccountFromCache()

            }
            chkFromAccountPending.isChecked =
                mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending
        }
    }

    private fun populateFromAccountFromCache() {
        binding.apply {
            mFromAccount = mainViewModel.getTransactionDetailed()!!.fromAccount!!
            tvFromAccount.text = mFromAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mFromAccount!!.accountId
                    )
                }
                mFromAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mFromAccountWithType!!.accountType!!.allowPending) {
                    chkFromAccountPending.visibility = View.VISIBLE
                } else {
                    chkFromAccountPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateToAccountFromCache() {
        binding.apply {
            mToAccount = mainViewModel.getTransactionDetailed()!!.toAccount!!
            tvToAccount.text =
                mToAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mToAccount!!.accountId
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccountPending.visibility = View.VISIBLE
                } else {
                    chkToAccountPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateBudgetRuleFromCache() {
        binding.apply {
            mBudgetRule = mainViewModel.getTransactionDetailed()!!.budgetRule!!
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
        }
    }

    private fun populateValuesFromTransactionInCache() {
        binding.apply {
            mTransaction = mainViewModel.getTransactionDetailed()!!.transaction
            etTransDate.text = mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
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
            etDescription.setText(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transName
            )
            etNote.setText(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transNote
            )
        }
    }

    private fun populateValuesFromOldTransaction() {
        binding.apply {
            val transFull = mainViewModel.getOldTransaction()!!
            mTransaction = transFull.transaction
            etTransDate.text = mTransaction!!.transDate
            etAmount.setText(nf.displayDollars(mTransaction!!.transAmount))
            etDescription.setText(mTransaction!!.transName)
            etNote.setText(mTransaction!!.transNote)
            mBudgetRule = transFull.budgetRule
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            mToAccount = transFull.toAccountAndType.account
            tvToAccount.text = mToAccount!!.accountName
            chkToAccountPending.isChecked = mTransaction!!.transToAccountPending
            mFromAccount = transFull.fromAccountAndType.account
            tvFromAccount.text = mFromAccount!!.accountName
            chkFromAccountPending.isChecked = mTransaction!!.transFromAccountPending
            updateAmountDisplay()
        }
    }

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete this Transaction")
            setMessage("Are you sure you want to delete this Transaction?")
            setPositiveButton("Delete") { _, _ ->
                transactionViewModel.deleteTransaction(
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transId,
                    df.getCurrentTimeAsString()
                )
                mainViewModel.setTransactionDetailed(null)
                reverseOldTransactionAccountBalances(mainViewModel.getOldTransaction()!!)
                mainViewModel.setCallingFragments(
                    mainViewModel.getCallingFragments()!!.replace(
                        FRAG_TRANS_UPDATE, ""
                    )
                )
                gotoCallingFragment()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            gotoBudgetViewFragment()
        } else if (mainViewModel.getCallingFragments()!!.contains(
                FRAG_TRANSACTION_VIEW
            )
        ) {
            gotoTransactionViewFragment()
        }
    }

    private fun gotoTransactionViewFragment() {
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToTransactionViewFragment()
        mView.findNavController().navigate(direction)
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun updateAccountsWithNewValues(
        newTransaction: TransactionFull
    ): Boolean {
        if (!newTransaction.transaction.transToAccountPending) {
            CoroutineScope(Dispatchers.IO).launch {
                if (newTransaction.toAccountAndType.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        newTransaction.toAccountAndType.account.accountBalance +
                                newTransaction.transaction.transAmount,
                        newTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (newTransaction.toAccountAndType.accountType!!.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        newTransaction.toAccountAndType.account.accountOwing -
                                newTransaction.transaction.transAmount,
                        newTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }

        }
        if (!newTransaction.transaction.transFromAccountPending) {
            CoroutineScope(Dispatchers.IO).launch {
                if (newTransaction.fromAccountAndType.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        newTransaction.fromAccountAndType.account.accountBalance -
                                newTransaction.transaction.transAmount,
                        newTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (newTransaction.fromAccountAndType.accountType!!.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        newTransaction.fromAccountAndType.account.accountOwing +
                                newTransaction.transaction.transAmount,
                        newTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }
        }
        return true
    }

    private fun reverseOldTransactionAccountBalances(oldTransaction: TransactionFull): Boolean {
        if (!oldTransaction.transaction.transToAccountPending) {
            if (oldTransaction.toAccountAndType.accountType!!.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldTransaction.toAccountAndType.account.accountBalance -
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldTransaction.toAccountAndType.accountType!!.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldTransaction.toAccountAndType.account.accountOwing +
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        if (!oldTransaction.transaction.transFromAccountPending) {
            if (oldTransaction.fromAccountAndType.accountType!!.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldTransaction.fromAccountAndType.account.accountBalance +
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldTransaction.fromAccountAndType.accountType!!.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldTransaction.fromAccountAndType.account.accountOwing -
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}