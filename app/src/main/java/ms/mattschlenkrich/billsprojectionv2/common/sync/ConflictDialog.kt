package ms.mattschlenkrich.billsprojectionv2.common.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun ConflictDialog(
    info: SyncViewModel.ConflictInfo,
    onChoice: (SyncViewModel.ConflictChoice, Boolean) -> Unit
) {
    var applyToAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text("Sync Conflict") },
        text = {
            Column {
                Text(
                    stringResource(
                        info.messageResId ?: R.string.sync_conflict_message,
                        info.tableName,
                        info.name,
                        info.localId,
                        info.localTime,
                        info.driveId,
                        info.driveTime
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { applyToAll = !applyToAll }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = applyToAll,
                        onCheckedChange = { applyToAll = it }
                    )
                    Text(
                        text = stringResource(R.string.apply_to_all_conflicts),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onChoice(
                    SyncViewModel.ConflictChoice.KEEP_LOCAL,
                    applyToAll
                )
            }) {
                Text("Keep Local")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onChoice(
                    SyncViewModel.ConflictChoice.KEEP_DRIVE,
                    applyToAll
                )
            }) {
                Text("Keep Drive")
            }
        }
    )
}