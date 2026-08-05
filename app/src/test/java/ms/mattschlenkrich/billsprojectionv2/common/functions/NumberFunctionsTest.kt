package ms.mattschlenkrich.billsprojectionv2.common.functions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFunctionsTest {

    private val numberFunctions = NumberFunctions()

    @Test
    fun getDoubleFromDollars_validString() {
        assertEquals(1234.56, numberFunctions.getDoubleFromDollars("$1,234.56"), 0.001)
    }

    @Test
    fun getDoubleFromDollars_validStringNoSymbols() {
        assertEquals(1234.56, numberFunctions.getDoubleFromDollars("1234.56"), 0.001)
    }

    @Test
    fun getDoubleFromDollars_validStringWithSpaces() {
        assertEquals(1234.56, numberFunctions.getDoubleFromDollars(" $ 1,234.56 "), 0.001)
    }

    @Test
    fun getDoubleFromDollars_invalidString() {
        assertEquals(0.0, numberFunctions.getDoubleFromDollars("abc"), 0.001)
    }

    @Test
    fun getDoubleFromDollars_emptyString() {
        assertEquals(0.0, numberFunctions.getDoubleFromDollars(""), 0.001)
    }

    @Test
    fun displayDollars_formatsCorrectly() {
        // Locale is hardcoded to CANADA in NumberFunctions
        val result = numberFunctions.displayDollars(1234.56)
        // Format can be $1,234.56 or potentially something else depending on environment if not careful,
        // but Locale.CANADA should be consistent.
        assertTrue(result.contains("$"))
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun getNumberFromDouble_formatsCorrectly() {
        val result = numberFunctions.getNumberFromDouble(1234.56)
        assertEquals("1,234.56", result)
    }

    @Test
    fun getDollarsFromDouble_formatsCorrectly() {
        val result = numberFunctions.getDollarsFromDouble(1234.56)
        assertTrue(result.contains("$"))
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun generateId_returnsPositiveLong() {
        val id = numberFunctions.generateId()
        assertTrue(id >= 0)
    }
}