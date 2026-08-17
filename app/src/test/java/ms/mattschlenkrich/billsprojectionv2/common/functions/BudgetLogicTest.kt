package ms.mattschlenkrich.billsprojectionv2.common.functions

import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_YEARLY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetLogicTest {

    @Test
    fun calculateSuggestedAmount_weekly() {
        // Weekly (interval 1): 100 spent over 28 days (4 weeks) -> should suggest 25
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 100.0,
            totalCount = 4,
            daysElapsed = 28,
            frequencyTypeId = FREQ_WEEKLY,
            frequencyCount = 1
        )
        assertEquals(25.0, result!!, 0.001)
    }

    @Test
    fun calculateSuggestedAmount_monthly() {
        // Monthly (interval 1): logic uses 30.4375 days per month.
        // If we spend 120 over 30.4375 * 2 days, it should suggest 60.
        // Since daysElapsed is Long, we use a multiple.
        val days = (30.4375 * 2).toLong() // 60
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 120.0,
            totalCount = 2,
            daysElapsed = days,
            frequencyTypeId = FREQ_MONTHLY,
            frequencyCount = 1
        )
        // expectedOccurrences = 60 / 30.4375 = 1.97125...
        // suggestion = 120 / 1.97125... = 60.875
        assertEquals(60.875, result!!, 0.001)
    }

    @Test
    fun calculateSuggestedAmount_yearly() {
        // Yearly (interval 1): 1000 spent over 366 days -> should suggest approx 1000
        // Expected occurrences = 366 / 365.25 = 1.00205...
        // Suggestion = 1000 / 1.00205... = 997.95...
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 1000.0,
            totalCount = 1,
            daysElapsed = 366,
            frequencyTypeId = FREQ_YEARLY,
            frequencyCount = 1
        )
        assertEquals(997.95, result!!, 0.01)
    }

    @Test
    fun calculateSuggestedAmount_otherType() {
        // Other type (manual/special): use average per transaction
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 150.0,
            totalCount = 3,
            daysElapsed = 100,
            frequencyTypeId = 5, // Special
            frequencyCount = 1
        )
        assertEquals(50.0, result!!, 0.001)
    }

    @Test
    fun calculateSuggestedAmount_zeroSum() {
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 0.0,
            totalCount = 0,
            daysElapsed = 100,
            frequencyTypeId = FREQ_WEEKLY,
            frequencyCount = 1
        )
        assertNull(result)
    }

    @Test
    fun calculateSuggestedAmount_zeroDays() {
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 100.0,
            totalCount = 1,
            daysElapsed = 0,
            frequencyTypeId = FREQ_WEEKLY,
            frequencyCount = 1
        )
        assertNull(result)
    }

    @Test
    fun calculateSuggestedAmount_lessThanOneOccurrence() {
        // 50 spent over 3 days for a weekly rule -> not enough data to suggest (expectedOccurrences < 1.0)
        val result = BudgetLogic.calculateSuggestedAmount(
            totalSum = 50.0,
            totalCount = 1,
            daysElapsed = 3,
            frequencyTypeId = FREQ_WEEKLY,
            frequencyCount = 1
        )
        assertNull(result)
    }
}