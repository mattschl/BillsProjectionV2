package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectIntField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account

@OptIn(ExperimentalMaterial3Api::class)
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
    suggestedAmount: Double? = null,
    floatingActionButton: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit = {},
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {}
) {
    val frequencyTypes = stringArrayResource(R.array.frequency_types)
    val daysOfWeek = stringArrayResource(R.array.days_of_week)
    val sheetState = rememberModalBottomSheetState()

    if (sheetOptions.isNotEmpty()) {
        ActionBottomSheet(
            title = sheetTitle,
            options = sheetOptions,
            sheetState = sheetState,
            onDismissRequest = onSheetDismiss
        )
    }

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val fontScale = density.fontScale.coerceAtLeast(0.8f)
    val effectiveWidthDp = with(density) { windowInfo.containerSize.width.toDp() } / fontScale
    val useTwoColumns = effectiveWidthDp >= 450.dp

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = 16.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            if (useTwoColumns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = stringResource(R.string.label_budget_rule_name),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.weight(2f)
                    )
                    Column(modifier = Modifier.weight(3f)) {
                        ProjectBalanceField(
                            value = amount,
                            onValueChange = onAmountChange,
                            label = stringResource(R.string.label_default_amount),
                            modifier = Modifier.fillMaxWidth(),
                            onIconClick = onGotoCalculator,
                            isHighlighted = true
                        )
                        suggestedAmount?.let {
                            val nf = LocalNumberFunctions.current
                            Text(
                                text = "${stringResource(R.string.label_average)}: ${
                                    nf.displayDollars(
                                        it
                                    )
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            } else {
                ProjectTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.label_budget_rule_name),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProjectBalanceField(
                        value = amount,
                        onValueChange = onAmountChange,
                        label = stringResource(R.string.label_default_amount),
                        modifier = Modifier.fillMaxWidth(),
                        onIconClick = onGotoCalculator,
                        isHighlighted = true
                    )
                    suggestedAmount?.let {
                        val nf = LocalNumberFunctions.current
                        Text(
                            text = "${stringResource(R.string.label_average)}: ${
                                nf.displayDollars(
                                    it
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (useTwoColumns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectTextBox(
                        label = stringResource(R.string.label_from_this_account),
                        value = fromAccount?.accountName ?: "",
                        onClick = { onChooseAccount(REQUEST_FROM_ACCOUNT) },
                        modifier = Modifier.weight(1f)
                    )

                    ProjectTextBox(
                        label = stringResource(R.string.label_to_this_account),
                        value = toAccount?.accountName ?: "",
                        onClick = { onChooseAccount(REQUEST_TO_ACCOUNT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                ProjectTextBox(
                    label = stringResource(R.string.label_from_this_account),
                    value = fromAccount?.accountName ?: "",
                    onClick = { onChooseAccount(REQUEST_FROM_ACCOUNT) },
                    modifier = Modifier.fillMaxWidth()
                )

                ProjectTextBox(
                    label = stringResource(R.string.label_to_this_account),
                    value = toAccount?.accountName ?: "",
                    onClick = { onChooseAccount(REQUEST_TO_ACCOUNT) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.title_scheduling_rules),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            val isStartZero = startDate.trim() == "0" || startDate.trim() == "0000-00-00"

            if (useTwoColumns || isStartZero) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectDateField(
                        value = startDate,
                        onValueChange = onStartDateChange,
                        label = stringResource(R.string.label_start_date),
                        modifier = if (isStartZero) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    )

                    if (!isStartZero) {
                        ProjectDateField(
                            value = endDate,
                            onValueChange = onEndDateChange,
                            label = stringResource(R.string.label_end_date),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                ProjectDateField(
                    value = startDate,
                    onValueChange = onStartDateChange,
                    label = stringResource(R.string.label_start_date),
                    modifier = Modifier.fillMaxWidth()
                )
                ProjectDateField(
                    value = endDate,
                    onValueChange = onEndDateChange,
                    label = stringResource(R.string.label_end_date),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!isStartZero) {
                if (useTwoColumns) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdown(
                            label = stringResource(R.string.title_budget_rules),
                            options = frequencyTypes.toList(),
                            selectedIndex = frequencyType,
                            onItemSelected = onFrequencyTypeChange,
                            modifier = Modifier.weight(1.5f)
                        )

                        ProjectIntField(
                            value = frequencyCount,
                            onValueChange = onFrequencyCountChange,
                            label = stringResource(R.string.label_times),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    ExposedDropdown(
                        label = stringResource(R.string.title_budget_rules),
                        options = frequencyTypes.toList(),
                        selectedIndex = frequencyType,
                        onItemSelected = onFrequencyTypeChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ProjectIntField(
                        value = frequencyCount,
                        onValueChange = onFrequencyCountChange,
                        label = stringResource(R.string.label_times),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (useTwoColumns) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdown(
                            label = stringResource(R.string.label_on_day),
                            options = daysOfWeek.toList(),
                            selectedIndex = dayOfWeek,
                            onItemSelected = onDayOfWeekChange,
                            modifier = Modifier.weight(1.5f)
                        )

                        ProjectIntField(
                            value = leadDays,
                            onValueChange = onLeadDaysChange,
                            label = stringResource(R.string.label_lead_days),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    ExposedDropdown(
                        label = stringResource(R.string.label_on_day),
                        options = daysOfWeek.toList(),
                        selectedIndex = dayOfWeek,
                        onItemSelected = onDayOfWeekChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ProjectIntField(
                        value = leadDays,
                        onValueChange = onLeadDaysChange,
                        label = stringResource(R.string.label_lead_days),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LabeledCheckbox(
                        label = stringResource(R.string.label_fixed),
                        checked = isFixed,
                        onCheckedChange = onIsFixedChange
                    )
                    LabeledCheckbox(
                        label = stringResource(R.string.label_pay_day),
                        checked = isPayDay,
                        onCheckedChange = onIsPayDayChange
                    )
                    LabeledCheckbox(
                        label = stringResource(R.string.label_automatic),
                        checked = isAuto,
                        onCheckedChange = onIsAutoChange
                    )
                }
            }

            bottomContent()
        }
    }
}