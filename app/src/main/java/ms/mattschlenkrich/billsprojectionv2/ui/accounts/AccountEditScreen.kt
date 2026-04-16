package ms.mattschlenkrich.billsprojectionv2.ui.accounts

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.components.TransactionHistoryItem
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun AccountEditScreen(
    name: String,
    onNameChange: (String) -> Unit,
    handle: String,
    onHandleChange: (String) -> Unit,
    accountType: AccountType?,
    onAccountTypeClick: () -> Unit,
    accountTypeDetails: String,
    balance: String,
    onBalanceChange: (String) -> Unit,
    onBalanceIconClick: () -> Unit,
    owing: String,
    onOwingChange: (String) -> Unit,
    onOwingIconClick: () -> Unit,
    budgeted: String,
    onBudgetedChange: (String) -> Unit,
    onBudgetedIconClick: () -> Unit,
    limit: String,
    onLimitChange: (String) -> Unit,
    accountId: Long = 0L,
    history: List<TransactionDetailed> = emptyList(),
    onHistoryItemClick: (TransactionDetailed) -> Unit = {},
    onSaveClick: (() -> Unit)? = null,
    nf: NumberFunctions = NumberFunctions(),
    df: DateFunctions = DateFunctions(),
    vf: VisualsFunctions = VisualsFunctions()
) {
    Scaffold(
        floatingActionButton = {
            if (onSaveClick != null) {
                FloatingActionButton(
                    onClick = onSaveClick,
                    containerColor = Color(0xFFB00020)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.save),
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.account_name),
                    modifier = Modifier.weight(1.5f),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                ProjectTextField(
                    value = handle,
                    onValueChange = onHandleChange,
                    label = stringResource(R.string.number),
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            ProjectTextBox(
                label = stringResource(R.string.account_type),
                value = accountType?.accountType ?: "",
                onClick = onAccountTypeClick,
                isError = accountType == null
            )
            if (accountType != null && accountTypeDetails.isNotEmpty()) {
                Text(
                    text = accountTypeDetails,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectBalanceField(
                    label = stringResource(R.string.balance),
                    value = balance,
                    onValueChange = onBalanceChange,
                    onIconClick = onBalanceIconClick,
                    modifier = Modifier.weight(1f)
                )
                ProjectBalanceField(
                    label = stringResource(R.string.owing),
                    value = owing,
                    onValueChange = onOwingChange,
                    onIconClick = onOwingIconClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectBalanceField(
                    label = stringResource(R.string.budgeted),
                    value = budgeted,
                    onValueChange = onBudgetedChange,
                    onIconClick = onBudgetedIconClick,
                    modifier = Modifier.weight(1f)
                )
                ProjectBalanceField(
                    label = stringResource(R.string.credit_limit),
                    value = limit,
                    onValueChange = onLimitChange,
                    modifier = Modifier.weight(1f)
                )
            }

            if (accountId != 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: $accountId",
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { transaction ->
                        TransactionHistoryItem(
                            transactionDetailed = transaction,
                            onClick = onHistoryItemClick,
                            nf = nf,
                            df = df
                        )
                    }
                }
            }
        }
    }
}