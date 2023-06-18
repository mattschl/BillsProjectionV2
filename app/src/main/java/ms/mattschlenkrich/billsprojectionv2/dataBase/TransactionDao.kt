package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.TRANSACTION_DATE
import ms.mattschlenkrich.billsprojectionv2.UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: Transactions)

    @Update
    suspend fun updateTransaction(transaction: Transactions)

    @Transaction
    @Query(
        "SELECT $TABLE_TRANSACTION.*," +
                "budgetRule.*,  " +
                "toAccount.*," +
                "fromAccount.* " +
                "FROM $TABLE_TRANSACTION " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule on " +
                "$TABLE_TRANSACTION.$BUDGET_RULE_ID = " +
                "budgetRule.$RULE_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_TRANSACTION.$TO_ACCOUNT_ID =" +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_TRANSACTION.$IS_DELETED = 0 " +
                "ORDER BY $TABLE_TRANSACTION.$TRANSACTION_DATE DESC, " +
                "$TABLE_TRANSACTION.$UPDATE_TIME DESC"
    )
    fun getActiveTransactionsDetailed():
            LiveData<List<TransactionDetailed>>

    
}