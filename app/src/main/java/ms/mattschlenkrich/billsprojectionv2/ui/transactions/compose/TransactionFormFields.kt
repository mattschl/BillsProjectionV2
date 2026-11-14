package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

@Composable
fun TransactionRuleDescriptionRow(
    budgetRule: BudgetRule?,
    onChooseBudgetRule: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: Boolean = false,
    useTwoColumns: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (useTwoColumns) {
        Row(
            modifier = modifier.fillMaxWidth(),
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
                modifier = Modifier.weight(1.5f)
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
    }
}

@Composable
fun TransactionDateAmountRow(
    date: String,
    onDateChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onGotoCalculator: () -> Unit,
    amountError: Boolean = false,
    useTwoColumnsForDateAndAmount: Boolean = false,
    dateLabel: String = stringResource(R.string.label_date),
    amountLabel: String = stringResource(R.string.label_amount),
    modifier: Modifier = Modifier
) {
    if (useTwoColumnsForDateAndAmount) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectDateField(
                value = date,
                onValueChange = onDateChange,
                label = dateLabel,
                modifier = Modifier.weight(1f)
            )
            ProjectBalanceField(
                label = amountLabel,
                value = amount,
                onValueChange = onAmountChange,
                onIconClick = onGotoCalculator,
                modifier = Modifier.weight(1.25f),
                isError = amountError,
                isHighlighted = true,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProjectDateField(
                value = date,
                onValueChange = onDateChange,
                label = dateLabel,
                modifier = Modifier.fillMaxWidth()
            )
            ProjectBalanceField(
                label = amountLabel,
                value = amount,
                onValueChange = onAmountChange,
                onIconClick = onGotoCalculator,
                modifier = Modifier.fillMaxWidth(),
                isError = amountError,
                isHighlighted = true,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransactionAccountsRow(
    fromAccount: Account?,
    fromPending: Boolean,
    onFromPendingChange: (Boolean) -> Unit,
    allowFromPending: Boolean,
    onChooseFromAccount: () -> Unit,
    fromAccountError: Boolean = false,
    toAccount: Account?,
    toPending: Boolean,
    onToPendingChange: (Boolean) -> Unit,
    allowToPending: Boolean,
    onChooseToAccount: () -> Unit,
    toAccountError: Boolean = false,
    useTwoColumns: Boolean = false,
    fromAccountLabel: String = stringResource(R.string.label_from_account_name),
    toAccountLabel: String = stringResource(R.string.label_to_account_name),
    modifier: Modifier = Modifier
) {
    if (useTwoColumns) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionAccountField(
                label = fromAccountLabel,
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onChooseFromAccount,
                isError = fromAccountError,
                modifier = Modifier.weight(1f)
            )
            TransactionAccountField(
                label = toAccountLabel,
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onChooseToAccount,
                isError = toAccountError,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionAccountField(
                label = fromAccountLabel,
                account = fromAccount,
                isPending = fromPending,
                onPendingChange = onFromPendingChange,
                allowPending = allowFromPending,
                onClick = onChooseFromAccount,
                isError = fromAccountError,
                modifier = Modifier.fillMaxWidth()
            )
            TransactionAccountField(
                label = toAccountLabel,
                account = toAccount,
                isPending = toPending,
                onPendingChange = onToPendingChange,
                allowPending = allowToPending,
                onClick = onChooseToAccount,
                isError = toAccountError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TransactionFormFields(
    budgetRule: BudgetRule?,
    onChooseBudgetRule: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onGotoCalculator: () -> Unit,
    fromAccount: Account?,
    fromPending: Boolean,
    onFromPendingChange: (Boolean) -> Unit,
    allowFromPending: Boolean,
    onChooseFromAccount: () -> Unit,
    toAccount: Account?,
    toPending: Boolean,
    onToPendingChange: (Boolean) -> Unit,
    allowToPending: Boolean,
    onChooseToAccount: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    useTwoColumns: Boolean,
    useTwoColumnsForDateAndAmount: Boolean,
    descriptionError: Boolean = false,
    amountError: Boolean = false,
    fromAccountError: Boolean = false,
    toAccountError: Boolean = false,
    fromAccountLabel: String = stringResource(R.string.label_from_account_name),
    toAccountLabel: String = stringResource(R.string.label_to_account_name),
    dateLabel: String = stringResource(R.string.label_date),
    amountLabel: String = stringResource(R.string.label_amount),
) {
    TransactionRuleDescriptionRow(
        budgetRule = budgetRule,
        onChooseBudgetRule = onChooseBudgetRule,
        description = description,
        onDescriptionChange = onDescriptionChange,
        descriptionError = descriptionError,
        useTwoColumns = useTwoColumns
    )

    TransactionDateAmountRow(
        date = date,
        onDateChange = onDateChange,
        amount = amount,
        onAmountChange = onAmountChange,
        onGotoCalculator = onGotoCalculator,
        amountError = amountError,
        useTwoColumnsForDateAndAmount = useTwoColumnsForDateAndAmount,
        dateLabel = dateLabel,
        amountLabel = amountLabel
    )

    TransactionAccountsRow(
        fromAccount = fromAccount,
        fromPending = fromPending,
        onFromPendingChange = onFromPendingChange,
        allowFromPending = allowFromPending,
        onChooseFromAccount = onChooseFromAccount,
        fromAccountError = fromAccountError,
        toAccount = toAccount,
        toPending = toPending,
        onToPendingChange = onToPendingChange,
        allowToPending = allowToPending,
        onChooseToAccount = onChooseToAccount,
        toAccountError = toAccountError,
        useTwoColumns = useTwoColumns,
        fromAccountLabel = fromAccountLabel,
        toAccountLabel = toAccountLabel
    )

    ProjectTextField(
        value = note,
        onValueChange = onNoteChange,
        label = stringResource(R.string.label_notes),
        modifier = Modifier.fillMaxWidth()
    )
}