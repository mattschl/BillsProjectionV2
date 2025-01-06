package ms.mattschlenkrich.billsprojectionv2

import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import org.junit.Assert.assertEquals
import org.junit.Test


class DateFunctionsTest {
    @Test
    fun getMonthsBetweenIsAccurate(
    ) {
        val df = DateFunctions()
        val startDate = "2024-10-23"
        val endDate = "2025-10-23"
        val monthsBetween = 12
        assertEquals(
            "getMonthsBetween $startDate and $endDate is $monthsBetween",
            monthsBetween,
            df.getMonthsBetween(
                startDate, endDate
            )
        )
    }
}