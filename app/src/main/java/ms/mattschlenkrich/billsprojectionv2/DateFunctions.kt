package ms.mattschlenkrich.billsprojectionv2

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    fun getCurrentTimeAsString(): String {
        return timeFormatter.format(Calendar.getInstance().time)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }
}