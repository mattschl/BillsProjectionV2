package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANS_PERFORM

class TransactionPerformFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf(df.getCurrentDateAsString())
    private var descriptionState = mutableStateOf("")
    private var noteState = mutableStateOf("")
    private var amountState = mutableStateOf("")
    private var budgetedAmountState = mutableStateOf("")
    private var toAccountState = mutableStateOf<Account?>(null)
    private var fromAccountState = mutableStateOf<Account?>(null)
    private var budgetRuleState = mutableStateOf<BudgetRule?>(null)
    private var toPendingState = mutableStateOf(false)
    private var fromPendingState = mutableStateOf(false)
    private var allowToPendingState = mutableStateOf(false)
    private var allowFromPendingState = mutableStateOf(false)
    private var remainderState = mutableDoubleStateOf(0.0)
    private val refreshKey = mutableStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.value >= 0) {
                        TransactionPerformScreen(
                            date = dateState.value,
                            onDateChange = { dateState.value = it },
                            budgetRule = budgetRuleState.value,
                            amount = amountState.value,
                            onAmountChange = {
                                amountState.value = it
                                calculateRemainder()
                            },
                            onSplitClick = { splitTransaction() },
                            budgetedAmount = budgetedAmountState.value,
                            onBudgetedAmountChange = {
                                budgetedAmountState.value = it
                                calculateRemainder()
                                updateBudgetItemInCache()
                            },
                            remainder = remainderState.doubleValue,
                            toAccount = toAccountState.value,
                            toPending = toPendingState.value,
                            onToPendingChange = { toPendingState.value = it },
                            allowToPending = allowToPendingState.value,
                            onToAccountClick = { chooseToAccount() },
                            fromAccount = fromAccountState.value,
                            fromPending = fromPendingState.value,
                            onFromPendingChange = { fromPendingState.value = it },
                            allowFromPending = allowFromPendingState.value,
                            onFromAccountClick = { chooseFromAccount() },
                            onChooseBudgetRule = { chooseBudgetRule() },
                            description = descriptionState.value,
                            onDescriptionChange = { descriptionState.value = it },
                            note = noteState.value,
                            onNoteChange = { noteState.value = it },
                            onSaveClick = { performTransactionIfValid() },
                            isSplitEnabled = nf.getDoubleFromDollars(amountState.value) > 2.0 && fromAccountState.value != null,
                            nf = nf
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshData()
    }

    override fun refreshData() {
        mainActivity.topMenuBar.title = getString(R.string.perform_a_transaction)
        populateValues()
        refreshKey.value++
    }

    fun populateValues() {
        if (mainViewModel.getTransactionDetailed() != null) {
            populateValuesFromTransactionDetailedInCache()
        } else if (mainViewModel.getBudgetItemDetailed() != null) {
            populateValuesFromBudgetItemDetailedInCache()
        }
    }

    private fun populateValuesFromTransactionDetailedInCache() {
        val detailed = mainViewModel.getTransactionDetailed()!!
        val trans = detailed.transaction ?: return

        mBudgetRule = detailed.budgetRule
        budgetRuleState.value = detailed.budgetRule
        toAccountState.value = detailed.toAccount
        fromAccountState.value = detailed.fromAccount

        descriptionState.value = trans.transName
        noteState.value = trans.transNote
        dateState.value = trans.transDate

        val amount = if (mainViewModel.getTransferNum() != 0.0) {
            mainViewModel.getTransferNum()!!
        } else {
            trans.transAmount
        }
        amountState.value = nf.displayDollars(amount)
        mainViewModel.setTransferNum(0.0)

        val budgetedAmount =
            mainViewModel.getBudgetItemDetailed()?.budgetItem?.biProjectedAmount ?: 0.0
        budgetedAmountState.value = nf.displayDollars(budgetedAmount)

        toPendingState.value = trans.transToAccountPending
        fromPendingState.value = trans.transFromAccountPending

        detailed.toAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowToPendingState.value = it.accountType?.allowPending == true
            }
        }

        detailed.fromAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowFromPendingState.value = it.accountType?.allowPending == true
            }
        }

        calculateRemainder()
    }

    private fun populateValuesFromBudgetItemDetailedInCache() {
        val detailed = mainViewModel.getBudgetItemDetailed()!!
        val budgetItem = detailed.budgetItem!!

        toAccountState.value = detailed.toAccount
        fromAccountState.value = detailed.fromAccount
        budgetRuleState.value = detailed.budgetRule

        budgetRuleState.value?.let { mBudgetRule = it }

        descriptionState.value = budgetItem.biBudgetName
        dateState.value = df.getCurrentDateAsString()
        budgetedAmountState.value = nf.displayDollars(budgetItem.biProjectedAmount)
        amountState.value = nf.displayDollars(0.0)

        detailed.toAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowToPendingState.value = it.accountType?.allowPending == true
                if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                    toPendingState.value = true
                }
            }
        }

        detailed.fromAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowFromPendingState.value = it.accountType?.allowPending == true
                if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                    fromPendingState.value = true
                }
            }
        }

        calculateRemainder()
    }

    private fun updateBudgetItemInCache() {
        val amount = nf.getDoubleFromDollars(budgetedAmountState.value)
        mainViewModel.getBudgetItemDetailed()?.let { detailed ->
            if (amount != detailed.budgetItem?.biProjectedAmount) {
                detailed.budgetItem?.biProjectedAmount = amount
                mainViewModel.setBudgetItemDetailed(detailed)
            }
        }
    }

    private fun splitTransaction() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.setTransferNum(0.0)
        val amount = nf.getDoubleFromDollars(amountState.value)
        if (fromAccountState.value != null && amount > 2.0) {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            gotoTransactionSplitFragment()
        }
    }

    private fun calculateRemainder() {
        val amt = nf.getDoubleFromDollars(amountState.value)
        val budgeted = nf.getDoubleFromDollars(budgetedAmountState.value)
        remainderState.doubleValue = budgeted - amt
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoBudgetRuleChooseFragment()
    }

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRuleState.value,
            toAccountState.value,
            fromAccountState.value
        )
    }

    private fun performTransactionIfValid() {
        calculateRemainder()
        val message = validateTransaction()
        if (message == ANSWER_OK) {
            confirmPerformTransaction()
        } else {
            Toast.makeText(requireContext(), getString(R.string.error) + message, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun confirmPerformTransaction() {
        val amount = nf.getDoubleFromDollars(amountState.value)
        var display = getString(R.string.this_will_perform) + " " + descriptionState.value +
                getString(R.string._for_) + " " + nf.displayDollars(amount) +
                getString(R.string.__from) + " " + (fromAccountState.value?.accountName ?: "")
        if (fromPendingState.value) display += " " + getString(R.string.pending)
        display += " " + getString(R.string._to) + " " + (toAccountState.value?.accountName ?: "")
        if (toPendingState.value) display += " " + getString(R.string.pending)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_performing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                performTransaction()
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    private fun validateTransaction(): String {
        val amount = nf.getDoubleFromDollars(amountState.value)
        if (descriptionState.value.isBlank()) {
            return getString(R.string.please_enter_a_name_or_description)
        }
        if (amount == 0.0) {
            return getString(R.string.please_enter_an_amount_for_this_transaction)
        }
        if (fromAccountState.value == null || toAccountState.value == null) {
            return " " + getString(R.string.choose_an_account)
        }
        return ANSWER_OK
    }

    private fun performTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        lifecycleScope.launch {
            accountUpdateViewModel.performTransaction(mTransaction)
            updateBudgetItem()
            gotoCallingFragment()
        }
    }

    private fun updateBudgetItem() {
        val remainder = remainderState.doubleValue
        val completed = remainder < 2.0
        val detailed = mainViewModel.getBudgetItemDetailed() ?: return
        val mBudget = detailed.budgetItem ?: return

        budgetItemViewModel.updateBudgetItem(
            mBudget.copy(
                biProjectedAmount = remainder,
                biIsCompleted = completed,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        )
    }

    private fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            transId = nf.generateId(),
            transDate = dateState.value,
            transName = descriptionState.value,
            transNote = noteState.value,
            transRuleId = budgetRuleState.value?.ruleId ?: 0L,
            transToAccountId = toAccountState.value?.accountId ?: 0L,
            transToAccountPending = toPendingState.value,
            transFromAccountId = fromAccountState.value?.accountId ?: 0L,
            transFromAccountPending = fromPendingState.value,
            transAmount = nf.getDoubleFromDollars(amountState.value),
            transIsDeleted = false,
            transUpdateTime = df.getCurrentTimeAsString()
        )
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().popBackStack()
    }

    private fun gotoAccountChooseFragment() {
        findNavController().navigate(
            TransactionPerformFragmentDirections.actionTransactionPerformFragmentToAccountChooseFragment()
        )
    }

    private fun gotoBudgetRuleChooseFragment() {
        findNavController().navigate(
            TransactionPerformFragmentDirections.actionTransactionPerformFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun gotoTransactionSplitFragment() {
        findNavController().navigate(
            TransactionPerformFragmentDirections.actionTransactionPerformFragmentToTransactionSplitFragment()
        )
    }

    private var mBudgetRule: BudgetRule? = null // For compatibility if needed
}