package ms.mattschlenkrich.billsprojectionv2.ui.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import java.io.File

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onConnectLegacy: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onRestore: (String) -> Unit,
    onRestoreLocal: (File) -> Unit,
    onRepairLocal: () -> Unit,
    onUploadNow: () -> Unit,
    onDeleteBackup: (DriveFileMeta) -> Unit,
    onDeleteOtherBackups: (List<String>) -> Unit,
    onDownloadBackups: (List<String>) -> Unit,
) {
    var showRestoreConfirm by remember { mutableStateOf<String?>(null) }
    var showRestoreLocalConfirm by remember { mutableStateOf<File?>(null) }
    var showRepairConfirm by remember { mutableStateOf(value = false) }
    var showBackupList by remember { mutableStateOf(value = false) }
    var showAdvancedOptions by remember { mutableStateOf(value = false) }
    var isDownloadMode by remember { mutableStateOf(value = false) }
    var selectedBackups by remember { mutableStateOf(emptySet<String>()) }
    var showDeleteConfirm by remember { mutableStateOf<DriveFileMeta?>(null) }
    var showDeleteOthersConfirm by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.driveServiceHelper) {
        if (viewModel.driveServiceHelper != null) {
            viewModel.fetchLastBackupTime()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(id = R.string.title_sync),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        viewModel.lastBackupTime?.let {
                            Text(
                                "Last Backup: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_go_back),
                            modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.ic_bills_projection_background),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var textFieldValue by remember {
                    mutableStateOf(TextFieldValue(viewModel.docContent))
                }
                LaunchedEffect(viewModel.docContent) {
                    if (textFieldValue.text != viewModel.docContent) {
                        textFieldValue = textFieldValue.copy(text = viewModel.docContent)
                    }
                }
                ProjectTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        if (viewModel.docContent != it.text) {
                            viewModel.docContent = it.text
                        }
                    },
                    label = stringResource(R.string.label_document_content),
                    modifier = Modifier.fillMaxHeight(.65f)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.driveServiceHelper == null) {
                            Button(
                                onClick = onConnect,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_connect_to_drive)) }
                            Button(
                                onClick = onConnectLegacy,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_connect_to_drive_legacy)) }
                        } else {
                            Button(
                                onClick = onSync,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.title_sync)) }
                            Button(
                                onClick = { showAdvancedOptions = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Advanced") }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.driveServiceHelper != null) {
                            Button(
                                onClick = onDisconnect,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_disconnect)) }
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.action_done)) }
                    }
                }
            }

            if (showAdvancedOptions) {
                AlertDialog(
                    onDismissRequest = { showAdvancedOptions = false },
                    title = { Text("Advanced Options") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    isDownloadMode = false
                                    viewModel.fetchAvailableBackups()
                                    showBackupList = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) { Text("Restore") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    isDownloadMode = true
                                    selectedBackups = emptySet()
                                    viewModel.fetchAvailableBackups()
                                    showBackupList = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Query Drive / Download") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    onUploadNow()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Upload Current State") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    showRepairConfirm = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Repair Local Database") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAdvancedOptions = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showRepairConfirm) {
                AlertDialog(
                    onDismissRequest = { showRepairConfirm = false },
                    title = { Text("Repair Local Database") },
                    text = { Text("This will attempt to fix metadata errors in your current local database file. Use this if you have manually replaced the database file but the app isn't recognizing it.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRepairConfirm = false
                                onRepairLocal()
                            }
                        ) {
                            Text("Repair Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRepairConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showBackupList && (viewModel.availableBackups.isNotEmpty() || viewModel.localBackups.isNotEmpty())) {
                AlertDialog(
                    onDismissRequest = { showBackupList = false },
                    title = { Text(if (isDownloadMode) "Select Backups to Download" else "Select Backup to Restore") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            if (viewModel.localBackups.isNotEmpty() && !isDownloadMode) {
                                Text(
                                    "Local Backups:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                viewModel.localBackups.forEach { file ->
                                    TextButton(
                                        onClick = {
                                            showBackupList = false
                                            showRestoreLocalConfirm = file
                                        },
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

                            viewModel.availableBackups.forEach { meta ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (isDownloadMode) {
                                        Checkbox(
                                            checked = selectedBackups.contains(meta.name),
                                            onCheckedChange = { checked ->
                                                selectedBackups = if (checked) {
                                                    selectedBackups + meta.name
                                                } else {
                                                    selectedBackups - meta.name
                                                }
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
                                            onClick = {
                                                showBackupList = false
                                                showRestoreConfirm = meta.name
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                meta.name,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        showDeleteConfirm = meta
                                    }) {
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
                                onClick = {
                                    showBackupList = false
                                    onDownloadBackups(selectedBackups.toList())
                                },
                                enabled = selectedBackups.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(Modifier.size(4.dp))
                                Text("Download")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBackupList = false }) {
                            Text("Cancel")
                        }
                    }
                )
            } else if (showBackupList && (viewModel.progressMessage == null)) {
                // If list is empty and not loading, show a message
                AlertDialog(
                    onDismissRequest = { showBackupList = false },
                    title = { Text("No Backups Found") },
                    text = { Text("No database backups were found in your Google Drive App Data folder.") },
                    confirmButton = {
                        TextButton(onClick = { showBackupList = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            showDeleteConfirm?.let { meta ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = null },
                    title = { Text("Confirm Delete") },
                    text = { Text("Are you sure you want to delete '${meta.name}' from Google Drive? This will also remove associated temporary files and cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = null
                            onDeleteBackup(meta)
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showRestoreLocalConfirm?.let { file ->
                AlertDialog(
                    onDismissRequest = { showRestoreLocalConfirm = null },
                    title = { Text("Confirm Local Restore") },
                    text = { Text("This will overwrite your local records with data from '${file.name}'. This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val target = file
                            showRestoreLocalConfirm = null
                            onRestoreLocal(target)
                        }) {
                            Text("Restore Now", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreLocalConfirm = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showRestoreConfirm?.let { fileName ->
                AlertDialog(
                    onDismissRequest = { showRestoreConfirm = null },
                    title = { Text("Confirm Restore") },
                    text = { Text("This will overwrite your local records with '$fileName'. This cannot be undone.") },
                    confirmButton = {
                        Column {
                            TextButton(onClick = {
                                val target = fileName
                                showRestoreConfirm = null
                                onRestore(target)
                            }) {
                                Text("Restore Now", color = MaterialTheme.colorScheme.error)
                            }
                            TextButton(onClick = {
                                showDeleteOthersConfirm = fileName
                                showRestoreConfirm = null
                            }) {
                                Text(
                                    "Restore & Delete Others",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirm = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showDeleteOthersConfirm?.let { fileName ->
                AlertDialog(
                    onDismissRequest = { showDeleteOthersConfirm = null },
                    title = { Text("Confirm Restore & Purge") },
                    text = { Text("This will restore local records from '$fileName' AND DELETE ALL OTHER BACKUPS from Google Drive. This action is permanent.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val target = fileName
                            showDeleteOthersConfirm = null
                            onRestore(target)
                            onDeleteOtherBackups(listOf(target))
                        }) {
                            Text("Confirm & Purge", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteOthersConfirm = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (viewModel.progressMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = viewModel.progressMessage ?: "",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            viewModel.showConflictDialog?.let { info ->
                ConflictDialog(
                    info = info,
                ) { choice, applyToAll ->
                    viewModel.onConflictChoice(choice, applyToAll)
                }
            }

            if (viewModel.showTransactionWarning) {
                AlertDialog(
                    onDismissRequest = { viewModel.showTransactionWarning = false },
                    title = { Text(stringResource(R.string.title_sync_transaction_warning)) },
                    text = { Text(stringResource(R.string.msg_sync_transaction_warning)) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.showTransactionWarning = false }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                )
            }

            if (viewModel.syncErrors.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { viewModel.syncErrors = emptyList() },
                    title = { Text("Sync Errors Encountered") },
                    text = {
                        Column {
                            Text("The following errors occurred but the sync attempted to continue:")
                            Spacer(modifier = Modifier.height(8.dp))
                            viewModel.syncErrors.forEach { error ->
                                Text("• $error", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.syncErrors = emptyList() }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                )
            }
        }
    }
}