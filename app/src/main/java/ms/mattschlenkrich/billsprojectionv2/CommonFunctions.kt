package ms.mattschlenkrich.billsprojectionv2

import java.text.NumberFormat
import java.util.Locale

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
}