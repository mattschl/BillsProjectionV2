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
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_VIEW
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

    val allPayDaysList by budgetItemViewModel.getPayDays(ALL_ITEMS)
        .observeAsState(initial = emptyList())
    val earliestPayDay = remember(allPayDaysList) {
        allPayDaysList.firstOrNull()
    }

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

    var selectedItems by remember { mutableStateOf(emptySet<String>()) }
    var selectedPendingItems by remember { mutableStateOf(emptySet<Long>()) }

    var showAllBudgetItems by remember { mutableStateOf(value = false) }
    var showAllPendingItems by remember { mutableStateOf(value = false) }

    val allBudgetList by budgetItemViewModel.getBudgetItemsAll(selectedAsset, selectedPayDay)
        .observeAsState(initial = emptyList())

    LaunchedEffect(selectedAsset, selectedPayDay) {
        selectedItems = emptySet()
        selectedPendingItems = emptySet()
    }

    val budgetList = remember(allBudgetList, showAllBudgetItems) {
        if (showAllBudgetItems) {
            allBudgetList
        } else {
            allBudgetList.filter {
                val item = it.budgetItem
                if (item != null) {
                    !item.biIsCancelled && !item.biIsCompleted && !item.biIsDeleted
                } else false
            }
        }
    }

    val pendingAmount = remember(pendingList, selectedAsset, assetList) {
        BudgetViewCalculations.calculatePendingAmount(pendingList, selectedAsset, assetList)
    }

    val selectedSum = remember(selectedItems, allBudgetList, selectedAsset, assetList) {
        BudgetViewCalculations.calculateSelectedSum(
            selectedItems,
            allBudgetList,
            selectedAsset,
            assetList
        )
    }

    val selectedPendingSum = remember(selectedPendingItems, pendingList, selectedAsset, assetList) {
        BudgetViewCalculations.calculateSelectedPendingSum(
            selectedPendingItems,
            pendingList,
            selectedAsset,
            assetList
        )
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
        earliestPayDay = earliestPayDay,
        curAsset = curAsset,
        pendingList = pendingList,
        pendingAmount = pendingAmount,
        budgetList = budgetList,
        hasAnyBudgetItems = allBudgetList.isNotEmpty(),
        onAccountClick = {
            mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
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
        onBudgetItemLongClick = { budgetItemDetailed ->
            val key =
                "${budgetItemDetailed.budgetItem?.biRuleId}_${budgetItemDetailed.budgetItem?.biProjectedDate}"
            selectedItems = if (selectedItems.contains(key)) {
                selectedItems - key
            } else {
                selectedItems + key
            }
        },
        selectedItems = selectedItems,
        selectedSum = selectedSum,
        onPendingItemLongClick = { pendingDetailed ->
            pendingDetailed.transaction?.transId?.let { id ->
                selectedPendingItems = if (selectedPendingItems.contains(id)) {
                    selectedPendingItems - id
                } else {
                    selectedPendingItems + id
                }
            }
        },
        selectedPendingItems = selectedPendingItems,
        selectedPendingSum = selectedPendingSum,
        onAddClick = {
            actionSheetState.show(
                activity.getString(R.string.title_choose_action),
                BudgetViewActionHelper.getAddOptions(
                    activity = activity,
                    onNewBudgetItem = {
                        mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
                        navController.navigate(Screen.BudgetItemAdd.route)
                    },
                    onUnscheduledTransaction = {
                        mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
                        mainViewModel.setTransactionDetailed(null)
                        navController.navigate(Screen.TransactionAdd.route)
                    },
                ),
            )
        },
        onBudgetItemClick = { curBudgetDetailed ->
            if (selectedItems.isNotEmpty() || selectedPendingItems.isNotEmpty()) {
                selectedItems = emptySet()
                selectedPendingItems = emptySet()
            } else {
                curBudgetDetailed.budgetItem?.let { curBudget ->
                    actionSheetState.show(
                        "${activity.getString(R.string.title_choose_action_for)} ${curBudget.biBudgetName}",
                        BudgetViewActionHelper.getBudgetItemOptions(
                            activity = activity,
                            curBudgetDetailed = curBudgetDetailed,
                            nf = nf,
                            onPerformCustom = {
                                mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                                mainViewModel.setTransactionDetailed(null)
                                mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
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
                                                    curBudgetDetailed,
                                                ),
                                                nf,
                                            )

                                        AlertDialog.Builder(activity)
                                            .setTitle(activity.getString(R.string.title_confirm_complete_transaction))
                                            .setMessage(display)
                                            .setPositiveButton(activity.getString(R.string.action_confirm)) { _, _ ->
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
                                            .setNegativeButton(
                                                activity.getString(R.string.action_cancel),
                                                null
                                            )
                                            .show()
                                    }
                                }
                            },
                            onAdjustProjection = {
                                mainViewModel.setBudgetItemDetailed(curBudgetDetailed)
                                mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
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
                                mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
                                navController.navigate(Screen.BudgetRuleUpdate.route)
                            },
                            onCancelItem = {
                                AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.title_confirm_cancel_budget_item))
                                    .setMessage(
                                        "${activity.getString(R.string.msg_will_cancel)}${curBudget.biBudgetName}${
                                            activity.getString(
                                                R.string.msg_with_amount
                                            )
                                        }${nf.displayDollars(curBudget.biProjectedAmount)}${
                                            activity.getString(
                                                R.string.text_remaining_suffix
                                            )
                                        }"
                                    )
                                    .setPositiveButton(activity.getString(R.string.action_confirm)) { _, _ ->
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
                                    }.setNegativeButton(
                                        activity.getString(R.string.action_cancel),
                                        null
                                    )
                                    .show()
                            }
                        )
                    )
                }
            }
        },
        onBudgetItemLockClick = { budgetItemDetailed ->
            budgetItemDetailed.budgetItem?.let { budgetItem ->
                actionSheetState.show(
                    activity.getString(R.string.title_lock_unlock),
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
            }
        },
        onTransactionClick = { pendingTransaction ->
            if (selectedItems.isNotEmpty() || selectedPendingItems.isNotEmpty()) {
                selectedItems = emptySet()
                selectedPendingItems = emptySet()
            } else {
                pendingTransaction.transaction?.let { trans ->
                    actionSheetState.show(
                        "${activity.getString(R.string.title_choose_action_for)}${
                            nf.displayDollars(
                                trans.transAmount
                            )
                        }${
                            activity.getString(R.string.text_to_padded)
                        }${trans.transName}",
                        BudgetViewActionHelper.getPendingTransactionOptions(
                            activity = activity,
                            onComplete = {
                                val display =
                                    TransactionMessageHelper.buildPendingCompletionMessage(
                                        activity, pendingTransaction, nf
                                    )
                                AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.title_confirm_complete_transaction))
                                    .setMessage(display)
                                    .setPositiveButton(activity.getString(R.string.action_confirm)) { _, _ ->
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
                                    .setNegativeButton(
                                        activity.getString(R.string.action_cancel),
                                        null
                                    )
                                    .show()
                            },
                            onEdit = {
                                mainViewModel.setCallingFragments(SCREEN_BUDGET_VIEW)
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
                }
            }
        },
        onScheduledExpensesLongClick = {
            actionSheetState.show(
                activity.getString(R.string.title_choose_action),
                BudgetViewActionHelper.getScheduledHeaderOptions(
                    activity = activity,
                    isShowingAll = showAllBudgetItems,
                    onToggleShowAll = {
                        showAllBudgetItems = !showAllBudgetItems
                    },
                    onCancelAllRegular = {
                        AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.title_confirm_cancel_all))
                            .setMessage(activity.getString(R.string.msg_confirm_cancel_all_regular))
                            .setPositiveButton(activity.getString(R.string.action_confirm)) { _, _ ->
                                budgetItemViewModel.cancelAllRegularItemsForPayDay(
                                    selectedPayDay,
                                    df.getCurrentTimeAsString()
                                )
                            }
                            .setNegativeButton(activity.getString(R.string.action_cancel), null)
                            .show()
                    }
                )
            )
        },
        onPendingHeaderLongClick = {
            showAllPendingItems = !showAllPendingItems
        },
        isShowingAllPending = showAllPendingItems,
        isShowingAll = showAllBudgetItems,
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
    ) {
        actionSheetState.dismiss()
    }
    ManagedActionBottomSheet(actionSheetState)
}