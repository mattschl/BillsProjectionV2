package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType

class AccountRepository(private val db: BillsDatabase) {

    //Account queries connected to @AccountDao
    suspend fun insertAccount(account: Account) =
        db.getAccountDao().insertAccount(account)

    suspend fun updateAccount(account: Account) =
        db.getAccountDao().updateAccount(account)

    suspend fun deleteAccount(accountId: Long, updateTime: String) =
        db.getAccountDao().deleteAccount(accountId, updateTime)

    fun getActiveAccounts() =
        db.getAccountDao().getActiveAccounts()

    fun getActiveAccountsDetailed() =
        db.getAccountDao().getActiveAccountsDetailed()

    fun findAccount(accountId: Long) =
        db.getAccountDao().findAccount(accountId)

    fun findAccountByName(accountName: String) =
        db.getAccountDao().findAccountByName(accountName)

    fun searchAccounts(query: String?) =
        db.getAccountDao().searchAccounts(query)

    fun getAccountNameList() =
        db.getAccountDao().getAccountNameList()

    //AccountType queries connected with AccountCategoriesDao
    suspend fun insertAccountType(accountType: AccountType) =
        db.getAccountTypesDao().insertAccountType(accountType)

    suspend fun updateAccountType(accountType: AccountType) =
        db.getAccountTypesDao().updateAccountType(accountType)

    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String) =
        db.getAccountTypesDao().deleteAccountType(accountTypeId, updateTime)

    fun findAccountType(accountTypeId: Long) =
        db.getAccountTypesDao().findAccountType(accountTypeId)

    fun findAccountTypeByName(accountTypeName: String) =
        db.getAccountTypesDao().findAccountTypeByName(accountTypeName)

    fun getActiveAccountTypes() =
        db.getAccountTypesDao().getActiveAccountTypes()

    fun searchAccountType(query: String) =
        db.getAccountTypesDao().searchAccountType(query)

    fun searchAccountsWithType(query: String?) =
        db.getAccountDao().searchAccountsWithType(query)

    fun getAccountsWithType() =
        db.getAccountDao().getAccountsWithType()

    fun getAccountWithType(accountId: Long) =
        db.getAccountDao().getAccountWithType(accountId)
}