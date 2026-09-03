package ms.mattschlenkrich.billsprojectionv2.common.functions

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TransactionMessageHelperTest {

    private val context: Context = mockk()
    private val nf = NumberFunctions()

    @Before
    fun setUp() {
        // Mock common string resources
        every { context.getString(R.string.msg_will_perform) } returns "Will perform "
        every { context.getString(R.string.text_for_padded) } returns " for "
        every { context.getString(R.string.text_from_header) } returns "\n\nFROM: "
        every { context.getString(R.string.text_to_header) } returns "\nTO: "
        every { context.getString(R.string.text_pending_suffix) } returns " *pending*"
        every { context.getString(R.string.msg_will_apply_amount) } returns "Will apply amount of "
        every { context.getString(R.string.label_to_colon) } returns " To: "
        every { context.getString(R.string.label_from_header) } returns " From: "
        every { context.getString(R.string.text_and_header) } returns "\n and "
    }

    @Test
    fun `buildConfirmationMessage should return correct message for non-pending transaction`() {
        // Given
        val transaction = Transactions(
            transId = 1L,
            transDate = "2023-01-01",
            transName = "Coffee",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = 10L,
            transToAccountPending = false,
            transFromAccountId = 20L,
            transFromAccountPending = false,
            transAmount = 5.50,
            transIsDeleted = false,
            transUpdateTime = ""
        )
        val toAccount = Account(10L, "Food", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(20L, "Bank", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = TransactionDetailed(transaction, null, toAccount, fromAccount)

        // When
        val result = TransactionMessageHelper.buildConfirmationMessage(context, detailed, nf)

        // Then
        // Expected: "Will perform Coffee for $5.50\n\nFROM: Bank\nTO: Food"
        // Note: NumberFunctions uses Locale.CANADA, so it might be "$5.50" or similar.
        val expectedAmount = nf.getDollarsFromDouble(5.50)
        val expected = "Will perform Coffee for $expectedAmount\n\nFROM: Bank\nTO: Food"
        assertEquals(expected, result)
    }

    @Test
    fun `buildConfirmationMessage should include pending suffix when applicable`() {
        // Given
        val transaction = Transactions(
            transId = 1L,
            transDate = "2023-01-01",
            transName = "Gas",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = 10L,
            transToAccountPending = true,
            transFromAccountId = 20L,
            transFromAccountPending = true,
            transAmount = 50.0,
            transIsDeleted = false,
            transUpdateTime = ""
        )
        val toAccount = Account(10L, "Travel", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(20L, "Visa", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = TransactionDetailed(transaction, null, toAccount, fromAccount)

        // When
        val result = TransactionMessageHelper.buildConfirmationMessage(context, detailed, nf)

        // Then
        val expectedAmount = nf.getDollarsFromDouble(50.0)
        val expected =
            "Will perform Gas for $expectedAmount\n\nFROM: Visa *pending*\nTO: Travel *pending*"
        assertEquals(expected, result)
    }

    @Test
    fun `buildPendingCompletionMessage should return correct message for both pending`() {
        // Given
        val transaction = Transactions(
            transId = 1L,
            transDate = "2023-01-01",
            transName = "Rent",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = 10L,
            transToAccountPending = true,
            transFromAccountId = 20L,
            transFromAccountPending = true,
            transAmount = 1200.0,
            transIsDeleted = false,
            transUpdateTime = ""
        )
        val toAccount = Account(10L, "Landlord", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(20L, "Chequing", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = TransactionDetailed(transaction, null, toAccount, fromAccount)

        // When
        val result = TransactionMessageHelper.buildPendingCompletionMessage(context, detailed, nf)

        // Then
        val expectedAmount = nf.displayDollars(1200.0)
        val expected = "Will apply amount of $expectedAmount To: Landlord\n and  From: Chequing"
        assertEquals(expected, result)
    }

    @Test
    fun `buildPendingCompletionMessage should return correct message for only to pending`() {
        // Given
        val transaction = Transactions(
            transId = 1L,
            transDate = "2023-01-01",
            transName = "Refund",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = 10L,
            transToAccountPending = true,
            transFromAccountId = 20L,
            transFromAccountPending = false,
            transAmount = 25.0,
            transIsDeleted = false,
            transUpdateTime = ""
        )
        val toAccount = Account(10L, "Wallet", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(20L, "Store", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = TransactionDetailed(transaction, null, toAccount, fromAccount)

        // When
        val result = TransactionMessageHelper.buildPendingCompletionMessage(context, detailed, nf)

        // Then
        val expectedAmount = nf.displayDollars(25.0)
        val expected = "Will apply amount of $expectedAmount To: Wallet"
        assertEquals(expected, result)
    }

    @Test
    fun `buildPendingCompletionMessage should return empty for non-pending transaction`() {
        // Given
        val transaction = Transactions(
            transId = 1L,
            transDate = "2023-01-01",
            transName = "Lunch",
            transNote = "",
            transRuleId = 1L,
            transToAccountId = 10L,
            transToAccountPending = false,
            transFromAccountId = 20L,
            transFromAccountPending = false,
            transAmount = 15.0,
            transIsDeleted = false,
            transUpdateTime = ""
        )
        val detailed = TransactionDetailed(transaction, null, null, null)

        // When
        val result = TransactionMessageHelper.buildPendingCompletionMessage(context, detailed, nf)

        // Then
        assertEquals("", result)
    }
}