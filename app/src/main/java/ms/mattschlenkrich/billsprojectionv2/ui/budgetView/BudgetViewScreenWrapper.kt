package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.common.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose.BudgetViewScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController,
) {
    val mainViewModel = activity.mainViewModel
    val accountViewModel = activity.accountViewModel
    val budgetItemViewModel = activity.budgetItemViewModel
    val transactionViewModel = activity.transactionViewModel
    val accountUpdateViewModel = activity.accountUpdateViewModel
    val df = LocalDateFunctions.current
    val nf = LocalNumberFunctions.current
    val actionSheetState = rememberActionSheetState()

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

    LaunchedEffect(assetList, selectedAsset) {
        if (selectedAsset.isEmpty() && assetList.isNotEmpty()) {
            val settings = SettingsManager(activity).getSettings()
            val defaultAccount = settings.defaultAccount ?: ALL_ITEMS
            if (assetList.contains(defaultAccount)) {
                mainViewModel.setReturnToAsset(defaultAccount)
            } else {
                mainViewModel.setReturnToAsset(assetList.first())
            }
        }
    }

    val payDayList by budgetItemViewModel.getPayDays(selectedAsset)
        .observeAsState(initial = emptyList())
    val selectedPayDay = mainViewModel.getReturnToPayDay() ?: ""

    LaunchedEffect(payDayList, selectedAsset) {
        if (payDayList.isNotEmpty()) {
            val currentPayDay = mainViewModel.getReturnToPayDay()
            if (currentPayDay == null) {
                mainViewModel.setReturnToPayDay(payDayList.first())
            } else if (!payDayList.contains(currentPayDay)) {
                val today = df.getCurrentDateAsString()
                val nextBestPayDay = payDayList.find { it >= today } ?: payDayList.first()
                mainViewModel.setReturnToPayDay(nextBestPayDay)
            }
        }
    }

    val curAsset by accountViewModel.getAccountDetailed(selectedAsset)
        .observeAsState(initial = null)

    val pendingList by transactionViewModel.getPendingTransactionsDetailed(selectedAsset)
        .observeAsState(initial = emptyList())

    var showAllBudgetItems by remember { mutableStateOf(value = false) }

    val allBudgetList by budgetItemViewModel.getBudgetItemsAll(selectedAsset, selectedPayDay)
        .observeAsState(initial = emptyList())

    val budgetList = remember(allBudgetList, showAllBudgetItems) {
        if (showAllBudgetItems) {
            allBudgetList
        } else {
            allBudgetList.filter {
                val item = it.budgetItem!!
                !item.biIsCancelled && !item.biIsCompleted && !item.biIsDeleted
            }
        }
    }

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
        hasAnyBudgetItems = allBudgetList.isNotEmpty(),
        onAddClick = {
            actionSheetState.show(
                activity.getString(R.string.choose_an_action),
                BudgetViewActionHelper.getAddOptions(
                    activity = activity,
                    onNewBudgetItem = {
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetItemAdd.route)
                    },
                    onUnscheduledTransaction = {
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        mainViewModel.setTransactionDetailed(null)
                        navController.navigate(Screen.TransactionAdd.route)
                    }
                )
            )
        },
        onBudgetItemClick = { curBudgetDetailed ->
            val curBudget = curBudgetDetailed.budgetItem!!
            actionSheetState.show(
                "${activity.getString(R.string.choose_an_action_for)} ${curBudget.biBudgetName}",
                BudgetViewActionHelper.getBudgetItemOptions(
                    activity = activity,
                    curBudgetDetailed = curBudgetDetailed,
                    nf = nf,
                    onPerformCustom = {
                        mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                        mainViewModel.setTransactionDetailed(null)
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.TransactionPerform.route)
                    },
                    onPerformFull = {
                        if (curBudget.biProjectedAmount > 0.0) {
                            activity.lifecycleScope.launch {
                                val toPending =
                                    accountUpdateViewModel.isTransactionPending(curBudget.biToAccountId)
                                val fromPending =
                                    accountUpdateViewModel.isTransactionPending(curBudget.biFromAccountId)

                                val display =
                                    TransactionMessageHelper.buildConfirmationMessage(
                                        activity,
                                        activity.transactionViewModel.createTransactionDetailedFromBudgetItem(
                                            curBudgetDetailed
                                        ),
                                        nf
                                    )

                                AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.confirm_completing_transaction))
                                    .setMessage(display)
                                    .setPositiveButton(activity.getString(R.string.perform_action)) { _, _ ->
                                        activity.lifecycleScope.launch {
                                            accountUpdateViewModel.performTransaction(
                                                Transactions(
                                                    transId = nf.generateId(),
                                                    transDate = df.getCurrentDateAsString(),
                                                    transName = curBudget.biBudgetName,
                                                    transNote = "",
                                                    transRuleId = curBudget.biRuleId,
                                                    transToAccountId = curBudget.biToAccountId,
                                                    transToAccountPending = toPending,
                                                    transFromAccountId = curBudget.biFromAccountId,
                                                    transFromAccountPending = fromPending,
                                                    transAmount = curBudget.biProjectedAmount,
                                                    transIsDeleted = false,
                                                    transUpdateTime = df.getCurrentTimeAsString(),
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
                    },
                    onAdjustProjection = {
                        mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetItemUpdate.route)
                    },
                    onGoToRule = {
                        mainViewModel.setBudgetRuleDetailed(
                            BudgetRuleDetailed(
                                curBudgetDetailed.budgetRule,
                                curBudgetDetailed.toAccount,
                                curBudgetDetailed.fromAccount
                            )
                        )
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    },
                    onCancelItem = {
                        AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.confirm_cancelling_budget_item))
                            .setMessage(
                                "${activity.getString(R.string.this_will_cancel)}${curBudget.biBudgetName}${
                                    activity.getString(
                                        R.string.with_the_amount_of
                                    )
                                }${nf.displayDollars(curBudget.biProjectedAmount)}${
                                    activity.getString(
                                        R.string._remaining
                                    )
                                }"
                            ).setPositiveButton(activity.getString(R.string.cancel_now)) { _, _ ->
                                budgetItemViewModel.cancelBudgetItem(
                                    curBudget.biRuleId,
                                    curBudget.biProjectedDate,
                                    df.getCurrentTimeAsString()
                                )
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (budgetList.isEmpty()) {
                                        withContext(Dispatchers.IO) {
                                            UpdateBudgetPredictions(activity).updatePredictions(
                                                LocalDate.now().plusMonths(2).toString()
                                            )
                                        }
                                    }
                                }
                            }.setNegativeButton(activity.getString(R.string.ignore_this), null)
                            .show()
                    },
                )
            )
        },
        onBudgetItemLockClick = { budgetItemDetailed ->
            val budgetItem = budgetItemDetailed.budgetItem!!
            actionSheetState.show(
                activity.getString(R.string.lock_or_unlock),
                BudgetViewActionHelper.getLockOptions(
                    activity = activity,
                    budgetItemName = budgetItem.biBudgetName,
                    onLockItem = {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            lock = true,
                            budgetRuleId = budgetItem.biRuleId,
                            payDay = budgetItem.biPayDay,
                            updateTime = df.getCurrentTimeAsString(),
                        )
                    },
                    onUnlockItem = {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            lock = false,
                            budgetRuleId = budgetItem.biRuleId,
                            payDay = budgetItem.biPayDay,
                            updateTime = df.getCurrentTimeAsString(),
                        )
                    },
                    onLockPayDay = {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            lock = true,
                            payDay = budgetItem.biPayDay,
                            updateTime = df.getCurrentTimeAsString(),
                        )
                    },
                    onUnlockPayDay = {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            lock = false,
                            payDay = budgetItem.biPayDay,
                            updateTime = df.getCurrentTimeAsString(),
                        )
                    }
                )
            )
        },
        onTransactionClick = { pendingTransaction ->
            val trans = pendingTransaction.transaction!!
            actionSheetState.show(
                "${activity.getString(R.string.choose_an_action_for)}${nf.displayDollars(trans.transAmount)}${
                    activity.getString(R.string._to_)
                }${trans.transName}",
                BudgetViewActionHelper.getPendingTransactionOptions(
                    activity = activity,
                    onComplete = {
                        val display = TransactionMessageHelper.buildPendingCompletionMessage(
                            activity, pendingTransaction, nf
                        )
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
                    },
                    onEdit = {
                        mainViewModel.setCallingFragments(FRAG_BUDGET_VIEW)
                        mainViewModel.setTransactionDetailed(pendingTransaction)
                        activity.lifecycleScope.launch {
                            val transId = pendingTransaction.transaction.transId
                            val transactionFull = transactionViewModel.getTransactionFull(
                                transId,
                                pendingTransaction.transaction.transToAccountId,
                                pendingTransaction.transaction.transFromAccountId
                            )
                            mainViewModel.setOldTransaction(transactionFull)
                            navController.navigate(Screen.TransactionUpdate.route)
                        }
                    },
                    onDelete = {
                        activity.lifecycleScope.launch {
                            accountUpdateViewModel.deleteTransaction(
                                pendingTransaction.transaction
                            )
                        }
                    }
                )
            )
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
        },
        onScheduledExpensesLongClick = {
            showAllBudgetItems = !showAllBudgetItems
        },
        isShowingAll = showAllBudgetItems,
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
    ) {
        actionSheetState.dismiss()
    }
    ManagedActionBottomSheet(actionSheetState)
}