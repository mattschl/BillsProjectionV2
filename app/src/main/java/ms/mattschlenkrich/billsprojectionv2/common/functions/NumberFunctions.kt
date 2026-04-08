package ms.mattschlenkrich.billsprojectionv2.common.functions

import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class NumberFunctions {
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val dollarFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val numberDisplay: NumberFormat = NumberFormat.getNumberInstance(Locale.CANADA)

    fun getDoubleFromDollars(dollars: String): Double {
        val cleanString = dollars.trim().replace("$", "").replace(",", "")
        return cleanString.toDoubleOrNull() ?: 0.0
    }

    fun displayDollars(num: Double): String {
        return dollarFormat.format(num)
    }


    fun getNumberFromDouble(num: Double): String {
        return numberDisplay.format(num)
    }

    fun getDollarsFromDouble(num: Double): String {
        return currencyFormat.format(num)
    }

    /**
     * Generates a unique 64-bit ID.
     * Uses the most significant bits of a UUID to minimize collision risk
     * in a distributed environment (multi-device sync).
     */
    fun generateId(): Long {
        return UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
    }
}