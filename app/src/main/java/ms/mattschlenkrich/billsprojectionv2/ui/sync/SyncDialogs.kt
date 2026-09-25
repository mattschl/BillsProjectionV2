package ms.mattschlenkrich.billsprojectionv2.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import java.io.File

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

@Composable
fun SyncBackupListDialog(
    isDownloadMode: Boolean,
    localBackups: List<File>,
    availableBackups: List<DriveFileMeta>,
    selectedBackups: Set<String>,
    onSelectedBackupsChange: (Set<String>) -> Unit,
    onSelectLocalBackup: (File) -> Unit,
    onSelectCloudBackup: (String) -> Unit,
    onDeleteBackup: (DriveFileMeta) -> Unit,
    onDownloadConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isDownloadMode) "Select Backups to Download" else "Select Backup to Restore") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (localBackups.isNotEmpty() && !isDownloadMode) {
                    Text(
                        "Local Backups:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    localBackups.forEach { file ->
                        TextButton(
                            onClick = { onSelectLocalBackup(file) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "[Local] ${file.name}",
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cloud Backups:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                availableBackups.forEach { meta ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isDownloadMode) {
                            Checkbox(
                                checked = selectedBackups.contains(meta.name),
                                onCheckedChange = { checked ->
                                    val newSet = if (checked) {
                                        selectedBackups + meta.name
                                    } else {
                                        selectedBackups - meta.name
                                    }
                                    onSelectedBackupsChange(newSet)
                                }
                            )
                            Text(
                                meta.name,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                        } else {
                            TextButton(
                                onClick = { onSelectCloudBackup(meta.name) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    meta.name,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteBackup(meta) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete backup",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isDownloadMode) {
                TextButton(
                    onClick = onDownloadConfirm,
                    enabled = selectedBackups.isNotEmpty()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("Download")
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

@Composable
fun SyncDeleteConfirmDialog(
    meta: DriveFileMeta,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete '${meta.name}' from Google Drive? This will also remove associated temporary files and cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

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