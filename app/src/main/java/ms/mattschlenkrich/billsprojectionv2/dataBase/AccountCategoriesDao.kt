package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import ms.mattschlenkrich.billsprojectionv2.model.AccountCategory

@Dao
interface AccountCategoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountCategory(accountCategory: AccountCategory)

    @Update
    suspend fun updateAccountCategory(accountCategory: AccountCategory)

    @Query(
        "UPDATE accountCategory " +
                "SET isDeleted = 1, " +
                "updateTime = :updateTime " +
                "WHERE accountCategoryId = :accountCategoryId"
    )
    suspend fun deleteAccountCategory(accountCategoryId: Long, updateTime: String)


    @Query(
        "SELECT * FROM accountCategory " +
                "WHERE accountCategoryId = :accountCategoryId"
    )
    fun findAccountCategory(accountCategoryId: Long): LiveData<List<AccountCategory>>

    @Query(
        "SELECT * FROM accountCategory " +
                "ORDER BY accountCategory " +
                "COLLATE NOCASE ASC"
    )
    fun getAccountCategories(): LiveData<List<AccountCategory>>

    @Query(
        "SELECT * FROM accountCategory " +
                "WHERE accountCategory LIKE :category " +
                "ORDER BY accountCategory " +
                "COLLATE NOCASE ASC"
    )
    fun searchAccountCategories(category: String): LiveData<List<AccountCategory>>
}