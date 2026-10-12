package ms.mattschlenkrich.billsprojectionv2.ui.sync.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun SyncRepairConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repair Local Database") },
        text = { Text("This will attempt to fix metadata errors in your current local database file. Use this if you have manually replaced the database file but the app isn't recognizing it.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}