package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountDaoTest : BaseDaoTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndGetAccount() = runBlocking {
        val accountType = AccountType(
            1L, "Bank", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val account = Account(
            1L, "Checking", "12345", 1L, 0.0, 100.0, 0.0, 0.0, false, "2023-01-01"
        )
        accountDao.insertAccount(account)

        val accountList = accountDao.getActiveAccounts().getOrAwaitValue()
        assertEquals(1, accountList.size)
        assertEquals("Checking", accountList[0].accountName)
    }

    @Test
    fun updateAccount() = runBlocking {
        val accountType = AccountType(
            1L, "Bank", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val account = Account(
            1L, "Checking", "12345", 1L, 0.0, 100.0, 0.0, 0.0, false, "2023-01-01"
        )
        accountDao.insertAccount(account)

        val updatedAccount = account.copy(accountBalance = 200.0)
        accountDao.updateAccount(updatedAccount)

        val retrievedAccount = accountDao.getAccountSync(1L)
        assertNotNull(retrievedAccount)
        assertEquals(200.0, retrievedAccount?.accountBalance!!, 0.0)
    }

    @Test
    fun deleteAccount() = runBlocking {
        val accountType = AccountType(
            1L, "Bank", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val account = Account(
            1L, "Checking", "12345", 1L, 0.0, 100.0, 0.0, 0.0, false, "2023-01-01"
        )
        accountDao.insertAccount(account)

        accountDao.deleteAccount(1L, "2023-01-02")

        val activeAccounts = accountDao.getActiveAccountsSync()
        assertTrue(activeAccounts.isEmpty())

        val allAccounts = accountDao.getAllAccountsSync()
        assertEquals(1, allAccounts.size)
        assertTrue(allAccounts[0].accIsDeleted)
    }

    @Test
    fun findAccountByName() = runBlocking {
        val accountType = AccountType(
            1L, "Bank", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val account = Account(
            1L, "Savings", "67890", 1L, 0.0, 500.0, 0.0, 0.0, false, "2023-01-01"
        )
        accountDao.insertAccount(account)

        val foundAccount = accountDao.findAccountByName("Savings")
        assertNotNull(foundAccount)
        assertEquals(1L, foundAccount?.accountId)
    }

    @Test
    fun searchAccounts() = runBlocking {
        val accountType = AccountType(
            1L, "Bank", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        accountDao.insertAccount(Account(1L, "Apple", "1", 1L, 0.0, 0.0, 0.0, 0.0, false, ""))
        accountDao.insertAccount(Account(2L, "Banana", "2", 1L, 0.0, 0.0, 0.0, 0.0, false, ""))
        accountDao.insertAccount(Account(3L, "Cherry", "3", 1L, 0.0, 0.0, 0.0, 0.0, false, ""))

        val searchResult = accountDao.searchAccounts("%an%").getOrAwaitValue()
        assertEquals(1, searchResult.size)
        assertEquals("Banana", searchResult[0].accountName)
    }
}