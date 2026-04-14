package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainActivity.topMenuBar.title = getString(R.string.view_the_budget)
        updateViewModels()

        return ComposeView(requireContext()).apply {
            setContent {
                val assetList by budgetItemViewModel.getAssetsForBudget()
                    .observeAsState(initial = emptyList())
                var selectedAsset by remember {
                    mutableStateOf(
                        mainViewModel.getReturnToAsset() ?: assetList.firstOrNull() ?: ""
                    )
                }

                if (selectedAsset.isEmpty() && assetList.isNotEmpty()) {
                    selectedAsset = assetList.first()
                }

                if (selectedAsset.isNotEmpty()) {
                    mainViewModel.setReturnToAsset(selectedAsset)
                }

                val payDayList by budgetItemViewModel.getPayDays(selectedAsset)
                    .observeAsState(initial = emptyList())
                var selectedPayDay by remember {
                    mutableStateOf(
                        mainViewModel.getReturnToPayDay() ?: payDayList.firstOrNull() ?: ""
                    )
                }

                if (selectedPayDay.isEmpty() && payDayList.isNotEmpty()) {
                    selectedPayDay = payDayList.first()
                }

                if (selectedPayDay.isNotEmpty()) {
                    mainViewModel.setReturnToPayDay(selectedPayDay)
                }

                val curAsset by accountViewModel.getAccountDetailed(selectedAsset)
                    .observeAsState(initial = null)

                val pendingList by transactionViewModel.getPendingTransactionsDetailed(selectedAsset)
                    .observeAsState(initial = emptyList())

                val budgetList by budgetItemViewModel.getBudgetItems(selectedAsset, selectedPayDay)
                    .observeAsState(initial = emptyList())

                var pendingAmount = 0.0
                pendingList.forEach {
                    if (it.toAccount?.accountName == selectedAsset) {
                        pendingAmount += it.transaction?.transAmount ?: 0.0
                    } else {
                        pendingAmount -= it.transaction?.transAmount ?: 0.0
                    }
                }

                BillsProjectionTheme {
                    BudgetViewScreen(
                        assetList = assetList,
                        selectedAsset = selectedAsset,
                        onAssetSelected = {
                            selectedAsset = it
                            mainViewModel.setReturnToAsset(it)
                        },
                        payDayList = payDayList,
                        selectedPayDay = selectedPayDay,
                        onPayDaySelected = {
                            selectedPayDay = it
                            mainViewModel.setReturnToPayDay(it)
                        },
                        curAsset = curAsset,
                        pendingList = pendingList,
                        pendingAmount = pendingAmount,
                        budgetList = budgetList,
                        onAddClick = { onAddButtonPress() },
                        onBudgetItemClick = { chooseOptionsForBudget(it) },
                        onBudgetItemLockClick = { chooseLockUnlock(it) },
                        onTransactionClick = { chooseOptionsForTransaction(it) },
                        onAccountClick = { gotoAccount() }
                    )
                }
            }
        }
    }

    private fun updateViewModels() {
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        transactionViewModel = mainActivity.transactionViewModel
    }

    private fun onAddButtonPress() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_an_action)).setItems(
                arrayOf(
                    getString(R.string.schedule_a_new_budget_item),
                    getString(R.string.add_an_unscheduled_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> addNewBudgetItem()
                    1 -> addNewTransaction()
                }
            }.show()
    }

    private fun chooseLockUnlock(budgetItemDetailed: BudgetItemDetailed) {
        val budgetItem = budgetItemDetailed.budgetItem!!
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.lock_or_unlock)).setItems(
                arrayOf(
                    getString(R.string.lock) + budgetItem.biBudgetName,
                    getString(R.string.un_lock) + budgetItem.biBudgetName,
                    getString(R.string.lock_all_items_for_this_payday),
                    getString(R.string.un_lock_all_items_for_this_payday)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> budgetItemViewModel.lockUnlockBudgetItem(
                        true,
                        budgetItem.biRuleId,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    1 -> budgetItemViewModel.lockUnlockBudgetItem(
                        false,
                        budgetItem.biRuleId,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    2 -> budgetItemViewModel.lockUnlockBudgetItem(
                        true,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    3 -> budgetItemViewModel.lockUnlockBudgetItem(
                        false,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )
                }
            }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun chooseOptionsForBudget(curBudgetDetailed: BudgetItemDetailed) {
        val curBudget = curBudgetDetailed.budgetItem!!
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + curBudget.biBudgetName
        ).setItems(
            arrayOf(
                getString(R.string.perform_a_transaction_on_) + " \"${curBudget.biBudgetName}\" ",
                if (curBudget.biProjectedAmount == 0.0) "" else getString(R.string.perform_action) + "\"${curBudget.biBudgetName}\" " + getString(
                    R.string.for_amount_of_the_full_amount_
                ) + nf.displayDollars(curBudget.biProjectedAmount),
                getString(R.string.adjust_the_projections_for_this_item),
                getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                getString(R.string.cancel_this_projected_item),
            )
        ) { _, pos ->
            when (pos) {
                0 -> performTransaction(curBudgetDetailed)
                1 -> confirmTransaction(curBudgetDetailed)
                2 -> gotoBudgetItem(curBudgetDetailed)
                3 -> gotoBudgetRule(curBudgetDetailed)
                4 -> confirmCancelBudgetItem(curBudgetDetailed)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun chooseOptionsForTransaction(pendingTransaction: TransactionDetailed) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + nf.displayDollars(pendingTransaction.transaction!!.transAmount) + getString(
                R.string._to_
            ) + pendingTransaction.transaction.transName
        ).setItems(
            arrayOf(
                getString(R.string.complete_this_pending_transaction),
                getString(R.string.open_the_transaction_to_edit_it),
                getString(R.string.delete_this_pending_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> confirmPendingTransaction(pendingTransaction)
                1 -> editTransaction(pendingTransaction)
                2 -> deleteTransaction(pendingTransaction)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun confirmPendingTransaction(pendingTransaction: TransactionDetailed) {
        val display =
            getString(R.string.this_will_apply_the_amount_of) + nf.displayDollars(pendingTransaction.transaction!!.transAmount) + (if (pendingTransaction.transaction.transToAccountPending) getString(
                R.string._to_
            ) else getString(R.string._From_)) + mainViewModel.getReturnToAsset()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                completePendingTransaction(
                    pendingTransaction
                )
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun completePendingTransaction(transaction: TransactionDetailed) {
        lifecycleScope.launch {
            val trans = transaction.transaction!!
            val selectedAsset = mainViewModel.getReturnToAsset()
            if (selectedAsset != null) {
                val asset = withContext(Dispatchers.IO) {
                    accountViewModel.getAccountWithType(selectedAsset)
                }
                if (transaction.toAccount!!.accountName == selectedAsset) {
                    transactionViewModel.updateTransaction(
                        trans.copy(
                            transToAccountPending = false,
                            transUpdateTime = df.getCurrentTimeAsString()
                        )
                    )
                    if (asset.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            asset.account.accountBalance + trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (asset.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            asset.account.accountOwing - trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                } else {
                    transactionViewModel.updateTransaction(
                        trans.copy(
                            transFromAccountPending = false,
                            transUpdateTime = df.getCurrentTimeAsString()
                        )
                    )
                    if (asset.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            asset.account.accountBalance - trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (asset.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            asset.account.accountOwing + trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                }
            }
            delay(WAIT_500)
        }
    }

    private fun editTransaction(transaction: TransactionDetailed) {
        setReturnVariables()
        mainViewModel.setTransactionDetailed(transaction)
        lifecycleScope.launch {
            val trans = transaction.transaction!!
            val transactionFull = transactionViewModel.getTransactionFull(
                trans.transId,
                trans.transToAccountId,
                trans.transFromAccountId
            )
            mainViewModel.setOldTransaction(transactionFull)
            delay(WAIT_250)
            gotoTransactionUpdateFragment()
        }
    }

    private fun deleteTransaction(transaction: TransactionDetailed) {
        transactionViewModel.deleteTransaction(
            transaction.transaction!!.transId,
            df.getCurrentTimeAsString()
        )
    }

    private fun performTransaction(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetItemDetailed(curBudget)
        mainViewModel.setTransactionDetailed(null)
        setReturnVariables()
        gotoTransactionPerformFragment()
    }

    private fun confirmTransaction(curBudgetDetailed: BudgetItemDetailed) {
        val curBudget = curBudgetDetailed.budgetItem!!
        if (curBudget.biProjectedAmount > 0.0) {
            lifecycleScope.launch {
                val display = getConfirmationsDisplay(curBudget, curBudgetDetailed)
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_completing_transaction))
                    .setMessage(display)
                    .setPositiveButton(getString(R.string.perform_action)) { _, _ ->
                        completeBudgetTransaction(
                            curBudgetDetailed
                        )
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }
        }
    }

    private suspend fun getConfirmationsDisplay(
        budgetItem: BudgetItem,
        curBudgetDetailed: BudgetItemDetailed
    ): String {
        var display =
            getString(R.string.this_will_perform) + budgetItem.biBudgetName + getString(R.string.applying_the_amount_of) + nf.displayDollars(
                budgetItem.biProjectedAmount
            ) + getString(R.string.from) + curBudgetDetailed.fromAccount!!.accountName
        display += if (accountUpdateViewModel.isTransactionPending(budgetItem.biFromAccountId)) getString(
            R.string._pending
        ) else ""
        delay(WAIT_100)
        display += getString(R.string._to) + curBudgetDetailed.toAccount!!.accountName
        display += if (accountUpdateViewModel.isTransactionPending(budgetItem.biToAccountId)) getString(
            R.string._pending
        ) else ""
        return display
    }

    private fun completeBudgetTransaction(curBudgetDetailed: BudgetItemDetailed) {
        val budgetItem = curBudgetDetailed.budgetItem!!
        lifecycleScope.launch {
            val toPending = accountUpdateViewModel.isTransactionPending(budgetItem.biToAccountId)
            val fromPending =
                accountUpdateViewModel.isTransactionPending(budgetItem.biFromAccountId)
            accountUpdateViewModel.performTransaction(
                Transactions(
                    nf.generateId(),
                    df.getCurrentDateAsString(),
                    budgetItem.biBudgetName,
                    "",
                    budgetItem.biRuleId,
                    budgetItem.biToAccountId,
                    toPending,
                    budgetItem.biFromAccountId,
                    fromPending,
                    budgetItem.biProjectedAmount,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            budgetItemViewModel.updateBudgetItem(
                budgetItem.copy(
                    biActualDate = df.getCurrentDateAsString(),
                    biProjectedAmount = 0.0,
                    biIsCompleted = true,
                    biUpdateTime = df.getCurrentTimeAsString()
                )
            )
            delay(WAIT_500)
        }
    }

    private fun confirmCancelBudgetItem(curBudgetDetailed: BudgetItemDetailed) {
        val budgetItem = curBudgetDetailed.budgetItem!!
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_cancelling_budget_item)).setMessage(
                getString(R.string.this_will_cancel) + budgetItem.biBudgetName + getString(R.string.with_the_amount_of) + nf.displayDollars(
                    budgetItem.biProjectedAmount
                ) + getString(R.string._remaining)
            ).setPositiveButton(getString(R.string.cancel_now)) { _, _ ->
                budgetItemViewModel.cancelBudgetItem(
                    budgetItem.biRuleId,
                    budgetItem.biProjectedDate,
                    df.getCurrentTimeAsString()
                )
            }.setNegativeButton(getString(R.string.ignore_this), null).show()
    }

    private fun gotoBudgetItem(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetItemDetailed(curBudget)
        setReturnVariables()
        gotoBudgetItemUpdateFragment()
    }

    private fun gotoBudgetRule(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curBudget.budgetRule,
                curBudget.toAccount,
                curBudget.fromAccount
            )
        )
        setReturnVariables()
        gotoBudgetRuleUpdateFragment()
    }

    private fun addNewTransaction() {
        setReturnVariables()
        mainViewModel.setTransactionDetailed(null)
        gotoTransactionAddFragment()
    }

    private fun addNewBudgetItem() {
        setReturnVariables()
        gotoBudgetItemAddFragment()
    }

    private fun setReturnVariables() {
        mainViewModel.setCallingFragments(TAG)
    }

    private fun gotoAccount() {
        setReturnVariables()
        val selectedAsset = mainViewModel.getReturnToAsset()
        lifecycleScope.launch {
            if (selectedAsset != null) {
                val account = withContext(Dispatchers.IO) {
                    accountViewModel.getAccountWithType(selectedAsset)
                }
                mainViewModel.setAccountWithType(account)
                findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToAccountUpdateFragment())
            }
        }
    }

    fun gotoBudgetItemUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemUpdateFragment())
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetRuleUpdateFragment())
    }

    private fun gotoTransactionAddFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionAddFragment())
    }

    fun gotoTransactionPerformFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionPerformFragment())
    }

    private fun gotoBudgetItemAddFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemAddFragment())
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionUpdateFragment())
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.budget_view)
    }
}