package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType

@Dao
interface AccountTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountType(accountType: AccountType)

    @Update
    suspend fun updateAccountType(accountType: AccountType)

    @Query(
        "UPDATE accountTypes " +
                "SET isDeleted = 1, " +
                "updateTime = :updateTime " +
                "WHERE accountTypeId = :accountTypeId"
    )
    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String)

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE accountTypeId = :accountTypeId"
    )
    fun findAccountType(accountTypeId: Long): LiveData<List<AccountType>>

    @Query(
        "SELECT * FROM accountTypes " +
                "ORDER BY accountType " +
                " COLLATE NOCASE ASC"
    )

    fun getAccountTypes(): LiveData<List<AccountType>>

    @Query(
        "SELECT * FROM accountTypes " +
                "WHERE accountType LIKE :query " +
                "ORDER BY accountType " +
                " COLLATE NOCASE ASC"
    )
    fun searchAccountType(query: String?): LiveData<List<AccountType>>
}