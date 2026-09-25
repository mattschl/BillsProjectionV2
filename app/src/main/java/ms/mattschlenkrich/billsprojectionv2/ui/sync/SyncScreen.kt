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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

    LaunchedEffect(viewModel.deviceId) {
        if (viewModel.deviceId != 0L) {
            viewModel.loadInitialDocContent()
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
                                color = Color.DarkGray,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_go_back),
                            modifier = Modifier.size(ProjectFieldDefaults.iconSize()),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.ic_bills_projection_background),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
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
                SyncAdvancedDialog(
                    onDismiss = { showAdvancedOptions = false },
                    onRestore = {
                        showAdvancedOptions = false
                        isDownloadMode = false
                        viewModel.fetchAvailableBackups()
                        showBackupList = true
                    },
                    onQueryDownload = {
                        showAdvancedOptions = false
                        isDownloadMode = true
                        selectedBackups = emptySet()
                        viewModel.fetchAvailableBackups()
                        showBackupList = true
                    },
                    onUploadNow = {
                        showAdvancedOptions = false
                        onUploadNow()
                    },
                    onRepairLocal = {
                        showAdvancedOptions = false
                        showRepairConfirm = true
                    }
                )
            }

            if (showRepairConfirm) {
                SyncRepairConfirmDialog(
                    onDismiss = { showRepairConfirm = false },
                    onConfirm = {
                        showRepairConfirm = false
                        onRepairLocal()
                    }
                )
            }

            if (showBackupList && (viewModel.availableBackups.isNotEmpty() || viewModel.localBackups.isNotEmpty())) {
                SyncBackupListDialog(
                    isDownloadMode = isDownloadMode,
                    localBackups = viewModel.localBackups,
                    availableBackups = viewModel.availableBackups,
                    selectedBackups = selectedBackups,
                    onSelectedBackupsChange = { selectedBackups = it },
                    onSelectLocalBackup = { file ->
                        showBackupList = false
                        showRestoreLocalConfirm = file
                    },
                    onSelectCloudBackup = { fileName ->
                        showBackupList = false
                        showRestoreConfirm = fileName
                    },
                    onDeleteBackup = { meta -> showDeleteConfirm = meta },
                    onDownloadConfirm = {
                        showBackupList = false
                        onDownloadBackups(selectedBackups.toList())
                    },
                    onDismiss = { showBackupList = false }
                )
            } else if (showBackupList && (viewModel.progressMessage == null)) {
                AlertDialog(
                    onDismissRequest = { showBackupList = false },
                    title = { Text("No Backups Found") },
                    text = { Text("No database backups were found in your Google Drive App Data folder.") },
                    confirmButton = {
                        TextButton(onClick = { showBackupList = false }) {
                            Text(stringResource(R.string.action_ok))
                        }
                    }
                )
            }

            showDeleteConfirm?.let { meta ->
                SyncDeleteConfirmDialog(
                    meta = meta,
                    onDismiss = { showDeleteConfirm = null },
                    onConfirm = {
                        showDeleteConfirm = null
                        onDeleteBackup(meta)
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
                            Text(
                                stringResource(R.string.action_confirm),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreLocalConfirm = null }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }

            showRestoreConfirm?.let { fileName ->
                SyncRestoreConfirmDialog(
                    fileName = fileName,
                    onDismiss = { showRestoreConfirm = null },
                    onRestoreOnly = {
                        val target = fileName
                        showRestoreConfirm = null
                        onRestore(target)
                    },
                    onRestoreAndDeleteOthers = {
                        showDeleteOthersConfirm = fileName
                        showRestoreConfirm = null
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
                            Text(
                                stringResource(R.string.action_confirm),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteOthersConfirm = null }) {
                            Text(stringResource(R.string.action_cancel))
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
                            Text(stringResource(R.string.action_ok))
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
                            Text(stringResource(R.string.action_ok))
                        }
                    }
                )
            }
        }
    }
}