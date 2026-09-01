package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextBox
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account

@Composable
fun TransactionAccountField(
    label: String,
    account: Account?,
    isPending: Boolean,
    onPendingChange: (Boolean) -> Unit,
    allowPending: Boolean,
    onClick: (() -> Unit)? = null,
    isError: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProjectTextBox(
            label = label,
            value = account?.accountName ?: "",
            onClick = onClick ?: {},
            isError = isError
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
                    text = stringResource(R.string.label_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}