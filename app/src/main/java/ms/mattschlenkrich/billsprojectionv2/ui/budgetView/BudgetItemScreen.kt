package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItemScreen(
    date: String,
    onDateChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    payDay: String,
    onPayDayChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    isFixed: Boolean,
    onIsFixedChange: (Boolean) -> Unit,
    isPayDayItem: Boolean,
    onIsPayDayItemChange: (Boolean) -> Unit,
    isAuto: Boolean,
    onIsAutoChange: (Boolean) -> Unit,
    isLocked: Boolean,
    onIsLockedChange: (Boolean) -> Unit,
    budgetItemDetailed: BudgetItemDetailed?,
    payDays: List<String>,
    onSaveClick: () -> Unit,
    onChooseBudgetRule: () -> Unit,
    onChooseAccount: (String) -> Unit,
    onGotoCalculator: () -> Unit,
) {
    val toAccount = budgetItemDetailed?.toAccount
    val fromAccount = budgetItemDetailed?.fromAccount
    val budgetRule = budgetItemDetailed?.budgetRule

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Done, contentDescription = "Done")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProjectDateField(
                value = date,
                onValueChange = onDateChange,
                label = stringResource(R.string.projected_date),
                modifier = Modifier.fillMaxWidth(),
            )

            ProjectTextField(
                value = name,
                onValueChange = onNameChange,
                label = stringResource(R.string.description),
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                ProjectTextField(
                    value = payDay,
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(R.string.pay_day),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    payDays.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onPayDayChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            ProjectTextBox(
                label = stringResource(R.string.rules),
                value = budgetRule?.budgetRuleName ?: "",
                onClick = onChooseBudgetRule
            )

            ProjectTextBox(
                label = stringResource(R.string.to_this_account),
                value = toAccount?.accountName ?: "",
                onClick = { onChooseAccount(REQUEST_TO_ACCOUNT) }
            )

            ProjectTextBox(
                label = stringResource(R.string.from_this_account),
                value = fromAccount?.accountName ?: "",
                onClick = { onChooseAccount(REQUEST_FROM_ACCOUNT) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProjectBalanceField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = stringResource(R.string.projected_amount),
                    modifier = Modifier.weight(1f),
                    onIconClick = onGotoCalculator
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Checkbox(checked = isFixed, onCheckedChange = onIsFixedChange)
                    Text(
                        stringResource(R.string.fixed),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabeledCheckbox(
                    label = stringResource(R.string.pay_day),
                    checked = isPayDayItem,
                    onCheckedChange = onIsPayDayItemChange
                )
                LabeledCheckbox(
                    label = stringResource(R.string.automatic),
                    checked = isAuto,
                    onCheckedChange = onIsAutoChange
                )
                LabeledCheckbox(
                    label = stringResource(R.string.lock),
                    checked = isLocked,
                    onCheckedChange = onIsLockedChange
                )
            }
        }
    }
}

@Composable
private fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}