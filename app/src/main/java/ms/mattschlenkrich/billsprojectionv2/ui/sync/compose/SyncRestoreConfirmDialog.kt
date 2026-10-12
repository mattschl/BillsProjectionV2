package ms.mattschlenkrich.billsprojectionv2.ui.sync.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun SyncRestoreConfirmDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onRestoreOnly: () -> Unit,
    onRestoreAndDeleteOthers: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Restore") },
        text = { Text("This will overwrite your local records with '$fileName'. This cannot be undone.") },
        confirmButton = {
            Column {
                TextButton(onClick = onRestoreOnly) {
                    Text(
                        stringResource(R.string.action_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(onClick = onRestoreAndDeleteOthers) {
                    Text(
                        "Restore & Delete Others",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}