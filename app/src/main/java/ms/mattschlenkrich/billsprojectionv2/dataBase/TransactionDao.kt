package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)


}