package ms.mattschlenkrich.billsprojectionv2.common

import java.text.NumberFormat
import java.util.Locale
import java.util.Random

class CommonFunctions {
    private val dollarFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    fun getDoubleFromDollars(dollars: String): Double {
        return dollars.trim()
            .replace("$", "")
            .replace(",", "")
            .toDouble()
    }

    fun displayDollars(num: Double): String {
        return dollarFormat.format(num)
    }

    fun generateId(): Long {
        var id =
            Random().nextInt(Int.MAX_VALUE).toLong()
        id = if (Random().nextBoolean()) -id
        else id
        return id
    }
}