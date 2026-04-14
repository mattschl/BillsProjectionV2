package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
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

            OutlinedCard(
                onClick = onAccountTypeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.account_type) + ":",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = accountType?.accountType
                                ?: stringResource(R.string.choose_an_account_type),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (accountType == null) MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    }
                    if (accountType != null && accountTypeDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = accountTypeDetails,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                    }
                }
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
                        AccountHistoryItem(transaction, nf, df, vf, onHistoryItemClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountHistoryItem(
    transactionDetailed: TransactionDetailed,
    nf: NumberFunctions,
    df: DateFunctions,
    vf: VisualsFunctions,
    onClick: (TransactionDetailed) -> Unit
) {
    val transaction = transactionDetailed.transaction ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable { onClick(transactionDetailed) },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color(vf.getRandomColorInt()))
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.transName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        text = nf.displayDollars(transaction.transAmount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = df.getDisplayDateWithYear(transaction.transDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    val toFromText = (transactionDetailed.fromAccount?.accountName ?: "") +
                            " -> " + (transactionDetailed.toAccount?.accountName ?: "")
                    Text(
                        text = toFromText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                if (transaction.transNote.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.note_) + transaction.transNote,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun AccountsListScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    accountsWithType: List<AccountWithType>,
    onAddAccountClick: () -> Unit,
    onAccountClick: (AccountWithType) -> Unit,
    getAccountInfoText: (AccountWithType) -> String,
    vf: VisualsFunctions = VisualsFunctions()
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_a_new_account),
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
                if (accountsWithType.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_accounts_to_view),
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.headlineSmall,
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
                        items(accountsWithType) { account ->
                            AccountListItem(account, onAccountClick, getAccountInfoText, vf)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountListItem(
    accountWithType: AccountWithType,
    onClick: (AccountWithType) -> Unit,
    getAccountInfoText: (AccountWithType) -> String,
    vf: VisualsFunctions
) {
    val account = accountWithType.account
    val isDeleted = account.accIsDeleted
    val containerColor = if (isDeleted) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isDeleted) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(accountWithType) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = account.accountName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(vf.getRandomColorInt()))
                )
            }
            Text(
                text = accountWithType.accountType?.accountType ?: "",
                style = MaterialTheme.typography.labelSmall
            )

            val info = getAccountInfoText(accountWithType)
            if (info.isNotEmpty()) {
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}