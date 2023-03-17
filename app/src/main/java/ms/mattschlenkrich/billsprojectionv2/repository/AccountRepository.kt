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
    fun getAccounts() =
        db.getAccountDao().getAccounts()
    fun searchAccounts(accountId: Long) =
        db.getAccountDao().searchAccounts(accountId)


    //AccountType queries connected with AccountCategoriesDao
    suspend fun insertAccountType(accountType: AccountType) =
        db.getAccountTypesDao().insertAccountType(accountType)
    suspend fun updateAccountYpe(accountType: AccountType) =
        db.getAccountTypesDao().updateAccountType(accountType)
    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String) =
        db.getAccountTypesDao().deleteAccountType(accountTypeId, updateTime)
    fun getAccountTypes() =
        db.getAccountTypesDao().getAccountTypes()
    fun searchAccountType(type: String) =
        db.getAccountTypesDao().searchAccountType(type)


    //AccountCategories queries connected with AccountCategoriesDao
    suspend fun insertAccountCategory(accountCategory: AccountCategory) =
        db.getAccountCategoriesDao().insertAccountCategory(accountCategory)
    suspend fun updateAccountCategory(accountCategory: AccountCategory) =
        db.getAccountCategoriesDao().updateAccountCategory(accountCategory)
    suspend fun deleteAccountCategory(accountCategory: AccountCategory, updateTime: String) =
        db.getAccountCategoriesDao().deleteAccountCategory(accountCategory, updateTime)
    fun getAccountCategories() =
        db.getAccountCategoriesDao().getAccountCategories()
    fun searchAccountCategories(category: String) =
        db.getAccountCategoriesDao().searchAccountCategories(category)
}