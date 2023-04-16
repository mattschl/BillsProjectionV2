package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import java.util.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query(
        "UPDATE $TABLE_ACCOUNTS " +
                "SET $IS_DELETED = 1, " +
                "$UPDATE_TIME = :updateTime " +
                "WHERE $ACCOUNT_ID = :accountId"
    )
    suspend fun deleteAccount(accountId: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_ACCOUNTS " +
                "WHERE $IS_DELETED = 0 " +
                "ORDER BY $ACCOUNT_NAME " +
                "COLLATE NOCASE ASC "
    )
    fun getActiveAccounts(): LiveData<List<Account>>

    @Query(
        "SELECT $TABLE_ACCOUNTS.*, " +
                "$TABLE_ACCOUNT_TYPES.* " +
                "FROM $TABLE_ACCOUNTS " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES ON " +
                "$TABLE_ACCOUNT_TYPES.$TYPE_ID = $TABLE_ACCOUNTS.$ACCOUNT_TYPE_ID " +
                "WHERE $TABLE_ACCOUNTS.$IS_DELETED = 0 " +
                "ORDER BY $TABLE_ACCOUNTS.$ACCOUNT_NAME " +
                "COLLATE NOCASE ASC "
    )
    fun getActiveAccountsDetailed(): LiveData<List<AccountWithType>>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_ID = :accountId "
    )
    fun findAccount(accountId: Long): LiveData<List<Account>>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :accountName "
    )
    fun findAccountByName(accountName: String): List<Account>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME LIKE :query " +
                "ORDER BY $ACCOUNT_NAME "
    )
    fun searchAccounts(query: String?): LiveData<List<Account>>

    @Query(
        "SELECT $TABLE_ACCOUNTS.*, $TABLE_ACCOUNT_TYPES.* FROM $TABLE_ACCOUNTS " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES ON " +
                "$TABLE_ACCOUNT_TYPES.$TYPE_ID = $TABLE_ACCOUNTS.$ACCOUNT_TYPE_ID " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_NAME LIKE :query " +
                "ORDER BY $TABLE_ACCOUNTS.$ACCOUNT_NAME " +
                "COLLATE NOCASE ASC "
    )
    fun searchAccountsWithType(query: String?): LiveData<List<AccountWithType>>


    @Transaction
    @Query(
        "SELECT $TABLE_ACCOUNTS.*, $TABLE_ACCOUNT_TYPES.* " +
                "FROM $TABLE_ACCOUNTS " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES ON " +
                "$TABLE_ACCOUNT_TYPES.$TYPE_ID = " +
                "$TABLE_ACCOUNTS.$ACCOUNT_TYPE_ID " +
                "WHERE $TABLE_ACCOUNTS.$IS_DELETED = 0  " +
                "ORDER BY $TABLE_ACCOUNTS.$ACCOUNT_NAME " +
                "COLLATE NOCASE ASC "
    )
    fun getAccountWithType(): LiveData<List<AccountWithType>>
}