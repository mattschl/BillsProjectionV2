package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest : BaseDaoTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetRuleDao: BudgetRuleDao

    @Before
    fun setup() = runBlocking {
        transactionDao = db.getTransactionDao()
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
    }

    @Test
    fun insertAndGetTransaction() = runBlocking {
        val transaction = Transactions(
            1L,
            "2023-06-01",
            "Rent June",
            "Note",
            1L,
            10L,
            false,
            20L,
            false,
            1000.0,
            false,
            "2023-01-01"
        )
        transactionDao.insertTransaction(transaction)

        val retrieved = transactionDao.getTransaction(1L)
        assertNotNull(retrieved)
        assertEquals("Rent June", retrieved?.transName)
        assertEquals(1000.0, retrieved?.transAmount!!, 0.0)
    }

    @Test
    fun deleteTransaction() = runBlocking {
        val transaction = Transactions(
            1L, "2023-06-01", "Rent June", "", 1L, 10L, false, 20L, false, 1000.0, false, ""
        )
        transactionDao.insertTransaction(transaction)

        transactionDao.deleteTransaction(1L, "2023-01-02")

        val retrieved = transactionDao.getTransaction(1L)
        assertTrue(retrieved?.transIsDeleted == true)

        val active = transactionDao.getActiveTransactionsSync()
        assertTrue(active.isEmpty())
    }

    @Test
    fun getSumTransactionByBudgetRuleSync() = runBlocking {
        transactionDao.insertTransaction(
            Transactions(
                1L,
                "2023-06-01",
                "T1",
                "",
                1L,
                10L,
                false,
                20L,
                false,
                100.0,
                false,
                ""
            )
        )
        transactionDao.insertTransaction(
            Transactions(
                2L,
                "2023-06-15",
                "T2",
                "",
                1L,
                10L,
                false,
                20L,
                false,
                200.0,
                false,
                ""
            )
        )
        transactionDao.insertTransaction(
            Transactions(
                3L,
                "2023-07-01",
                "T3",
                "",
                1L,
                10L,
                false,
                20L,
                false,
                400.0,
                false,
                ""
            )
        )

        val sum = transactionDao.getSumTransactionByBudgetRuleSync(1L, "2023-06-01", "2023-06-30")
        assertEquals(300.0, sum!!, 0.0)
    }

    @Test
    fun getTransactionsFiltered_byQuery() = runBlocking {
        transactionDao.insertTransaction(
            Transactions(
                1L,
                "2023-06-01",
                "Apple",
                "Red",
                1L,
                10L,
                false,
                20L,
                false,
                1.0,
                false,
                ""
            )
        )
        transactionDao.insertTransaction(
            Transactions(
                2L,
                "2023-06-02",
                "Banana",
                "Yellow",
                1L,
                10L,
                false,
                20L,
                false,
                2.0,
                false,
                ""
            )
        )

        val result =
            transactionDao.getTransactionsFiltered(-1, -1, "%Red%", "", "").getOrAwaitValue()
        assertEquals(1, result.size)
        assertEquals("Apple", result[0].transaction?.transName)
    }

    @Test
    fun getSumToAccountFiltered() = runBlocking {
        // T1: To Account 10 (Target)
        transactionDao.insertTransaction(
            Transactions(
                1L,
                "2023-06-01",
                "T1",
                "",
                1L,
                10L,
                false,
                20L,
                false,
                100.0,
                false,
                ""
            )
        )
        // T2: From Account 10 (Not To)
        transactionDao.insertTransaction(
            Transactions(
                2L,
                "2023-06-02",
                "T2",
                "",
                1L,
                20L,
                false,
                10L,
                false,
                50.0,
                false,
                ""
            )
        )

        val sumTo = transactionDao.getSumToAccountFiltered(10L, "", "", "").getOrAwaitValue()
        assertEquals(100.0, sumTo, 0.0)
    }

    @Test
    fun getTransactionFull_joins() = runBlocking {
        val transaction = Transactions(
            1L, "2023-06-01", "JoinTest", "", 1L, 10L, false, 20L, false, 100.0, false, ""
        )
        transactionDao.insertTransaction(transaction)

        val full = transactionDao.getTransactionFull(1L, 10L, 20L)
        assertNotNull(full)
        assertEquals("JoinTest", full.transaction.transName)
        assertEquals("Rent", full.budgetRule?.budgetRuleName)
        assertEquals("ToAcc", full.toAccountAndType.account.accountName)
        assertEquals("FromAcc", full.fromAccountAndType.account.accountName)
    }
}