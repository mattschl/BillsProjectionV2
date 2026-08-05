package ms.mattschlenkrich.billsprojectionv2.common.projections

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ProjectBudgetDatesTest {

    private val frequencyTypes = arrayOf(
        "Monthly", "Weekly", "Yearly", "On Payday", "Manually", "Special"
    )
    private val daysOfWeek = arrayOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday", "Week Day", "Week End", "Any Day"
    )

    private val projectBudgetDates = ProjectBudgetDates(frequencyTypes, daysOfWeek)

    @Test
    fun projectWeekly_interval1() {
        val startDate = LocalDate.now().minusWeeks(1).toString()
        val endDate = LocalDate.now().plusWeeks(3).toString()
        val result = projectBudgetDates.projectWeekly(startDate, endDate, 1)

        assertTrue("Should have at least 3 future dates", result.size >= 3)
        for (i in 0 until result.size - 1) {
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
}