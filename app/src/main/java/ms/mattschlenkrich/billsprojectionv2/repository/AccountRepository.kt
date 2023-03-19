package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountCategory
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType

class AccountRepository(private val db: BillsDatabase) {

    //Account queries connected to @AccountDao
    suspend fun insertAccount(account: Account) =
        db.getAccountDao().insertAccount(account)

    suspend fun updateAccount(account: Account) =
        db.getAccountDao().updateAccount(account)

    suspend fun deleteAccount(accountId: Long, updateTime: String) =
        db.getAccountDao().deleteAccount(accountId, updateTime)

    fun getAllAccounts() =
        db.getAccountDao().getAllAccounts()

    fun findAccount(accountId: Long) =
        db.getAccountDao().findAccount(accountId)

    fun searchAccounts(query: String?) =
        db.getAccountDao().searchAccounts(query)


    //AccountType queries connected with AccountCategoriesDao
    suspend fun insertAccountType(accountType: AccountType) =
        db.getAccountTypesDao().insertAccountType(accountType)

    suspend fun updateAccountType(accountType: AccountType) =
        db.getAccountTypesDao().updateAccountType(accountType)

    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String) =
        db.getAccountTypesDao().deleteAccountType(accountTypeId, updateTime)

    fun findAccountType(accountTypeId: Long) =
        db.getAccountTypesDao().findAccountType(accountTypeId)

    fun getAccountTypes() =
        db.getAccountTypesDao().getAccountTypes()

    fun searchAccountType(query: String) =
        db.getAccountTypesDao().searchAccountType(query)


    //AccountCategories queries connected with AccountCategoriesDao
    suspend fun insertAccountCategory(accountCategory: AccountCategory) =
        db.getAccountCategoriesDao().insertAccountCategory(accountCategory)

    suspend fun updateAccountCategory(accountCategory: AccountCategory) =
        db.getAccountCategoriesDao().updateAccountCategory(accountCategory)

    suspend fun deleteAccountCategory(accountCategoryId: Long, updateTime: String) =
        db.getAccountCategoriesDao().deleteAccountCategory(accountCategoryId, updateTime)

    fun findAccountCategory(accountCategoryId: Long) =
        db.getAccountCategoriesDao().findAccountCategory(accountCategoryId)

    fun getAccountCategories() =
        db.getAccountCategoriesDao().getAccountCategories()

    fun searchAccountCategories(query: String) =
        db.getAccountCategoriesDao().searchAccountCategories(query)
}