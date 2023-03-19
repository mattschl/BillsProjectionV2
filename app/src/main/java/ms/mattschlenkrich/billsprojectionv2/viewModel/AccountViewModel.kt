package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountCategory
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType
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

    fun getAllAccounts() =
        accountRepository.getAllAccounts()


    fun findAccount(accountId: Long) =
        accountRepository.findAccount(accountId)


    fun searchAccounts(query: String) =
        accountRepository.searchAccounts(query)


    fun insertAccountType(accountType: AccountType) =
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

    fun findAccountType(accountTypeId: Long) =
            accountRepository.findAccountType(accountTypeId)


    fun getAccountTypes() =
            accountRepository.getAccountTypes()


    fun searchAccountTypes(query: String) =
            accountRepository.searchAccountType(query)


    fun insertAccountCategory(accountCategory: AccountCategory) =
        viewModelScope.launch {
            accountRepository.insertAccountCategory(accountCategory)
        }

    fun updateAccountCategory(accountCategory: AccountCategory) =
        viewModelScope.launch {
            accountRepository.updateAccountCategory(accountCategory)
        }

    fun deleteAccountCategory(accountCategoryId: Long, updateTime: String) =
        viewModelScope.launch {
            accountRepository.deleteAccountCategory(accountCategoryId, updateTime)
        }

    fun getAccountCategories() =
            accountRepository.getAccountCategories()
    
    fun findAccountCategory(accountCategoryId: Long) =
            accountRepository.findAccountCategory(accountCategoryId)

    fun searchAccountCategories(query: String) =
            accountRepository.searchAccountCategories(query)

}
