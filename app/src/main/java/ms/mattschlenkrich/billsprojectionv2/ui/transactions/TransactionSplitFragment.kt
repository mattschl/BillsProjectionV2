package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANSACTION_SPLIT

class TransactionSplitFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf("")
    private var descriptionState = mutableStateOf("")
    private var noteState = mutableStateOf("")
    private var amountState = mutableStateOf("")
    private var originalAmountState = mutableStateOf(0.0)
    private var remainderState = mutableStateOf(0.0)

    private var budgetRuleState = mutableStateOf<BudgetRule?>(null)
    private var toAccountState = mutableStateOf<Account?>(null)
    private var fromAccountState = mutableStateOf<Account?>(null)

    private var toPendingState = mutableStateOf(false)
    private var fromPendingState = mutableStateOf(false)

    private var toAccountWithTypeState = mutableStateOf<AccountWithType?>(null)
    private var fromAccountWithTypeState = mutableStateOf<AccountWithType?>(null)
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        transactionViewModel = mainActivity.transactionViewModel
        mainActivity.topMenuBar.title = getString(R.string.splitting_transaction)

        populateValues()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.intValue >= 0) {
                        TransactionSplitScreen(
                            date = dateState.value,
                            onDateChange = { dateState.value = it },
                            budgetRule = budgetRuleState.value,
                            onChooseBudgetRule = { chooseBudgetRule() },
                            amount = amountState.value,
                            onAmountChange = {
                                amountState.value = it
                                updateAmountsDisplay()
                            },
                            onGotoCalculator = { gotoCalculator() },
                            originalAmount = originalAmountState.value,
                            remainder = remainderState.value,
                            toAccount = toAccountState.value,
                            onChooseToAccount = { chooseToAccount() },
                            toPending = toPendingState.value,
                            onToPendingChange = { toPendingState.value = it },
                            allowToPending = toAccountWithTypeState.value?.accountType?.allowPending == true,
                            fromAccount = fromAccountState.value,
                            fromPending = fromPendingState.value,
                            onFromPendingChange = { fromPendingState.value = it },
                            allowFromPending = fromAccountWithTypeState.value?.accountType?.allowPending == true,
                            onFromAccountClick = { },
                            description = descriptionState.value,
                            onDescriptionChange = { descriptionState.value = it },
                            note = noteState.value,
                            onNoteChange = { noteState.value = it },
                            onSaveClick = { saveTransactionIfValid() },
                            nf = nf
                        )
                    }
                }
            }
        }
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.split_transaction)
        populateValues()
        refreshKey.intValue++
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        transactionViewModel = mainActivity.transactionViewModel
    }

    fun populateValues() {
        if (mainViewModel.getTransactionDetailed() != null) {
            populateValuesFromOriginalTransaction()
        }
        if (mainViewModel.getSplitTransactionDetailed() != null) {
            populateValuesFromSplitTransaction()
        }
    }

    private fun populateValuesFromOriginalTransaction() {
        val mTransactionDetailed = mainViewModel.getTransactionDetailed()!!
        val transaction = mTransactionDetailed.transaction!!
        originalAmountState.value = transaction.transAmount
        dateState.value = transaction.transDate
        fromAccountState.value = mTransactionDetailed.fromAccount!!
        fromPendingState.value = transaction.transFromAccountPending

        lifecycleScope.launch {
            val accountWithType =
                accountViewModel.getAccountWithType(fromAccountState.value!!.accountId)
            fromAccountWithTypeState.value = accountWithType
        }
        updateAmountsDisplay()
    }

    private fun populateValuesFromBudgetRule() {
        val mTransactionDetailed = mainViewModel.getSplitTransactionDetailed()!!
        budgetRuleState.value = mTransactionDetailed.budgetRule!!
        if (descriptionState.value.isBlank()) {
            descriptionState.value = budgetRuleState.value!!.budgetRuleName
        }
        if (toAccountState.value == null) {
            populateToAccountFromBudgetRule()
        }
    }

    private fun populateValuesFromSplitTransaction() {
        val splitDetailed = mainViewModel.getSplitTransactionDetailed()!!
        val transaction = splitDetailed.transaction!!
        descriptionState.value = transaction.transName
        noteState.value = transaction.transNote
        fromPendingState.value = transaction.transFromAccountPending

        if (mainViewModel.getTransferNum() != null && mainViewModel.getTransferNum() != 0.0) {
            amountState.value = nf.displayDollars(mainViewModel.getTransferNum()!!)
        } else if (transaction.transAmount != 0.0) {
            amountState.value = nf.displayDollars(transaction.transAmount)
        } else {
            amountState.value = nf.displayDollars(0.0)
        }
        updateAmountsDisplay()

        if (splitDetailed.toAccount != null) {
            populateToAccountFromTransaction()
        }
        if (splitDetailed.budgetRule != null) {
            populateValuesFromBudgetRule()
        }
    }

    private fun populateToAccountFromTransaction() {
        toAccountState.value = mainViewModel.getSplitTransactionDetailed()!!.toAccount
        lifecycleScope.launch {
            val accountWithType =
                accountViewModel.getAccountWithType(toAccountState.value!!.accountId)
            toAccountWithTypeState.value = accountWithType
            if (accountWithType.accountType!!.allowPending) {
                toPendingState.value =
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transToAccountPending
            }
        }
    }

    private fun populateToAccountFromBudgetRule() {
        lifecycleScope.launch {
            val ruleFull = budgetRuleViewModel.getBudgetRuleDetailed(budgetRuleState.value!!.ruleId)
            if (ruleFull != null) {
                toAccountState.value = ruleFull.toAccount
                val accountWithType =
                    accountViewModel.getAccountWithType(toAccountState.value!!.accountId)
                toAccountWithTypeState.value = accountWithType
                if (accountWithType.accountType!!.allowPending) {
                    toPendingState.value =
                        mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transToAccountPending
                }
            }
        }
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setSplitTransactionDetailed(
            getSplitTransDetailed()
        )
        gotoBudgetRuleChooseFragment()
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        gotoAccountChooseFragment()
    }

    private fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            nf.generateId(),
            dateState.value,
            descriptionState.value,
            noteState.value,
            budgetRuleState.value?.ruleId ?: 0L,
            toAccountState.value?.accountId ?: 0L,
            toPendingState.value,
            fromAccountState.value!!.accountId,
            fromPendingState.value,
            nf.getDoubleFromDollars(amountState.value),
            transIsDeleted = false,
            transUpdateTime = df.getCurrentTimeAsString()
        )
    }

    private fun getSplitTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRuleState.value,
            toAccountState.value,
            fromAccountState.value,
        )
    }

    private fun updateAmountsDisplay() {
        val amount = nf.getDoubleFromDollars(amountState.value)
        val original = originalAmountState.value
        if (original < amount) {
            showMessage(
                getString(R.string.error) + getString(R.string.new_amount_cannot_be_more_than_the_original_amount)
            )
            amountState.value = nf.displayDollars(0.0)
            remainderState.value = original
        } else {
            remainderState.value = original - amount
        }
    }

    private fun saveTransactionIfValid() {
        updateAmountsDisplay()
        val message = validateTransaction()
        if (message == ANSWER_OK) {
            confirmPerformTransaction()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun confirmPerformTransaction() {
        var display =
            getString(R.string.this_will_perform) + descriptionState.value + getString(R.string._for_) + nf.getDollarsFromDouble(
                nf.getDoubleFromDollars(amountState.value)
            ) + getString(R.string.__from) + fromAccountState.value!!.accountName
        display += if (fromPendingState.value) getString(R.string._pending) else ""
        display += getString(R.string._to) + toAccountState.value!!.accountName
        display += if (toPendingState.value) getString(R.string._pending) else ""
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_performing_transaction)).setMessage(
                display
            ).setPositiveButton(getString(R.string.confirm)) { _, _ ->
                saveTransaction()
            }.setNegativeButton(getString(R.string.go_back), null).show()
    }

    private fun saveTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        lifecycleScope.launch {
            accountUpdateViewModel.performTransaction(
                mTransaction
            )
            gotoCallingFragment()
        }
    }

    private fun validateTransaction(): String {
        if (amountState.value.isBlank()) {
            return getString(R.string.please_enter_an_amount_for_this_transaction)
        }
        if (nf.getDoubleFromDollars(amountState.value) >= originalAmountState.value
        ) {
            return getString(R.string.the_amount_of_a_split_transaction_must_be_less_than_the_original)
        }
        if (descriptionState.value.isBlank()) {
            return getString(R.string.please_enter_a_name_or_description)
        }
        if (toAccountState.value == null) {
            return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        }
        if (budgetRuleState.value == null) {
            return if (!saveWithoutBudget()) {
                getString(R.string.choose_a_budget_rule)
            } else {
                getString(R.string.choose_a_budget_rule)
            }
        }
        return ANSWER_OK
    }

    private fun saveWithoutBudget(): Boolean {
        AlertDialog.Builder(requireActivity()).apply {
            setMessage(
                getString(R.string.there_is_no_budget_rule) + getString(R.string.budget_rules_are_used_to_update_the_budget)
            )
            setNegativeButton(getString(R.string.retry), null)
        }.create().show()
        return false
    }

    private fun gotoCallingFragment() {
        val transactionDetailed = mainViewModel.getTransactionDetailed()!!
        val oldTransaction = transactionDetailed.transaction!!
        oldTransaction.transAmount = remainderState.value
        if (mainViewModel.getUpdatingTransaction()) {
            transactionViewModel.updateTransaction(oldTransaction)
        }
        mainViewModel.setTransactionDetailed(
            TransactionDetailed(
                oldTransaction,
                transactionDetailed.budgetRule,
                transactionDetailed.toAccount,
                transactionDetailed.fromAccount
            )
        )
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.removeCallingFragment(TAG)
        findNavController().popBackStack()
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                amountState.value.ifBlank {
                    getString(R.string.zero_double)
                })
        )
        mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoAccountChooseFragment() {
        findNavController().navigate(
            TransactionSplitFragmentDirections.actionTransactionSplitFragmentToAccountChooseFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        findNavController().navigate(
            TransactionSplitFragmentDirections.actionTransactionSplitFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetRuleChooseFragment() {
        findNavController().navigate(
            TransactionSplitFragmentDirections.actionTransactionSplitFragmentToBudgetRuleChooseFragment()
        )
    }
}