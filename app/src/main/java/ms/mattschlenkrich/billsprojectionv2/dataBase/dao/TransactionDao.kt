package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_OWING
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_DATE
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_PENDING
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_NAME
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_NOTE
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_PENDING
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionFull
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

@Dao
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: Transactions)

    @Update
    suspend fun updateTransaction(transaction: Transactions)

    @Query(
        "UPDATE $TABLE_TRANSACTION " +
                "SET transIsDeleted = 1, " +
                "transUpdateTime = :updateTime " +
                "WHERE $TRANSACTION_ID = :transId"
    )
    suspend fun deleteTransaction(transId: Long, updateTime: String)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getActiveTransactionsDetailed():
            LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*, " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "AND $TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getActiveTransactionsDetailed(budgetRuleId: Long):
            LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "AND $TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getActiveTransactionsDetailed(
        budgetRuleId: Long, startDate: String, endDate: String
    ): LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "AND ($TABLE_TRANSACTION.$TRANSACTION_NAME LIKE :query " +
                "OR $TABLE_TRANSACTION.$TRANSACTION_NOTE LIKE :query " +
                "OR toAccount.accountName LIKE :query " +
                "OR fromAccount.accountName LIKE :query) " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC;"
    )
    fun searchActiveTransactionsDetailed(query: String?):
            LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.transId = :transId;"
    )
    fun getTransactionDetailed(transId: Long):
            TransactionDetailed

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
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
    fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ): TransactionFull

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

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "AND (" +
                "($TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_PENDING = 1)" +
                "OR ($TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_PENDING = 1)" +
                ")" +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE ASC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getPendingTransactionsDetailed(asset: String):
            LiveData<List<TransactionDetailed>>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMaxTransactionByBudgetRule(budgetRuleId: Long):
            LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMaxTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMaxTransactionByAccount(accountId: Long):
            LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMaxTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ):
            LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMinTransactionByAccount(accountId: Long):
            LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMinTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ):
            LiveData<Double>


    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMinTransactionByBudgetRule(budgetRuleId: Long):
            LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANS_UPDATE_TIME <= :endDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getMinTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getSumTransactionByBudgetRule(budgetRuleId: Long):
            LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANS_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getSumTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ): LiveData<Double>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE ($TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getActiveTransactionByAccount(accountId: Long):
            LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$TRANS_BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE ($TABLE_TRANSACTION.$TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "OR $TABLE_TRANSACTION.$TRANSACTION_FROM_ACCOUNT_ID = :accountId) " +
                "AND $TABLE_TRANSACTION.$TRANS_IS_DELETED = 0 " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun getActiveTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ): LiveData<List<TransactionDetailed>>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getSumTransactionToAccount(accountId: Long):
            LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANSACTION_TO_ACCOUNT_ID = :accountId " +
                "AND $TRANS_IS_DELETED = 0 " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate;"
    )
    fun getSumTransactionToAccount(
        accountId: Long, startDate: String, endDate: String
    ):
            LiveData<Double>


    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANSACTION_FROM_ACCOUNT_ID = :accountId " +
                "AND $TRANS_IS_DELETED = 0"
    )
    fun getSumTransactionFromAccount(accountId: Long):
            LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE $TRANSACTION_FROM_ACCOUNT_ID = :accountId " +
                "AND $TRANS_IS_DELETED = 0 " +
                "AND $TRANSACTION_DATE >= :startDate " +
                "AND $TRANSACTION_DATE <= :endDate;"
    )
    fun getSumTransactionFromAccount(
        accountId: Long, startDate: String, endDate: String
    ): LiveData<Double>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*, " +
                "budgetRule.*, " +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES AS budgetRule ON " +
                "$TABLE_TRANSACTION.transRuleId = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS AS toAccount ON " +
                "$TABLE_TRANSACTION.transToAccountId = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS AS fromAccount ON " +
                "$TABLE_TRANSACTION.transFromAccountId = " +
                "fromAccount.accountId " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.transUpdateTime DESC"
    )
    fun getActiveTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ): LiveData<List<TransactionDetailed>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT trans.*, " +
                "budgetRule.*, " +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION AS trans " +
                "LEFT JOIN $TABLE_BUDGET_RULES AS budgetRule ON " +
                "trans.transRuleId = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS AS toAccount ON " +
                "trans.transToAccountId = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS AS fromAccount ON " +
                "trans.transFromAccountId = " +
                "fromAccount.accountId " +
                "WHERE (trans.transName LIKE :query OR " +
                "trans.transNote LIKE :query ) AND " +
                "trans.transIsDeleted =  0 " +
                "ORDER BY trans.transDate DESC, " +
                "trans.transUpdateTime DESC"
    )
    fun getActiveTransactionBySearch(query: String?):
            LiveData<List<TransactionDetailed>>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate "
    )
    fun getSumTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ):
            LiveData<Double>

    @Query(
        "SELECT SUM($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 "
    )
    fun getSumTransactionBySearch(query: String?):
            LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 "
    )
    fun getMaxTransactionBySearch(query: String?):
            LiveData<Double>

    @Query(
        "SELECT MAX($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate "
    )
    fun getMaxTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ):
            LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 "
    )
    fun getMinTransactionBySearch(query: String?):
            LiveData<Double>

    @Query(
        "SELECT MIN($TRANSACTION_AMOUNT) FROM $TABLE_TRANSACTION " +
                "WHERE ($TABLE_TRANSACTION.transName LIKE :query OR " +
                "$TABLE_TRANSACTION.transNote LIKE :query ) AND " +
                "$TABLE_TRANSACTION.transIsDeleted =  0 " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE >= :startDate " +
                "AND $TABLE_TRANSACTION.$TRANSACTION_DATE <= :endDate "
    )
    fun getMinTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ):
            LiveData<Double>
}