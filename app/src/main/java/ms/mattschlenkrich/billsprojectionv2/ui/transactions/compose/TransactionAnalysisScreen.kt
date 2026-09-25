package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.AnalysisMode
import ms.mattschlenkrich.billsprojectionv2.common.TimeRange
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
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
        modifier = Modifier.imePadding(),
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
                AnalysisCard(
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

@Composable
fun AnalysisCard(
    transactionList: List<TransactionDetailed>,
    sumToAccount: Double?,
    sumFromAccount: Double?,
    sumCredits: Double?,
    maxVal: Double?,
    minVal: Double?,
    effectiveEndDate: String,
    selectedSum: Double = 0.0,
    showSelectedSum: Boolean = false
) {
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.title_analysis_help),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.action_add)} ${transactionList.size}", // Reusing 'add' if count is missing
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${stringResource(R.string.label_end_date)} ${
                        df.getDisplayDate(
                            effectiveEndDate
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (sumCredits != null) {
                Text(
                    text = "${stringResource(R.string.label_total_credits)} ${
                        nf.displayDollars(
                            sumCredits
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (sumToAccount != null || sumFromAccount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (sumToAccount != null) {
                        Text(
                            text = "${stringResource(R.string.label_to_colon)} ${
                                nf.displayDollars(
                                    sumToAccount
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (sumFromAccount != null) {
                        Text(
                            text = "${stringResource(R.string.label_from_colon)} ${
                                nf.displayDollars(
                                    sumFromAccount
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (maxVal != null || minVal != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (maxVal != null) {
                        Text(
                            text = "${stringResource(R.string.label_highest)} ${
                                nf.displayDollars(
                                    maxVal
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (minVal != null) {
                        Text(
                            text = "${stringResource(R.string.label_lowest)} ${
                                nf.displayDollars(
                                    minVal
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (showSelectedSum) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${stringResource(R.string.label_selected_colon)} ${
                            nf.displayDollars(
                                selectedSum
                            )
                        }",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}