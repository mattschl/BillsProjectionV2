package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.common.DB_IDENTITY_HASH
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
    private val onTransactionWarning: () -> Unit,
    private val onSyncError: (String) -> Unit,
) {
    private var transactionWarningShownThisSync = false

    private val backupDir = File(application.cacheDir, "backup").apply { mkdirs() }

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
                val newestLock = lockFiles.maxByOrNull { it.modifiedTime.value }
                if (newestLock != null) {
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
            }

            val tempLockFile = File(backupDir, "sync.lock")
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
                    try {
                        val result = processBackupFile(file, allFiles)
                        syncReport.append("- ${file.name}: $result\n")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing backup file ${file.name}", e)
                        val errorMsg = "Failed to sync ${file.name}: ${e.message}"
                        onSyncError(errorMsg)
                        syncReport.append("- ${file.name}: FAILED\n")
                    }
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

            // Force a full checkpoint before upload to merge WAL into DB file.
            withContext(Dispatchers.IO) {
                try {
                    val db = appDb.openHelper.writableDatabase
                    db.execSQL("PRAGMA wal_checkpoint(FULL)")
                    Log.d(TAG, "Checkpoint successful before upload.")
                } catch (e: Exception) {
                    Log.e(TAG, "Checkpoint failed before upload", e)
                }
            }

            val uploadedFile = performUpload(uploadTimestamp)
            syncReport.append("\nMerged database uploaded: $uploadedFile")

            cleanupOldBackups(fileList, appDb)

            status = "Success"
        } catch (e: Exception) {
            status = "Error: ${e.message}"
            syncReport.append("\nError: ${e.message}")
            throw e
        } finally {
            val finalSyncTime = if ((status == "Success") && (uploadTimestamp != null)) {
                val date = df.parseFileTimestamp(uploadTimestamp)
                if (date != null) df.getDateTimeStringFromDate(date) else startTime
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

    suspend fun getAvailableBackups(): List<DriveFileMeta> {
        val allFiles = driveServiceHelper.queryFiles()
        return allFiles.files?.asSequence()
            ?.filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
            ?.sortedByDescending { it.name }
            ?.map { DriveFileMeta(it.id, it.name) }
            ?.toList() ?: emptyList()
    }

    fun getLocalBackups(): List<File> {
        return backupDir.listFiles()?.asSequence()?.filter {
            it.name.startsWith("bills2_") && it.name.endsWith(".db")
        }?.sortedByDescending { it.name }?.toList() ?: emptyList()
    }

    suspend fun deleteBackup(file: DriveFileMeta): String {
        onProgressUpdate("Deleting ${file.name}...")
        try {
            // Delete the main .db file
            driveServiceHelper.deleteFile(file.id)

            // Try to find and delete associated WAL/SHM files
            val allFiles = driveServiceHelper.queryFiles()
            allFiles.files?.filter { (it.name == "${file.name}-wal") || (it.name == "${file.name}-shm") }
                ?.forEach { auxFile ->
                    driveServiceHelper.deleteFile(auxFile.id)
                }

            return "Successfully deleted ${file.name}."
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup", e)
            throw e
        }
    }

    suspend fun downloadBackup(fileName: String): String {
        onProgressUpdate("Downloading $fileName...")
        val allFiles = driveServiceHelper.queryFiles()
        val localFile = File(backupDir, fileName)

        driveServiceHelper.downloadBinaryFile(fileName, localFile, allFiles)

        // Also download WAL/SHM if they exist
        allFiles.files?.find { it.name == "$fileName-wal" }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, File(backupDir, it.name), allFiles)
        }
        allFiles.files?.find { it.name == "$fileName-shm" }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, File(backupDir, it.name), allFiles)
        }

        return "Downloaded $fileName to local backup folder."
    }

    suspend fun restoreSpecific(fileName: String): String {
        Log.d(TAG, "Starting restore of $fileName (Cloud)")
        onProgressUpdate("Downloading $fileName...")
        val allFiles = driveServiceHelper.queryFiles()

        val localTempFile = File(backupDir, "restore_temp.db")
        val localTempWal = File(backupDir, "restore_temp.db-wal")
        val localTempShm = File(backupDir, "restore_temp.db-shm")

        try {
            driveServiceHelper.downloadBinaryFile(fileName, localTempFile, allFiles)
            if (localTempFile.length() == 0L) {
                throw Exception("Downloaded backup file is empty.")
            }

            // Try to download WAL/SHM if they exist on Drive for this specific backup
            allFiles.files?.find { it.name == "$fileName-wal" }?.let {
                driveServiceHelper.downloadBinaryFile(it.name, localTempWal, allFiles)
            }
            allFiles.files?.find { it.name == "$fileName-shm" }?.let {
                driveServiceHelper.downloadBinaryFile(it.name, localTempShm, allFiles)
            }

            return restoreFromFile(localTempFile, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            throw e
        } finally {
            if (localTempFile.exists()) localTempFile.delete()
            if (localTempWal.exists()) localTempWal.delete()
            if (localTempShm.exists()) localTempShm.delete()
        }
    }

    suspend fun restoreLocal(file: File): String {
        Log.d(TAG, "Starting record-level restore of local file: ${file.name}")
        return restoreFromFile(file, file.name)
    }

    suspend fun repairLocalDatabase(): String {
        return withContext(Dispatchers.IO) {
            try {
                onProgressUpdate("Repairing local database...")
                val dbName = "bills2.db"
                val dbPath = application.getDatabasePath(dbName)
                if (!dbPath.exists()) return@withContext "Local database file not found."

                Log.d(TAG, "Closing database for repair...")
                BillsDatabase.closeDatabase()

                val db = SQLiteDatabase.openDatabase(
                    dbPath.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE,
                )

                Log.d(TAG, "Forcing identity hash...")
                db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
                db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '$DB_IDENTITY_HASH')")

                Log.d(TAG, "Recreating views...")
                db.execSQL("DROP VIEW IF EXISTS `AccountAndType`")
                db.execSQL("CREATE VIEW `AccountAndType` AS SELECT accounts.*,accountTypes.* FROM accounts LEFT JOIN accountTypes on accounts.accountTypeId =accountTypes.typeId")

                // Final safety: clear WAL
                db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")

                db.close()
                "Local database metadata repaired successfully. Restarting..."
            } catch (e: Exception) {
                Log.e(TAG, "Repair failed", e)
                "Repair failed: ${e.message}"
            }
        }
    }

    private suspend fun restoreFromFile(
        dbFile: File,
        displayName: String
    ): String {
        Log.d(TAG, "Starting robust record-level restore of $displayName")
        onProgressUpdate("Clearing local data...")

        return withContext(Dispatchers.IO) {
            try {
                val appDb = BillsDatabase(application)

                // 1. Clear all local data to ensure a clean restore
                try {
                    appDb.clearAllTables()
                    Log.d(TAG, "Local tables cleared successfully.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to clear tables, continuing with overwrite mode", e)
                }

                // 2. Open the backup database
                val backupDb = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )

                // 3. Use syncHelper with isRestore=true to force full data copy
                val syncHelper = DatabaseSyncHelper(
                    appDb,
                    df,
                    deviceId,
                    onConflict,
                    onSyncError,
                    isRestore = true
                )

                onProgressUpdate("Copying records...")
                var totalCount = 0

                val at = syncHelper.syncAccountTypes(backupDb)
                Log.d(TAG, "Restore: Copied ${at.first + at.second} account types")
                totalCount += at.first + at.second

                val acc = syncHelper.syncAccounts(backupDb)
                Log.d(TAG, "Restore: Copied ${acc.first + acc.second} accounts")
                totalCount += acc.first + acc.second

                val br = syncHelper.syncBudgetRules(backupDb)
                Log.d(TAG, "Restore: Copied ${br.first + br.second} budget rules")
                totalCount += br.first + br.second

                val trans = syncHelper.syncTransactions(backupDb)
                Log.d(TAG, "Restore: Copied ${trans.first + trans.second} transactions")
                totalCount += trans.first + trans.second

                val bi = syncHelper.syncBudgetItems(backupDb)
                Log.d(TAG, "Restore: Copied ${bi.first + bi.second} budget items")
                totalCount += bi.first + bi.second

                val sh = syncHelper.syncSyncHistory(backupDb)
                totalCount += sh.first + sh.second

                backupDb.close()

                // 4. Force a checkpoint to make sure all copied data is on disk
                try {
                    val db = appDb.openHelper.writableDatabase
                    db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
                } catch (e: Exception) {
                    Log.e(TAG, "Post-restore checkpoint failed", e)
                }

                "Successfully restored $totalCount records from $displayName."
            } catch (e: Exception) {
                Log.e(TAG, "Robust restore failed: $displayName", e)
                throw Exception("Restore error: ${e.message ?: "Unknown error"}", e)
            }
        }
    }

    private suspend fun processBackupFile(
        file: com.google.api.services.drive.model.File,
        allFiles: FileList,
    ): String {
        val localBackupFile = File(backupDir, file.name)
        val localWalFile = File(backupDir, "${file.name}-wal")
        val localShmFile = File(backupDir, "${file.name}-shm")

        driveServiceHelper.downloadBinaryFile(file.name, localBackupFile, allFiles)
        allFiles.files?.find { it.name == localWalFile.name }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, localWalFile, allFiles)
        }
        allFiles.files?.find { it.name == localShmFile.name }?.let {
            driveServiceHelper.downloadBinaryFile(it.name, localShmFile, allFiles)
        }

        val result = withContext(Dispatchers.IO) {
            try {
                val backupDb = SQLiteDatabase.openDatabase(
                    localBackupFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )
                val appDb = BillsDatabase(application)
                val syncHelper = DatabaseSyncHelper(appDb, df, deviceId, onConflict, onSyncError)

                var totalCount = 0

                val at = syncHelper.syncAccountTypes(backupDb); totalCount += at.first + at.second
                val acc = syncHelper.syncAccounts(backupDb); totalCount += acc.first + acc.second
                val br = syncHelper.syncBudgetRules(backupDb); totalCount += br.first + br.second
                val trans = syncHelper.syncTransactions(backupDb)
                if ((trans.first > 0) || (trans.second > 0)) {
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
            } catch (e: Exception) {
                Log.e(TAG, "Error processing backup file ${file.name}", e)
                throw Exception("Failed to sync ${file.name}: ${e.message}", e)
            }
        }

        if (localBackupFile.exists()) localBackupFile.delete()
        if (localWalFile.exists()) localWalFile.delete()
        if (localShmFile.exists()) localShmFile.delete()

        return result
    }

    suspend fun manualUpload(): String {
        Log.d(TAG, "Starting manual upload of current database.")
        onProgressUpdate("Preparing database...")
        return withContext(Dispatchers.IO) {
            try {
                val appDb = BillsDatabase(application)
                // Force a full checkpoint before upload to merge WAL into DB file.
                try {
                    val db = appDb.openHelper.writableDatabase
                    db.execSQL("PRAGMA wal_checkpoint(FULL)")
                    Log.d(TAG, "Checkpoint successful before manual upload.")
                } catch (e: Exception) {
                    Log.e(TAG, "Checkpoint failed before manual upload", e)
                }

                onProgressUpdate("Uploading...")
                val timestamp = df.getCurrentFileTimestamp()
                val uploadedFile = performUpload(timestamp)

                "Successfully uploaded current state as $uploadedFile"
            } catch (e: Exception) {
                Log.e(TAG, "Manual upload failed", e)
                throw e
            }
        }
    }

    private suspend fun performUpload(timestamp: String): String {
        return withContext(Dispatchers.IO) {
            val dbName = "bills2.db"
            val dbPath = application.getDatabasePath(dbName)
            val walPath = File(dbPath.path + "-wal")
            val shmPath = File(dbPath.path + "-shm")

            val driveBaseName = "bills2_$timestamp.db"
            val filesToUpload = mutableListOf(dbPath to driveBaseName)
            if (walPath.exists() && walPath.length() > 0) {
                Log.d(TAG, "Including WAL file in upload: ${walPath.length()} bytes")
                filesToUpload.add(walPath to "$driveBaseName-wal")
            }
            if (shmPath.exists() && shmPath.length() > 0) {
                filesToUpload.add(shmPath to "$driveBaseName-shm")
            }

            for ((localFile, driveName) in filesToUpload) {
                val uploadFile = File(backupDir, "upload_$driveName")
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
        appDb: BillsDatabase,
    ) {
        val driveBackups = fileList.asSequence()
            .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
            .sortedByDescending { it.name }
            .toList()

        if (driveBackups.size <= 3) return

        val staleDate = LocalDate.now().minusDays(28).toString()
        val allSuccessfulSyncs = withContext(Dispatchers.IO) {
            appDb.getSyncHistoryDao().getAllSuccessfulSyncHistory()
        }

        val preservedByMachine = allSuccessfulSyncs.groupBy { it.syncDeviceId }
            .mapValues { entry -> entry.value.maxOf { it.syncTime } }.values.toSet()

        val safeZone = driveBackups.asSequence().take(3).toSet()
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
                    syncStatus = status, syncRecordsProcessed = records,
                )
                BillsDatabase(application).getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "History log failed", e)
            }
        }
    }
}