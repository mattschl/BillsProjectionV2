package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

enum class TimeRange {
    SHOW_ALL,
    LAST_MONTH,
    DATE_RANGE
}

enum class AnalysisMode {
    BUDGET_RULE,
    ACCOUNT,
    SEARCH,
    NONE
}

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
        modifier = Modifier.fillMaxSize(),
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

            if (transactionList.isEmpty()) {
                item {
                    HelpCard()
                }
            } else {
                items(
                    transactionList,
                    key = { it.transaction?.transId ?: it.hashCode() }
                ) { transaction ->
                    TransactionItemCompose(
                        transactionDetailed = transaction,
                        onClick = { onTransactionClick(transaction) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.rules),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = budgetRuleName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBudgetRuleClick() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.account_name),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAccountClick() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.deep_red))
                    ) {
                        Text(
                            stringResource(R.string.go),
                            color = Color.White,
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
                    text = stringResource(R.string.show_all),
                    selected = timeRange == TimeRange.SHOW_ALL,
                    onSelect = { onTimeRangeChange(TimeRange.SHOW_ALL) },
                    modifier = Modifier.weight(1f)
                )
                TimeRangeOption(
                    text = stringResource(R.string.previous_month),
                    selected = timeRange == TimeRange.LAST_MONTH,
                    onSelect = { onTimeRangeChange(TimeRange.LAST_MONTH) },
                    modifier = Modifier.weight(1f)
                )
                TimeRangeOption(
                    text = stringResource(R.string.date_range),
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

@Composable
fun TimeRangeOption(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AnalysisCard(
    mode: AnalysisMode,
    transactionList: List<TransactionDetailed>,
    sumToAccount: Double?,
    sumFromAccount: Double?,
    sumCredits: Double?,
    maxVal: Double?,
    minVal: Double?,
    effectiveEndDate: String,
    nf: NumberFunctions,
    df: DateFunctions
) {
    if (mode == AnalysisMode.NONE) return

    var totals = 0.0
    transactionList.forEach { totals += it.transaction?.transAmount ?: 0.0 }

    val months = if (transactionList.isNotEmpty()) {
        val startDateStr = transactionList.last().transaction?.transDate ?: effectiveEndDate
        df.getMonthsBetween(startDateStr, effectiveEndDate) + 1
    } else 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (mode == AnalysisMode.ACCOUNT) {
                AnalysisRow(
                    label1 = stringResource(R.string.credit_average),
                    value1 = nf.displayDollars((sumToAccount ?: 0.0) / months) + " / $months",
                    label2 = stringResource(R.string.debit_average),
                    value2 = nf.displayDollars((sumFromAccount ?: 0.0) / months),
                    value2Color = Color.Red
                )
            } else {
                AnalysisRow(
                    label1 = stringResource(R.string.average),
                    value1 = nf.displayDollars(totals / months) + " / $months",
                    label2 = stringResource(R.string.highest),
                    value2 = nf.displayDollars(maxVal ?: 0.0)
                )
            }

            AnalysisRow(
                label1 = stringResource(R.string.lowest),
                value1 = nf.displayDollars(minVal ?: 0.0),
                label2 = stringResource(R.string.most_recent),
                value2 = nf.displayDollars(
                    transactionList.firstOrNull()?.transaction?.transAmount ?: 0.0
                )
            )

            if (mode == AnalysisMode.ACCOUNT) {
                AnalysisRow(
                    label1 = stringResource(R.string.total_credits),
                    value1 = nf.displayDollars(sumToAccount ?: 0.0),
                    label2 = stringResource(R.string.total_debits),
                    value2 = nf.displayDollars(sumFromAccount ?: 0.0),
                    value2Color = Color.Red
                )
            } else {
                AnalysisRow(
                    label1 = "Total (${transactionList.size})",
                    value1 = nf.displayDollars(sumCredits ?: 0.0),
                    label2 = "",
                    value2 = ""
                )
            }
        }
    }
}

@Composable
fun AnalysisRow(
    label1: String, value1: String,
    label2: String, value2: String,
    value1Color: Color = Color.Black,
    value2Color: Color = Color.Black
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            Text(
                text = label1,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value1,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = value1Color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text(
                text = label2,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value2,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = value2Color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun HelpCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.nothing_to_view_choose),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionItemCompose(
    transactionDetailed: TransactionDetailed,
    onClick: () -> Unit,
    nf: NumberFunctions,
    df: DateFunctions
) {
    val vf = remember { VisualsFunctions() }
    val trans = transactionDetailed.transaction!!
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .background(
                    Color(vf.getRandomColorInt()),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = trans.transName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = nf.displayDollars(trans.transAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${transactionDetailed.fromAccount?.accountName} → ${transactionDetailed.toAccount?.accountName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = df.getDisplayDate(trans.transDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (trans.transToAccountPending || trans.transFromAccountPending) {
                Text(
                    text = stringResource(R.string.pending),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            if (trans.transNote.isNotEmpty()) {
                Text(
                    text = trans.transNote,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}