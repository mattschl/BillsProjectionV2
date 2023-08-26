package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
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
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: Transactions)

    @Query(
        "INSERT INTO $TABLE_TRANSACTION " +
                "($TRANSACTION_ID, " +
                "$TRANSACTION_DATE, " +
                "$TRANS_BUDGET_RULE_ID, " +
                "$TRANSACTION_TO_ACCOUNT_ID, " +
                "$TRANSACTION_FROM_ACCOUNT_ID, " +
                "$TRANSACTION_NAME, " +
                "$TRANSACTION_NOTE, " +
                "$TRANSACTION_AMOUNT) " +
                "VALUES (" +
                ":transId, " +
                ":transDate, " +
                ":BRuleId, " +
                ":toAccountId, " +
                ":fromAccountId," +
                ":transName, " +
                ":transNote, " +
                ":transAmount); "
    )
    suspend fun insertTransaction(
        transId: Long,
        transDate: String,
        BRuleId: Long,
        toAccountId: Long,
        fromAccountId: Long,
        transName: String,
        transNote: String,
        transAmount: Double,
    )

    @Update
    suspend fun updateTransaction(transaction: Transactions)

    @Query(
        "UPDATE $TABLE_TRANSACTION " +
                "SET transIsDeleted = 1, " +
                "transUpdateTime = :updateTime " +
                "WHERE $TRANSACTION_ID = :transId"
    )
    suspend fun deleteTransaction(transId: Long, updateTime: String)

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
                "OR $TABLE_TRANSACTION.$TRANSACTION_NOTE LIKE :query) " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$TRANS_UPDATE_TIME DESC"
    )
    fun searchActiveTransactionsDetailed(query: String?):
            LiveData<List<TransactionDetailed>>

}