package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetItemDaoTest : BaseDaoTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var budgetItemDao: BudgetItemDao
    private lateinit var budgetRuleDao: BudgetRuleDao

    @Before
    fun setup() = runBlocking {
        budgetItemDao = db.getBudgetItemDao()
        budgetRuleDao = db.getBudgetRuleDao()

        // Insert prerequisites
        accountTypeDao.insertAccountType(
            AccountType(
                1L,
                "Type",
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
        accountDao.insertAccount(Account(10L, "ToAcc", "1", 1L, 0.0, 0.0, 0.0, 0.0, false, ""))
        accountDao.insertAccount(Account(20L, "FromAcc", "2", 1L, 0.0, 0.0, 0.0, 0.0, false, ""))

        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                1L, "Rent", 10L, 20L, 1000.0, true, false, true,
                "2023-01-01", "2099-12-31", 1, FREQ_MONTHLY, 1, 0, false, ""
            )
        )
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                2L, "Pay Day", 10L, 20L, 2000.0, false, true, false,
                "2023-01-01", "2099-12-31", 1, FREQ_MONTHLY, 1, 0, false, ""
            )
        )
    }

    @Test
    fun insertAndGetBudgetItem() = runBlocking {
        val item = BudgetItem(
            1L, "2023-06-01", "2023-06-01", "2023-06-01", "Rent June",
            false, 10L, 20L, 1000.0, false, true, true, false,
            false, false, false, "2023-01-01", false
        )
        budgetItemDao.insertBudgetItem(item)

        val retrieved = budgetItemDao.getBudgetItem(1L, "2023-06-01")
        assertNotNull(retrieved)
        assertEquals("Rent June", retrieved?.biBudgetName)
    }

    @Test
    fun deleteBudgetItem() = runBlocking {
        val item = BudgetItem(
            1L, "2023-06-01", "2023-06-01", "2023-06-01", "Rent June",
            false, 10L, 20L, 1000.0, false, true, true, false,
            false, false, false, "2023-01-01", false
        )
        budgetItemDao.insertBudgetItem(item)

        budgetItemDao.deleteBudgetItem(1L, "2023-06-01", "2023-01-02")

        val retrieved = budgetItemDao.getBudgetItem(1L, "2023-06-01")
        assertTrue(retrieved?.biIsDeleted == true)
    }

    @Test
    fun getPayDaysActive() = runBlocking {
        // Item 1: Pay day item, active
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                2L, "2023-06-01", "2023-06-01", "2023-06-01", "Pay",
                true, 10L, 20L, 2000.0, false, false, false, false,
                false, false, false, "", false
            )
        )
        // Item 2: Pay day item, cancelled (should be excluded)
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                2L, "2023-06-15", "2023-06-15", "2023-06-15", "Pay Cancelled",
                true, 10L, 20L, 2000.0, false, false, false, false,
                false, true, false, "", false
            )
        )

        val payDays = budgetItemDao.getPayDaysActive()
        assertEquals(1, payDays.size)
        assertEquals("2023-06-01", payDays[0])
    }

    @Test
    fun deleteFutureItems() = runBlocking {
        // Item 1: Today (2023-06-01), not deleted
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                1L, "2023-06-01", "2023-06-01", "2023-06-01", "Today",
                false, 10L, 20L, 100.0, false, false, false, false,
                false, false, false, "", false
            )
        )
        // Item 2: Future (2023-07-01), will be deleted
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                1L, "2023-07-01", "2023-07-01", "2023-07-01", "Future",
                false, 10L, 20L, 100.0, false, false, false, false,
                false, false, false, "", false
            )
        )
        // Item 3: Future (2023-08-01), locked (should NOT be deleted)
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                1L, "2023-08-01", "2023-08-01", "2023-08-01", "Locked",
                false, 10L, 20L, 100.0, false, false, false, false,
                false, false, false, "", true
            )
        )

        budgetItemDao.deleteFutureItems("2023-06-01", "2023-06-01 12:00:00")

        val allItems = budgetItemDao.getAllBudgetItemsSync()
        val todayItem = allItems.find { it.biBudgetName == "Today" }
        val futureItem = allItems.find { it.biBudgetName == "Future" }
        val lockedItem = allItems.find { it.biBudgetName == "Locked" }

        assertTrue(todayItem?.biIsDeleted == false)
        assertTrue(futureItem?.biIsDeleted == true)
        assertTrue(lockedItem?.biIsDeleted == false)
    }

    @Test
    fun getBudgetItems_byPayDay() = runBlocking {
        val payDay = "2023-06-01"
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                1L, "2023-06-01", "2023-06-01", payDay, "Rent",
                false, 10L, 20L, 1000.0, false, false, false, false,
                false, false, false, "", false
            )
        )
        budgetItemDao.insertBudgetItem(
            BudgetItem(
                1L, "2023-07-01", "2023-07-01", "2023-07-01", "Other Month",
                false, 10L, 20L, 100.0, false, false, false, false,
                false, false, false, "", false
            )
        )

        val items = budgetItemDao.getBudgetItems("All Items", payDay).getOrAwaitValue()
        assertEquals(1, items.size)
        assertEquals("Rent", items[0].budgetItem?.biBudgetName)
    }
}