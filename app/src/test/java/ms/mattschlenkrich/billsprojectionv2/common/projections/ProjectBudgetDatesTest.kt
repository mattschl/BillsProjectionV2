package ms.mattschlenkrich.billsprojectionv2.common.projections

import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ProjectBudgetDatesTest {

    private val frequencyTypes = arrayOf(
        "Monthly", "Weekly", "Yearly", "On Payday", "Manually", "Special",
    )
    private val daysOfWeek = arrayOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday", "Week Day", "Week End", "Any Day",
    )

    private val projectBudgetDates = ProjectBudgetDates(frequencyTypes, daysOfWeek)

    @Test
    fun projectWeekly_interval1() {
        val startDate = LocalDate.now().minusWeeks(1).toString()
        val endDate = LocalDate.now().plusWeeks(3).toString()
        val result = projectBudgetDates.projectWeekly(startDate, endDate, 1)

        assertTrue("Should have at least 3 future dates", result.size >= 3)
        for (i in 0 until (result.size - 1)) {
            assertEquals(
                "Dates should be exactly 1 week apart",
                result[i].plusWeeks(1),
                result[i + 1]
            )
        }
    }

    @Test
    fun projectMonthly_anyDay() {
        val futureStart = LocalDate.now().plusMonths(1).withDayOfMonth(15)
        val futureEnd = futureStart.plusMonths(3)
        val futureResult = projectBudgetDates.projectMonthly(
            futureStart.toString(), futureEnd.toString(), 1, "Any Day", 0
        )

        assertEquals(4, futureResult.size)
        assertEquals(futureStart, futureResult[0])
        assertEquals(futureStart.plusMonths(1), futureResult[1])
        assertEquals(futureStart.plusMonths(2), futureResult[2])
        assertEquals(futureStart.plusMonths(3), futureResult[3])
    }

    @Test
    fun projectYearly_isCorrect() {
        val futureStart = LocalDate.now().plusYears(1).withMonth(1).withDayOfMonth(1)
        val futureEnd = futureStart.plusYears(2)
        val result = projectBudgetDates.projectYearly(
            futureStart.toString(), futureEnd.toString(), 1, "Any Day", 0
        )

        assertEquals(3, result.size)
        assertEquals(futureStart, result[0])
        assertEquals(futureStart.plusYears(1), result[1])
        assertEquals(futureStart.plusYears(2), result[2])
    }

    @Test
    fun projectOneTime_isCorrect() {
        val date = LocalDate.now().plusDays(5)
        val result = projectBudgetDates.projectOneTime(date.toString(), "Any Day", 0)
        assertEquals(1, result.size)
        assertEquals(date, result[0])
    }

    @Test
    fun fixDates_leadDays() {
        val date = LocalDate.of(2023, 10, 27) // Friday
        val dates = arrayListOf(date)
        val result = projectBudgetDates.fixDates(dates, "Any Day", 2)
        assertEquals(date.minusDays(2), result[0])
        assertEquals(DayOfWeek.WEDNESDAY, result[0].dayOfWeek)
    }

    @Test
    fun fixDates_weekDayShift() {
        val saturday = LocalDate.of(2023, 10, 28)
        val sunday = LocalDate.of(2023, 10, 29)
        val dates = arrayListOf(saturday, sunday)
        val result = projectBudgetDates.fixDates(dates, "Week Day", 0)

        assertEquals(LocalDate.of(2023, 10, 27), result[0]) // Sat -> Fri
        assertEquals(LocalDate.of(2023, 10, 27), result[1]) // Sun -> Fri (minus 2)
    }

    @Test
    fun fixDates_specificDayShift() {
        val friday = LocalDate.of(2023, 10, 27)
        val dates = arrayListOf(friday)

        // Shift to Monday
        val toMonday = projectBudgetDates.fixDates(dates, "Monday", 0)
        assertEquals(LocalDate.of(2023, 10, 23), toMonday[0])

        // Shift to Saturday
        val toSaturday = projectBudgetDates.fixDates(dates, "Saturday", 0)
        assertEquals(LocalDate.of(2023, 10, 21), toSaturday[0]) // Previous Saturday
    }

    @Test
    fun projectOnPayDay_isCorrect() {
        val today = LocalDate.now()
        val futurePayDays = listOf(
            today.plusDays(1).toString(),
            today.plusDays(15).toString(),
            today.plusDays(29).toString(),
            today.plusDays(43).toString()
        )

        val result = projectBudgetDates.projectOnPayDay(
            futurePayDays[0], 2, futurePayDays, futurePayDays[3]
        )

        // (d+1) % 2 == 0 means 2nd, 4th, etc.
        // indices 1 and 3 in the list
        assertEquals(2, result.size)
        assertEquals(LocalDate.parse(futurePayDays[1]), result[0])
        assertEquals(LocalDate.parse(futurePayDays[3]), result[1])
    }

    private fun createTestRule(
        startDate: String,
        endDate: String? = "0",
        frequencyTypeId: Int = 0, // Monthly
        frequencyCount: Int = 1,
        dayOfWeekId: Int = 9, // Any Day
    ): BudgetRule {
        return BudgetRule(
            ruleId = 1L,
            budgetRuleName = "Test Rule",
            budToAccountId = 1L,
            budFromAccountId = 2L,
            budgetAmount = 100.0,
            budFixedAmount = true,
            budIsPayDay = false,
            budIsAutoPay = false,
            budStartDate = startDate,
            budEndDate = endDate,
            budDayOfWeekId = dayOfWeekId,
            budFrequencyTypeId = frequencyTypeId,
            budFrequencyCount = frequencyCount,
            budLeadDays = 0,
            budIsDeleted = false,
            budUpdateTime = "2026-01-01 00:00:00",
        )
    }

    @Test
    fun getNextScheduledDate_unscheduledRule_returnsNull() {
        val ruleStartZero = createTestRule(startDate = "0")
        val resultZero = projectBudgetDates.getNextScheduledDate(ruleStartZero)
        assertNull("Rule with startDate '0' should return null for next scheduled date", resultZero)

        val ruleStartBlank = createTestRule(startDate = "")
        val resultBlank = projectBudgetDates.getNextScheduledDate(ruleStartBlank)
        assertNull(
            "Rule with blank startDate should return null for next scheduled date",
            resultBlank
        )
    }

    @Test
    fun getNextScheduledDate_monthlyRule_returnsNextFutureDate() {
        val today = LocalDate.now()
        val futureStart = today.plusDays(5)
        val rule = createTestRule(startDate = futureStart.toString(), endDate = "0")

        val result = projectBudgetDates.getNextScheduledDate(rule)
        assertNotNull("Next scheduled date should not be null", result)
        assertEquals(futureStart.toString(), result)
    }

    @Test
    fun getNextScheduledDate_openEndedEndDateZero_returnsNextFutureDate() {
        val today = LocalDate.now()
        val pastStart = today.minusMonths(2).withDayOfMonth(15)
        val rule = createTestRule(startDate = pastStart.toString(), endDate = "0")

        val result = projectBudgetDates.getNextScheduledDate(rule)
        assertNotNull("Rule with endDate '0' should calculate next date", result)
        val parsedNext = LocalDate.parse(result!!)
        assertTrue("Next date should be on or after today", parsedNext >= today)
    }

    @Test
    fun getNextScheduledDate_expiredRule_returnsNull() {
        val pastStart = LocalDate.now().minusYears(2).toString()
        val pastEnd = LocalDate.now().minusYears(1).toString()
        val rule = createTestRule(startDate = pastStart, endDate = pastEnd)

        val result = projectBudgetDates.getNextScheduledDate(rule)
        assertNull("Expired rule should return null for next scheduled date", result)
    }

    @Test
    fun getNextScheduledDate_onPaydayRule_returnsNextPayday() {
        val today = LocalDate.now()
        val nextPayday = today.plusDays(10).toString()
        val payDays = listOf(
            today.minusDays(4).toString(),
            nextPayday,
            today.plusDays(24).toString(),
        )
        val rule = createTestRule(
            startDate = today.minusDays(4).toString(),
            endDate = "0",
            frequencyTypeId = 3, // On Payday
            frequencyCount = 1,
        )

        val result = projectBudgetDates.getNextScheduledDate(rule, payDays)
        assertNotNull("On-payday rule should find next payday", result)
        assertEquals(nextPayday, result)
    }
}