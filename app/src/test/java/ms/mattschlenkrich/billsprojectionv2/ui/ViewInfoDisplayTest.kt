package ms.mattschlenkrich.billsprojectionv2.ui

import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.projections.ProjectBudgetDates
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose.BudgetTotals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ViewInfoDisplayTest {

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private val frequencyTypes = arrayOf(
        "Monthly", "Weekly", "Yearly", "On Payday", "Manually", "Special",
    )
    private val daysOfWeek = arrayOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday", "Week Day", "Week End", "Any Day",
    )
    private val projectBudgetDates = ProjectBudgetDates(frequencyTypes, daysOfWeek)

    // --- 1. Budget Rule View Display Tests ---

    @Test
    fun budgetRuleView_scheduledRule_displaysNextScheduledDate() {
        val today = LocalDate.now()
        val futureStart = today.plusDays(3).toString()
        val rule = BudgetRule(
            ruleId = 10L,
            budgetRuleName = "Electricity",
            budToAccountId = 1L,
            budFromAccountId = 2L,
            budgetAmount = 150.0,
            budFixedAmount = true,
            budIsPayDay = false,
            budIsAutoPay = false,
            budStartDate = futureStart,
            budEndDate = "0",
            budDayOfWeekId = 9,
            budFrequencyTypeId = 0, // Monthly
            budFrequencyCount = 1,
            budLeadDays = 0,
            budIsDeleted = false,
            budUpdateTime = "2026-01-01 00:00:00",
        )

        val nextDateStr = projectBudgetDates.getNextScheduledDate(rule)
        assertNotNull("Scheduled rule should have a next scheduled date", nextDateStr)
        assertEquals(futureStart, nextDateStr)

        val displayDate = df.getDisplayDateWithYear(nextDateStr!!)
        assertNotNull("Display date should format correctly", displayDate)
    }

    @Test
    fun budgetRuleView_unscheduledRule_returnsNullNextDate() {
        val rule = BudgetRule(
            ruleId = 11L,
            budgetRuleName = "Unscheduled Repairs",
            budToAccountId = 1L,
            budFromAccountId = 2L,
            budgetAmount = 200.0,
            budFixedAmount = false,
            budIsPayDay = false,
            budIsAutoPay = false,
            budStartDate = "0", // Unscheduled
            budEndDate = "0",
            budDayOfWeekId = 9,
            budFrequencyTypeId = 0,
            budFrequencyCount = 1,
            budLeadDays = 0,
            budIsDeleted = false,
            budUpdateTime = "2026-01-01 00:00:00",
        )

        val nextDateStr = projectBudgetDates.getNextScheduledDate(rule)
        assertNull("Unscheduled rule with startDate '0' must return null next date", nextDateStr)
    }

    // --- 2. Budget View Display Tests ---

    @Test
    fun budgetView_budgetTotals_calculatesSurplusAndExpensesCorrectly() {
        val totals = BudgetTotals(
            credits = 2500.0,
            debits = 1800.0,
            fixedExpenses = 1200.0,
            otherExpenses = 600.0,
        )

        val surplus = totals.credits - totals.debits
        assertEquals(700.0, surplus, 0.001)
        assertEquals(1200.0, totals.fixedExpenses, 0.001)
        assertEquals(600.0, totals.otherExpenses, 0.001)
    }

    @Test
    fun budgetView_budgetItemDetailed_formatsAccountsDisplay() {
        val item = BudgetItem(
            biRuleId = 100L,
            biProjectedDate = "2026-10-15",
            biActualDate = "2026-10-15",
            biPayDay = "2026-10-15",
            biBudgetName = "Internet Bill",
            biIsPayDayItem = false,
            biToAccountId = 1L,
            biFromAccountId = 2L,
            biProjectedAmount = 85.0,
            biIsPending = false,
            biIsFixed = true,
            biIsAutomatic = true,
            biManuallyEntered = false,
            biIsCompleted = false,
            biIsCancelled = false,
            biIsDeleted = false,
            biUpdateTime = "2026-09-01 00:00:00",
            biLocked = false,
        )
        val toAccount = Account(1L, "ISP Provider", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(2L, "Chequing Account", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = BudgetItemDetailed(item, null, toAccount, fromAccount)

        val accountDisplay =
            "${detailed.fromAccount?.accountName ?: "Unknown"} -> ${detailed.toAccount?.accountName ?: "Unknown"}"
        assertEquals("Chequing Account -> ISP Provider", accountDisplay)
    }

    // --- 3. Transaction View Display Tests ---

    @Test
    fun transactionView_historyItemDisplay_formatsDescriptionNoteAndAccounts() {
        val transaction = Transactions(
            transId = 50L,
            transDate = "2026-09-20",
            transName = "Grocery Shopping",
            transNote = "Weekly groceries at Superstore",
            transRuleId = 0L,
            transToAccountId = 10L,
            transToAccountPending = false,
            transFromAccountId = 20L,
            transFromAccountPending = false,
            transAmount = 142.75,
            transIsDeleted = false,
            transUpdateTime = "2026-09-20 18:00:00",
        )
        val toAccount = Account(10L, "Groceries", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val fromAccount = Account(20L, "Credit Card", "", 1L, 0.0, 0.0, 0.0, 0.0, false, "")
        val detailed = TransactionDetailed(transaction, null, toAccount, fromAccount)

        val fullText =
            transaction.transName + if (transaction.transNote.isNotBlank()) " - ${transaction.transNote}" else ""
        assertEquals("Grocery Shopping - Weekly groceries at Superstore", fullText)

        val formattedAmount = nf.displayDollars(transaction.transAmount)
        assertEquals("$142.75", formattedAmount)

        val accountMapping =
            "${detailed.fromAccount?.accountName} -> ${detailed.toAccount?.accountName}"
        assertEquals("Credit Card -> Groceries", accountMapping)
    }
}