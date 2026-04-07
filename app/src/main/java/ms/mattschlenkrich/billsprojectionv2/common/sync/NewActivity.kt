package ms.mattschlenkrich.billsprojectionv2.common.sync

import android.accounts.Account
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityNewBinding
import java.io.File
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.time.LocalDate
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account as AccountModel

private const val TAG: String = "NewActivity"

class NewActivity : AppCompatActivity() {

    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var mCurrentAccount: Account? = null
    private var pendingAction: (() -> Unit)? = null
    private var mDeviceId: Long = 0L

    private lateinit var binding: ActivityNewBinding
    private lateinit var credentialManager: CredentialManager
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsManager(this)
        val fontSize = settingsManager.getSettings().fontSize
        when (fontSize) {
            "small" -> setTheme(R.style.Theme_BillsProjectionV2_Small)
            "large" -> setTheme(R.style.Theme_BillsProjectionV2_Large)
            else -> setTheme(R.style.Theme_BillsProjectionV2_Medium)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager = CredentialManager.create(this)

        binding.queryBtn.setOnClickListener { query() }
        binding.upButton.setOnClickListener { testUpload() }
        binding.downButton.setOnClickListener { testDownload() }
        binding.updateButton.setOnClickListener { testUpdate() }
        binding.syncButton.setOnClickListener { sync() }

        binding.returnButton.setOnClickListener {
            finish()
        }

        assignDeviceId()
        signInWithCredentialManager()
    }

    private fun assignDeviceId() {
        val settings = SettingsManager(this).getSettings()
        mDeviceId = settings.deviceId
        Log.d(TAG, "Assigned Device ID: $mDeviceId")
    }

    private fun showProgress(message: String) {
        binding.progressText.text = message
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressOverlay.visibility = View.GONE
    }

    private suspend fun getTargetFolderId(helper: DriveServiceHelper): String {
        val appsFolderId = helper.getOrCreateFolder("Apps")
        val appName = getString(R.string.app_name)
        return helper.getOrCreateFolder(appName, appsFolderId)
    }

