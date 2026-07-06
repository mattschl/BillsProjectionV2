package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate

private const val TAG = "SyncViewModel"

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    var driveServiceHelper by mutableStateOf<DriveServiceHelper?>(null)
    var deviceId by mutableLongStateOf(0L)
    var progressMessage by mutableStateOf<String?>(null)
    var docContent by mutableStateOf(
        "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
                application.getString(R.string.sync_help_text),
    )

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    private var applyToAllChoice: ConflictChoice? = null

    var showConflictDialog by mutableStateOf<ConflictInfo?>(null)
    var showTransactionWarning by mutableStateOf(false)
    private var transactionWarningShownThisSync = false
    private var conflictDeferred: CompletableDeferred<ConflictChoice>? = null

    fun onConflictChoice(choice: ConflictChoice, applyToAll: Boolean) {
        if (applyToAll) {
            applyToAllChoice = choice
        }
        conflictDeferred?.complete(choice)
        showConflictDialog = null
    }

    fun queryDriveFiles() {
        progressMessage = "Querying Drive..."
        viewModelScope.launch {
            try {
                val helper = driveServiceHelper ?: return@launch
                val fileList = helper.queryFiles()
                val files = fileList.files ?: emptyList()

                val report = StringBuilder("Files in App Data Folder:\n")
                if (files.isEmpty()) {
                    report.append("No files found.")
                } else {
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

    fun sync(onError: (String, Exception, (() -> Unit)) -> Unit) {
        progressMessage = "Synchronizing..."
        transactionWarningShownThisSync = false
        viewModelScope.launch {
            var status = "Failed"
            val syncReport = StringBuilder("Sync Report:\n")
            val startTime = df.getCurrentTimeAsString()
            var uploadTimestamp: String? = null
            applyToAllChoice = null
            try {
                val helper = driveServiceHelper ?: return@launch
                val appDb = BillsDatabase(getApplication())

                val myLastSync = withContext(Dispatchers.IO) {
                    appDb.getSyncHistoryDao().getLastSyncTime(deviceId)
                } ?: "1970-01-01 00:00:00"

                syncReport.append("My last sync: $myLastSync\n")

                val allFiles: FileList = helper.queryFiles()
                val fileList = allFiles.files ?: emptyList()

                // Check for sync lock
                val lockFiles = fileList.filter { it.name == "sync.lock" }
                if (lockFiles.isNotEmpty()) {
                    val newestLock = lockFiles.maxByOrNull { it.modifiedTime.value }!!
                    val modifiedTime = newestLock.modifiedTime.value
                    val currentTime = System.currentTimeMillis()
                    val diffMinutes = (currentTime - modifiedTime) / (60 * 1000)
                    if (diffMinutes < 5) {
                        status = "Busy"
                        syncReport.append("\nAborted: Sync already in progress on another device.")
                        android.widget.Toast.makeText(
                            getApplication(),
                            R.string.sync_already_in_progress,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    } else {
                        // Stale locks, delete them all
                        for (lock in lockFiles) {
                            helper.deleteFile(lock.id)
                        }
                        syncReport.append("\nRemoved stale lock file(s).\n")
                    }
                }

                // Create new sync lock
                val tempLockFile = File(getApplication<Application>().cacheDir, "sync.lock")
                tempLockFile.writeText("Device: $deviceId\nStarted: $startTime")
                helper.uploadFile(tempLockFile, "text/plain", "sync.lock")
                tempLockFile.delete()

                val driveFiles = fileList.asSequence()
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .mapNotNull { file ->
                        val tsPart = file.name.substringAfter("bills2_").substringBefore(".db")
                        val date = df.parseFileTimestamp(tsPart)
                        if (date != null) {
                            val sqliteTs = df.getDateTimeStringFromDate(date)
                            file to sqliteTs
                        } else null
                    }
                    .filter { it.second > myLastSync }
                    .sortedBy { it.second }

                val driveFilesList = driveFiles.toList()

                if (driveFilesList.isEmpty()) {
                    syncReport.append("No new backups found on Drive.\n")
                } else {
                    syncReport.append("Found ${driveFilesList.size} backups to evaluate.\n")

                    for ((file, _) in driveFilesList) {
                        progressMessage = "Syncing ${file.name}..."
                        val context = getApplication<Application>()
                        val localBackupFile = File(context.cacheDir, file.name)
                        val localWalFile = File(context.cacheDir, "${file.name}-wal")
                        val localShmFile = File(context.cacheDir, "${file.name}-shm")

                        helper.downloadBinaryFile(file.name, localBackupFile, allFiles)
                        fileList.find { it.name == localWalFile.name }?.let {
                            helper.downloadBinaryFile(it.name, localWalFile, allFiles)
                        }
                        fileList.find { it.name == localShmFile.name }?.let {
                            helper.downloadBinaryFile(it.name, localShmFile, allFiles)
                        }

                        val result = processSync(localBackupFile)
                        syncReport.append("- ${file.name}: $result\n")

                        if (localBackupFile.exists()) localBackupFile.delete()
                        if (localWalFile.exists()) localWalFile.delete()
                        if (localShmFile.exists()) localShmFile.delete()
                    }
                }

                progressMessage = "Purging old records..."
                val budgetCutoff = LocalDate.now().minusMonths(2).toString()
                val syncCutoff = df.getTimeThreeWeeksAgo()
                withContext(Dispatchers.IO) {
                    appDb.getBudgetItemDao().purgeOldBudgetItems(budgetCutoff)
                    appDb.getSyncHistoryDao().purgeOldSyncHistory(syncCutoff)
                }
                syncReport.append("\nOld budget items purged (cutoff: $budgetCutoff).")
                syncReport.append("\nOld sync history records purged (cutoff: $syncCutoff).")

                progressMessage = "Uploading merged database..."
                uploadTimestamp = df.getCurrentFileTimestamp()
                val uploadedFile = performUpload(helper, uploadTimestamp)
                syncReport.append("\nMerged database uploaded: $uploadedFile")

                progressMessage = "Cleaning up old backups..."
                val driveBackups = fileList
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .sortedByDescending { it.name }

                val staleDate = LocalDate.now().minusDays(28).toString()

                // Get all successful sync records to identify redundancy and latest machine backups
                val allSuccessfulSyncs = withContext(Dispatchers.IO) {
                    appDb.getSyncHistoryDao().getAllSuccessfulSyncHistory()
                }

                val latestByMachine = allSuccessfulSyncs.groupBy { it.syncDeviceId }
                    .mapValues { entry -> entry.value.maxOf { it.syncTime } }
                val preservedByMachine = latestByMachine.values.toSet()

                // Identify candidates for deletion (stale or already synced)
                val candidates = driveBackups.filter { backup ->
                    val tsPart = backup.name.substringAfter("bills2_").substringBefore(".db")
                    val date = df.parseFileTimestamp(tsPart)
                    val sqliteTs = date?.let { df.getDateTimeStringFromDate(it) } ?: ""

                    // Safety: Never delete the latest backup from any machine
                    if (sqliteTs in preservedByMachine) return@filter false

                    // Cull if older than 28 days OR if already successfully synced
                    val isRedundant = allSuccessfulSyncs.any { it.syncTime == sqliteTs }
                    (sqliteTs < staleDate) || isRedundant
                }

                // Keep a minimum of 3 most recent backups regardless of age or redundancy
                val backupsToDelete = if (driveBackups.size <= 3) {
                    emptyList()
                } else {
                    val safeZone = driveBackups.take(3).toSet()
                    candidates.filter { it !in safeZone }
                }

                if (backupsToDelete.isNotEmpty()) {
                    for (baseFile in backupsToDelete) {
                        helper.deleteFile(baseFile.id)
                        fileList.find { it.name == "${baseFile.name}-wal" }
                            ?.let { helper.deleteFile(it.id) }
                        fileList.find { it.name == "${baseFile.name}-shm" }
                            ?.let { helper.deleteFile(it.id) }
                    }
                    syncReport.append("\nDeleted ${backupsToDelete.size} old/redundant backups from Drive.")
                }

                status = "Success"
                docContent = syncReport.toString()

            } catch (e: Exception) {
                status = "Error: ${e.message}"
                syncReport.append("\nError: ${e.message}")
                onError("Sync failed", e) { sync(onError) }
            } finally {
                val finalSyncTime = if (status == "Success" && uploadTimestamp != null) {
                    val driveDate = df.parseFileTimestamp(uploadTimestamp)
                    if (driveDate != null) {
                        df.getDateTimeStringFromDate(driveDate)
                    } else {
                        startTime
                    }
                } else {
                    startTime
                }
                logSyncHistory(finalSyncTime, status, syncReport.toString())

                // Release sync lock
                if (status != "Busy") {
                    try {
                        driveServiceHelper?.let { h ->
                            h.queryFiles().files?.filter { it.name == "sync.lock" }
                                ?.forEach { lock ->
                                    h.deleteFile(lock.id)
                                }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to release sync lock", e)
                    }
                }

                progressMessage = null
            }
        }
    }

    private suspend fun processSync(backupFile: File): String {
        return withContext(Dispatchers.IO) {
            val backupDb = SQLiteDatabase.openDatabase(
                backupFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
            val appDb = BillsDatabase(getApplication())
            val syncHelper = DatabaseSyncHelper(
                appDb = appDb,
                df = df,
                deviceId = deviceId,
                onConflict = { info -> showConflictDialog(info) }
            )

            val report = StringBuilder("Sync Results:\n")
            var totalCount = 0

            val at = syncHelper.syncAccountTypes(backupDb)
            if (at.first > 0 || at.second > 0) report.append("- Account Types: ${at.first} added, ${at.second} updated\n")
            totalCount += at.first + at.second

            val acc = syncHelper.syncAccounts(backupDb)
            if (acc.first > 0 || acc.second > 0) report.append("- Accounts: ${acc.first} added, ${acc.second} updated\n")
            totalCount += acc.first + acc.second

            val br = syncHelper.syncBudgetRules(backupDb)
            if (br.first > 0 || br.second > 0) report.append("- Budget Rules: ${br.first} added, ${br.second} updated\n")
            totalCount += br.first + br.second

            val trans = syncHelper.syncTransactions(backupDb)
            if (trans.first > 0 || trans.second > 0) {
                report.append("- Transactions: ${trans.first} added, ${trans.second} updated\n")
                if (!transactionWarningShownThisSync) {
                    showTransactionWarning = true
                    transactionWarningShownThisSync = true
                }
            }
            totalCount += trans.first + trans.second

            val bi = syncHelper.syncBudgetItems(backupDb)
            if (bi.first > 0 || bi.second > 0) report.append("- Budget Items: ${bi.first} added, ${bi.second} updated\n")
            totalCount += bi.first + bi.second

            val sh = syncHelper.syncSyncHistory(backupDb)
            if (sh.first > 0 || sh.second > 0) report.append("- Sync History: ${sh.first} added, ${sh.second} updated\n")
            totalCount += sh.first + sh.second

            backupDb.close()
            if (totalCount == 0) report.append("All local tables were already up to date Pull-side.\n")
            else report.append("\nTotal records synchronized from Drive: $totalCount\n")

            report.toString()
        }
    }

    private suspend fun showConflictDialog(info: ConflictInfo): ConflictChoice {
        applyToAllChoice?.let { return it }
        val deferred = CompletableDeferred<ConflictChoice>()
        conflictDeferred = deferred
        showConflictDialog = info
        return deferred.await()
    }

    private suspend fun logSyncHistory(time: String, status: String, records: String) {
        withContext(Dispatchers.IO) {
            try {
                val db = BillsDatabase(getApplication())
                val syncHistory = SyncHistory(
                    syncId = nf.generateId(),
                    syncTime = time,
                    syncSourceName = "Google Drive",
                    syncDeviceId = deviceId,
                    syncStatus = status,
                    syncRecordsProcessed = records
                )
                db.getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log sync history", e)
            }
        }
    }

    private suspend fun performUpload(
        helper: DriveServiceHelper,
        timestamp: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            val dbName = "bills2.db"
            val context = getApplication<Application>()
            val dbPath = context.getDatabasePath(dbName)
            val walPath = File(dbPath.path + "-wal")
            val shmPath = File(dbPath.path + "-shm")

            val db = BillsDatabase(context)
            db.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)").close()

            if (!dbPath.exists()) throw FileNotFoundException("Database file not found: ${dbPath.absolutePath}")

            val time = timestamp ?: df.getCurrentFileTimestamp()
            val driveBaseName = "bills2_$time.db"

            val filesToUpload = mutableListOf<Pair<File, String>>()
            filesToUpload.add(dbPath to driveBaseName)
            if (walPath.exists()) filesToUpload.add(walPath to "$driveBaseName-wal")
            if (shmPath.exists()) filesToUpload.add(shmPath to "$driveBaseName-shm")

            for ((localFile, driveName) in filesToUpload) {
                val uploadFile = File(context.cacheDir, "upload_$driveName")
                localFile.inputStream().use { input ->
                    uploadFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val mimeType = "application/vnd.sqlite3"
                helper.uploadFile(uploadFile, mimeType, driveName)
                uploadFile.delete()
            }

            driveBaseName
        }
    }
}