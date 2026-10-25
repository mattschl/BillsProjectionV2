package ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField

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
                label = stringResource(R.string.label_account_type),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )

            CheckboxRow(
                text = stringResource(R.string.msg_account_no_balance),
                checked = keepTotals,
                onCheckedChange = onKeepTotalsChange
            )
            CheckboxRow(
                text = stringResource(R.string.msg_is_asset),
                checked = isAsset,
                onCheckedChange = onIsAssetChange
            )
            CheckboxRow(
                text = stringResource(R.string.msg_balance_owing_calc),
                checked = keepOwing,
                onCheckedChange = onKeepOwingChange
            )
            CheckboxRow(
                text = stringResource(R.string.msg_used_for_budget),
                checked = displayAsAsset,
                onCheckedChange = onDisplayAsAssetChange
            )
            CheckboxRow(
                text = stringResource(R.string.msg_transactions_postponed),
                checked = allowPending,
                onCheckedChange = onAllowPendingChange
            )
        }
    }
}