    fun sync() {
        showProgress("Synchronizing...")
        // Reset the database instance to ensure we are working with the latest disk state
        BillsDatabase.resetInstance()
        lifecycleScope.launch {
            var status = "Failed"
            val syncReport = StringBuilder("Sync Report:\n")
            val startTime = df.getCurrentTimeAsString()
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val appDb = BillsDatabase(this@NewActivity)

                // 1. Determine start sync time
                val myLastSync = withContext(Dispatchers.IO) {
                    appDb.getSyncHistoryDao().getLastSyncTime(mDeviceId)
                } ?: "1970-01-01 00:00:00"

                syncReport.append("My last sync: $myLastSync\n")

                // 2. Query for backups on Drive
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val allFiles = fileList.files ?: emptyList()
                val driveFiles = allFiles
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .mapNotNull { file ->
                        val tsPart = file.name.substringAfter("bills2_").substringBefore(".db")
                        val date = df.parseFileTimestamp(tsPart)
                        if (date != null) {
                            val sqliteTs = df.getDateTimeStringFromDate(date)
                            file to sqliteTs
                        } else null
                    }
                    .filter { it.second >= myLastSync }
                    .sortedBy { it.second }

                if (driveFiles.isEmpty()) {
                    syncReport.append("No new backups found on Drive.\n")
                } else {
                    syncReport.append("Found ${driveFiles.size} backups to evaluate.\n")

                    for ((file, _) in driveFiles) {
                        showProgress("Syncing ${file.name}...")
                        val localBackupFile = File(cacheDir, file.name)
                        val localWalFile = File(cacheDir, "${file.name}-wal")
                        val localShmFile = File(cacheDir, "${file.name}-shm")

                        helper.downloadBinaryFile(file.name, localBackupFile, targetFolderId)
                        allFiles.find { it.name == localWalFile.name }?.let {
                            helper.downloadBinaryFile(it.name, localWalFile, targetFolderId)
                        }
                        allFiles.find { it.name == localShmFile.name }?.let {
                            helper.downloadBinaryFile(it.name, localShmFile, targetFolderId)
                        }

                        val result = processSync(localBackupFile)
                        syncReport.append("- ${file.name}: $result\n")

                        // Cleanup temporary backup files immediately
                        if (localBackupFile.exists()) localBackupFile.delete()
                        if (localWalFile.exists()) localWalFile.delete()
                        if (localShmFile.exists()) localShmFile.delete()
                    }
                }

                // 4. Purge old budget items (more than 2 months old)
                showProgress("Purging old budget items...")
                val cutoffDate = LocalDate.now().minusMonths(2).toString()
                withContext(Dispatchers.IO) {
                    appDb.getBudgetItemDao().purgeOldBudgetItems(cutoffDate)
                }
                syncReport.append("\nOld budget items purged from database (cutoff: $cutoffDate).")

                // 5. Final upload of merged database
                showProgress("Uploading merged database...")
                val uploadTimestamp = df.getCurrentFileTimestamp()
                val uploadedFile = performUpload(helper, targetFolderId, uploadTimestamp)
                syncReport.append("\nMerged database uploaded: $uploadedFile")

                // 4. Cleanup redundant backups
                showProgress("Cleaning up old backups...")
                val driveFileList = helper.queryFiles(targetFolderId)
                val allDriveFiles = driveFileList.files ?: emptyList()
                val driveBackups = allDriveFiles
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .sortedByDescending { it.name }

                if (driveBackups.size > 6) {
                    val backupsToDelete = driveBackups.drop(6)
                    for (baseFile in backupsToDelete) {
                        helper.deleteFile(baseFile.id)
                        allDriveFiles.find { it.name == "${baseFile.name}-wal" }
                            ?.let { helper.deleteFile(it.id) }
                        allDriveFiles.find { it.name == "${baseFile.name}-shm" }
                            ?.let { helper.deleteFile(it.id) }
                    }
                    syncReport.append("\nDeleted ${backupsToDelete.size} old backups from Drive.")
                }

                status = "Success"
                withContext(Dispatchers.Main) {
                    binding.docContentEdittext.setText(syncReport.toString())
                    Toast.makeText(this@NewActivity, "Sync complete", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                status = "Error: ${e.message}"
                syncReport.append("\nError: ${e.message}")
                handleError("Sync failed", e) { sync() }
            } finally {
                val finalSyncTime = if (status == "Success") {
                    val reportStr = syncReport.toString()
                    val driveTimestamp =
                        reportStr.substringAfter("Merged database uploaded: bills2_")
                            .substringBefore(".db")
                    val driveDate = df.parseFileTimestamp(driveTimestamp)
                    if (driveDate != null) {
                        df.getDateTimeStringFromDate(driveDate)
                    } else {
                        startTime
                    }
                } else {
                    startTime
                }
                logSyncHistory(finalSyncTime, status, syncReport.toString())
                hideProgress()
            }
        }
    }

    private suspend fun processSync(backupFile: File): String {
        return withContext(Dispatchers.IO) {
            val backupDb = SQLiteDatabase.openDatabase(
                backupFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val appDb = BillsDatabase(this@NewActivity)
            val dbPath =
                this@NewActivity.getDatabasePath(ms.mattschlenkrich.billsprojectionv2.common.DB_NAME)
            Log.d(TAG, "ProcessSync: Syncing with local database at: ${dbPath.absolutePath}")

            val report = StringBuilder("Sync Results:\n")
            var totalCount = 0

            // Helper to handle sync logic for each table
            suspend fun <T> syncTable(
                tableName: String,
                mapCursorToItem: (android.database.Cursor) -> T,
                getExisting: suspend (T) -> T?,
                getUpdateTime: (T) -> String,
                insert: suspend (T) -> Unit,
                update: suspend (T) -> Unit
            ): Pair<Int, Int> {
                var inserts = 0
                var updates = 0
                backupDb.query(tableName, null, null, null, null, null, null).use { cursor ->
                    Log.d(TAG, "Syncing table $tableName, records in backup: ${cursor.count}")
                    while (cursor.moveToNext()) {
                        val item = mapCursorToItem(cursor)
                        val existing = getExisting(item)
                        val backupTime = getUpdateTime(item)

                        if (existing == null) {
                            Log.d(
                                TAG,
                                "Table $tableName: Record not found locally. Inserting ID: $item"
                            )
                            insert(item)
                            inserts++
                        } else {
                            val localTime = getUpdateTime(existing)
                            // "Latest Wins" logic: Only update if the backup record is strictly newer.
                            // This ensures that local changes are preserved and deletions (via isDeleted flags)
                            // are synchronized correctly based on the most recent action.
                            if (backupTime > localTime) {
//                                Log.d(
//                                    TAG,
//                                    "Table $tableName: Backup is newer ($backupTime > $localTime). Updating record: $item"
//                                )
                                update(item)
                                updates++
                            } else {
//                                Log.d(
//                                    TAG,
//                                    "Table $tableName: Skipping record. Local version ($localTime) is same or newer than backup ($backupTime)"
//                                )
                            }
                        }
                    }
                }
                // Flush changes to disk after each table to ensure consistency for subsequent steps
                try {
                    appDb.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)").close()
                } catch (e: Exception) {
                    Log.e(TAG, "Checkpoint failed for $tableName", e)
                }
                return Pair(inserts, updates)
            }

            // Sync Account Types
            val at = syncTable(
                TABLE_ACCOUNT_TYPES,
                { cursor ->
                    AccountType(
                        typeId = cursor.getLong(cursor.getColumnIndexOrThrow("typeId")),
                        accountType = cursor.getString(cursor.getColumnIndexOrThrow("accountType")),
                        keepTotals = cursor.getInt(cursor.getColumnIndexOrThrow("keepTotals")) != 0,
                        isAsset = cursor.getInt(cursor.getColumnIndexOrThrow("isAsset")) != 0,
                        tallyOwing = cursor.getInt(cursor.getColumnIndexOrThrow("tallyOwing")) != 0,
                        keepMileage = cursor.getInt(cursor.getColumnIndexOrThrow("keepMileage")) != 0,
                        allowPending = cursor.getInt(cursor.getColumnIndexOrThrow("allowPending")) != 0,
                        displayAsAsset = cursor.getInt(cursor.getColumnIndexOrThrow("displayAsAsset")) != 0,
                        acctIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("acctIsDeleted")) != 0,
                        acctUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("acctUpdateTime"))
                    )
                },
                { appDb.getAccountTypesDao().findAccountType(it.typeId).firstOrNull() },
                { it.acctUpdateTime },
                {
                    withContext(Dispatchers.IO) {
                        appDb.getAccountTypesDao().insertAccountType(it)
                    }
                },
                { withContext(Dispatchers.IO) { appDb.getAccountTypesDao().updateAccountType(it) } }
            )
            if (at.first > 0 || at.second > 0) report.append("- Account Types: ${at.first} added, ${at.second} updated\n")
            totalCount += at.first + at.second

            // Sync Accounts
            val acc = syncTable(
                TABLE_ACCOUNTS,
                { cursor ->
                    AccountModel(
                        accountId = cursor.getLong(cursor.getColumnIndexOrThrow("accountId")),
                        accountName = cursor.getString(cursor.getColumnIndexOrThrow("accountName")),
                        accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("accountNumber")),
                        accountTypeId = cursor.getLong(cursor.getColumnIndexOrThrow("accountTypeId")),
                        accBudgetedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("accBudgetedAmount")),
                        accountBalance = cursor.getDouble(cursor.getColumnIndexOrThrow("accountBalance")),
                        accountOwing = cursor.getDouble(cursor.getColumnIndexOrThrow("accountOwing")),
                        accountCreditLimit = cursor.getDouble(cursor.getColumnIndexOrThrow("accountCreditLimit")),
                        accIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("accIsDeleted")) != 0,
                        accUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("accUpdateTime"))
                    )
                },
                { appDb.getAccountDao().findAccount(it.accountId).firstOrNull() },
                { it.accUpdateTime },
                { withContext(Dispatchers.IO) { appDb.getAccountDao().insertAccount(it) } },
                { withContext(Dispatchers.IO) { appDb.getAccountDao().updateAccount(it) } }
            )
            if (acc.first > 0 || acc.second > 0) report.append("- Accounts: ${acc.first} added, ${acc.second} updated\n")
            totalCount += acc.first + acc.second

            // Sync Budget Rules
            val br = syncTable(
                TABLE_BUDGET_RULES,
                { cursor ->
                    BudgetRule(
                        ruleId = cursor.getLong(cursor.getColumnIndexOrThrow("ruleId")),
                        budgetRuleName = cursor.getString(cursor.getColumnIndexOrThrow("budgetRuleName")),
                        budToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("budToAccountId")),
                        budFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("budFromAccountId")),
                        budgetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("budgetAmount")),
                        budFixedAmount = cursor.getInt(cursor.getColumnIndexOrThrow("budFixedAmount")) != 0,
                        budIsPayDay = cursor.getInt(cursor.getColumnIndexOrThrow("budIsPayDay")) != 0,
                        budIsAutoPay = cursor.getInt(cursor.getColumnIndexOrThrow("budIsAutoPay")) != 0,
                        budStartDate = cursor.getString(cursor.getColumnIndexOrThrow("budStartDate")),
                        budEndDate = cursor.getString(cursor.getColumnIndexOrThrow("budEndDate")),
                        budDayOfWeekId = cursor.getInt(cursor.getColumnIndexOrThrow("budDayOfWeekId")),
                        budFrequencyTypeId = cursor.getInt(cursor.getColumnIndexOrThrow("budFrequencyTypeId")),
                        budFrequencyCount = cursor.getInt(cursor.getColumnIndexOrThrow("budFrequencyCount")),
                        budLeadDays = cursor.getInt(cursor.getColumnIndexOrThrow("budLeadDays")),
                        budIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("budIsDeleted")) != 0,
                        budUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("budUpdateTime"))
                    )
                },
                { appDb.getBudgetRuleDao().getBudgetRule(it.ruleId) },
                { it.budUpdateTime },
                { withContext(Dispatchers.IO) { appDb.getBudgetRuleDao().insertBudgetRule(it) } },
                { withContext(Dispatchers.IO) { appDb.getBudgetRuleDao().updateBudgetRule(it) } }
            )
            if (br.first > 0 || br.second > 0) report.append("- Budget Rules: ${br.first} added, ${br.second} updated\n")
            totalCount += br.first + br.second

            // Sync Transactions
            val trans = syncTable(
                TABLE_TRANSACTION,
                { cursor ->
                    Transactions(
                        transId = cursor.getLong(cursor.getColumnIndexOrThrow("transId")),
                        transDate = cursor.getString(cursor.getColumnIndexOrThrow("transDate")),
                        transName = cursor.getString(cursor.getColumnIndexOrThrow("transName")),
                        transNote = cursor.getString(cursor.getColumnIndexOrThrow("transNote")),
                        transRuleId = cursor.getLong(cursor.getColumnIndexOrThrow("transRuleId")),
                        transToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("transToAccountId")),
                        transToAccountPending = cursor.getInt(cursor.getColumnIndexOrThrow("transToAccountPending")) != 0,
                        transFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("transFromAccountId")),
                        transFromAccountPending = cursor.getInt(cursor.getColumnIndexOrThrow("transFromAccountPending")) != 0,
                        transAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("transAmount")),
                        transIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("transIsDeleted")) != 0,
                        transUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("transUpdateTime"))
                    )
                },
                { appDb.getTransactionDao().getTransaction(it.transId) },
                { it.transUpdateTime },
                { withContext(Dispatchers.IO) { appDb.getTransactionDao().insertTransaction(it) } },
                { withContext(Dispatchers.IO) { appDb.getTransactionDao().updateTransaction(it) } }
            )
            if (trans.first > 0 || trans.second > 0) report.append("- Transactions: ${trans.first} added, ${trans.second} updated\n")
            totalCount += trans.first + trans.second

            // Sync Budget Items
            val bi = syncTable(
                TABLE_BUDGET_ITEMS,
                { cursor ->
                    BudgetItem(
                        biRuleId = cursor.getLong(cursor.getColumnIndexOrThrow("biRuleId")),
                        biProjectedDate = cursor.getString(cursor.getColumnIndexOrThrow("biProjectedDate")),
                        biActualDate = cursor.getString(cursor.getColumnIndexOrThrow("biActualDate")),
                        biPayDay = cursor.getString(cursor.getColumnIndexOrThrow("biPayDay")),
                        biBudgetName = cursor.getString(cursor.getColumnIndexOrThrow("biBudgetName")),
                        biIsPayDayItem = cursor.getInt(cursor.getColumnIndexOrThrow("biIsPayDayItem")) != 0,
                        biToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("biToAccountId")),
                        biFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("biFromAccountId")),
                        biProjectedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("biProjectedAmount")),
                        biIsPending = cursor.getInt(cursor.getColumnIndexOrThrow("biIsPending")) != 0,
                        biIsFixed = cursor.getInt(cursor.getColumnIndexOrThrow("biIsFixed")) != 0,
                        biIsAutomatic = cursor.getInt(cursor.getColumnIndexOrThrow("biIsAutomatic")) != 0,
                        biManuallyEntered = cursor.getInt(cursor.getColumnIndexOrThrow("biManuallyEntered")) != 0,
                        biIsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("biIsCompleted")) != 0,
                        biIsCancelled = cursor.getInt(cursor.getColumnIndexOrThrow("biIsCancelled")) != 0,
                        biIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("biIsDeleted")) != 0,
                        biUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("biUpdateTime")),
                        biLocked = cursor.getInt(cursor.getColumnIndexOrThrow("biLocked")) != 0
                    )
                },
                { appDb.getBudgetItemDao().getBudgetItem(it.biRuleId, it.biProjectedDate) },
                { it.biUpdateTime },
                { withContext(Dispatchers.IO) { appDb.getBudgetItemDao().insertBudgetItem(it) } },
                { withContext(Dispatchers.IO) { appDb.getBudgetItemDao().updateBudgetItem(it) } }
            )
            if (bi.first > 0 || bi.second > 0) report.append("- Budget Items: ${bi.first} added, ${bi.second} updated\n")
            totalCount += bi.first + bi.second

            // Sync Sync History
            val sh = syncTable(
                TABLE_SYNC_HISTORY,
                { cursor ->
                    SyncHistory(
                        syncId = cursor.getLong(cursor.getColumnIndexOrThrow("syncId")),
                        syncTime = cursor.getString(cursor.getColumnIndexOrThrow("syncTime")),
                        syncSourceName = cursor.getString(cursor.getColumnIndexOrThrow("syncSourceName")),
                        syncDeviceId = cursor.getLong(cursor.getColumnIndexOrThrow("syncDeviceId")),
                        syncStatus = cursor.getString(cursor.getColumnIndexOrThrow("syncStatus")),
                        syncRecordsProcessed = cursor.getString(cursor.getColumnIndexOrThrow("syncRecordsProcessed"))
                    )
                },
                { appDb.getSyncHistoryDao().getSyncHistory(it.syncId) },
                { it.syncTime },
                {
                    // Avoid circular updates: don't insert our own device's sync history from other backups
                    if (it.syncDeviceId != mDeviceId) {
                        withContext(Dispatchers.IO) {
                            appDb.getSyncHistoryDao().insertSyncHistory(it)
                        }
                    }
                },
                {
                    if (it.syncDeviceId != mDeviceId) {
                        withContext(Dispatchers.IO) {
                            appDb.getSyncHistoryDao().updateSyncHistory(it)
                        }
                    }
                }
            )
            if (sh.first > 0 || sh.second > 0) report.append("- Sync History: ${sh.first} added, ${sh.second} updated\n")
            totalCount += sh.first + sh.second

            backupDb.close()
            if (totalCount == 0) report.append("All local tables were already up to date Pull-side.\n")
            else report.append("\nTotal records synchronized from Drive: $totalCount\n")

            report.toString()
        }
    }

    private suspend fun logSyncHistory(time: String, status: String, records: String) {
        withContext(Dispatchers.IO) {
            try {
                val db = BillsDatabase(this@NewActivity)
                val syncHistory = SyncHistory(
                    syncId = nf.generateId(),
                    syncTime = time,
                    syncSourceName = "Google Drive",
                    syncDeviceId = mDeviceId,
                    syncStatus = status,
                    syncRecordsProcessed = records
                )
                db.getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log sync history", e)
            }
        }
    }

    fun testDownload() {
        showProgress("Searching for latest backup...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val allFiles = fileList.files ?: emptyList()

                val latestFile = allFiles
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .maxByOrNull { it.name }

                if (latestFile == null) {
                    Toast.makeText(
                        this@NewActivity,
                        "No backups found on Drive",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val localFile = File(cacheDir, latestFile.name)
                val localWalFile = File(cacheDir, "${latestFile.name}-wal")
                val localShmFile = File(cacheDir, "${latestFile.name}-shm")

                localFile.parentFile?.mkdirs()
                showProgress("Downloading ${latestFile.name}...")
                helper.downloadBinaryFile(latestFile.name, localFile, targetFolderId)
                allFiles.find { it.name == localWalFile.name }?.let {
                    helper.downloadBinaryFile(it.name, localWalFile, targetFolderId)
                }
                allFiles.find { it.name == localShmFile.name }?.let {
                    helper.downloadBinaryFile(it.name, localShmFile, targetFolderId)
                }

                Toast.makeText(
                    this@NewActivity,
                    "Downloaded to temporary cache: ${latestFile.name}",
                    Toast.LENGTH_SHORT
                ).show()

                // Do not keep backup copies on the device
                if (localFile.exists()) localFile.delete()
                if (localWalFile.exists()) localWalFile.delete()
                if (localShmFile.exists()) localShmFile.delete()
            } catch (e: Exception) {
                handleError("Download failed", e) { testDownload() }
            } finally {
                hideProgress()
            }
        }
    }

    fun testUpload() {
        showProgress("Uploading database...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val uploadedFile = performUpload(helper, targetFolderId)
                Toast.makeText(
                    this@NewActivity,
                    "Upload successful: $uploadedFile",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                handleError("Upload failed", e) { testUpload() }
            } finally {
                hideProgress()
            }
        }
    }

    private suspend fun performUpload(
        helper: DriveServiceHelper,
        targetFolderId: String,
        timestamp: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            val dbName = "bills2.db"
            val dbPath = getDatabasePath(dbName)
            val walPath = File(dbPath.path + "-wal")
            val shmPath = File(dbPath.path + "-shm")

            val db = BillsDatabase(this@NewActivity)
            // Ensure all data is flushed from WAL to the main DB file
            db.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)").close()
            // Close the database to ensure the file is not busy and fully written
            db.close()
            BillsDatabase.resetInstance()

            if (!dbPath.exists()) throw FileNotFoundException("Database file not found: ${dbPath.absolutePath}")

            val time = timestamp ?: df.getCurrentFileTimestamp()
            val driveBaseName = "bills2_$time.db"

            val filesToUpload = mutableListOf<Pair<File, String>>()
            filesToUpload.add(dbPath to driveBaseName)
            if (walPath.exists()) filesToUpload.add(walPath to "$driveBaseName-wal")
            if (shmPath.exists()) filesToUpload.add(shmPath to "$driveBaseName-shm")

            for ((localFile, driveName) in filesToUpload) {
                val uploadFile = File(cacheDir, "upload_$driveName")
                localFile.inputStream().use { input ->
                    uploadFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val mimeType =
                    if (driveName.endsWith(".db")) "application/vnd.sqlite3" else "application/octet-stream"
                helper.uploadFile(uploadFile, mimeType, driveName, targetFolderId)
                uploadFile.delete()
            }

            driveBaseName
        }
    }

    fun testUpdate() {
        showProgress("Cleaning up old backups...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)

                val driveFileList = helper.queryFiles(targetFolderId)
                val allFiles = driveFileList.files ?: emptyList()
                val driveBackups = allFiles
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .sortedByDescending { it.name }

                if (driveBackups.size > 6) {
                    val backupsToDelete = driveBackups.drop(6)
                    for (baseFile in backupsToDelete) {
                        helper.deleteFile(baseFile.id)
                        allFiles.find { it.name == "${baseFile.name}-wal" }
                            ?.let { helper.deleteFile(it.id) }
                        allFiles.find { it.name == "${baseFile.name}-shm" }
                            ?.let { helper.deleteFile(it.id) }
                    }
                }

                hideProgress()
                testDownload()
            } catch (e: Exception) {
                handleError("Update failed", e) { testUpdate() }
            } finally {
                hideProgress()
            }
        }
    }

    private fun handleError(message: String, e: Exception, action: (() -> Unit)? = null) {
        Log.e(TAG, message, e)
        if (e is UserRecoverableAuthIOException) {
            pendingAction = action
            recoverAuthLauncher.launch(e.intent)
        } else {
            val errMsg = if (e.message?.contains("verification process") == true) {
                "Access Blocked: Add your email as a 'Test User' in Google Cloud Console."
            } else {
                "$message: ${e.message}"
            }
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
        }
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mCurrentAccount?.name?.let {
                mDriveServiceHelper = null
                initializeDriveService(it)
                pendingAction?.invoke()
            }
        }
    }

    private fun signInWithCredentialManager() {
        lifecycleScope.launch {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            try {
                val result = credentialManager.getCredential(this@NewActivity, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                initializeDriveService(googleIdTokenCredential.id)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
            }
        }
    }

    private fun initializeDriveService(email: String) {
        val account = Account(email, "com.google")
        try {
            val credential = GoogleAccountCredential.usingOAuth2(this, DRIVE_SCOPES)
            credential.selectedAccount = account
            val googleDriveService = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
            mDriveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
        } catch (e: Exception) {
            Log.e(TAG, "Drive init failed", e)
        }
    }

    private fun query() {
        val helper = mDriveServiceHelper ?: return
        showProgress("Querying...")
        lifecycleScope.launch {
            try {
                val targetFolderId = getTargetFolderId(helper)
                val fileList = helper.queryFiles(targetFolderId)
                val names = fileList.files.joinToString("\n") { it.name }
                binding.docContentEdittext.setText(names)
            } catch (e: Exception) {
                handleError("Query failed", e) { query() }
            } finally {
                hideProgress()
            }
        }
    }

    private fun generateNonce() = Base64.encodeToString(
        ByteArray(16).apply { SecureRandom().nextBytes(this) },
        Base64.URL_SAFE
    )

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_FILE)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}