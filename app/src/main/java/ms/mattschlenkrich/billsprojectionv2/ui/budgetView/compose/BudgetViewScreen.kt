package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import ms.mattschlenkrich.billsprojectionv2.common.settings.ColumnSettingsHelper
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
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
    onAccountClick: () -> Unit,
    onAddClick: () -> Unit,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onBudgetItemClick: (BudgetItemDetailed) -> Unit,
    onBudgetItemLockClick: (BudgetItemDetailed) -> Unit,
    onPendingHeaderLongClick: () -> Unit = {},
    onScheduledExpensesLongClick: () -> Unit = {},
    onPendingItemLongClick: (TransactionDetailed) -> Unit = {},
    onBudgetItemLongClick: (BudgetItemDetailed) -> Unit = {},
    selectedItems: Set<String> = emptySet(),
    selectedSum: Double = 0.0,
    selectedPendingItems: Set<Long> = emptySet(),
    selectedPendingSum: Double = 0.0,
    earliestPayDay: String? = null,
    isShowingAll: Boolean = false,
    isShowingAllPending: Boolean = false,
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnPreference = remember { SettingsManager(context).getSettings().columnPreference }
    val pendingColumnCount = ColumnSettingsHelper.calculateColumnCount(
        widthFactor = PendingItemDefaults.WIDTH_FACTOR,
        columnPreference = columnPreference,
        isLandscape = isLandscape
    )
    val budgetColumnCount = ColumnSettingsHelper.calculateColumnCount(
        widthFactor = BudgetListItemDefaults.WIDTH_FACTOR,
        columnPreference = columnPreference,
        isLandscape = isLandscape
    )

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val fontScale = density.fontScale.coerceAtLeast(0.8f)
    val effectiveWidthDp = with(density) { windowInfo.containerSize.width.toDp() } / fontScale
    val isTablet = effectiveWidthDp >= 600.dp
    val nf = LocalNumberFunctions.current
    val haptic = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState()
    val lazyListState = rememberLazyListState()

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
            onDismissRequest = onSheetDismiss,
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
            val budgetItem = details.budgetItem ?: return@forEach
            val isCredit = if (selectedAsset == ALL_ITEMS) {
                assetList.contains(details.toAccount?.accountName)
            } else {
                details.toAccount?.accountName == selectedAsset
            }
            if (isCredit) {
                credits += budgetItem.biProjectedAmount
            } else {
                debits += budgetItem.biProjectedAmount
            }
            if (budgetItem.biIsFixed) {
                fixedExpenses += budgetItem.biProjectedAmount
            } else {
                otherExpenses += budgetItem.biProjectedAmount
            }
        }
        BudgetTotals(credits, debits, fixedExpenses, otherExpenses)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_schedule_budget_item),
                    modifier = Modifier.size(ProjectFieldDefaults.iconSize() * 2f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 2.dp)
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
                selectedSum = selectedSum,
                showSelectedSum = selectedItems.isNotEmpty(),
                earliestPayDay = earliestPayDay,
            )

            if (pendingList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.title_pending_items) + if (selectedPendingSum != 0.0) " (${
                        stringResource(R.string.label_selected_colon)
                    }${
                        nf.displayDollars(
                            selectedPendingSum
                        )
                    })" else "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPendingHeaderLongClick()
                            }
                        )
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    color = if (pendingAmount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(pendingColumnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isShowingAllPending) 1000.dp else if (isTablet) 150.dp else 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalItemSpacing = 2.dp
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
                            onLongClick = { onPendingItemLongClick(pending) },
                            isSelected = selectedPendingItems.contains(pending.transaction?.transId)
                        )
                    }
                }
            }

            if (hasAnyBudgetItems) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.title_scheduled_expenses),
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
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(budgetColumnCount),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalItemSpacing = 2.dp
                    ) {
                        items(
                            budgetList,
                            key = { details ->
                                val item = details.budgetItem
                                if (item != null) "${item.biRuleId}_${item.biProjectedDate}"
                                else details.hashCode().toString()
                            }
                        ) { budgetItem ->
                            BudgetItemDisplay(
                                budgetItemDetailed = budgetItem,
                                isCredit = if (selectedAsset == ALL_ITEMS) {
                                    assetList.contains(budgetItem.toAccount?.accountName)
                                } else {
                                    budgetItem.toAccount?.accountName == selectedAsset
                                },
                                onClick = { onBudgetItemClick(budgetItem) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBudgetItemLongClick(budgetItem)
                                },
                                onLockClick = { onBudgetItemLockClick(budgetItem) },
                                isSelected = selectedItems.contains(
                                    "${budgetItem.budgetItem?.biRuleId}_${budgetItem.budgetItem?.biProjectedDate}"
                                )
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