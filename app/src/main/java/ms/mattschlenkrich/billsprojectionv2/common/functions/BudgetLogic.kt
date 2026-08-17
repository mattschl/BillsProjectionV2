package ms.mattschlenkrich.billsprojectionv2.common.functions

import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_YEARLY

object BudgetLogic {
    fun calculateSuggestedAmount(
        totalSum: Double,
        totalCount: Int,
        daysElapsed: Long,
        frequencyTypeId: Int,
        frequencyCount: Int
    ): Double? {
        if (totalSum <= 0.0) return null
        if (daysElapsed <= 0) return null

        val daysPerOccurrence = when (frequencyTypeId) {
            FREQ_MONTHLY -> 30.4375 * frequencyCount
            FREQ_WEEKLY -> 7.0 * frequencyCount
            FREQ_YEARLY -> 365.25 * frequencyCount
            else -> 0.0
        }

        if (daysPerOccurrence > 0) {
            val expectedOccurrences = daysElapsed.toDouble() / daysPerOccurrence
            if (expectedOccurrences >= 1.0) {
                return totalSum / expectedOccurrences
            }
        } else {
            // For other types, use average per actual transaction
            if (totalCount > 0) {
                return totalSum / totalCount
            }
        }
        return null
    }
}