package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.BudgetItemDisplay
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BudgetViewScreen(
    assetList: List<String>,
    selectedAsset: String,
    onAssetSelected: (String) -> Unit,
    payDayList: List<String>,
    selectedPayDay: String,
    onPayDaySelected: (String) -> Unit,
    curAsset: AccountWithType?,
    pendingList: List<TransactionDetailed>,
    pendingAmount: Double,
    budgetList: List<BudgetItemDetailed>,
    hasAnyBudgetItems: Boolean,
    onAddClick: () -> Unit,
    onBudgetItemClick: (BudgetItemDetailed) -> Unit,
    onBudgetItemLockClick: (BudgetItemDetailed) -> Unit,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onAccountClick: () -> Unit,
    onScheduledExpensesLongClick: () -> Unit = {},
    isShowingAll: Boolean = false,
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {},
) {
    val nf = LocalNumberFunctions.current
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(sheetOptions) {
        if (sheetOptions.isNotEmpty()) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    if (sheetOptions.isNotEmpty()) {
        ActionBottomSheet(
            title = sheetTitle,
            options = sheetOptions,
            sheetState = sheetState,
            onDismissRequest = onSheetDismiss
        )
    }

    LaunchedEffect(isShowingAll) {
        if (isShowingAll) {
            lazyListState.animateScrollToItem(0)
        }
    }

    val budgetTotals = remember(budgetList, selectedAsset, assetList) {
        var credits = 0.0
        var debits = 0.0
        var fixedExpenses = 0.0
        var otherExpenses = 0.0

        budgetList.forEach { details ->
            val isCredit = if (selectedAsset == ALL_ITEMS) {
                assetList.contains(details.toAccount?.accountName)
            } else {
                details.toAccount?.accountName == selectedAsset
            }

            if (isCredit) {
                credits += details.budgetItem!!.biProjectedAmount
            } else {
                debits += details.budgetItem!!.biProjectedAmount
            }

            val isAssetRelated = if (selectedAsset == ALL_ITEMS) {
                assetList.contains(details.fromAccount?.accountName)
            } else {
                details.fromAccount?.accountName == selectedAsset
            }

            if (isAssetRelated) {
                if (details.budgetItem!!.biIsFixed) {
                    fixedExpenses += details.budgetItem!!.biProjectedAmount
                } else {
                    otherExpenses += details.budgetItem!!.biProjectedAmount
                }
            }
        }
        BudgetTotals(credits, debits, fixedExpenses, otherExpenses)
    }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val isTablet = with(density) { windowInfo.containerSize.width.toDp() >= 600.dp }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(if (isTablet) 8.dp else 4.dp)
        ) {
            SummaryCard(
                assetList = assetList,
                selectedAsset = selectedAsset,
                onAssetSelected = onAssetSelected,
                payDayList = payDayList,
                selectedPayDay = selectedPayDay,
                onPayDaySelected = onPayDaySelected,
                curAsset = curAsset,
                budgetTotals = budgetTotals,
                pendingAmount = pendingAmount,
                onAccountClick = onAccountClick,
            )

            if (pendingList.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.pending_items)} ${
                        nf.displayDollars(
                            pendingAmount
                        )
                    }",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    color = if (pendingAmount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isTablet) 150.dp else 100.dp)
                ) {
                    items(
                        pendingList,
                        key = { it.transaction?.transId ?: it.hashCode() }
                    ) { pending ->
                        PendingItem(
                            pending = pending,
                            selectedAsset = selectedAsset,
                            assetList = assetList,
                            onTransactionClick = onTransactionClick,
                        )
                    }
                }
            }

            if (hasAnyBudgetItems) {
                Text(
                    text = stringResource(R.string.budgeted_expenses),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onScheduledExpensesLongClick()
                            }
                        )
                        .padding(vertical = 1.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                if (budgetList.isNotEmpty()) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            budgetList,
                            key = { "${it.budgetItem?.biRuleId}_${it.budgetItem?.biProjectedDate}" }
                        ) { budgetItem ->
                            BudgetItemDisplay(
                                budgetItemDetailed = budgetItem,
                                isCredit = if (selectedAsset == ALL_ITEMS) {
                                    assetList.contains(budgetItem.toAccount?.accountName)
                                } else {
                                    budgetItem.toAccount?.accountName == selectedAsset
                                },
                                onClick = { onBudgetItemClick(budgetItem) },
                                onLockClick = { onBudgetItemLockClick(budgetItem) }
                            )
                        }
                    }
                }
            } else {
                NoBudgetItemsCard()
            }
        }
    }
}

data class BudgetTotals(
    val credits: Double,
    val debits: Double,
    val fixedExpenses: Double,
    val otherExpenses: Double
)