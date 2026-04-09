package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        mainActivity.topMenuBar.title = getString(R.string.transaction_analysis)
        transactionViewModel = mainActivity.transactionViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.intValue >= 0) {
                        TransactionAnalysisScreen()
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun TransactionAnalysisScreenPreview() {
        BillsProjectionTheme {
            TransactionAnalysisScreen()
        }
    }

    @Composable
    fun TransactionAnalysisScreen() {
        var timeRange by remember { mutableStateOf(TimeRange.SHOW_ALL) }
        var isSearchEnabled by remember { mutableStateOf(false) }
        var searchQueryInput by remember { mutableStateOf("") }
        var searchQueryActual by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf(df.getFirstOfMonth(df.getCurrentDateAsString())) }
        var endDate by remember { mutableStateOf(df.getCurrentDateAsString()) }

        val budgetRuleDetailed = mainViewModel.getBudgetRuleDetailed()
        val accountWithType = mainViewModel.getAccountWithType()

        val mode = when {
            isSearchEnabled -> AnalysisMode.SEARCH
            budgetRuleDetailed != null -> AnalysisMode.BUDGET_RULE
            accountWithType != null -> AnalysisMode.ACCOUNT
            else -> AnalysisMode.NONE
        }

        val effectiveStartDate = when (timeRange) {
            TimeRange.LAST_MONTH -> df.getFirstOfPreviousMonth(df.getCurrentDateAsString())
            TimeRange.DATE_RANGE -> startDate
            else -> null
        }
        val effectiveEndDate = when (timeRange) {
            TimeRange.LAST_MONTH -> df.getLastOfPreviousMonth(df.getCurrentDateAsString())
            TimeRange.DATE_RANGE -> endDate
            else -> null
        }

        val transactionListResult by remember(
            mode,
            budgetRuleDetailed?.budgetRule?.ruleId,
            accountWithType?.account?.accountId,
            searchQueryActual,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.BUDGET_RULE -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getActiveTransactionsDetailed(
                            budgetRuleDetailed!!.budgetRule!!.ruleId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getActiveTransactionsDetailed(
                            budgetRuleDetailed!!.budgetRule!!.ruleId
                        )
                    }
                }

                AnalysisMode.ACCOUNT -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getActiveTransactionByAccount(
                            accountWithType!!.account.accountId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getActiveTransactionByAccount(
                            accountWithType!!.account.accountId
                        )
                    }
                }

                AnalysisMode.SEARCH -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getActiveTransactionBySearch(
                            "%$searchQueryActual%",
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getActiveTransactionBySearch("%$searchQueryActual%")
                    }
                }

                else -> MutableLiveData(emptyList())
            } as LiveData<List<TransactionDetailed>>
        }.observeAsState(emptyList())
        val transactionList = transactionListResult ?: emptyList()

        val sumToAccount by remember(
            mode,
            accountWithType?.account?.accountId,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.ACCOUNT -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getSumTransactionToAccount(
                            accountWithType!!.account.accountId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getSumTransactionToAccount(
                            accountWithType!!.account.accountId
                        )
                    }
                }

                else -> MutableLiveData(null)
            } as LiveData<Double?>
        }.observeAsState(null)

        val sumFromAccount by remember(
            mode,
            accountWithType?.account?.accountId,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.ACCOUNT -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getSumTransactionFromAccount(
                            accountWithType!!.account.accountId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getSumTransactionFromAccount(
                            accountWithType!!.account.accountId
                        )
                    }
                }

                else -> MutableLiveData(null)
            } as LiveData<Double?>
        }.observeAsState(null)

        val sumCredits by remember(
            mode,
            budgetRuleDetailed?.budgetRule?.ruleId,
            searchQueryActual,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.BUDGET_RULE -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getSumTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getSumTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId
                        )
                    }
                }

                AnalysisMode.SEARCH -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getSumTransactionBySearch(
                            "%$searchQueryActual%",
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getSumTransactionBySearch("%$searchQueryActual%")
                    }
                }

                else -> MutableLiveData(null)
            } as LiveData<Double?>
        }.observeAsState(null)

        val maxVal by remember(
            mode,
            budgetRuleDetailed?.budgetRule?.ruleId,
            accountWithType?.account?.accountId,
            searchQueryActual,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.BUDGET_RULE -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMaxTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMaxTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId
                        )
                    }
                }

                AnalysisMode.ACCOUNT -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMaxTransactionByAccount(
                            accountWithType!!.account.accountId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMaxTransactionByAccount(
                            accountWithType!!.account.accountId
                        )
                    }
                }

                AnalysisMode.SEARCH -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMaxTransactionBySearch(
                            "%$searchQueryActual%",
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMaxTransactionBySearch("%$searchQueryActual%")
                    }
                }

                else -> MutableLiveData(null)
            } as LiveData<Double?>
        }.observeAsState(null)

        val minVal by remember(
            mode,
            budgetRuleDetailed?.budgetRule?.ruleId,
            accountWithType?.account?.accountId,
            searchQueryActual,
            effectiveStartDate,
            effectiveEndDate
        ) {
            when (mode) {
                AnalysisMode.BUDGET_RULE -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMinTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMinTransactionByBudgetRule(
                            budgetRuleDetailed!!.budgetRule!!.ruleId
                        )
                    }
                }

                AnalysisMode.ACCOUNT -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMinTransactionByAccount(
                            accountWithType!!.account.accountId,
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMinTransactionByAccount(
                            accountWithType!!.account.accountId
                        )
                    }
                }

                AnalysisMode.SEARCH -> {
                    if (effectiveStartDate != null && effectiveEndDate != null) {
                        transactionViewModel.getMinTransactionBySearch(
                            "%$searchQueryActual%",
                            effectiveStartDate,
                            effectiveEndDate
                        )
                    } else {
                        transactionViewModel.getMinTransactionBySearch("%$searchQueryActual%")
                    }
                }

                else -> MutableLiveData(null)
            } as LiveData<Double?>
        }.observeAsState(null)

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
                        budgetRuleName = budgetRuleDetailed?.budgetRule?.budgetRuleName
                            ?: stringResource(R.string.no_budget_rule_selected),
                        accountName = accountWithType?.account?.accountName
                            ?: stringResource(R.string.no_account_selected),
                        isSearchEnabled = isSearchEnabled,
                        onSearchToggle = { isSearchEnabled = it },
                        searchQueryInput = searchQueryInput,
                        onSearchQueryChange = { searchQueryInput = it },
                        onSearchGo = { searchQueryActual = searchQueryInput },
                        timeRange = timeRange,
                        onTimeRangeChange = { timeRange = it },
                        startDate = startDate,
                        onStartDateChange = { startDate = it },
                        endDate = endDate,
                        onEndDateChange = { endDate = it },
                        onDateRangeGo = { /* Just triggers recompose as effective dates change */ },
                        onBudgetRuleClick = { gotoBudgetRule() },
                        onAccountClick = { gotoAccount() }
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
                        effectiveEndDate = effectiveEndDate ?: df.getCurrentDateAsString()
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
                        TransactionItemCompose(transaction)
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
        effectiveEndDate: String
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun TransactionItemCompose(transactionDetailed: TransactionDetailed) {
        val vf = remember { VisualsFunctions() }
        val trans = transactionDetailed.transaction!!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptions(transactionDetailed) }
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

    private fun chooseOptions(transactionDetailed: TransactionDetailed) {
        var display = ""
        if (transactionDetailed.transaction!!.transToAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + getString(R.string._to_) + (transactionDetailed.toAccount?.accountName ?: "")
        }
        if (transactionDetailed.transaction.transToAccountPending) {
            display += getString(R.string._pending)
        }
        if (display != "" && transactionDetailed.transaction.transFromAccountPending) {
            display += getString(R.string._and)
        }
        if (transactionDetailed.transaction.transFromAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName
        }
        android.app.AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + transactionDetailed.transaction.transName
        ).setItems(
            arrayOf(
                getString(R.string.edit_this_transaction),
                display,
                getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                getString(R.string.delete_this_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    gotoTransactionUpdate(transactionDetailed)
                }

                1 -> {
                    if (transactionDetailed.transaction.transToAccountPending || transactionDetailed.transaction.transFromAccountPending) {
                        completePendingTransactions(transactionDetailed)
                    }
                }

                2 -> {
                    gotoBudgetRuleUpdate(transactionDetailed)
                }

                3 -> {
                    confirmDeleteTransaction(transactionDetailed)
                }
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun gotoBudgetRuleUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(TAG)
        budgetRuleViewModel.getBudgetRuleFullLive(
            transactionDetailed.transaction!!.transRuleId
        ).observe(viewLifecycleOwner) { bRuleDetailed ->
            mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
            gotoBudgetRuleUpdateFragment()
        }
    }

    private fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        transactionDetailed.transaction!!.apply {
            val newTransaction = Transactions(
                transId,
                transDate,
                transName,
                transNote,
                transRuleId,
                transToAccountId,
                false,
                transFromAccountId,
                false,
                transAmount,
                transIsDeleted,
                transUpdateTime
            )
            lifecycleScope.launch(Dispatchers.IO) {
                accountUpdateViewModel.updateTransaction(
                    transactionDetailed.transaction, newTransaction
                )
            }
        }
    }

    private fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        android.app.AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.are_you_sure_you_want_to_delete) + transactionDetailed.transaction!!.transName
        ).setPositiveButton(getString(R.string.delete)) { _, _ ->
            deleteTransaction(transactionDetailed.transaction!!)
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun deleteTransaction(transaction: Transactions) {
        lifecycleScope.launch(Dispatchers.IO) {
            accountUpdateViewModel.deleteTransaction(
                transaction
            )
        }
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(transactionDetailed)
        lifecycleScope.launch(Dispatchers.IO) {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transactionDetailed.transaction!!.transId,
                    transactionDetailed.transaction.transToAccountId,
                    transactionDetailed.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(oldTransactionFull.await())
            launch(Dispatchers.Main) {
                delay(ms.mattschlenkrich.billsprojectionv2.common.WAIT_250)
                gotoTransactionUpdateFragment()
            }
        }
    }

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

    private fun gotoAccount() {
        mainViewModel.eraseAll()
        mainViewModel.setCallingFragments(TAG)

        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_accountChooseFragment
        )
    }

    private fun gotoBudgetRule() {
        mainViewModel.eraseAll()
        mainViewModel.setCallingFragments(TAG)
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_budgetRuleChooseFragment
        )
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_transactionUpdateFragment
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_budgetRuleUpdateFragment
        )
    }

    // Keep this for MainActivity.onResume compatibility if needed
    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.transaction_analysis)
        populateValues()
        refreshKey.intValue++
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
    }

    fun populateValues() {
        // Compose handles this through state, but we might want to trigger a refresh
        // by updating a state variable if necessary.
    }
}