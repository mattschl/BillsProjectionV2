package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.common.AnalysisMode
import ms.mattschlenkrich.billsprojectionv2.common.TimeRange
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.settings.ColumnSettingsHelper
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionAnalysisScreen(
    // Filter State
    timeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    isSearchEnabled: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    searchQueryInput: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchGo: () -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    onDateRangeGo: () -> Unit,

    // Data
    budgetRuleName: String,
    accountName: String,
    mode: AnalysisMode,
    transactionList: List<TransactionDetailed>,
    sumToAccount: Double?,
    sumFromAccount: Double?,
    sumCredits: Double?,
    maxVal: Double?,
    minVal: Double?,
    effectiveEndDate: String,

    // Actions
    onBudgetRuleClick: () -> Unit,
    onAccountClick: () -> Unit,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onTransactionLongClick: (TransactionDetailed) -> Unit = {},
    selectedItems: Set<Long> = emptySet(),
    selectedSum: Double = 0.0,
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnPreference = remember { SettingsManager(context).getSettings().columnPreference }
    val columnCount = ColumnSettingsHelper.calculateColumnCount(
        widthFactor = TransactionHistoryItemDefaults.WIDTH_FACTOR,
        columnPreference = columnPreference,
        isLandscape = isLandscape
    )
    val sheetState = rememberModalBottomSheetState()

    if (sheetOptions.isNotEmpty()) {
        ActionBottomSheet(
            title = sheetTitle,
            options = sheetOptions,
            sheetState = sheetState,
            onDismissRequest = onSheetDismiss
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            item {
                CriteriaCard(
                    budgetRuleName = budgetRuleName,
                    accountName = accountName,
                    isSearchEnabled = isSearchEnabled,
                    onSearchToggle = onSearchToggle,
                    searchQueryInput = searchQueryInput,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchGo = onSearchGo,
                    timeRange = timeRange,
                    onTimeRangeChange = onTimeRangeChange,
                    startDate = startDate,
                    onStartDateChange = onStartDateChange,
                    endDate = endDate,
                    onEndDateChange = onEndDateChange,
                    onDateRangeGo = onDateRangeGo,
                    onBudgetRuleClick = onBudgetRuleClick,
                    onAccountClick = onAccountClick
                )
            }

            item {
                TransactionAnalysisSummaryCard(
                    transactionList = transactionList,
                    sumToAccount = sumToAccount,
                    sumFromAccount = sumFromAccount,
                    sumCredits = sumCredits,
                    maxVal = maxVal,
                    minVal = minVal,
                    effectiveEndDate = effectiveEndDate,
                    selectedSum = selectedSum,
                    showSelectedSum = selectedItems.isNotEmpty()
                )
            }

            if (mode == AnalysisMode.NONE || transactionList.isEmpty()) {
                item {
                    HelpCard(mode = mode)
                }
            } else if (columnCount > 1) {
                items(
                    transactionList.chunked(columnCount)
                ) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until columnCount) {
                            if (i < chunk.size) {
                                val transaction = chunk[i]
                                val haptic =
                                    androidx.compose.ui.platform.LocalHapticFeedback.current
                                Box(modifier = Modifier.weight(1f)) {
                                    TransactionHistoryItem(
                                        transactionDetailed = transaction,
                                        onClick = onTransactionClick,
                                        onLongClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            onTransactionLongClick(it)
                                        },
                                        isSelected = selectedItems.contains(transaction.transaction?.transId)
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                items(
                    transactionList,
                    key = { it.transaction?.transId ?: it.hashCode() }
                ) { transaction ->
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    TransactionHistoryItem(
                        transactionDetailed = transaction,
                        onClick = onTransactionClick,
                        onLongClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onTransactionLongClick(it)
                        },
                        isSelected = selectedItems.contains(transaction.transaction?.transId)
                    )
                }
            }
        }
    }
}