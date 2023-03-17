package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType
import java.text.SimpleDateFormat

@Dao
interface AccountTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountType(accountType: AccountType)

    @Update
    suspend fun updateAccountType(accountType: AccountType)

    @Query("UPDATE accountTypes " +
            "SET accountDeleted = 1, " +
            "updateTime = :updateTime"
            )
    suspend fun deleteAccountType(accountType: AccountType, updateTime: String)

    @Query("SELECT * FROM accountTypes " +
            "ORDER BY accountName " +
            "DESC COLLATE NOCASE")
    fun getAccountTypes(): LiveData<List<AccountType>>

    @Query("SELECT * FROM accountTypes" +
            "WHERE accountName LIKE :type")
    fun searchAccountType(type: String): LiveData<List<AccountType>>
}