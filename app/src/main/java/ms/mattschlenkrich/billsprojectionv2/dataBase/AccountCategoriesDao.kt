package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.model.AccountCategories

@Dao
interface AccountCategoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountCategory(accountCategory: AccountCategories)

    @Update
    suspend fun updateAccountCategory(accountCategory: AccountCategories)

    @Query(
        "UPDATE accountCategories " +
                "SET isDeleted = 1, " +
                "updateTime = :updateTime " +
                "WHERE accountCategoryId = :accountCategoryId"
    )
    suspend fun deleteAccountCategory(accountCategoryId: Long, updateTime: String)


    @Query(
        "SELECT * FROM accountCategories " +
                "WHERE accountCategoryId = :accountCategoryId"
    )
    fun findAccountCategory(accountCategoryId: Long): LiveData<List<AccountCategories>>

    @Query(
        "SELECT * FROM accountCategories " +
                "ORDER BY accountCategory " +
                "COLLATE NOCASE ASC"
    )
    fun getAccountCategories(): LiveData<List<AccountCategories>>

    @Query(
        "SELECT * FROM accountCategories " +
                "WHERE accountCategory LIKE :category " +
                "ORDER BY accountCategory " +
                "COLLATE NOCASE ASC"
    )
    fun searchAccountCategories(category: String): LiveData<List<AccountCategories>>
}