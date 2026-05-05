package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

@Composable
fun TransactionEditScreen(
    date: String,
    onDateChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    toAccount: Account?,
    fromAccount: Account?,
    budgetRule: BudgetRule?,
    toPending: Boolean,
    onToPendingChange: (Boolean) -> Unit,
    fromPending: Boolean,
    onFromPendingChange: (Boolean) -> Unit,
    allowToPending: Boolean,
    allowFromPending: Boolean,
    onSaveClick: () -> Unit,
    onChooseBudgetRule: () -> Unit,
    onChooseFromAccount: () -> Unit,
    onChooseToAccount: () -> Unit,
    onSplitClick: () -> Unit,
    onGotoCalculator: () -> Unit,
    isSplitEnabled: Boolean,
    splitButtonText: String = stringResource(R.string.splitting_transaction)
) {
    val nf = remember { NumberFunctions() }

    Scaffold(
        modifier = Modifier.imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFB00020)
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(R.string.save),
                    tint = Color.White
                )
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
            ProjectTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.description),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectDateField(
                    value = date,
                    onValueChange = onDateChange,
                    label = stringResource(R.string.date),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ProjectBalanceField(
                    label = stringResource(R.string.amount),
                    value = amount,
                    onValueChange = onAmountChange,
                    onIconClick = onGotoCalculator,
                    modifier = Modifier.weight(1f)
                )
            }

            ProjectTextBox(
                label = stringResource(R.string.rules),
                value = budgetRule?.budgetRuleName ?: "",
                onClick = onChooseBudgetRule
            )

            TransactionAccountField(
                label = stringResource(R.string.from_account_name),
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onChooseFromAccount
            )

            TransactionAccountField(
                label = stringResource(R.string.to_account_name),
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onChooseToAccount
            )

            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.note)
            )

            Button(
                onClick = onSplitClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isSplitEnabled
            ) {
                Text(splitButtonText)
            }
        }
    }
}

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
    isSplitEnabled: Boolean,
) {
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    val remainder by remember(amount, budgetedAmount) {
        derivedStateOf {
            nf.getDoubleFromDollars(budgetedAmount) - nf.getDoubleFromDollars(amount)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFB00020)
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(R.string.save),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProjectDateField(
                value = date,
                onValueChange = onDateChange,
                label = stringResource(R.string.date),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProjectTextBox(
                    label = stringResource(R.string.rules),
                    value = budgetRule?.budgetRuleName ?: "",
                    onClick = onChooseBudgetRule,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.amount), modifier = Modifier.weight(1f))
                ProjectBalanceField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1.5f),
                    label = stringResource(R.string.amount)
                )
                Button(
                    onClick = onSplitClick,
                    enabled = isSplitEnabled,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = stringResource(R.string.split))
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.budgeted),
                            style = MaterialTheme.typography.labelMedium
                        )
                        ProjectBalanceField(
                            value = budgetedAmount,
                            onValueChange = onBudgetedAmountChange,
                            label = stringResource(R.string.budgeted)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.remainder),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = nf.displayDollars(remainder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            TransactionAccountField(
                label = stringResource(R.string.to_this_account),
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onToAccountClick
            )

            TransactionAccountField(
                label = stringResource(R.string.from_this_account),
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onFromAccountClick
            )

            ProjectTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.description)
            )

            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.notes)
            )
        }
    }
}

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
) {
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    val remainder by remember(amount, originalAmount) {
        derivedStateOf {
            originalAmount - nf.getDoubleFromDollars(amount)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFB00020)
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(R.string.save),
                    tint = Color.White
                )
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
                label = stringResource(R.string.date),
                modifier = Modifier.fillMaxWidth()
            )

            ProjectTextBox(
                label = stringResource(R.string.rules),
                value = budgetRule?.budgetRuleName ?: "",
                onClick = onChooseBudgetRule
            )

            ProjectBalanceField(
                label = stringResource(R.string.transaction_amount),
                value = amount,
                onValueChange = onAmountChange,
                onIconClick = onGotoCalculator
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.original_amount),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = nf.displayDollars(originalAmount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.remainder),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = nf.displayDollars(remainder),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            TransactionAccountField(
                label = stringResource(R.string.to_this_account),
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onChooseToAccount
            )

            TransactionAccountField(
                label = stringResource(R.string.from_this_account),
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onFromAccountClick
            )

            ProjectTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.description),
                singleLine = true
            )

            ProjectTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.notes)
            )
        }
    }
}

@Composable
fun TransactionAccountField(
    label: String,
    account: Account?,
    isPending: Boolean,
    onPendingChange: (Boolean) -> Unit,
    allowPending: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProjectTextBox(
            label = label,
            value = account?.accountName ?: "",
            onClick = onClick ?: {}
        )
        if (allowPending) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onPendingChange(!isPending) }
            ) {
                Checkbox(
                    checked = isPending,
                    onCheckedChange = onPendingChange,
                )
                Text(
                    text = stringResource(R.string.pending),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}