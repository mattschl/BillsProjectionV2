package ms.mattschlenkrich.billsprojectionv2.common.functions

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorLogicTest {

    @Test
    fun addDigit_initialZero() {
        val result = CalculatorLogic.addDigit("0", "5")
        assertEquals("5", result)
    }

    @Test
    fun addDigit_decimal() {
        val result = CalculatorLogic.addDigit("5", ".")
        assertEquals("5.", result)
    }

    @Test
    fun addDigit_decimalTwice() {
        val result = CalculatorLogic.addDigit("5.", ".")
        assertEquals("5.", result)
    }

    @Test
    fun addDigit_negative() {
        val result = CalculatorLogic.addDigit("5", "-")
        assertEquals("-5", result)
    }

    @Test
    fun addDigit_negativeToggle() {
        val result = CalculatorLogic.addDigit("-5", "-")
        assertEquals("5", result)
    }

    @Test
    fun backspace_normal() {
        val result = CalculatorLogic.backspace("123")
        assertEquals("12", result)
    }

    @Test
    fun backspace_singleDigit() {
        val result = CalculatorLogic.backspace("5")
        assertEquals("0", result)
    }

    @Test
    fun calculate_addition() {
        val result = CalculatorLogic.calculate(10.0, 5.0, "+", NumberFunctions())
        assertEquals(15.0, result, 0.001)
    }

    @Test
    fun calculate_divisionByZero() {
        val result = CalculatorLogic.calculate(10.0, 0.0, "/", NumberFunctions())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun formatFormula_addition() {
        val nf = NumberFunctions()
        val result = CalculatorLogic.formatFormula(10.0, 5.0, "+", 15.0, nf)
        assertEquals("10 + 5 = $15.00", result)
    }
}