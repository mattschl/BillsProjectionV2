package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.repository.AccountRepository

class AccountViewModel(
    app: Application,
    private val accountRepository: AccountRepository
) : AndroidViewModel(app) {

    fun addAccount(account: Account) =
        viewModelScope.launch {
            accountRepository.insertAccount(account)
        }

    fun updateAccount(account: Account) =
        viewModelScope.launch {
            accountRepository.updateAccount(account)
        }

    fun deleteAccount(accountId: Long, updateTime: String) =
        viewModelScope.launch {
            accountRepository.deleteAccount(accountId, updateTime)
        }

    fun getAccountNameList() =
        accountRepository.getAccountNameList()

    fun getAccountsWithType() =
        accountRepository.getAccountsWithType()

    fun addAccountType(accountType: AccountType) =
        viewModelScope.launch {
            accountRepository.insertAccountType(accountType)
        }

    fun updateAccountType(accountType: AccountType) =
        viewModelScope.launch {
            accountRepository.updateAccountType(accountType)
        }

    fun deleteAccountType(accountTypeId: Long, updateTime: String) =
        viewModelScope.launch {
            accountRepository.deleteAccountType(accountTypeId, updateTime)
        }

    fun getActiveAccountTypes() =
        accountRepository.getActiveAccountTypes()

    fun searchAccountTypes(query: String) =
        accountRepository.searchAccountType(query)

    fun searchAccountsWithType(query: String) =
        accountRepository.searchAccountsWithType(query)

    fun getAccountWithType(accountId: Long) =
        accountRepository.getAccountWithType(accountId)

    fun getAccountWithType(accountName: String) =
        accountRepository.getAccountWithType(accountName)

    fun getAccountDetailed(accountId: Long) =
        accountRepository.getAccountDetailed(accountId)

    fun getAccountDetailed(accountName: String) =
        accountRepository.getAccountDetailed(accountName)

    fun getAccount(accountId: Long) =
        accountRepository.getAccount(accountId)

    fun getAccountTypeNames() =
        accountRepository.getAccountTypeNames()
}
