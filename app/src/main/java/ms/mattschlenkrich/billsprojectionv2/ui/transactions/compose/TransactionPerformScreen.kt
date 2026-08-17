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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                    contentDescription = stringResource(R.string.save)
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

            ProjectTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.description),
                isError = descriptionError
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectDateField(
                    value = date,
                    onValueChange = onDateChange,
                    label = stringResource(R.string.date),
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ProjectBalanceField(
                    label = stringResource(R.string.amount),
                    value = amount,
                    onValueChange = onAmountChange,
                    onIconClick = onGotoCalculator,
                    modifier = Modifier.weight(3f),
                    isError = amountError,
                    isHighlighted = true
                )
            }

            ProjectTextBox(
                label = stringResource(R.string.rules),
                value = budgetRule?.budgetRuleName ?: "",
                onClick = onChooseBudgetRule,
            )

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(3f)) {
                        ProjectBalanceField(
                            value = budgetedAmount,
                            onValueChange = onBudgetedAmountChange,
                            label = stringResource(R.string.budgeted)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(2f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.remainder),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = nf.displayDollars(remainder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainder >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            TransactionAccountField(
                label = stringResource(R.string.from_this_account),
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onFromAccountClick,
                isError = fromAccountError
            )

            TransactionAccountField(
                label = stringResource(R.string.to_this_account),
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onToAccountClick,
                isError = toAccountError
            )

            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.notes)
            )

            Button(
                onClick = onSplitClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isSplitEnabled
            ) {
                Text(stringResource(R.string.split))
            }
        }
    }
}