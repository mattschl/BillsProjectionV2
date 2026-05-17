package ms.mattschlenkrich.billsprojectionv2.common.sync

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
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
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
                application.getString(R.string.sync_help_text)
    )

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    var showConflictDialog by mutableStateOf<ConflictInfo?>(null)
    private var conflictDeferred: CompletableDeferred<ConflictChoice>? = null

    data class ConflictInfo(
        val tableName: String,
        val name: String,
        val localId: Long,
        val localTime: String,
        val driveId: Long,
        val driveTime: String,
        val messageResId: Int? = null
    )

    enum class ConflictChoice { KEEP_LOCAL, KEEP_DRIVE }

    fun onConflictChoice(choice: ConflictChoice) {
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
                    files.filter {
                        it.name.startsWith("bills2_") && it.name.endsWith(".db") || it.name.endsWith(
                            "-wal"
                        ) || it.name.endsWith("-shm")
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
        viewModelScope.launch {
            var status = "Failed"
            val syncReport = StringBuilder("Sync Report:\n")
            val startTime = df.getCurrentTimeAsString()
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
                val lockFile = fileList.find { it.name == "sync.lock" }
                if (lockFile != null) {
                    val modifiedTime = lockFile.modifiedTime.value
                    val currentTime = System.currentTimeMillis()
                    val diffMinutes = (currentTime - modifiedTime) / (60 * 1000)
                    if (diffMinutes < 10) {
                        status = "Busy"
                        syncReport.append("\nAborted: Sync already in progress on another device.")
                        android.widget.Toast.makeText(
                            getApplication(),
                            R.string.sync_already_in_progress,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    } else {
                        // Stale lock, delete it
                        helper.deleteFile(lockFile.id)
                        syncReport.append("\nRemoved stale lock file.\n")
                    }
                }

                // Create new sync lock
                val tempLockFile = File(getApplication<Application>().cacheDir, "sync.lock")
                tempLockFile.writeText("Device: $deviceId\nStarted: $startTime")
                helper.uploadFile(tempLockFile, "text/plain", "sync.lock")
                tempLockFile.delete()

                val driveFiles = fileList
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

                if (driveFiles.isEmpty()) {
                    syncReport.append("No new backups found on Drive.\n")
                } else {
                    syncReport.append("Found ${driveFiles.size} backups to evaluate.\n")

                    for ((file, _) in driveFiles) {
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
                val uploadTimestamp = df.getCurrentFileTimestamp()
                val uploadedFile = performUpload(helper, uploadTimestamp)
                syncReport.append("\nMerged database uploaded: $uploadedFile")

                progressMessage = "Cleaning up old backups..."
                val driveBackups = fileList
                    .filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    .sortedByDescending { it.name }

                if (driveBackups.size > 6) {
                    val backupsToDelete = driveBackups.drop(6)
                    for (baseFile in backupsToDelete) {
                        helper.deleteFile(baseFile.id)
                        fileList.find { it.name == "${baseFile.name}-wal" }
                            ?.let { helper.deleteFile(it.id) }
                        fileList.find { it.name == "${baseFile.name}-shm" }
                            ?.let { helper.deleteFile(it.id) }
                    }
                    syncReport.append("\nDeleted ${backupsToDelete.size} old backups from Drive.")
                }

                status = "Success"
                docContent = syncReport.toString()

            } catch (e: Exception) {
                status = "Error: ${e.message}"
                syncReport.append("\nError: ${e.message}")
                onError("Sync failed", e) { sync(onError) }
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

                // Release sync lock
                if (status != "Busy") {
                    try {
                        driveServiceHelper?.let { h ->
                            h.queryFiles().files?.find { it.name == "sync.lock" }?.let { lock ->
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
                SQLiteDatabase.OPEN_READONLY
            )
            val appDb = BillsDatabase(getApplication())

            val report = StringBuilder("Sync Results:\n")
            var totalCount = 0

            suspend fun <T> syncTable(
                tableName: String,
                mapCursorToItem: (android.database.Cursor) -> T,
                getExistingLocalMap: (suspend () -> Map<String, T>)? = null,
                getItemKey: (T) -> String,
                getExistingById: suspend (T) -> T?,
                getExistingByName: (suspend (T) -> T?)? = null,
                getUpdateTime: (T) -> String,
                getName: ((T) -> String)? = null,
                getId: ((T) -> Long)? = null,
                insert: suspend (T) -> Unit,
                update: suspend (T) -> Unit,
                rename: (suspend (Long, String, String) -> Unit)? = null,
                copyWithName: ((T, String) -> T)? = null
            ): Pair<Int, Int> {
                var inserts = 0
                var updates = 0
                val localMap = getExistingLocalMap?.invoke() ?: emptyMap()

                backupDb.query(tableName, null, null, null, null, null, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val backupItem = mapCursorToItem(cursor)
                        val existingById =
                            localMap[getItemKey(backupItem)] ?: getExistingById(backupItem)
                        val backupTime = getUpdateTime(backupItem)

                        if (existingById == null) {
                            val existingByName = getExistingByName?.invoke(backupItem)
                            if (existingByName != null && getName != null && getId != null && rename != null) {
                                val localName = getName(existingByName)
                                val localId = getId(existingByName)
                                val localTime = getUpdateTime(existingByName)

                                val choice = showConflictDialog(
                                    tableName,
                                    localName,
                                    localId,
                                    localTime,
                                    getId(backupItem),
                                    backupTime
                                )

                                when (choice) {
                                    ConflictChoice.KEEP_LOCAL -> {
                                        val newBackupName =
                                            "${getName(backupItem)}_DRIVE_${getId(backupItem)}"
                                        val renamedItem =
                                            copyWithName?.invoke(backupItem, newBackupName)
                                                ?: backupItem
                                        insert(renamedItem)
                                        inserts++
                                    }

                                    ConflictChoice.KEEP_DRIVE -> {
                                        val newLocalName = "${localName}_LOCAL_$localId"
                                        rename(localId, newLocalName, df.getCurrentTimeAsString())
                                        insert(backupItem)
                                        inserts++
                                    }
                                }
                            } else {
                                insert(backupItem)
                                inserts++
                            }
                        } else {
                            val localTime = getUpdateTime(existingById)
                            if (backupTime > localTime) {
                                update(backupItem)
                                updates++
                            }
                        }
                    }
                }
                try {
                    appDb.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)").close()
                } catch (e: Exception) {
                    Log.e(TAG, "Checkpoint failed for $tableName", e)
                }
                return Pair(inserts, updates)
            }

            val at = syncTable(
                tableName = TABLE_ACCOUNT_TYPES,
                mapCursorToItem = { cursor ->
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
                getItemKey = { it.typeId.toString() },
                getExistingById = {
                    appDb.getAccountTypesDao().findAccountType(it.typeId).firstOrNull()
                },
                getExistingByName = {
                    appDb.getAccountTypesDao().findAccountTypeByName(it.accountType)
                },
                getUpdateTime = { it.acctUpdateTime },
                getName = { it.accountType },
                getId = { it.typeId },
                insert = { appDb.getAccountTypesDao().insertAccountType(it) },
                update = { appDb.getAccountTypesDao().updateAccountType(it) },
                rename = { id, name, time ->
                    appDb.getAccountTypesDao().renameAccountType(id, name, time)
                },
                copyWithName = { item, name -> item.copy(accountType = name) }
            )
            if (at.first > 0 || at.second > 0) report.append("- Account Types: ${at.first} added, ${at.second} updated\n")
            totalCount += at.first + at.second

            val backupTransIds = mutableSetOf<Long>()
            val trans = syncTable(
                tableName = "Transactions",
                mapCursorToItem = { cursor ->
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
                    ).also { backupTransIds.add(it.transId) }
                },
                getExistingLocalMap = {
                    appDb.getTransactionDao().getAllTransactionsSync()
                        .associateBy { it.transId.toString() }
                },
                getItemKey = { it.transId.toString() },
                getExistingById = { appDb.getTransactionDao().getTransaction(it.transId) },
                getExistingByName = { backupItem ->
                    val backupDate = LocalDate.parse(backupItem.transDate)
                    appDb.getTransactionDao().getAllTransactionsSync().find { localItem ->
                        val localDate = LocalDate.parse(localItem.transDate)
                        val daysDiff =
                            java.time.temporal.ChronoUnit.DAYS.between(backupDate, localDate)

                        localItem.transId != backupItem.transId &&
                                Math.abs(daysDiff) <= 2 &&
                                localItem.transAmount == backupItem.transAmount &&
                                localItem.transToAccountId == backupItem.transToAccountId &&
                                localItem.transFromAccountId == backupItem.transFromAccountId &&
                                !localItem.transIsDeleted
                    }
                },
                getUpdateTime = { it.transUpdateTime },
                getName = { it.transName },
                getId = { it.transId },
                insert = { backupItem ->
                    val backupDate = LocalDate.parse(backupItem.transDate)
                    val existingDuplicate =
                        appDb.getTransactionDao().getAllTransactionsSync().find { localItem ->
                            val localDate = LocalDate.parse(localItem.transDate)
                            val daysDiff =
                                java.time.temporal.ChronoUnit.DAYS.between(backupDate, localDate)

                            localItem.transId != backupItem.transId &&
                                    Math.abs(daysDiff) <= 2 &&
                                    localItem.transAmount == backupItem.transAmount &&
                                    localItem.transToAccountId == backupItem.transToAccountId &&
                                    localItem.transFromAccountId == backupItem.transFromAccountId &&
                                    !localItem.transIsDeleted
                        }

                    if (existingDuplicate != null) {
                        val choice = showConflictDialog(
                            "Transactions",
                            backupItem.transName,
                            existingDuplicate.transId,
                            existingDuplicate.transUpdateTime,
                            backupItem.transId,
                            backupItem.transUpdateTime,
                            R.string.duplicate_transaction_message
                        )
                        if (choice == ConflictChoice.KEEP_DRIVE) {
                            appDb.getTransactionDao().deleteTransaction(
                                existingDuplicate.transId, df.getCurrentTimeAsString()
                            )
                            applyTransactionEffect(appDb, backupItem, 1.0)
                            appDb.getTransactionDao().insertTransaction(backupItem)
                        }
                    } else {
                        applyTransactionEffect(appDb, backupItem, 1.0)
                        appDb.getTransactionDao().insertTransaction(backupItem)
                    }
                },
                update = { backupItem ->
                    val localItem = appDb.getTransactionDao().getTransaction(backupItem.transId)
                    if (localItem != null) {
                        applyTransactionEffect(appDb, localItem, -1.0)
                    }
                    applyTransactionEffect(appDb, backupItem, 1.0)
                    appDb.getTransactionDao().updateTransaction(backupItem)
                }
            )
            if (trans.first > 0 || trans.second > 0) report.append("- Transactions: ${trans.first} added, ${trans.second} updated\n")
            totalCount += trans.first + trans.second

            val acc = syncTable(
                tableName = "Accounts",
                mapCursorToItem = { cursor ->
                    Account(
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
                getItemKey = { it.accountId.toString() },
                getExistingById = { appDb.getAccountDao().findAccount(it.accountId).firstOrNull() },
                getExistingByName = { appDb.getAccountDao().findAccountByName(it.accountName) },
                getUpdateTime = { it.accUpdateTime },
                getName = { it.accountName },
                getId = { it.accountId },
                insert = { appDb.getAccountDao().insertAccount(it) },
                update = { backupAccount ->
                    val localAccount =
                        appDb.getAccountDao().getAccountSync(backupAccount.accountId)
                    if (localAccount != null && backupAccount.accUpdateTime > localAccount.accUpdateTime) {
                        val localTransactions =
                            appDb.getTransactionDao().getAllTransactionsSync()
                        val unaccounted = localTransactions.filter {
                            it.transId !in backupTransIds && !it.transIsDeleted
                        }

                        var reconciledBalance = backupAccount.accountBalance
                        var reconciledOwing = backupAccount.accountOwing

                        for (lt in unaccounted) {
                            if (lt.transToAccountId == backupAccount.accountId && !lt.transToAccountPending) {
                                reconciledBalance += lt.transAmount
                            }
                            if (lt.transFromAccountId == backupAccount.accountId && !lt.transFromAccountPending) {
                                reconciledBalance -= lt.transAmount
                            }
                            val type = appDb.getAccountTypesDao()
                                .getAccountTypeSync(backupAccount.accountTypeId)
                            if (type?.tallyOwing == true) {
                                if (lt.transToAccountId == backupAccount.accountId) reconciledOwing -= lt.transAmount
                                if (lt.transFromAccountId == backupAccount.accountId) reconciledOwing += lt.transAmount
                            }
                        }

                        appDb.getAccountDao().updateAccount(
                            backupAccount.copy(
                                accountBalance = reconciledBalance,
                                accountOwing = reconciledOwing,
                                accUpdateTime = df.getCurrentTimeAsString()
                            )
                        )
                    }
                },
                rename = { id, name, time ->
                    appDb.getAccountDao().renameAccount(id, name, time)
                },
                copyWithName = { item, name -> item.copy(accountName = name) }
            )
            if (acc.first > 0 || acc.second > 0) report.append("- Accounts: ${acc.first} added, ${acc.second} updated\n")
            totalCount += acc.first + acc.second

            val br = syncTable(
                tableName = TABLE_BUDGET_RULES,
                mapCursorToItem = { cursor ->
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
                getItemKey = { it.ruleId.toString() },
                getExistingById = { appDb.getBudgetRuleDao().getBudgetRule(it.ruleId) },
                getExistingByName = {
                    appDb.getBudgetRuleDao().findBudgetRuleByName(it.budgetRuleName)
                },
                getUpdateTime = { it.budUpdateTime },
                getName = { it.budgetRuleName },
                getId = { it.ruleId },
                insert = { appDb.getBudgetRuleDao().insertBudgetRule(it) },
                update = { appDb.getBudgetRuleDao().updateBudgetRule(it) },
                rename = { id, name, time ->
                    appDb.getBudgetRuleDao().renameBudgetRule(id, name, time)
                },
                copyWithName = { item, name -> item.copy(budgetRuleName = name) }
            )
            if (br.first > 0 || br.second > 0) report.append("- Budget Rules: ${br.first} added, ${br.second} updated\n")
            totalCount += br.first + br.second

            val bi = syncTable(
                tableName = TABLE_BUDGET_ITEMS,
                mapCursorToItem = { cursor ->
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
                getExistingLocalMap = {
                    appDb.getBudgetItemDao().getAllBudgetItemsSync()
                        .associateBy { "${it.biRuleId}_${it.biProjectedDate}" }
                },
                getItemKey = { "${it.biRuleId}_${it.biProjectedDate}" },
                getExistingById = {
                    appDb.getBudgetItemDao().getBudgetItem(it.biRuleId, it.biProjectedDate)
                },
                getUpdateTime = { it.biUpdateTime },
                insert = { appDb.getBudgetItemDao().insertBudgetItem(it) },
                update = { appDb.getBudgetItemDao().updateBudgetItem(it) }
            )
            if (bi.first > 0 || bi.second > 0) report.append("- Budget Items: ${bi.first} added, ${bi.second} updated\n")
            totalCount += bi.first + bi.second

            val sh = syncTable(
                tableName = TABLE_SYNC_HISTORY,
                mapCursorToItem = { cursor ->
                    SyncHistory(
                        syncId = cursor.getLong(cursor.getColumnIndexOrThrow("syncId")),
                        syncTime = cursor.getString(cursor.getColumnIndexOrThrow("syncTime")),
                        syncSourceName = cursor.getString(cursor.getColumnIndexOrThrow("syncSourceName")),
                        syncDeviceId = cursor.getLong(cursor.getColumnIndexOrThrow("syncDeviceId")),
                        syncStatus = cursor.getString(cursor.getColumnIndexOrThrow("syncStatus")),
                        syncRecordsProcessed = cursor.getString(cursor.getColumnIndexOrThrow("syncRecordsProcessed"))
                    )
                },
                getItemKey = { it.syncId.toString() },
                getExistingById = { appDb.getSyncHistoryDao().getSyncHistory(it.syncId) },
                getUpdateTime = { it.syncTime },
                insert = {
                    if (it.syncDeviceId != deviceId) {
                        appDb.getSyncHistoryDao().insertSyncHistory(it)
                    }
                },
                update = {
                    if (it.syncDeviceId != deviceId) {
                        appDb.getSyncHistoryDao().updateSyncHistory(it)
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

    private suspend fun showConflictDialog(
        tableName: String,
        name: String,
        localId: Long,
        localTime: String,
        driveId: Long,
        driveTime: String,
        messageResId: Int? = null
    ): ConflictChoice {
        val deferred = CompletableDeferred<ConflictChoice>()
        conflictDeferred = deferred
        showConflictDialog =
            ConflictInfo(tableName, name, localId, localTime, driveId, driveTime, messageResId)
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

    private suspend fun applyTransactionEffectToAccount(
        db: BillsDatabase,
        accountId: Long,
        amount: Double,
        isCredit: Boolean,
        multiplier: Double
    ) {
        val account = db.getAccountDao().getAccountSync(accountId) ?: return
        val type = db.getAccountTypesDao().getAccountTypeSync(account.accountTypeId) ?: return

        var balance = account.accountBalance
        var owing = account.accountOwing

        if (type.keepTotals) {
            balance += (if (isCredit) amount else -amount) * multiplier
        }
        if (type.tallyOwing) {
            owing += (if (isCredit) -amount else amount) * multiplier
        }

        db.getTransactionDao().updateAccountBalanceAndOwing(
            balance, owing, accountId, df.getCurrentTimeAsString()
        )
    }

    private suspend fun applyTransactionEffect(
        db: BillsDatabase,
        transaction: Transactions,
        multiplier: Double
    ) {
        if (transaction.transIsDeleted) return

        if (!transaction.transToAccountPending && transaction.transToAccountId != 0L) {
            applyTransactionEffectToAccount(
                db, transaction.transToAccountId, transaction.transAmount, true, multiplier
            )
        }
        if (!transaction.transFromAccountPending && transaction.transFromAccountId != 0L) {
            applyTransactionEffectToAccount(
                db, transaction.transFromAccountId, transaction.transAmount, false, multiplier
            )
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