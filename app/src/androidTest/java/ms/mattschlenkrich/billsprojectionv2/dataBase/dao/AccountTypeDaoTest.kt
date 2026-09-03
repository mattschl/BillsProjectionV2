package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountTypeDaoTest : BaseDaoTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndGetAccountType() = runBlocking {
        val accountType = AccountType(
            1L, "Credit Card", true, false, true, false, false, true, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val activeTypes = accountTypeDao.getActiveAccountTypes().getOrAwaitValue()
        assertEquals(1, activeTypes.size)
        assertEquals("Credit Card", activeTypes[0].accountType)
    }

    @Test
    fun updateAccountType() = runBlocking {
        val accountType = AccountType(
            1L, "Savings", true, true, false, false, true, false, false, "2023-01-01"
        )
        accountTypeDao.insertAccountType(accountType)

        val updatedType = accountType.copy(accountType = "High Interest Savings")
        accountTypeDao.updateAccountType(updatedType)

        val activeTypes = accountTypeDao.getActiveAccountTypes().getOrAwaitValue()
        assertEquals("High Interest Savings", activeTypes[0].accountType)
    }

    @Test
    fun searchAccountType() = runBlocking {
        accountTypeDao.insertAccountType(
            AccountType(
                1L,
                "Checking",
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                ""
            )
        )
        accountTypeDao.insertAccountType(
            AccountType(
                2L,
                "Savings",
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                ""
            )
        )
        accountTypeDao.insertAccountType(
            AccountType(
                3L,
                "Investment",
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                ""
            )
        )

        val searchResult = accountTypeDao.searchAccountType("%ing%").getOrAwaitValue()
        assertEquals(2, searchResult.size)
        assertTrue(searchResult.any { it.accountType == "Checking" })
        assertTrue(searchResult.any { it.accountType == "Savings" })
    }

    @Test
    fun getAccountTypeNames() = runBlocking {
        accountTypeDao.insertAccountType(
            AccountType(
                1L,
                "A",
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                ""
            )
        )
        accountTypeDao.insertAccountType(
            AccountType(
                2L,
                "B",
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                ""
            )
        )

        val names = accountTypeDao.getAccountTypeNames().getOrAwaitValue()
        assertTrue(names.contains("A"))
        assertTrue(names.contains("B"))
    }
}