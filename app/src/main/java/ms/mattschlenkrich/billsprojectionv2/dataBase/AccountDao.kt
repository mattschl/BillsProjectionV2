package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query(
        "UPDATE accounts " +
                "SET isDeleted = 1, " +
                "updateTime = :updateTime " +
                "WHERE accountId = :accountId"
    )
    suspend fun deleteAccount(accountId: Long, updateTime: String)

    @Query(
        "SELECT * FROM accounts " +
                "ORDER BY accountName " +
                "COLLATE NOCASE ASC "
    )
    fun getAccounts(): LiveData<List<Account>>

    @Query(
        "SELECT * FROM accounts " +
                "WHERE accountID = :accountId "
    )
    fun findAccount(accountId: Long): LiveData<List<Account>>

    @Query(
        "SELECT * FROM accounts " +
                "WHERE accountName LIKE :query " +
                "ORDER BY accountName " +
                "COLLATE NOCASE ASC "
    )
    fun searchAccounts(query: String): LiveData<List<Account>>
}