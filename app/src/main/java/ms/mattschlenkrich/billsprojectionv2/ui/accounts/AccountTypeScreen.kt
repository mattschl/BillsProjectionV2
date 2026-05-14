package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType

@Composable
fun AccountTypeListScreen(
    accountTypes: List<AccountType>,
    onAddClick: () -> Unit,
    onAccountTypeClick: (AccountType) -> Unit,
    onAccountTypeLongClick: (AccountType) -> Unit,
    getAccountTypeInfo: (AccountType) -> String
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_a_new_account_type)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (accountTypes.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.no_account_types_found),
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(accountTypes) { type ->
                        AccountTypeItem(
                            accountType = type,
                            onClick = { onAccountTypeClick(type) },
                            onLongClick = { onAccountTypeLongClick(type) },
                            infoText = getAccountTypeInfo(type)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountTypeItem(
    accountType: AccountType,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    infoText: String
) {
    val isDeleted = accountType.acctIsDeleted
    val bgColor =
        if (isDeleted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
    val textColor =
        if (isDeleted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    val vf = VisualsFunctions()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = accountType.accountType,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(vf.getRandomColorInt()))
                )
            }

            if (infoText.isNotEmpty()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AccountTypeFormScreen(
    name: String,
    onNameChange: (String) -> Unit,
    keepTotals: Boolean,
    onKeepTotalsChange: (Boolean) -> Unit,
    isAsset: Boolean,
    onIsAssetChange: (Boolean) -> Unit,
    keepOwing: Boolean,
    onKeepOwingChange: (Boolean) -> Unit,
    displayAsAsset: Boolean,
    onDisplayAsAssetChange: (Boolean) -> Unit,
    allowPending: Boolean,
    onAllowPendingChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    fabContentDescription: String
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = fabContentDescription
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            ProjectTextField(
                value = name,
                onValueChange = onNameChange,
                label = stringResource(R.string.account_type),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )

            CheckboxRow(
                text = stringResource(R.string.this_account_does_not_keep_a_balance_owing_amount),
                checked = keepTotals,
                onCheckedChange = onKeepTotalsChange
            )
            CheckboxRow(
                text = stringResource(R.string.this_is_an_asset),
                checked = isAsset,
                onCheckedChange = onIsAssetChange
            )
            CheckboxRow(
                text = stringResource(R.string.balance_owing_will_be_calculated),
                checked = keepOwing,
                onCheckedChange = onKeepOwingChange
            )
            CheckboxRow(
                text = stringResource(R.string.this_will_be_used_for_the_budget),
                checked = displayAsAsset,
                onCheckedChange = onDisplayAsAssetChange
            )
            CheckboxRow(
                text = stringResource(R.string.transactions_may_be_postponed),
                checked = allowPending,
                onCheckedChange = onAllowPendingChange
            )
        }
    }
}

@Composable
fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}