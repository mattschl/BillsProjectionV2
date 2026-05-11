package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.AnalysisMode
import ms.mattschlenkrich.billsprojectionv2.common.TimeRange
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.components.TransactionHistoryItem
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

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
    onTransactionClick: (TransactionDetailed) -> Unit
) {
    val nf = NumberFunctions()
    val df = DateFunctions()

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
                    mode = mode,
                    transactionList = transactionList,
                    sumToAccount = sumToAccount,
                    sumFromAccount = sumFromAccount,
                    sumCredits = sumCredits,
                    maxVal = maxVal,
                    minVal = minVal,
                    effectiveEndDate = effectiveEndDate,
                    nf = nf,
                    df = df
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
                    TransactionHistoryItem(
                        transactionDetailed = transaction,
                        onClick = onTransactionClick,
                        nf = nf,
                        df = df
                    )
                }
            }
        }
    }
}

@Composable
fun CriteriaCard(
    budgetRuleName: String,
    accountName: String,
    isSearchEnabled: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    searchQueryInput: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchGo: () -> Unit,
    timeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    onDateRangeGo: () -> Unit,
    onBudgetRuleClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ProjectTextBox(
                label = stringResource(R.string.rules),
                value = budgetRuleName,
                onClick = onBudgetRuleClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProjectTextBox(
                label = stringResource(R.string.account_name),
                value = accountName,
                onClick = onAccountClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1.2f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isSearchEnabled, onCheckedChange = onSearchToggle)
                    Text(
                        text = stringResource(R.string.use_search_criteria),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isSearchEnabled) {
                    ProjectTextField(
                        value = searchQueryInput,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                stringResource(R.string.enter_criteria),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Button(
                        onClick = onSearchGo,
                        modifier = Modifier.padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text(
                            stringResource(R.string.go),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeRangeOption(
                    text = stringResource(R.string.all),
                    selected = timeRange == TimeRange.SHOW_ALL,
                    onSelect = { onTimeRangeChange(TimeRange.SHOW_ALL) },
                    modifier = Modifier.weight(1f)
                )
                TimeRangeOption(
                    text = stringResource(R.string.month),
                    selected = timeRange == TimeRange.LAST_MONTH,
                    onSelect = { onTimeRangeChange(TimeRange.LAST_MONTH) },
                    modifier = Modifier.weight(1f)
                )
                TimeRangeOption(
                    text = stringResource(R.string.year),
                    selected = timeRange == TimeRange.LAST_YEAR,
                    onSelect = { onTimeRangeChange(TimeRange.LAST_YEAR) },
                    modifier = Modifier.weight(1f)
                )
                TimeRangeOption(
                    text = stringResource(R.string.custom),
                    selected = timeRange == TimeRange.DATE_RANGE,
                    onSelect = { onTimeRangeChange(TimeRange.DATE_RANGE) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (timeRange == TimeRange.DATE_RANGE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectDateField(
                        value = startDate,
                        label = stringResource(R.string.start_date),
                        onValueChange = onStartDateChange,
                        modifier = Modifier.weight(1f)
                    )
                    ProjectDateField(
                        value = endDate,
                        label = stringResource(R.string.end_date),
                        onValueChange = onEndDateChange,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onDateRangeGo) {
                        Text(stringResource(R.string.go))
                    }
                }
            }
        }
    }
}