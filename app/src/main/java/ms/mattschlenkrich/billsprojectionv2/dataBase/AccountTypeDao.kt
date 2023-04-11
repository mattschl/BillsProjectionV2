package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.model.AccountType

@Dao
interface AccountTypeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountType(accountType: AccountType)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateAccountType(accountType: AccountType)

    @Query(
        "UPDATE accountTypes " +
                "SET isDeleted = 1, " +
                "updateTime = :updateTime " +
                "WHERE typeId = :accountTypeId"
    )
    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String)

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE typeId = :accountTypeId"
    )
    fun findAccountType(accountTypeId: Long): List<AccountType>

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE accountType = :accountTypeName"
    )
    fun findAccountTypeByName(accountTypeName: String): List<AccountType>

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE isDeleted <> 1 " +
                "ORDER BY accountType " +
                " COLLATE NOCASE ASC"
    )
    fun getActiveAccountTypes(): LiveData<List<AccountType>>

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE accountType LIKE :query " +
                "ORDER BY accountType " +
                " COLLATE NOCASE ASC"
    )
    fun searchAccountType(query: String): LiveData<List<AccountType>>
}