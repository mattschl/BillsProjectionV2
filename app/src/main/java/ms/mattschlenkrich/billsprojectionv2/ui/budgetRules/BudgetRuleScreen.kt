package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectIntField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed

@Composable
fun BudgetRulesListScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    budgetRulesDetailed: List<BudgetRuleDetailed>,
    onAddClick: () -> Unit,
    onItemClick: (BudgetRuleDetailed) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_budget_rule),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            ProjectTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = stringResource(R.string.search),
                placeholder = { Text(stringResource(R.string.enter_criteria)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (budgetRulesDetailed.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_budget_rules_to_view),
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(budgetRulesDetailed) { budgetRuleDetailed ->
                            BudgetRuleItem(
                                budgetRuleDetailed = budgetRuleDetailed,
                                onClick = { onItemClick(budgetRuleDetailed) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRuleScreen(
    name: String,
    onNameChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    isFixed: Boolean,
    onIsFixedChange: (Boolean) -> Unit,
    isPayDay: Boolean,
    onIsPayDayChange: (Boolean) -> Unit,
    isAuto: Boolean,
    onIsAutoChange: (Boolean) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    frequencyType: Int,
    onFrequencyTypeChange: (Int) -> Unit,
    frequencyCount: String,
    onFrequencyCountChange: (String) -> Unit,
    dayOfWeek: Int,
    onDayOfWeekChange: (Int) -> Unit,
    leadDays: String,
    onLeadDaysChange: (String) -> Unit,
    toAccount: Account?,
    fromAccount: Account?,
    onChooseAccount: (String) -> Unit,
    onGotoCalculator: () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit = {}
) {
    val frequencyTypes = stringArrayResource(R.array.frequency_types)
    val daysOfWeek = stringArrayResource(R.array.days_of_week)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectTextField(
                value = name,
                onValueChange = onNameChange,
                label = stringResource(R.string.budget_rule_name),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectTextBox(
                    label = stringResource(R.string.to_this_account),
                    value = toAccount?.accountName ?: "",
                    onClick = { onChooseAccount(REQUEST_TO_ACCOUNT) },
                    modifier = Modifier.weight(1f)
                )

                ProjectTextBox(
                    label = stringResource(R.string.from_this_account),
                    value = fromAccount?.accountName ?: "",
                    onClick = { onChooseAccount(REQUEST_FROM_ACCOUNT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectBalanceField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = stringResource(R.string.amount),
                    modifier = Modifier.weight(1f),
                    onIconClick = onGotoCalculator
                )
                LabeledCheckbox(
                    label = stringResource(R.string.fixed),
                    checked = isFixed,
                    onCheckedChange = onIsFixedChange,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledCheckbox(
                    label = stringResource(R.string.make_a_pay_day),
                    checked = isPayDay,
                    onCheckedChange = onIsPayDayChange,
                    modifier = Modifier.weight(1f)
                )
                LabeledCheckbox(
                    label = stringResource(R.string.automatic_payment),
                    checked = isAuto,
                    onCheckedChange = onIsAutoChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectDateField(
                    value = startDate,
                    onValueChange = onStartDateChange,
                    label = stringResource(R.string.start_date),
                    modifier = Modifier.weight(1f)
                )
                ProjectDateField(
                    value = endDate,
                    onValueChange = onEndDateChange,
                    label = stringResource(R.string.end_date),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.scheduling_rules),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdown(
                    label = stringResource(R.string.budget_rules),
                    options = frequencyTypes.toList(),
                    selectedIndex = frequencyType,
                    onItemSelected = onFrequencyTypeChange,
                    modifier = Modifier.weight(1.5f)
                )

                ProjectIntField(
                    value = frequencyCount,
                    onValueChange = onFrequencyCountChange,
                    label = stringResource(R.string.times),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdown(
                    label = stringResource(R.string.on_day),
                    options = daysOfWeek.toList(),
                    selectedIndex = dayOfWeek,
                    onItemSelected = onDayOfWeekChange,
                    modifier = Modifier.weight(1.5f)
                )

                ProjectIntField(
                    value = leadDays,
                    onValueChange = onLeadDaysChange,
                    label = stringResource(R.string.lead_days),
                    modifier = Modifier.weight(1f),
                )
            }

            bottomContent()
        }
    }
}