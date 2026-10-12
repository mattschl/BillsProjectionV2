package ms.mattschlenkrich.billsprojectionv2.ui.sync.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun SyncAdvancedDialog(
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onQueryDownload: () -> Unit,
    onUploadNow: () -> Unit,
    onRepairLocal: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Options") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Text("Restore") }

                Button(
                    onClick = onQueryDownload,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Query Drive / Download") }

                Button(
                    onClick = onUploadNow,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Upload Current State") }

                Button(
                    onClick = onRepairLocal,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Repair Local Database") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}