package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
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
class BudgetRuleDaoTest : BaseDaoTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var budgetRuleDao: BudgetRuleDao

    @Before
    fun setup() = runBlocking {
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
    }

    @Test
    fun insertAndGetBudgetRule() = runBlocking {
        val rule = BudgetRule(
            1L, "Rent", 10L, 20L, 1000.0, true, false, true,
            "2023-01-01", "2099-12-31", 1, FREQ_MONTHLY, 1, 0, false, "2023-01-01"
        )
        budgetRuleDao.insertBudgetRule(rule)

        val retrieved = budgetRuleDao.getBudgetRule(1L)
        assertNotNull(retrieved)
        assertEquals("Rent", retrieved?.budgetRuleName)
    }

    @Test
    fun findBudgetRuleByName() = runBlocking {
        val rule = BudgetRule(
            1L, "Electricity", 10L, 20L, 50.0, false, false, true,
            "2023-01-01", "2099-12-31", 1, FREQ_MONTHLY, 1, 0, false, "2023-01-01"
        )
        budgetRuleDao.insertBudgetRule(rule)

        val found = budgetRuleDao.findBudgetRuleByName("Electricity")
        assertNotNull(found)
        assertEquals(1L, found?.ruleId)
    }

    @Test
    fun renameBudgetRule() = runBlocking {
        val rule = BudgetRule(
            1L, "Old Name", 10L, 20L, 10.0, false, false, false,
            "2023-01-01", null, 1, FREQ_WEEKLY, 1, 0, false, ""
        )
        budgetRuleDao.insertBudgetRule(rule)

        budgetRuleDao.renameBudgetRule(1L, "New Name", "2023-01-02")

        val retrieved = budgetRuleDao.getBudgetRule(1L)
        assertEquals("New Name", retrieved?.budgetRuleName)
    }

    @Test
    fun deleteBudgetRule() = runBlocking {
        val rule = BudgetRule(
            1L, "To Delete", 10L, 20L, 10.0, false, false, false,
            "2023-01-01", null, 1, FREQ_WEEKLY, 1, 0, false, ""
        )
        budgetRuleDao.insertBudgetRule(rule)

        budgetRuleDao.deleteBudgetRule(1L, "2023-01-02")

        val activeRules = budgetRuleDao.getBudgetRulesActive()
        assertTrue(activeRules.isEmpty())

        val retrieved = budgetRuleDao.getBudgetRule(1L)
        assertTrue(retrieved?.budIsDeleted == true)
    }

    @Test
    fun getActiveBudgetRulesDetailed() = runBlocking {
        val rule = BudgetRule(
            1L, "Active Rule", 10L, 20L, 100.0, false, false, false,
            "2023-01-01", "2099-12-31", 1, FREQ_MONTHLY, 1, 0, false, ""
        )
        budgetRuleDao.insertBudgetRule(rule)

        val detailedList = budgetRuleDao.getActiveBudgetRulesDetailed().getOrAwaitValue()
        assertEquals(1, detailedList.size)
        assertEquals("Active Rule", detailedList[0].budgetRule?.budgetRuleName)
        assertEquals("ToAcc", detailedList[0].toAccount?.accountName)
        assertEquals("FromAcc", detailedList[0].fromAccount?.accountName)
    }

    @Test
    fun getBudgetRulesMonthly_filterLogic() = runBlocking {
        // Today is 2023-06-01
        val today = "2023-06-01"

        // Rule 1: Weekly, count 1 (Should be included)
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                1L, "Weekly 1", 10L, 20L, 10.0, false, false, false,
                "2023-01-01", "2023-12-31", 1, FREQ_WEEKLY, 1, 0, false, ""
            )
        )

        // Rule 2: Weekly, count 5 (Should be excluded - Occasional)
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                2L, "Weekly 5", 10L, 20L, 10.0, false, false, false,
                "2023-01-01", "2023-12-31", 1, FREQ_WEEKLY, 5, 0, false, ""
            )
        )

        // Rule 3: Monthly, count 1 (Should be included)
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                3L, "Monthly 1", 10L, 20L, 10.0, false, false, false,
                "2023-01-01", "2023-12-31", 1, FREQ_MONTHLY, 1, 0, false, ""
            )
        )

        // Rule 4: Monthly, count 2 (Should be excluded - Occasional)
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                4L, "Monthly 2", 10L, 20L, 10.0, false, false, false,
                "2023-01-01", "2023-12-31", 1, FREQ_MONTHLY, 2, 0, false, ""
            )
        )

        // Rule 5: Expired (Should be excluded)
        budgetRuleDao.insertBudgetRule(
            BudgetRule(
                5L, "Expired", 10L, 20L, 10.0, false, false, false,
                "2022-01-01", "2022-12-31", 1, FREQ_MONTHLY, 1, 0, false, ""
            )
        )

        val monthlyRules = budgetRuleDao.getBudgetRulesMonthly(today).getOrAwaitValue()
        assertEquals(2, monthlyRules.size)
        assertTrue(monthlyRules.any { it.budgetRule?.budgetRuleName == "Weekly 1" })
        assertTrue(monthlyRules.any { it.budgetRule?.budgetRuleName == "Monthly 1" })
    }
}