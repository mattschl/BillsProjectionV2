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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.columns.TransactionEntryDefaults
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
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
    widthFactor: Int = TransactionEntryDefaults.WIDTH_FACTOR,
) {
    val nf = LocalNumberFunctions.current

    val remainder by remember(amount, originalAmount) {
        derivedStateOf {
            originalAmount - nf.getDoubleFromDollars(amount)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnPreference = remember {
        ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager(context)
            .getSettings().columnPreference
    }
    val columnCount =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ColumnSettingsHelper.calculateColumnCount(
            widthFactor = widthFactor,
            columnPreference = columnPreference,
            isLandscape = isLandscape
        )
    val useTwoColumns = columnCount > 1

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val fontScale = density.fontScale.coerceAtLeast(0.8f)
    val effectiveWidthDp = with(density) { windowInfo.containerSize.width.toDp() } / fontScale
    val isVeryWideScreen = effectiveWidthDp >= 600.dp
    val useTwoColumnsForDateAndAmount = isLandscape || isVeryWideScreen

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

            TransactionFormFields(
                budgetRule = budgetRule,
                onChooseBudgetRule = onChooseBudgetRule,
                description = description,
                onDescriptionChange = onDescriptionChange,
                date = date,
                onDateChange = onDateChange,
                amount = amount,
                onAmountChange = onAmountChange,
                onGotoCalculator = onGotoCalculator,
                fromAccount = fromAccount,
                fromPending = fromPending,
                onFromPendingChange = onFromPendingChange,
                allowFromPending = allowFromPending,
                onChooseFromAccount = onFromAccountClick,
                toAccount = toAccount,
                toPending = toPending,
                onToPendingChange = onToPendingChange,
                allowToPending = allowToPending,
                onChooseToAccount = onChooseToAccount,
                note = note,
                onNoteChange = onNoteChange,
                useTwoColumns = useTwoColumns,
                useTwoColumnsForDateAndAmount = useTwoColumnsForDateAndAmount,
                descriptionError = descriptionError,
                amountError = amountError,
                fromAccountError = fromAccountError,
                toAccountError = toAccountError,
                fromAccountLabel = stringResource(R.string.label_from_this_account),
                toAccountLabel = stringResource(R.string.label_to_this_account)
            )

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                if (useTwoColumns) {
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
                } else {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProjectTextBox(
                            value = nf.displayDollars(originalAmount),
                            label = stringResource(R.string.label_original_amount),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ProjectTextBox(
                            value = nf.displayDollars(remainder),
                            label = stringResource(R.string.label_remainder),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }


        }
    }
}