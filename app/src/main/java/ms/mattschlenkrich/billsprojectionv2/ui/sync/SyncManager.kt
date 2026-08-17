package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import java.io.File
import java.time.LocalDate

private const val TAG = "SyncManager"

class SyncManager(
    private val application: Application,
    private val deviceId: Long,
    private val driveServiceHelper: DriveServiceHelper,
    private val df: DateFunctions,
    private val nf: NumberFunctions,
    private val onProgressUpdate: (String) -> Unit,
    private val onConflict: suspend (ConflictInfo) -> ConflictChoice,
    private val onTransactionWarning: () -> Unit
) {
    private var transactionWarningShownThisSync = false

    suspend fun performSync(): Pair<String, String> {
        var status = "Failed"
        val syncReport = StringBuilder("Sync Report:\n")
        val startTime = df.getCurrentTimeAsString()
        var uploadTimestamp: String? = null

        try {
            val appDb = BillsDatabase(application)
            val myLastSync = withContext(Dispatchers.IO) {
                appDb.getSyncHistoryDao().getLastSyncTime(deviceId)
            } ?: "1970-01-01 00:00:00"

            syncReport.append("My last sync: $myLastSync\n")

            val allFiles: FileList = driveServiceHelper.queryFiles()
            val fileList = allFiles.files ?: emptyList()

            // Sync lock handling
            val lockFiles = fileList.filter { it.name == "sync.lock" }
            if (lockFiles.isNotEmpty()) {
                val newestLock = lockFiles.maxByOrNull { it.modifiedTime.value }!!
                val modifiedTime = newestLock.modifiedTime.value
                val diffMinutes = (System.currentTimeMillis() - modifiedTime) / (60 * 1000)
                if (diffMinutes < 5) {
                    status = "Busy"
                    return status to "Aborted: Sync already in progress on another device."
                } else {
                    for (lock in lockFiles) driveServiceHelper.deleteFile(lock.id)
                    syncReport.append("\nRemoved stale lock file(s).\n")
                }
            }

            val tempLockFile = File(application.cacheDir, "sync.lock")
            tempLockFile.writeText("Device: $deviceId\nStarted: $startTime")
            driveServiceHelper.uploadFile(tempLockFile, "text/plain", "sync.lock")
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
                .toList()

            if (driveFiles.isEmpty()) {
                syncReport.append("No new backups found on Drive.\n")
            } else {
                syncReport.append("Found ${driveFiles.size} backups to evaluate.\n")
                for ((file, _) in driveFiles) {
                    onProgressUpdate("Syncing ${file.name}...")
                    val result = processBackupFile(file, allFiles)
                    syncReport.append("- ${file.name}: $result\n")
                }
            }

            onProgressUpdate("Purging old records...")
            val budgetCutoff = LocalDate.now().minusMonths(2).toString()
            val syncCutoff = df.getTimeThreeWeeksAgo()
            withContext(Dispatchers.IO) {
                appDb.getBudgetItemDao().purgeOldBudgetItems(budgetCutoff)
                appDb.getSyncHistoryDao().purgeOldSyncHistory(syncCutoff)
            }

            onProgressUpdate("Uploading merged database...")
            uploadTimestamp = df.getCurrentFileTimestamp()
            val uploadedFile = performUpload(uploadTimestamp)
            syncReport.append("\nMerged database uploaded: $uploadedFile")

            cleanupOldBackups(fileList, appDb)

            status = "Success"
        } catch (e: Exception) {
            status = "Error: ${e.message}"
            syncReport.append("\nError: ${e.message}")
            throw e
        } finally {
            val finalSyncTime = if (status == "Success" && uploadTimestamp != null) {
                df.getDateTimeStringFromDate(df.parseFileTimestamp(uploadTimestamp)!!)
            } else startTime
            logSyncHistory(finalSyncTime, status, syncReport.toString())

            // Release lock
            if (status != "Busy") {
                driveServiceHelper.queryFiles().files?.filter { it.name == "sync.lock" }
                    ?.forEach { driveServiceHelper.deleteFile(it.id) }
            }
        }
        return status to syncReport.toString()
    }

    private suspend fun processBackupFile(
        file: com.google.api.services.drive.model.File,
        allFiles: FileList
    ): String {
        val localBackupFile = File(application.cacheDir, file.name)
        val localWalFile = File(application.cacheDir, "${file.name}-wal")
        val localShmFile = File(application.cacheDir, "${file.name}-shm")

        driveServiceHelper.downloadBinaryFile(file.name, localBackupFile, allFiles)
        allFiles.files?.find { it.name == localWalFile.name }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, localWalFile, allFiles)
        }
        allFiles.files?.find { it.name == localShmFile.name }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, localShmFile, allFiles)
        }

        val result = withContext(Dispatchers.IO) {
            val backupDb = SQLiteDatabase.openDatabase(
                localBackupFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val appDb = BillsDatabase(application)
            val syncHelper = DatabaseSyncHelper(appDb, df, deviceId, onConflict)

            var totalCount = 0

            val at = syncHelper.syncAccountTypes(backupDb); totalCount += at.first + at.second
            val acc = syncHelper.syncAccounts(backupDb); totalCount += acc.first + acc.second
            val br = syncHelper.syncBudgetRules(backupDb); totalCount += br.first + br.second
            val trans = syncHelper.syncTransactions(backupDb)
            if (trans.first > 0 || trans.second > 0) {
                if (!transactionWarningShownThisSync) {
                    onTransactionWarning()
                    transactionWarningShownThisSync = true
                }
            }
            totalCount += trans.first + trans.second
            val bi = syncHelper.syncBudgetItems(backupDb); totalCount += bi.first + bi.second
            val sh = syncHelper.syncSyncHistory(backupDb); totalCount += sh.first + sh.second

            backupDb.close()
            if (totalCount == 0) "All local tables were already up to date."
            else "Total records synchronized: $totalCount"
        }

        if (localBackupFile.exists()) localBackupFile.delete()
        if (localWalFile.exists()) localWalFile.delete()
        if (localShmFile.exists()) localShmFile.delete()

        return result
    }

    private suspend fun performUpload(timestamp: String): String {
        return withContext(Dispatchers.IO) {
            val dbName = "bills2.db"
            val dbPath = application.getDatabasePath(dbName)
            val walPath = File(dbPath.path + "-wal")
            val shmPath = File(dbPath.path + "-shm")

            BillsDatabase(application).openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)")
                .close()

            val driveBaseName = "bills2_$timestamp.db"
            val filesToUpload = mutableListOf(dbPath to driveBaseName)
            if (walPath.exists()) filesToUpload.add(walPath to "$driveBaseName-wal")
            if (shmPath.exists()) filesToUpload.add(shmPath to "$driveBaseName-shm")

            for ((localFile, driveName) in filesToUpload) {
                val uploadFile = File(application.cacheDir, "upload_$driveName")
                localFile.inputStream().use { input ->
                    uploadFile.outputStream().use { output -> input.copyTo(output) }
                }
                driveServiceHelper.uploadFile(uploadFile, "application/vnd.sqlite3", driveName)
                uploadFile.delete()
            }
            driveBaseName
        }
    }

    private suspend fun cleanupOldBackups(
        fileList: List<com.google.api.services.drive.model.File>,
        appDb: BillsDatabase
    ) {
        val driveBackups = fileList
            .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
            .sortedByDescending { it.name }

        if (driveBackups.size <= 3) return

        val staleDate = LocalDate.now().minusDays(28).toString()
        val allSuccessfulSyncs = withContext(Dispatchers.IO) {
            appDb.getSyncHistoryDao().getAllSuccessfulSyncHistory()
        }

        val preservedByMachine = allSuccessfulSyncs.groupBy { it.syncDeviceId }
            .mapValues { entry -> entry.value.maxOf { it.syncTime } }.values.toSet()

        val safeZone = driveBackups.take(3).toSet()
        val toDelete = driveBackups.filter { backup ->
            if (backup in safeZone) return@filter false
            val tsPart = backup.name.substringAfter("bills2_").substringBefore(".db")
            val date = df.parseFileTimestamp(tsPart) ?: return@filter false
            val sqliteTs = df.getDateTimeStringFromDate(date)
            if (sqliteTs in preservedByMachine) return@filter false
            (sqliteTs < staleDate) || allSuccessfulSyncs.any { it.syncTime == sqliteTs }
        }

        for (baseFile in toDelete) {
            driveServiceHelper.deleteFile(baseFile.id)
            fileList.find { it.name == "${baseFile.name}-wal" }
                ?.let { driveServiceHelper.deleteFile(it.id) }
            fileList.find { it.name == "${baseFile.name}-shm" }
                ?.let { driveServiceHelper.deleteFile(it.id) }
        }
    }

    private suspend fun logSyncHistory(time: String, status: String, records: String) {
        withContext(Dispatchers.IO) {
            try {
                val syncHistory = SyncHistory(
                    syncId = nf.generateId(), syncTime = time,
                    syncSourceName = "Google Drive", syncDeviceId = deviceId,
                    syncStatus = status, syncRecordsProcessed = records
                )
                BillsDatabase(application).getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "History log failed", e)
            }
        }
    }
}