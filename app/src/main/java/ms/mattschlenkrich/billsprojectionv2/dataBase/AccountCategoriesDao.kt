package ms.mattschlenkrich.billsprojectionv2

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountCategory
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType

@Dao
interface AccountCategoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountCategory(accountCategory: AccountCategory)

    @Update
    suspend fun updateAccountCategory(accountCategory: AccountCategory)

    @Query(
        "UPDATE accountCategory " +
                "SET categoryDeleted = 1, " +
                "updateTime = :updateTime"
    )
    suspend fun deleteAccountCategory(accountType: AccountType, updateTime: String)

    @Query(
        "SELECT * FROM accountCategory " +
                "ORDER BY accountCategory " +
                "ASC COLLATE NOCASE"
    )
    fun getAccountCategory(): LiveData<List<AccountType>>
}