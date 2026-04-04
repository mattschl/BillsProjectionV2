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
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA).apply {
        timeZone = utcTimeZone
    }
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA).apply {
        timeZone = utcTimeZone
    }
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA).apply {
        timeZone = utcTimeZone
    }
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA).apply {
        timeZone = utcTimeZone
    }
    private val displayDateWithYear =
        SimpleDateFormat(DISPLAY_DATE_WITH_YEAR, Locale.CANADA).apply {
            timeZone = utcTimeZone
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
        return dateFormat.format(Calendar.getInstance(utcTimeZone).time)
    }

    fun convertDateToString(date: LocalDate): String {
        return date.toString()
    }

    fun convertStringToDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    fun getDisplayDate(date: String): String {
        return displayDateString.format(
            dateChecker.parse(date)!!
        )
    }

    fun getDisplayDateWithYear(date: String): String {
        return displayDateWithYear.format(
            dateChecker.parse(date)!!
        )
    }

    fun getDisplayDateInComingYear(date: String): String {
        var mDate = LocalDate.parse(date)
//        Log.d(TAG, "FIRST date is $mDate")
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusYears(1)
//            Log.d(TAG, "new date is $mDate")
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getDisplayDateInComingYear(date: String, count: Long): String {
        val mDate = LocalDate.parse(date)
        return getDisplayDateWithYear(mDate.plusYears(count).toString())
    }

    fun getNextMonthlyDate(startDate: String, interval: Int): String {
        var mDate = LocalDate.parse(startDate)
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusMonths(interval.toLong())
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getNextWeeklyDate(startDate: String, interval: Int): String {
        var mDate = LocalDate.parse(startDate)
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusWeeks(interval.toLong())
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getDateStringFromDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrentFileTimestamp(): String {
        return fileTimestampFormat.format(Calendar.getInstance(utcTimeZone).time)
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
            timeFormatter.format(date!!) // timeFormatter is UTC
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
            localFormatter.format(date!!)
        } catch (e: Exception) {
            utcTimestamp
        }
    }

    fun getMonthsBetween(startDate: String, endDate: String): Int {
        val start = startDate.split("-")
        val end = endDate.split("-")
        val years = end[0].toInt() - start[0].toInt()
        var months = end[1].toInt() - start[1].toInt()
        months -= if (end[2].toInt() <= start[2].toInt()) 1 else 0
        return months + (years * 12)
    }

    fun getFirstOfMonth(date: String): String {
        return date.dropLast(2) + "01"
    }

    private fun getLastOfMonth(date: String): String {
        val mDate = LocalDate.parse(getFirstOfMonth(date))
        return mDate.plusMonths(1).minusDays(1).toString()
    }

    fun getFirstOfPreviousMonth(date: String): String {
        val mDate = LocalDate.parse(date)
        return getFirstOfMonth(mDate.minusMonths(1).toString())
    }

    fun getLastOfPreviousMonth(date: String): String {
        val mDate = LocalDate.parse(date).minusMonths(1)
        return getLastOfMonth(mDate.toString())
    }
}