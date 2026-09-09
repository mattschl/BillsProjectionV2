package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import java.io.File

private const val TAG = "SyncViewModel"

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    var driveServiceHelper by mutableStateOf<DriveServiceHelper?>(null)
    var deviceId by mutableLongStateOf(0L)
    var progressMessage by mutableStateOf<String?>(null)
    var lastBackupTime by mutableStateOf<String?>(null)
    var availableBackups by mutableStateOf<List<DriveFileMeta>>(emptyList())
    var localBackups by mutableStateOf<List<File>>(emptyList())
    var docContent by mutableStateOf(
        "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
                application.getString(R.string.sync_help_text),
    )

    private val df = DateFunctions()

    private var applyToAllChoice: ConflictChoice? = null

    var showConflictDialog by mutableStateOf<ConflictInfo?>(null)
    var showTransactionWarning by mutableStateOf(value = false)
    var syncErrors by mutableStateOf<List<String>>(emptyList())
    private var conflictDeferred: CompletableDeferred<ConflictChoice>? = null

    fun onConflictChoice(choice: ConflictChoice, applyToAll: Boolean) {
        if (applyToAll) {
            applyToAllChoice = choice
        }
        conflictDeferred?.complete(choice)
        showConflictDialog = null
    }

    fun disconnect() {
        driveServiceHelper = null
        lastBackupTime = null
    }

    fun queryDriveFiles() {
        progressMessage = "Querying local & cloud..."
        viewModelScope.launch {
            try {
                val helper = driveServiceHelper ?: return@launch
                val fileList = helper.queryFiles()
                val files = fileList.files ?: emptyList()

                val report = StringBuilder()

                // Add Local DB Stats
                report.append("--- Local Database Statistics ---\n")
                withContext(Dispatchers.IO) {
                    try {
                        val dbName = "bills2.db"
                        val dbPath = getApplication<Application>().getDatabasePath(dbName)
                        report.append("Path: ${dbPath.absolutePath}\n")
                        report.append("Size: ${dbPath.length()} bytes\n")

                        if (dbPath.exists()) {
                            val db = BillsDatabase(getApplication())
                            val accountCount = db.getAccountDao().getAllAccountsSync().size
                            val transCount = db.getTransactionDao().getAllTransactionsSync().size
                            val budgetCount = db.getBudgetItemDao().getAllBudgetItemsSync().size
                            report.append("- Accounts: $accountCount\n")
                            report.append("- Transactions: $transCount\n")
                            report.append("- Scheduled Items: $budgetCount\n")
                        } else {
                            report.append("- Database file does not exist!\n")
                        }
                    } catch (e: Exception) {
                        report.append("- Error reading local DB: ${e.message}\n")
                    }
                }
                report.append("\n--- Cloud Backups ---\n")

                if (files.isEmpty()) {
                    report.append("No files found on Drive.")
                } else {
                    val backups = files.asSequence()
                        .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                        .sortedByDescending { it.name }
                        .toList()

                    if (backups.isNotEmpty()) {
                        val newest = backups.first()
                        val tsPart = newest.name.substringAfter("bills2_").substringBefore(".db")
                        df.parseFileTimestamp(tsPart)?.let { date ->
                            val sqliteTs = df.getDateTimeStringFromDate(date)
                            lastBackupTime = df.getLocalDisplayTime(sqliteTs)
                        }
                    }

                    files.asSequence()
                        .filter {
                            (it.name.startsWith("bills2_") && it.name.endsWith(".db")) ||
                                    it.name.endsWith("-wal") ||
                                    it.name.endsWith("-shm")
                        }
                        .sortedByDescending { it.name }
                        .forEach { file ->
                            report.append("- ${file.name} (${file.size} bytes)\n")
                        }
                }
                docContent = report.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Query failed", e)
                docContent = "Query failed: ${e.message}"
            } finally {
                progressMessage = null
            }
        }
    }

    fun fetchLastBackupTime() {
        val helper = driveServiceHelper ?: return
        viewModelScope.launch {
            try {
                val fileList = helper.queryFiles()
                val lastBackup = fileList.files?.asSequence()
                    ?.filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    ?.maxByOrNull { it.name }

                lastBackup?.let { file ->
                    val tsPart = file.name.substringAfter("bills2_").substringBefore(".db")
                    df.parseFileTimestamp(tsPart)?.let { date ->
                        val sqliteTs = df.getDateTimeStringFromDate(date)
                        lastBackupTime = df.getLocalDisplayTime(sqliteTs)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch last backup time", e)
            }
        }
    }

    fun sync(onSuccess: () -> Unit, onError: (String, Exception, (() -> Unit)) -> Unit) {
        progressMessage = "Synchronizing..."
        val helper = driveServiceHelper ?: return
        applyToAllChoice = null
        syncErrors = emptyList()

        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = NumberFunctions(),
            onProgressUpdate = { progressMessage = it },
            onConflict = { info -> showConflictDialogWrapper(info) },
            onTransactionWarning = { showTransactionWarning = true },
            onSyncError = { error -> syncErrors += error }
        )

        viewModelScope.launch {
            try {
                val result = manager.performSync()
                docContent = result.second
                if (result.first == "Success") {
                    onSuccess()
                } else if (result.first == "Busy") {
                    Toast.makeText(
                        getApplication(),
                        R.string.msg_sync_in_progress,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            } catch (e: Exception) {
                onError("Sync failed", e) { sync(onSuccess, onError) }
            } finally {
                progressMessage = null
            }
        }
    }

    fun fetchAvailableBackups() {
        val helper = driveServiceHelper ?: return
        progressMessage = "Listing backups..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                availableBackups = manager.getAvailableBackups()
                localBackups = manager.getLocalBackups()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch backups", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun restoreLocal(file: File, onSuccess: () -> Unit, onError: (String, Exception) -> Unit) {
        progressMessage = "Restoring from local file..."
        val helper = driveServiceHelper ?: return

        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = NumberFunctions(),
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onTransactionWarning = { },
            onSyncError = { }
        )

        viewModelScope.launch {
            try {
                val result = manager.restoreLocal(file)
                docContent = result
                if (result.startsWith("Successfully")) {
                    onSuccess()
                }
            } catch (e: Exception) {
                onError("Restore failed", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun repairDatabase(onSuccess: () -> Unit) {
        val helper = driveServiceHelper ?: return
        progressMessage = "Repairing..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                val result = manager.repairLocalDatabase()
                docContent = result
                if (result.contains("successfully")) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Repair error", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun deleteBackup(file: DriveFileMeta, onError: (String, Exception) -> Unit) {
        val helper = driveServiceHelper ?: return
        progressMessage = "Deleting backup..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                val result = manager.deleteBackup(file)
                docContent = result
                // Refresh list
                availableBackups = manager.getAvailableBackups()
            } catch (e: Exception) {
                onError("Delete failed", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun deleteOtherBackups(exceptFileNames: List<String>, onError: (String, Exception) -> Unit) {
        val helper = driveServiceHelper ?: return
        progressMessage = "Deleting other backups..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                val others = availableBackups.filter { !exceptFileNames.contains(it.name) }
                others.forEach { manager.deleteBackup(it) }
                docContent = "Deleted ${others.size} other backups."
                availableBackups = manager.getAvailableBackups()
            } catch (e: Exception) {
                onError("Failed to delete some backups", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun restore(fileName: String, onSuccess: () -> Unit, onError: (String, Exception) -> Unit) {
        progressMessage = "Restoring from Drive..."
        val helper = driveServiceHelper ?: return

        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = NumberFunctions(),
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE }, // Not used in restore
            onTransactionWarning = { },
            onSyncError = { }
        )

        viewModelScope.launch {
            try {
                val result = manager.restoreSpecific(fileName)
                docContent = result
                if (result.startsWith("Successfully")) {
                    onSuccess()
                }
            } catch (e: Exception) {
                onError("Restore failed", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun downloadBackups(fileNames: List<String>, onError: (String, Exception) -> Unit) {
        val helper = driveServiceHelper ?: return
        progressMessage = "Downloading backups..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                val results = StringBuilder("Download results:\n")
                fileNames.forEach { name ->
                    try {
                        val res = manager.downloadBackup(name)
                        results.append("- $res\n")
                    } catch (e: Exception) {
                        results.append("- Failed to download $name: ${e.message}\n")
                    }
                }
                docContent = results.toString()
            } catch (e: Exception) {
                onError("Failed to download backups", e)
            } finally {
                progressMessage = null
            }
        }
    }

    fun uploadNow(onSuccess: (String) -> Unit, onError: (String, Exception) -> Unit) {
        val helper = driveServiceHelper ?: return
        progressMessage = "Uploading current database..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = NumberFunctions(),
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                    onTransactionWarning = { },
                    onSyncError = { }
                )
                val result = manager.manualUpload()
                docContent = result
                onSuccess(result)
            } catch (e: Exception) {
                onError("Upload failed", e)
            } finally {
                progressMessage = null
            }
        }
    }

    private suspend fun showConflictDialogWrapper(info: ConflictInfo): ConflictChoice {
        applyToAllChoice?.let { return it }
        val deferred = CompletableDeferred<ConflictChoice>()
        conflictDeferred = deferred
        showConflictDialog = info
        return deferred.await()
    }
}