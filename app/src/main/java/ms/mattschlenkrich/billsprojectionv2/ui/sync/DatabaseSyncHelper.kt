package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import java.time.LocalDate

private const val TAG = "DatabaseSyncHelper"

class DatabaseSyncHelper(
    private val appDb: BillsDatabase,
    private val df: DateFunctions,
    private val deviceId: Long,
    private val onConflict: suspend (ConflictInfo) -> ConflictChoice
) {

    suspend fun <T> syncTable(
        backupDb: SQLiteDatabase,
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
                    if ((existingByName != null) && (getName != null) && (getId != null) && (rename != null)) {
                        val localName = getName(existingByName)
                        val localId = getId(existingByName)
                        val localTime = getUpdateTime(existingByName)

                        val choice = onConflict(
                            ConflictInfo(
                                tableName,
                                localName,
                                localId,
                                localTime,
                                getId(backupItem),
                                backupTime
                            )
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

    suspend fun syncAccountTypes(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
    }

    suspend fun syncAccounts(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
                    appDb.getAccountDao().updateAccount(backupAccount)
                } else if (localAccount == null) {
                    appDb.getAccountDao().insertAccount(backupAccount)
                }
            },
            rename = { id, name, time ->
                appDb.getAccountDao().renameAccount(id, name, time)
            },
            copyWithName = { item, name -> item.copy(accountName = name) }
        )
    }

    suspend fun syncBudgetRules(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
    }

    suspend fun syncTransactions(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
                )
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
                            kotlin.math.abs(daysDiff) <= 2 &&
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
                                kotlin.math.abs(daysDiff) <= 2 &&
                                localItem.transAmount == backupItem.transAmount &&
                                localItem.transToAccountId == backupItem.transToAccountId &&
                                localItem.transFromAccountId == backupItem.transFromAccountId &&
                                !localItem.transIsDeleted
                    }

                if (existingDuplicate != null) {
                    val choice = onConflict(
                        ConflictInfo(
                            "Transactions",
                            backupItem.transName,
                            existingDuplicate.transId,
                            existingDuplicate.transUpdateTime,
                            backupItem.transId,
                            backupItem.transUpdateTime,
                            R.string.duplicate_transaction_message
                        )
                    )
                    if (choice == ConflictChoice.KEEP_DRIVE) {
                        appDb.getTransactionDao().deleteTransaction(
                            existingDuplicate.transId, df.getCurrentTimeAsString()
                        )
                        appDb.getTransactionDao().insertTransaction(backupItem)
                    }
                } else {
                    appDb.getTransactionDao().insertTransaction(backupItem)
                }
            },
            update = { backupItem ->
                appDb.getTransactionDao().updateTransaction(backupItem)
            }
        )
    }

    suspend fun syncBudgetItems(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
    }

    suspend fun syncSyncHistory(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
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
    }
}