package ms.mattschlenkrich.billsprojectionv2

class CommonFunctions {
    fun getDoubleFromDollars(dollars: String): Double {
        return dollars.trim()
            .replace("$", "")
            .replace(",", "")
            .toDouble()
    }
}