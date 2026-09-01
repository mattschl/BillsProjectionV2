package ms.mattschlenkrich.billsprojectionv2.common.functions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

class DateFunctionsTest {

    private val dateFunctions = DateFunctions()

    @Test
    fun convertDateToString_isCorrect() {
        val date = LocalDate.of(2023, 10, 27)
        assertEquals("2023-10-27", dateFunctions.convertDateToString(date))
    }

    @Test
    fun convertStringToDate_isCorrect() {
        val dateString = "2023-10-27"
        val expected = LocalDate.of(2023, 10, 27)
        assertEquals(expected, dateFunctions.convertStringToDate(dateString))
    }

    @Test
    fun getDisplayDate_isCorrect() {
        // DATE_FORMAT_SQL is yyyy-MM-dd (wait, Constants said yyyy-LL-dd for DATE_FORMAT_SQL but DATE_CHECK is yyyy-MM-dd)
        // DateFunctions uses dateChecker (DATE_CHECK) for parsing in getDisplayDate
        val result = dateFunctions.getDisplayDate("2023-10-27")
        // Locale.CANADA: EEE dd LLL
        // Oct 27, 2023 was a Friday
        assertTrue(result.contains("27"))
        assertTrue(result.contains("Oct"))
    }

    @Test
    fun getDisplayDateWithYear_isCorrect() {
        val result = dateFunctions.getDisplayDateWithYear("2023-10-27")
        // Locale.CANADA: EEE dd LLL /yy
        assertTrue(result.contains("27"))
        assertTrue(result.contains("Oct"))
        assertTrue(result.contains("/23"))
    }

    @Test
    fun getMonthsBetween_isCorrect() {
        assertEquals(1, dateFunctions.getMonthsBetween("2023-01-01", "2023-02-02"))
        assertEquals(12, dateFunctions.getMonthsBetween("2023-01-01", "2024-01-02"))
        assertEquals(0, dateFunctions.getMonthsBetween("2023-01-01", "2023-01-15"))
        assertEquals(11, dateFunctions.getMonthsBetween("2023-01-15", "2024-01-14"))
    }

    @Test
    fun getFirstOfMonth_isCorrect() {
        assertEquals("2023-10-01", dateFunctions.getFirstOfMonth("2023-10-27"))
    }

    @Test
    fun getFirstOfPreviousMonth_isCorrect() {
        assertEquals("2023-09-01", dateFunctions.getFirstOfPreviousMonth("2023-10-27"))
        assertEquals("2022-12-01", dateFunctions.getFirstOfPreviousMonth("2023-01-15"))
    }

    @Test
    fun getLastOfPreviousMonth_isCorrect() {
        assertEquals("2023-09-30", dateFunctions.getLastOfPreviousMonth("2023-10-27"))
        assertEquals("2023-02-28", dateFunctions.getLastOfPreviousMonth("2023-03-15"))
    }

    @Test
    fun getOneYearAgo_isCorrect() {
        assertEquals("2022-10-27", dateFunctions.getOneYearAgo("2023-10-27"))
    }

    @Test
    fun getCurrentTimeAsString_matchesPattern() {
        val result = dateFunctions.getCurrentTimeAsString()
        // yyyy-MM-dd HH:mm:ss
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun parseFileTimestamp_valid() {
        val timestamp = "20231027_123456"
        val date = dateFunctions.parseFileTimestamp(timestamp)
        assertNotNull(date)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date!!
        assertEquals(2023, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(27, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun parseFileTimestamp_invalid() {
        assertEquals(null, dateFunctions.parseFileTimestamp("invalid"))
    }

    @Test
    fun getDisplayDateInComingYear_futureDate_staysSame() {
        // Use a date far in the future to avoid "current date" issues
        val futureDate = LocalDate.now().plusYears(5).toString()
        val result = dateFunctions.getDisplayDateInComingYear(futureDate)
        assertTrue(result.contains(futureDate.substring(8, 10))) // check day
    }

    @Test
    fun getDisplayDateInComingYear_pastDate_advances() {
        val pastDate = "2020-01-01"
        val result = dateFunctions.getDisplayDateInComingYear(pastDate)
        // result should be Jan 01 of current or next year
        assertTrue(result.contains("01 Jan"))
        // It should definitely not be 2020
        assertTrue(!result.contains("/20"))
    }

    @Test
    fun getNextMonthlyDate_advances() {
        val startDate = "2020-01-01"
        val result = dateFunctions.getNextMonthlyDate(startDate, 1)
        assertTrue(result.contains("01"))
        assertTrue(!result.contains("/20"))
    }

    @Test
    fun getNextWeeklyDate_advances() {
        val startDate = "2020-01-01"
        val result = dateFunctions.getNextWeeklyDate(startDate, 1)
        // Should be same day of week
        assertTrue(!result.contains("/20"))
    }

    @Test
    fun getUtcFromLegacyLocal_converts() {
        // This is hard to test perfectly without knowing the local timezone of the test runner,
        // but we can check if it returns a valid string.
        val localTime = "2023-10-27 12:00:00"
        val result = dateFunctions.getUtcFromLegacyLocal(localTime)
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun getLocalDisplayTime_converts() {
        val utcTime = "2023-10-27 12:00:00"
        val result = dateFunctions.getLocalDisplayTime(utcTime)
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }
}