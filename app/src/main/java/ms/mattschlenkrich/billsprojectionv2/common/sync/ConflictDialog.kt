package ms.mattschlenkrich.billsprojectionv2.common.sync

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun ConflictDialog(
    info: SyncViewModel.ConflictInfo,
    onChoice: (SyncViewModel.ConflictChoice) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text("Sync Conflict") },
        text = {
            Text(
                stringResource(
                    R.string.sync_conflict_message,
                    info.tableName,
                    info.name,
                    info.localId,
                    info.localTime,
                    info.driveId,
                    info.driveTime
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onChoice(SyncViewModel.ConflictChoice.KEEP_LOCAL) }) {
                Text("Keep Local")
            }
        },
        dismissButton = {
            TextButton(onClick = { onChoice(SyncViewModel.ConflictChoice.KEEP_DRIVE) }) {
                Text("Keep Drive")
            }
        }
    )
}