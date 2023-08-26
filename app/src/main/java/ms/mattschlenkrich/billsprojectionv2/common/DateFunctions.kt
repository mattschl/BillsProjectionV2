package ms.mattschlenkrich.billsprojectionv2.common

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

@Suppress("unused")
class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

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
}