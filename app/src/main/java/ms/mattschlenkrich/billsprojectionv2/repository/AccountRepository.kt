package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType

class AccountRepository(private val db: BillsDatabase) {

    suspend fun insertAccount(account: Account) =
        db.getAccountDao().insertAccount(account)

    suspend fun updateAccount(account: Account) =
        db.getAccountDao().updateAccount(account)

    suspend fun deleteAccount(accountId: Long, updateTime: String) =
        db.getAccountDao().deleteAccount(accountId, updateTime)

    fun getAccountNameList() =
        db.getAccountDao().getAccountNameList()

    suspend fun insertAccountType(accountType: AccountType) =
        db.getAccountTypesDao().insertAccountType(accountType)

    suspend fun updateAccountType(accountType: AccountType) =
        db.getAccountTypesDao().updateAccountType(accountType)

    suspend fun deleteAccountType(accountTypeId: Long, updateTime: String) =
        db.getAccountTypesDao().deleteAccountType(accountTypeId, updateTime)

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

    fun getAccountWithType(accountName: String) =
        db.getAccountDao().getAccountWithType(accountName)

    fun getAccountAndType(accountId: Long) =
        db.getAccountDao().getAccountAndType(accountId)

    fun getAccountDetailed(accountId: Long) =
        db.getAccountDao().getAccountDetailed(accountId)

    fun getAccountDetailed(accountName: String) =
        db.getAccountDao().getAccountDetailed(accountName)

    fun getAccount(accountId: Long) =
        db.getAccountDao().getAccount(accountId)

    fun getAccountTypeNames() =
        db.getAccountTypesDao().getAccountTypeNames()
}