package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_TYPE
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType

@Dao
interface AccountTypeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountType(accountType: AccountType)

    @Update
    suspend fun updateAccountType(accountType: AccountType)

    @Query(
        "UPDATE $TABLE_ACCOUNT_TYPES " +
                "SET $ACCT_IS_DELETED = 1, " +
                "$ACCT_UPDATE_TIME = :updateTime " +
                "WHERE typeId = :accountTypeId"
    )
    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_ACCOUNT_TYPES " +
                "WHERE $TYPE_ID = :accountTypeId"
    )
    fun findAccountType(accountTypeId: Long): List<AccountType>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNT_TYPES " +
                "WHERE $ACCOUNT_TYPE = :accountTypeName"
    )
    fun findAccountTypeByName(accountTypeName: String): List<AccountType>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNT_TYPES " +
                "WHERE $ACCT_IS_DELETED <> 1 " +
                "ORDER BY $ACCOUNT_TYPE " +
                " COLLATE NOCASE ASC"
    )
    fun getActiveAccountTypes(): LiveData<List<AccountType>>

    @Query(
        "SELECT * FROM $TABLE_ACCOUNT_TYPES " +
                "WHERE $ACCOUNT_TYPE LIKE :query " +
                "ORDER BY $ACCOUNT_TYPE " +
                " COLLATE NOCASE ASC"
    )
    fun searchAccountType(query: String): LiveData<List<AccountType>>

    @Query(
        "SELECT $ACCOUNT_TYPE FROM $TABLE_ACCOUNT_TYPES " +
                "WHERE $ACCT_IS_DELETED = 0 " +
                "ORDER BY $ACCOUNT_TYPE "
    )
    fun getAccountTypeNames(): List<String>
}