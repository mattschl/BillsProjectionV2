package ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectBalanceField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
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
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()

    if (sheetOptions.isNotEmpty()) {
        ActionBottomSheet(
            title = sheetTitle,
            options = sheetOptions,
            sheetState = sheetState,
            onDismissRequest = onSheetDismiss
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnPreference = remember {
        ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager(context)
            .getSettings().columnPreference
    }
    val thresholdDp =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ColumnSettingsHelper.getThresholdDp(
            columnPreference,
            isLandscape
        )

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val fontScale = density.fontScale.coerceAtLeast(0.8f)
    val effectiveWidthDp = with(density) { windowInfo.containerSize.width.toDp() } / fontScale
    val useTwoColumns = effectiveWidthDp >= thresholdDp

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            if (onSaveClick != null) {
                FloatingActionButton(
                    onClick = onSaveClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.action_save)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (useTwoColumns) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProjectTextField(
                                value = name,
                                onValueChange = onNameChange,
                                label = stringResource(R.string.label_account_name),
                                modifier = Modifier.weight(1.5f),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                singleLine = true
                            )
                            ProjectTextField(
                                value = handle,
                                onValueChange = onHandleChange,
                                label = stringResource(R.string.label_number),
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true
                            )
                        }
                    } else {
                        ProjectTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = stringResource(R.string.label_account_name),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            singleLine = true
                        )
                        ProjectTextField(
                            value = handle,
                            onValueChange = onHandleChange,
                            label = stringResource(R.string.label_number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true
                        )
                    }

                    ProjectTextBox(
                        label = stringResource(R.string.label_account_type),
                        value = accountType?.accountType ?: "",
                        onClick = onAccountTypeClick,
                        isError = accountType == null
                    )
                    if (accountType != null && accountTypeDetails.isNotEmpty()) {
                        Text(
                            text = accountTypeDetails,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (useTwoColumns) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProjectBalanceField(
                                label = stringResource(R.string.label_balance),
                                value = balance,
                                onValueChange = onBalanceChange,
                                onIconClick = onBalanceIconClick,
                                modifier = Modifier.weight(1f)
                            )
                            ProjectBalanceField(
                                label = stringResource(R.string.label_owing),
                                value = owing,
                                onValueChange = onOwingChange,
                                onIconClick = onOwingIconClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ProjectBalanceField(
                            label = stringResource(R.string.label_balance),
                            value = balance,
                            onValueChange = onBalanceChange,
                            onIconClick = onBalanceIconClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ProjectBalanceField(
                            label = stringResource(R.string.label_owing),
                            value = owing,
                            onValueChange = onOwingChange,
                            onIconClick = onOwingIconClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (useTwoColumns) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProjectBalanceField(
                                label = stringResource(R.string.label_budgeted),
                                value = budgeted,
                                onValueChange = onBudgetedChange,
                                onIconClick = onBudgetedIconClick,
                                modifier = Modifier.weight(1f)
                            )
                            ProjectBalanceField(
                                label = stringResource(R.string.label_credit_limit),
                                value = limit,
                                onValueChange = onLimitChange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ProjectBalanceField(
                            label = stringResource(R.string.label_budgeted),
                            value = budgeted,
                            onValueChange = onBudgetedChange,
                            onIconClick = onBudgetedIconClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ProjectBalanceField(
                            label = stringResource(R.string.label_credit_limit),
                            value = limit,
                            onValueChange = onLimitChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (accountId != 0L) {
                item {
                    Text(
                        text = "ID: $accountId",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                items(
                    history,
                    key = { it.transaction?.transId ?: it.hashCode() }
                ) { transaction ->
                    TransactionHistoryItem(
                        transactionDetailed = transaction,
                        onClick = onHistoryItemClick,
                    )
                }
            }
        }
    }
}