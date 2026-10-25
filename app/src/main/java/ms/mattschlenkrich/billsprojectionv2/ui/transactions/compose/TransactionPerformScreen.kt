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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

@Composable
fun TransactionPerformScreen(
    date: String,
    onDateChange: (String) -> Unit,
    budgetRule: BudgetRule?,
    amount: String,
    onAmountChange: (String) -> Unit,
    onSplitClick: () -> Unit,
    budgetedAmount: String,
    onBudgetedAmountChange: (String) -> Unit,
    toAccount: Account?,
    toPending: Boolean,
    onToPendingChange: (Boolean) -> Unit,
    allowToPending: Boolean,
    onToAccountClick: () -> Unit,
    fromAccount: Account?,
    fromPending: Boolean,
    onFromPendingChange: (Boolean) -> Unit,
    allowFromPending: Boolean,
    onFromAccountClick: () -> Unit,
    onChooseBudgetRule: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onGotoCalculator: () -> Unit,
    isSplitEnabled: Boolean,
    descriptionError: Boolean = false,
    amountError: Boolean = false,
    toAccountError: Boolean = false,
    fromAccountError: Boolean = false,
) {
    val nf = LocalNumberFunctions.current
    val remainder by remember(amount, budgetedAmount) {
        derivedStateOf {
            nf.getDoubleFromDollars(budgetedAmount) - nf.getDoubleFromDollars(amount)
        }
    }

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val fontScale = density.fontScale.coerceAtLeast(0.8f)
    val effectiveWidthDp = with(density) { windowInfo.containerSize.width.toDp() } / fontScale
    val useTwoColumns = effectiveWidthDp >= 450.dp

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(R.string.action_save)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Row 1: Budget Rule -> Description
            if (useTwoColumns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectTextBox(
                        label = stringResource(R.string.label_rules),
                        value = budgetRule?.budgetRuleName ?: "",
                        onClick = onChooseBudgetRule,
                        modifier = Modifier.weight(1f)
                    )
                    ProjectTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = stringResource(R.string.label_description),
                        isError = descriptionError,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                ProjectTextBox(
                    label = stringResource(R.string.label_rules),
                    value = budgetRule?.budgetRuleName ?: "",
                    onClick = onChooseBudgetRule,
                    modifier = Modifier.fillMaxWidth()
                )
                ProjectTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(R.string.label_description),
                    isError = descriptionError,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Row 2: Date -> Amount
            if (useTwoColumns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectDateField(
                        value = date,
                        onValueChange = onDateChange,
                        label = stringResource(R.string.label_date),
                        modifier = Modifier.weight(2f)
                    )
                    ProjectBalanceField(
                        label = stringResource(R.string.label_amount),
                        value = amount,
                        onValueChange = onAmountChange,
                        onIconClick = onGotoCalculator,
                        modifier = Modifier.weight(4f),
                        isError = amountError,
                        isHighlighted = true
                    )
                }
            } else {
                ProjectDateField(
                    value = date,
                    onValueChange = onDateChange,
                    label = stringResource(R.string.label_date),
                    modifier = Modifier.fillMaxWidth()
                )
                ProjectBalanceField(
                    label = stringResource(R.string.label_amount),
                    value = amount,
                    onValueChange = onAmountChange,
                    onIconClick = onGotoCalculator,
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError,
                    isHighlighted = true
                )
            }

            // Row 3: From Account -> To Account
            if (useTwoColumns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionAccountField(
                        label = stringResource(R.string.label_from_this_account),
                        account = fromAccount,
                        isPending = fromPending,
                        onPendingChange = onFromPendingChange,
                        allowPending = allowFromPending,
                        onClick = onFromAccountClick,
                        isError = fromAccountError,
                        modifier = Modifier.weight(1f)
                    )
                    TransactionAccountField(
                        label = stringResource(R.string.label_to_this_account),
                        account = toAccount,
                        isPending = toPending,
                        onPendingChange = onToPendingChange,
                        allowPending = allowToPending,
                        onClick = onToAccountClick,
                        isError = toAccountError,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                TransactionAccountField(
                    label = stringResource(R.string.label_from_this_account),
                    account = fromAccount,
                    isPending = fromPending,
                    onPendingChange = onFromPendingChange,
                    allowPending = allowFromPending,
                    onClick = onFromAccountClick,
                    isError = fromAccountError,
                    modifier = Modifier.fillMaxWidth()
                )
                TransactionAccountField(
                    label = stringResource(R.string.label_to_this_account),
                    account = toAccount,
                    isPending = toPending,
                    onPendingChange = onToPendingChange,
                    allowPending = allowToPending,
                    onClick = onToAccountClick,
                    isError = toAccountError,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Row 4: Note
            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.label_notes)
            )

            // Row 5: Projected amount -> Remainder
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                if (useTwoColumns) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProjectBalanceField(
                            value = budgetedAmount,
                            label = stringResource(R.string.label_projected_amount),
                            modifier = Modifier.weight(1f),
                            onValueChange = onBudgetedAmountChange,
                            isHighlighted = true
                        )
                        ProjectTextBox(
                            value = nf.displayDollars(remainder),
                            label = stringResource(R.string.label_remainder),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProjectBalanceField(
                            value = budgetedAmount,
                            label = stringResource(R.string.label_projected_amount),
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = onBudgetedAmountChange,
                            isHighlighted = true
                        )
                        ProjectTextBox(
                            value = nf.displayDollars(remainder),
                            label = stringResource(R.string.label_remainder),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Row 6: Split Button
            Button(
                onClick = onSplitClick,
                enabled = isSplitEnabled
            ) {
                Text(stringResource(R.string.action_split))
            }
        }
    }
}