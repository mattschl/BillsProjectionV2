package ms.mattschlenkrich.billsprojectionv2.common.functions

import ms.mattschlenkrich.billsprojectionv2.common.DATE_CHECK
import ms.mattschlenkrich.billsprojectionv2.common.DISPLAY_DATE
import ms.mattschlenkrich.billsprojectionv2.common.DISPLAY_DATE_WITH_YEAR
import ms.mattschlenkrich.billsprojectionv2.common.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.common.SQLITE_TIME
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//private const val TAG = "DateFunctions"

@Suppress("unused")
class DateFunctions {
    private val utcTimeZone = TimeZone.getTimeZone("UTC")
    private val localTimeZone = TimeZone.getDefault()
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA).apply {
        timeZone = localTimeZone
    }
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA).apply {
        timeZone = utcTimeZone
    }
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA).apply {
        timeZone = localTimeZone
    }
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA).apply {
        timeZone = localTimeZone
    }
    private val displayDateWithYear =
        SimpleDateFormat(DISPLAY_DATE_WITH_YEAR, Locale.CANADA).apply {
            timeZone = localTimeZone
        }
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).apply {
        timeZone = utcTimeZone
    }

    fun getCurrentTimeAsString(): String {
        return timeFormatter.format(Calendar.getInstance(utcTimeZone).time)
    }

    fun getDateTimeStringFromDate(date: Date): String {
        return timeFormatter.format(date)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance(localTimeZone).time)
    }

    fun convertDateToString(date: LocalDate): String {
        return date.toString()
    }

    fun convertStringToDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    fun getDisplayDate(date: String): String {
        return try {
            if (date.isBlank()) ""
            else {
                val parsed = dateChecker.parse(date)
                if (parsed != null) displayDateString.format(parsed) else ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getDisplayDateWithYear(date: String): String {
        return try {
            if (date.isBlank()) ""
            else {
                val parsed = dateChecker.parse(date)
                if (parsed != null) displayDateWithYear.format(parsed) else ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getDisplayDateInComingYear(date: String): String {
        return try {
            if (date.isBlank()) ""
            else {
                var mDate = LocalDate.parse(date)
                while (mDate.toString() < getCurrentDateAsString()) {
                    mDate = mDate.plusYears(1)
                }
                getDisplayDateWithYear(mDate.toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getDisplayDateInComingYear(date: String, count: Long): String {
        return try {
            if (date.isBlank()) ""
            else {
                val mDate = LocalDate.parse(date)
                getDisplayDateWithYear(mDate.plusYears(count).toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getNextMonthlyDate(startDate: String, interval: Int): String {
        return try {
            if (startDate.isBlank()) ""
            else {
                var mDate = LocalDate.parse(startDate)
                while (mDate.toString() < getCurrentDateAsString()) {
                    mDate = mDate.plusMonths(interval.toLong())
                }
                getDisplayDateWithYear(mDate.toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getNextWeeklyDate(startDate: String, interval: Int): String {
        return try {
            if (startDate.isBlank()) ""
            else {
                var mDate = LocalDate.parse(startDate)
                while (mDate.toString() < getCurrentDateAsString()) {
                    mDate = mDate.plusWeeks(interval.toLong())
                }
                getDisplayDateWithYear(mDate.toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateStringFromDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrentFileTimestamp(): String {
        return fileTimestampFormat.format(Calendar.getInstance(utcTimeZone).time)
    }

    fun getTimeThreeWeeksAgo(): String {
        val calendar = Calendar.getInstance(utcTimeZone)
        calendar.add(Calendar.WEEK_OF_YEAR, -3)
        return timeFormatter.format(calendar.time)
    }

    fun getFileTimestampFromDate(date: Date): String {
        return fileTimestampFormat.format(date)
    }

    fun parseFileTimestamp(timestamp: String): Date? {
        return try {
            fileTimestampFormat.parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Legacy function to ensure that dates previously stored in local time
     * are correctly interpreted and converted to UTC for the new sync logic.
     */
    fun getUtcFromLegacyLocal(localTimestamp: String): String {
        return try {
            val localFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
            // Uses system default timezone for parsing
            val date = localFormatter.parse(localTimestamp)
            if (date != null) timeFormatter.format(date) else localTimestamp
        } catch (e: Exception) {
            localTimestamp
        }
    }

    /**
     * Converts a UTC timestamp from the database to a local time string for UI display.
     */
    fun getLocalDisplayTime(utcTimestamp: String): String {
        return try {
            val date = timeFormatter.parse(utcTimestamp)
            val localFormatter = SimpleDateFormat(SQLITE_TIME, Locale.getDefault())
            if (date != null) localFormatter.format(date) else utcTimestamp
        } catch (e: Exception) {
            utcTimestamp
        }
    }

    fun getMonthsBetween(startDate: String, endDate: String): Int {
        return try {
            val start = startDate.split("-")
            val end = endDate.split("-")
            if (start.size < 3 || end.size < 3) 0
            else {
                val years = end[0].toInt() - start[0].toInt()
                var months = end[1].toInt() - start[1].toInt()
                months -= if (end[2].toInt() <= start[2].toInt()) 1 else 0
                months + (years * 12)
            }
        } catch (e: Exception) {
            0
        }
    }

    fun getFirstOfMonth(date: String): String {
        return try {
            if (date.length < 10) date
            else date.dropLast(2) + "01"
        } catch (e: Exception) {
            date
        }
    }

    private fun getLastOfMonth(date: String): String {
        return try {
            val first = getFirstOfMonth(date)
            if (first.isBlank()) ""
            else {
                val mDate = LocalDate.parse(first)
                mDate.plusMonths(1).minusDays(1).toString()
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getFirstOfPreviousMonth(date: String): String {
        return try {
            if (date.isBlank()) ""
            else {
                val mDate = LocalDate.parse(date)
                getFirstOfMonth(mDate.minusMonths(1).toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getLastOfPreviousMonth(date: String): String {
        return try {
            if (date.isBlank()) ""
            else {
                val mDate = LocalDate.parse(date).minusMonths(1)
                getLastOfMonth(mDate.toString())
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getOneYearAgo(date: String): String {
        return try {
            if (date.isBlank()) ""
            else LocalDate.parse(date).minusYears(1).toString()
        } catch (e: Exception) {
            ""
        }
    }
}