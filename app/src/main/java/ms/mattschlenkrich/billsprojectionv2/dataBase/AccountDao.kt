package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import java.util.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: Account)

    @Update(onConflict = OnConflictStrategy.IGNORE)
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
                "WHERE isDeleted = 0 " +
                "ORDER BY accountName " +
                "COLLATE NOCASE ASC "
    )
    fun getActiveAccounts(): LiveData<List<Account>>

    @Query(
        "SELECT accounts.*, accountTypes.* FROM accounts " +
                "LEFT JOIN accountTypes ON " +
                "accountTypes.accountTypeId = accounts.accountTypeId " +
                "WHERE accounts.isDeleted = 0 " +
                "ORDER BY accounts.accountName " +
                "COLLATE NOCASE ASC "
    )
    fun getActiveAccountsDetailed(): LiveData<List<Account>>

    @Query(
        "SELECT * FROM accounts " +
                "WHERE accountID = :accountId "
    )
    fun findAccount(accountId: Long): LiveData<List<Account>>

    @Query(
        "SELECT * FROM accounts " +
                "WHERE accountName = :accountName "
    )
    fun findAccountByName(accountName: String): List<Account>

    @Query(
        "SELECT * FROM accounts " +
                "WHERE accountName LIKE :query " +
                "ORDER BY accountName "
    )
    fun searchAccounts(query: String?): LiveData<List<Account>>

    @Transaction
    @Query("SELECT * FROM accounts")
    fun getAccountWithType(): LiveData<List<AccountWithType>>
}