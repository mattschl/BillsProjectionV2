package ms.mattschlenkrich.billsprojectionv2.common

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("unused")
class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA)
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA)

    fun getCurrentTimeAsString(): String {
        return timeFormatter.format(Calendar.getInstance().time)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun convertDateToString(date: LocalDate): String {
        return dateFormat.format(date)
    }

    fun convertStringToDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    fun getDisplayDate(date: String): String {
        return displayDateString.format(
            dateChecker.parse(date)
        )
    }


    fun getDateStringFromDate(date: Date): String {
        return dateFormat.format(date)
    }
}