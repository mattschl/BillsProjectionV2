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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TransactionSplitScreen(
    date: String,
    onDateChange: (String) -> Unit,
    budgetRule: BudgetRule?,
    onChooseBudgetRule: () -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onGotoCalculator: () -> Unit,
    originalAmount: Double,
    toAccount: Account?,
    onChooseToAccount: () -> Unit,
    toPending: Boolean,
    onToPendingChange: (Boolean) -> Unit,
    allowToPending: Boolean,
    fromAccount: Account?,
    fromPending: Boolean,
    onFromPendingChange: (Boolean) -> Unit,
    allowFromPending: Boolean,
    onFromAccountClick: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    descriptionError: Boolean = false,
    amountError: Boolean = false,
    toAccountError: Boolean = false,
    fromAccountError: Boolean = false,
) {
    val nf = LocalNumberFunctions.current

    val remainder by remember(amount, originalAmount) {
        derivedStateOf {
            originalAmount - nf.getDoubleFromDollars(amount)
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

            // Row 2: Date -> Amount
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
                    modifier = Modifier.weight(3f),
                    isError = amountError,
                    isHighlighted = true
                )
            }

            // Row 3: From Account -> To Account
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
                    onClick = onChooseToAccount,
                    isError = toAccountError,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: Note
            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.label_notes)
            )

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    ProjectTextBox(
                        value = nf.displayDollars(originalAmount),
                        label = stringResource(R.string.label_original_amount),
                        modifier = Modifier.weight(1f),
                    )
                    ProjectTextBox(
                        value = nf.displayDollars(remainder),
                        label = stringResource(R.string.label_remainder),
                        modifier = Modifier.weight(1f),
                    )
                }
            }


        }
    }
}