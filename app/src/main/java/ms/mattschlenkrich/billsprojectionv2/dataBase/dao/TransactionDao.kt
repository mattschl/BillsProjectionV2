package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_OWING
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_DATE
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_NAME
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_NOTE
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionFull
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transactions)

    @Update
    suspend fun updateTransaction(transaction: Transactions)

    @Query("SELECT * FROM $TABLE_TRANSACTION WHERE $TRANS_IS_DELETED = 0")
    fun getActiveTransactionsSync(): List<Transactions>

    @Query("SELECT * FROM $TABLE_TRANSACTION")
    fun getAllTransactionsSync(): List<Transactions>

    @Query(
        "SELECT * FROM $TABLE_TRANSACTION " +
                "WHERE $TRANSACTION_ID = :transId"
    )
    suspend fun getTransaction(transId: Long): Transactions?

    @Query(
        "UPDATE $TABLE_TRANSACTION " +
                "SET transIsDeleted = 1, " +
                "transUpdateTime = :updateTime " +
                "WHERE $TRANSACTION_ID = :transId"
    )
    suspend fun deleteTransaction(transId: Long, updateTime: String)

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM Transactions WHERE transId = :transId;"
    )
    suspend fun getTransactionDetailed(transId: Long):
            TransactionDetailed

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*, " +
                "budgetRule.*," +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN AccountAndType as toAccount on " +
                "toAccount.accountId = " +
                ":toAccountID " +
                "LEFT JOIN AccountAndType as fromAccount on " +
                "fromAccount.accountId = " +
                ":fromAccountID " +
                "WHERE $TABLE_TRANSACTION.$TRANSACTION_ID = :transId;"
    )
    suspend fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ): TransactionFull

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM Transactions " +
                "WHERE transIsDeleted = 0 " +
                "AND (" +
                "((:asset = 'All Items' OR transToAccountId = " +
                "(SELECT accountId FROM Accounts " +
                "WHERE accountName = :asset)) " +
                "AND transToAccountPending = 1)" +
                "OR ((:asset = 'All Items' OR transFromAccountId = " +
                "(SELECT accountId FROM Accounts " +
                "WHERE accountName = :asset)) " +
                "AND transFromAccountPending = 1)" +
                ") " +
                "ORDER BY transDate ASC, transUpdateTime DESC"
    )
    fun getPendingTransactionsDetailed(asset: String):
            LiveData<List<TransactionDetailed>>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getSumTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ): Double?

    @Query(
        "SELECT COUNT($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getCountTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ): Int

    @Query(
        "UPDATE $TABLE_ACCOUNTS " +
                "SET $ACCOUNT_BALANCE = :newBalance, " +
                "$ACCOUNT_UPDATE_TIME = :updateTime " +
                "WHERE $ACCOUNT_ID = :accountId;"
    )
    suspend fun updateAccountBalance(
        newBalance: Double,
        accountId: Long,
        updateTime: String
    )

    @Query(
        "UPDATE $TABLE_ACCOUNTS " +
                "SET $ACCOUNT_OWING = :newOwing, " +
                "$ACCOUNT_UPDATE_TIME = :updateTime " +
                "WHERE $ACCOUNT_ID = :accountId;"
    )
    suspend fun updateAccountOwing(
        newOwing: Double,
        accountId: Long,
        updateTime: String
    )

    @Query(
        "UPDATE $TABLE_ACCOUNTS " +
                "SET $ACCOUNT_BALANCE = :newBalance, " +
                "$ACCOUNT_OWING = :newOwing, " +
                "$ACCOUNT_UPDATE_TIME = :updateTime " +
                "WHERE $ACCOUNT_ID = :accountId;"
    )
    suspend fun updateAccountBalanceAndOwing(
        newBalance: Double,
        newOwing: Double,
        accountId: Long,
        updateTime: String
    )

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT trans.*, " +
                "budgetRule.*, " +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION AS trans " +
                "LEFT JOIN $TABLE_BUDGET_RULES AS budgetRule ON " +
                "trans.$TRANS_BUDGET_RULE_ID = budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS AS toAccount ON " +
                "trans.$TRANSACTION_TO_ACCOUNT_ID = toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS AS fromAccount ON " +
                "trans.$TRANSACTION_FROM_ACCOUNT_ID = fromAccount.$ACCOUNT_ID " +
                "WHERE trans.$TRANS_IS_DELETED = 0 " +
                "AND (:budgetRuleId = -1 OR trans.$TRANS_BUDGET_RULE_ID = :budgetRuleId) " +
                "AND (:accountId = -1 OR (trans.$TRANSACTION_TO_ACCOUNT_ID = :accountId OR trans.$TRANSACTION_FROM_ACCOUNT_ID = :accountId)) " +
                "AND (:query = '' OR (trans.$TRANSACTION_NAME LIKE :query OR trans.$TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR trans.$TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR trans.$TRANSACTION_DATE <= :endDate) " +
                "ORDER BY trans.$TRANSACTION_DATE DESC, trans.$TRANS_UPDATE_TIME DESC"
    )
    fun getTransactionsFiltered(
        budgetRuleId: Long,
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<List<TransactionDetailed>>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_IS_DELETED = 0 " +
                "AND (:budgetRuleId = -1 OR $TRANS_BUDGET_RULE_ID = :budgetRuleId) " +
                "AND (:accountId = -1 OR ($TRANSACTION_TO_ACCOUNT_ID = :accountId OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId)) " +
                "AND (:query = '' OR ($TRANSACTION_NAME LIKE :query OR $TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR $TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR $TRANSACTION_DATE <= :endDate)"
    )
    fun getSumFiltered(
        budgetRuleId: Long,
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_IS_DELETED = 0 " +
                "AND $TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "AND (:query = '' OR ($TRANSACTION_NAME LIKE :query OR $TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR $TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR $TRANSACTION_DATE <= :endDate)"
    )
    fun getSumToAccountFiltered(
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_IS_DELETED = 0 " +
                "AND $TRANSACTION_FROM_ACCOUNT_ID = :accountId " +
                "AND (:query = '' OR ($TRANSACTION_NAME LIKE :query OR $TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR $TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR $TRANSACTION_DATE <= :endDate)"
    )
    fun getSumFromAccountFiltered(
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_IS_DELETED = 0 " +
                "AND (:budgetRuleId = -1 OR $TRANS_BUDGET_RULE_ID = :budgetRuleId) " +
                "AND (:accountId = -1 OR ($TRANSACTION_TO_ACCOUNT_ID = :accountId OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId)) " +
                "AND (:query = '' OR ($TRANSACTION_NAME LIKE :query OR $TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR $TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR $TRANSACTION_DATE <= :endDate)"
    )
    fun getMaxFiltered(
        budgetRuleId: Long,
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_IS_DELETED = 0 " +
                "AND (:budgetRuleId = -1 OR $TRANS_BUDGET_RULE_ID = :budgetRuleId) " +
                "AND (:accountId = -1 OR ($TRANSACTION_TO_ACCOUNT_ID = :accountId OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId)) " +
                "AND (:query = '' OR ($TRANSACTION_NAME LIKE :query OR $TRANSACTION_NOTE LIKE :query)) " +
                "AND (:startDate = '' OR $TRANSACTION_DATE >= :startDate) " +
                "AND (:endDate = '' OR $TRANSACTION_DATE <= :endDate)"
    )
    fun getMinFiltered(
        budgetRuleId: Long,
        accountId: Long,
        query: String,
        startDate: String,
        endDate: String
    ): LiveData<Double>
}