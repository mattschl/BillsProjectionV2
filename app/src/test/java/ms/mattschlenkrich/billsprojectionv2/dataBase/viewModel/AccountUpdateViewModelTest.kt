package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountUpdateViewModelTest {

    private val transactionViewModel: TransactionViewModel = mockk()
    private val accountViewModel: AccountViewModel = mockk()
    private val application: Application = mockk()
    private lateinit var viewModel: AccountUpdateViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        viewModel = AccountUpdateViewModel(transactionViewModel, accountViewModel, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `performTransaction should update account balances and insert transaction`() = runTest {
        // Given
        val toAccountId = 1L
        val fromAccountId = 2L
        val amount = 100.0
        val transaction = Transactions(
            transId = 0,
            transDate = "2023-01-01",
            transName = "Test",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = toAccountId,
            transToAccountPending = false,
            transFromAccountId = fromAccountId,
            transFromAccountPending = false,
            transAmount = amount,
            transIsDeleted = false,
            transUpdateTime = ""
        )

        val toAccount = Account(toAccountId, "To", "123", 1L, 0.0, 500.0, 0.0, 0.0, false, "")
        val fromAccount =
            Account(fromAccountId, "From", "456", 2L, 0.0, 1000.0, 0.0, 0.0, false, "")
        val toAccountType =
            AccountType(1L, "Type1", true, true, false, false, true, true, false, "")
        val fromAccountType =
            AccountType(2L, "Type2", true, true, false, false, true, true, false, "")

        coEvery { accountViewModel.getAccountAndType(toAccountId) } returns AccountAndType(
            toAccount,
            toAccountType
        )
        coEvery { accountViewModel.getAccountAndType(fromAccountId) } returns AccountAndType(
            fromAccount,
            fromAccountType
        )
        coEvery { accountViewModel.getAccount(toAccountId) } returns toAccount
        coEvery { accountViewModel.getAccount(fromAccountId) } returns fromAccount
        coEvery {
            transactionViewModel.updateAccountBalanceAndOwing(
                any(),
                any(),
                any(),
                any()
            )
        } returns Unit
        coEvery { transactionViewModel.insertTransaction(any()) } returns Unit

        // When
        viewModel.performTransaction(transaction)

        // Then
        // To Account: 500 + 100 = 600
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(600.0, 0.0, toAccountId, any())
        }
        // From Account: 1000 - 100 = 900
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(900.0, 0.0, fromAccountId, any())
        }
        coVerify { transactionViewModel.insertTransaction(transaction) }
    }

    @Test
    fun `deleteTransaction should update account balances and delete transaction`() = runTest {
        // Given
        val toAccountId = 1L
        val fromAccountId = 2L
        val amount = 100.0
        val transaction = Transactions(
            transId = 10L,
            transDate = "2023-01-01",
            transName = "Test",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = toAccountId,
            transToAccountPending = false,
            transFromAccountId = fromAccountId,
            transFromAccountPending = false,
            transAmount = amount,
            transIsDeleted = false,
            transUpdateTime = ""
        )

        val toAccount = Account(toAccountId, "To", "123", 1L, 0.0, 600.0, 0.0, 0.0, false, "")
        val fromAccount = Account(fromAccountId, "From", "456", 2L, 0.0, 900.0, 0.0, 0.0, false, "")
        val toAccountType =
            AccountType(1L, "Type1", true, true, false, false, true, true, false, "")
        val fromAccountType =
            AccountType(2L, "Type2", true, true, false, false, true, true, false, "")

        coEvery { accountViewModel.getAccountAndType(toAccountId) } returns AccountAndType(
            toAccount,
            toAccountType
        )
        coEvery { accountViewModel.getAccountAndType(fromAccountId) } returns AccountAndType(
            fromAccount,
            fromAccountType
        )
        coEvery { accountViewModel.getAccount(toAccountId) } returns toAccount
        coEvery { accountViewModel.getAccount(fromAccountId) } returns fromAccount
        coEvery {
            transactionViewModel.updateAccountBalanceAndOwing(
                any(),
                any(),
                any(),
                any()
            )
        } returns Unit
        coEvery { transactionViewModel.deleteTransaction(any(), any()) } returns Unit

        // When
        viewModel.deleteTransaction(transaction)

        // Then
        // To Account: 600 - 100 = 500
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(500.0, 0.0, toAccountId, any())
        }
        // From Account: 900 + 100 = 1000
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(1000.0, 0.0, fromAccountId, any())
        }
        coVerify { transactionViewModel.deleteTransaction(10L, any()) }
    }

    @Test
    fun `performTransaction with tallyOwing should update owing`() = runTest {
        // Given
        val toAccountId = 1L
        val fromAccountId = 2L
        val amount = 100.0
        val transaction = Transactions(
            transId = 0,
            transDate = "2023-01-01",
            transName = "Test",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = toAccountId,
            transToAccountPending = false,
            transFromAccountId = fromAccountId,
            transFromAccountPending = false,
            transAmount = amount,
            transIsDeleted = false,
            transUpdateTime = ""
        )

        val toAccount = Account(toAccountId, "To", "123", 1L, 0.0, 0.0, 500.0, 0.0, false, "")
        val fromAccount =
            Account(fromAccountId, "From", "456", 2L, 0.0, 1000.0, 0.0, 0.0, false, "")
        val toAccountType =
            AccountType(1L, "Type1", false, false, true, false, true, true, false, "")
        val fromAccountType =
            AccountType(2L, "Type2", true, true, false, false, true, true, false, "")

        coEvery { accountViewModel.getAccountAndType(toAccountId) } returns AccountAndType(
            toAccount,
            toAccountType
        )
        coEvery { accountViewModel.getAccountAndType(fromAccountId) } returns AccountAndType(
            fromAccount,
            fromAccountType
        )
        coEvery { accountViewModel.getAccount(toAccountId) } returns toAccount
        coEvery { accountViewModel.getAccount(fromAccountId) } returns fromAccount
        coEvery {
            transactionViewModel.updateAccountBalanceAndOwing(
                any(),
                any(),
                any(),
                any()
            )
        } returns Unit
        coEvery { transactionViewModel.insertTransaction(any()) } returns Unit

        // When
        viewModel.performTransaction(transaction)

        // Then
        // To Account: keepTotals=false, tallyOwing=true. isCredit=true (TO account)
        // newOwing = 500 - 100 = 400
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(0.0, 400.0, toAccountId, any())
        }
        // From Account: 1000 - 100 = 900
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(900.0, 0.0, fromAccountId, any())
        }
    }

    @Test
    fun `updateTransaction should correctly undo old and apply new balances`() = runTest {
        // Given
        val oldToId = 1L
        val oldFromId = 2L
        val newToId = 3L
        val newFromId = 4L
        val amount = 100.0

        val oldTransaction = Transactions(
            10,
            "2023-01-01",
            "Old",
            "",
            1,
            oldToId,
            false,
            oldFromId,
            false,
            amount,
            false,
            ""
        )
        val newTransaction = Transactions(
            10,
            "2023-01-01",
            "New",
            "",
            1,
            newToId,
            false,
            newFromId,
            false,
            amount,
            false,
            ""
        )

        val oldToAcc = Account(oldToId, "OldTo", "", 1, 0.0, 500.0, 0.0, 0.0, false, "")
        val oldFromAcc = Account(oldFromId, "OldFrom", "", 2, 0.0, 1000.0, 0.0, 0.0, false, "")
        val newToAcc = Account(newToId, "NewTo", "", 3, 0.0, 200.0, 0.0, 0.0, false, "")
        val newFromAcc = Account(newFromId, "NewFrom", "", 4, 0.0, 800.0, 0.0, 0.0, false, "")

        val accType = AccountType(1, "", true, true, false, false, true, true, false, "")

        coEvery { accountViewModel.getAccountAndType(any()) } returns AccountAndType(
            oldToAcc,
            accType
        )
        coEvery { accountViewModel.getAccount(oldToId) } returns oldToAcc
        coEvery { accountViewModel.getAccount(oldFromId) } returns oldFromAcc
        coEvery { accountViewModel.getAccount(newToId) } returns newToAcc
        coEvery { accountViewModel.getAccount(newFromId) } returns newFromAcc
        coEvery {
            transactionViewModel.updateAccountBalanceAndOwing(
                any(),
                any(),
                any(),
                any()
            )
        } returns Unit
        coEvery { transactionViewModel.updateTransaction(any()) } returns Unit

        // When
        viewModel.updateTransaction(oldTransaction, newTransaction)

        // Then
        // Undo old To: 500 - 100 = 400
        coVerify { transactionViewModel.updateAccountBalanceAndOwing(400.0, 0.0, oldToId, any()) }
        // Apply new To: 200 + 100 = 300
        coVerify { transactionViewModel.updateAccountBalanceAndOwing(300.0, 0.0, newToId, any()) }
        // Undo old From: 1000 + 100 = 1100
        coVerify {
            transactionViewModel.updateAccountBalanceAndOwing(
                1100.0,
                0.0,
                oldFromId,
                any()
            )
        }
        // Apply new From: 800 - 100 = 700
        coVerify { transactionViewModel.updateAccountBalanceAndOwing(700.0, 0.0, newFromId, any()) }

        coVerify { transactionViewModel.updateTransaction(newTransaction) }
    }
}