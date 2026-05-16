package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

//private const val TAG = "BudgetViewScreenWrapper"

@Composable
fun BudgetViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val accountViewModel = activity.accountViewModel
    val budgetItemViewModel = activity.budgetItemViewModel
    val transactionViewModel = activity.transactionViewModel
    val accountUpdateViewModel = activity.accountUpdateViewModel

    LaunchedEffect(Unit) {
        activity.topMenuBar.title = activity.getString(R.string.view_the_budget)
    }

    val rawAssetList by budgetItemViewModel.getAssetsForBudget()
        .observeAsState(initial = emptyList())
    val assetList = remember(rawAssetList) {
        if (rawAssetList.isEmpty()) emptyList()
        else listOf(ALL_ITEMS) + rawAssetList
    }

    val selectedAsset = mainViewModel.getReturnToAsset() ?: ""

    LaunchedEffect(assetList) {
        if (selectedAsset.isEmpty() && assetList.isNotEmpty()) {
            mainViewModel.setReturnToAsset(assetList.first())
        }
    }

    val payDayList by budgetItemViewModel.getPayDays(selectedAsset)
        .observeAsState(initial = emptyList())
    val selectedPayDay = mainViewModel.getReturnToPayDay() ?: ""

    LaunchedEffect(payDayList, selectedAsset) {
        if (payDayList.isNotEmpty()) {
            val currentPayDay = mainViewModel.getReturnToPayDay()
            if (currentPayDay == null || !payDayList.contains(currentPayDay)) {
                // If the current payday is not in the list for the new asset,
                // find the first payday that is equal to or after today.
                val today = DateFunctions().getCurrentDateAsString()
                val nextBestPayDay = payDayList.find { it >= today } ?: payDayList.first()
                mainViewModel.setReturnToPayDay(nextBestPayDay)
            }
        }
    }

    val curAsset by accountViewModel.getAccountDetailed(selectedAsset)
        .observeAsState(initial = null)

    val pendingList by transactionViewModel.getPendingTransactionsDetailed(selectedAsset)
        .observeAsState(initial = emptyList())

    val budgetList by budgetItemViewModel.getBudgetItems(selectedAsset, selectedPayDay)
        .observeAsState(initial = emptyList())

    val pendingAmount = remember(pendingList, selectedAsset, assetList) {
        var amount = 0.0
        pendingList.forEach {
            if (it.toAccount?.accountName == selectedAsset) {
                amount += it.transaction?.transAmount ?: 0.0
            } else if (it.fromAccount?.accountName == selectedAsset) {
                amount -= it.transaction?.transAmount ?: 0.0
            } else if (selectedAsset == ALL_ITEMS) {
                if (assetList.contains(it.toAccount?.accountName)) {
                    amount += it.transaction?.transAmount ?: 0.0
                } else if (assetList.contains(it.fromAccount?.accountName)) {
                    amount -= it.transaction?.transAmount ?: 0.0
                }
            }
        }
        amount
    }

    BudgetViewScreen(
        assetList = assetList,
        selectedAsset = selectedAsset,
        onAssetSelected = {
            mainViewModel.setReturnToAsset(it)
        },
        payDayList = payDayList,
        selectedPayDay = selectedPayDay,
        onPayDaySelected = {
            mainViewModel.setReturnToPayDay(it)
        },
        curAsset = curAsset,
        pendingList = pendingList,
        pendingAmount = pendingAmount,
        budgetList = budgetList,
        onAddClick = {
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.choose_an_action)).setItems(
                    arrayOf(
                        activity.getString(R.string.schedule_a_new_budget_item),
                        activity.getString(R.string.add_an_unscheduled_transaction)
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> {
                            mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                            navController.navigate(Screen.BudgetItemAdd.route)
                        }

                        1 -> {
                            mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                            mainViewModel.setTransactionDetailed(null)
                            navController.navigate(Screen.TransactionAdd.route)
                        }
                    }
                }.show()
        },
        onBudgetItemClick = { curBudgetDetailed ->
            val curBudget = curBudgetDetailed.budgetItem!!
            val nf = NumberFunctions()
            AlertDialog.Builder(activity).setTitle(
                activity.getString(R.string.choose_an_action_for) + curBudget.biBudgetName
            ).setItems(
                arrayOf(
                    activity.getString(R.string.perform_a_transaction_on_) + " \"${curBudget.biBudgetName}\" ",
                    if (curBudget.biProjectedAmount == 0.0) "" else activity.getString(R.string.perform_action) + "\"${curBudget.biBudgetName}\" " + activity.getString(
                        R.string.for_amount_of_the_full_amount_
                    ) + nf.displayDollars(curBudget.biProjectedAmount),
                    activity.getString(R.string.adjust_the_projections_for_this_item),
                    activity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                    activity.getString(R.string.cancel_this_projected_item),
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                        mainViewModel.setTransactionDetailed(null)
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.TransactionPerform.route)
                    }

                    1 -> {
                        if (curBudget.biProjectedAmount > 0.0) {
                            activity.lifecycleScope.launch {
                                val df = DateFunctions()
                                val toPending =
                                    accountUpdateViewModel.isTransactionPending(curBudget.biToAccountId)
                                val fromPending =
                                    accountUpdateViewModel.isTransactionPending(curBudget.biFromAccountId)

                                var display =
                                    activity.getString(R.string.this_will_perform) + curBudget.biBudgetName + activity.getString(
                                        R.string.applying_the_amount_of
                                    ) + nf.displayDollars(curBudget.biProjectedAmount) + activity.getString(
                                        R.string.from
                                    ) + curBudgetDetailed.fromAccount!!.accountName
                                display += if (fromPending) activity.getString(R.string._pending) else ""
                                delay(WAIT_100)
                                display += activity.getString(R.string._to) + curBudgetDetailed.toAccount!!.accountName
                                display += if (toPending) activity.getString(R.string._pending) else ""

                                AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.confirm_completing_transaction))
                                    .setMessage(display)
                                    .setPositiveButton(activity.getString(R.string.perform_action)) { _, _ ->
                                        activity.lifecycleScope.launch {
                                            accountUpdateViewModel.performTransaction(
                                                Transactions(
                                                    nf.generateId(),
                                                    df.getCurrentDateAsString(),
                                                    curBudget.biBudgetName,
                                                    "",
                                                    curBudget.biRuleId,
                                                    curBudget.biToAccountId,
                                                    toPending,
                                                    curBudget.biFromAccountId,
                                                    fromPending,
                                                    curBudget.biProjectedAmount,
                                                    false,
                                                    df.getCurrentTimeAsString()
                                                )
                                            )
                                            budgetItemViewModel.updateBudgetItem(
                                                curBudget.copy(
                                                    biActualDate = df.getCurrentDateAsString(),
                                                    biProjectedAmount = 0.0,
                                                    biIsCompleted = true,
                                                    biUpdateTime = df.getCurrentTimeAsString()
                                                )
                                            )
                                        }
                                    }
                                    .setNegativeButton(activity.getString(R.string.cancel), null)
                                    .show()
                            }
                        }
                    }

                    2 -> {
                        mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetItemUpdate.route)
                    }

                    3 -> {
                        mainViewModel.setBudgetRuleDetailed(
                            BudgetRuleDetailed(
                                curBudgetDetailed.budgetRule,
                                curBudgetDetailed.toAccount,
                                curBudgetDetailed.fromAccount
                            )
                        )
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    }

                    4 -> {
                        AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.confirm_cancelling_budget_item))
                            .setMessage(
                                activity.getString(R.string.this_will_cancel) + curBudget.biBudgetName + activity.getString(
                                    R.string.with_the_amount_of
                                ) + nf.displayDollars(
                                    curBudget.biProjectedAmount
                                ) + activity.getString(R.string._remaining)
                            ).setPositiveButton(activity.getString(R.string.cancel_now)) { _, _ ->
                                budgetItemViewModel.cancelBudgetItem(
                                    curBudget.biRuleId,
                                    curBudget.biProjectedDate,
                                    DateFunctions().getCurrentTimeAsString()
                                )
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(WAIT_100)
                                    if (budgetList.isEmpty()) {
                                        withContext(Dispatchers.IO) {
                                            UpdateBudgetPredictions(activity).updatePredictions(
                                                java.time.LocalDate.now().plusMonths(2).toString()
                                            )
                                        }
                                    }
                                }
                            }.setNegativeButton(activity.getString(R.string.ignore_this), null)
                            .show()
                    }
                }
            }.setNegativeButton(activity.getString(R.string.cancel), null).show()
        },
        onBudgetItemLockClick = { budgetItemDetailed ->
            val budgetItem = budgetItemDetailed.budgetItem!!
            val df = DateFunctions()
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.lock_or_unlock)).setItems(
                    arrayOf(
                        activity.getString(R.string.lock) + budgetItem.biBudgetName,
                        activity.getString(R.string.un_lock) + budgetItem.biBudgetName,
                        activity.getString(R.string.lock_all_items_for_this_payday),
                        activity.getString(R.string.un_lock_all_items_for_this_payday)
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
                }.setNegativeButton(activity.getString(R.string.cancel), null).show()
        },
        onTransactionClick = { pendingTransaction ->
            val nf = NumberFunctions()
            val df = DateFunctions()
            val trans = pendingTransaction.transaction!!
            AlertDialog.Builder(activity).setTitle(
                activity.getString(R.string.choose_an_action_for) + nf.displayDollars(
                    trans.transAmount
                ) + activity.getString(
                    R.string._to_
                ) + trans.transName
            ).setItems(
                arrayOf(
                    activity.getString(R.string.complete_this_pending_transaction),
                    activity.getString(R.string.open_the_transaction_to_edit_it),
                    activity.getString(R.string.delete_this_pending_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        val display =
                            activity.getString(R.string.this_will_apply_the_amount_of) + nf.displayDollars(
                                trans.transAmount
                            ) + " " + activity.getString(R.string._to_) + (pendingTransaction.toAccount?.accountName
                                ?: "") +
                                    activity.getString(R.string._and_) + activity.getString(R.string._From_) + (pendingTransaction.fromAccount?.accountName
                                ?: "")
                        AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.confirm_completing_transaction))
                            .setMessage(display)
                            .setPositiveButton(activity.getString(R.string.confirm)) { _, _ ->
                                activity.lifecycleScope.launch {
                                    val updatedTrans = trans.copy(
                                        transToAccountPending = false,
                                        transFromAccountPending = false,
                                        transUpdateTime = df.getCurrentTimeAsString()
                                    )
                                    accountUpdateViewModel.updateTransaction(
                                        trans, updatedTrans
                                    )
                                }
                            }
                            .setNegativeButton(activity.getString(R.string.cancel), null).show()
                    }

                    1 -> {
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        mainViewModel.setTransactionDetailed(pendingTransaction)
                        activity.lifecycleScope.launch {
                            val trans = pendingTransaction.transaction
                            val transactionFull = transactionViewModel.getTransactionFull(
                                trans.transId,
                                trans.transToAccountId,
                                trans.transFromAccountId
                            )
                            mainViewModel.setOldTransaction(transactionFull)
                            delay(WAIT_250)
                            navController.navigate(Screen.TransactionUpdate.route)
                        }
                    }

                    2 -> {
                        activity.lifecycleScope.launch {
                            accountUpdateViewModel.deleteTransaction(
                                pendingTransaction.transaction
                            )
                        }
                    }
                }
            }.setNegativeButton(activity.getString(R.string.cancel), null).show()
        },
        onAccountClick = {
            mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
            val currentSelectedAsset = mainViewModel.getReturnToAsset()
            activity.lifecycleScope.launch {
                if (currentSelectedAsset != null) {
                    val account = withContext(Dispatchers.IO) {
                        accountViewModel.getAccountWithType(currentSelectedAsset)
                    }
                    mainViewModel.setAccountWithType(account)
                    navController.navigate(Screen.AccountUpdate.route)
                }
            }
        }
    )
}