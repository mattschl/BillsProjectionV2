package ms.mattschlenkrich.billsprojectionv2.projections

import android.util.Log
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.DAY_ANY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_FRIDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_MONDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_SATURDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_SUNDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_THURSDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_TUESDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_WEDNESDAY
import ms.mattschlenkrich.billsprojectionv2.common.DAY_WEEK_DAY
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_ONE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_ON_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_SPECIAL
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.INTERVAL_YEARLY
import java.time.DayOfWeek
import java.time.LocalDate

private const val TAG = "ProjectBudgetDates"

//@Suppress("unused")
class ProjectBudgetDates(private val mainActivity: MainActivity) {

    fun projectDates(
        startDate: String,
        endDate: String,
        interval: Long,
        intervalTypeId: Int,
        dayOfWeekId: Int,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val intervalType = mainActivity.baseContext.resources
            .getStringArray(R.array.frequency_types)[intervalTypeId]
        val dayOfWeek = mainActivity.baseContext.resources
            .getStringArray(R.array.days_of_week)[dayOfWeekId]
        when (intervalType) {
            INTERVAL_WEEKLY -> {
                return projectWeekly(
                    startDate, endDate, interval
                )
            }

            INTERVAL_MONTHLY -> {
                return projectMonthly(
                    startDate, endDate, interval, dayOfWeek, leadDays
                )
            }

            INTERVAL_YEARLY -> {
                return projectYearly(
                    startDate, endDate, interval, dayOfWeek, leadDays
                )
            }

            INTERVAL_ON_PAY_DAY -> {
                return projectOnPayDay(
                    startDate, interval
                )
            }

            INTERVAL_SPECIAL -> {
                //special projections
                return ArrayList()
            }

            INTERVAL_ONE_TIME -> {
                return projectOneTime(
                    startDate, dayOfWeek, leadDays
                )
            }

            else -> {
                return ArrayList()
            }
        }
    }

    private fun fixDates(
        datesToFix: ArrayList<LocalDate>,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        var minusDates = ArrayList<LocalDate>()
        if (leadDays == 0L) {
            minusDates = datesToFix
        } else {
            for (i in 0 until datesToFix.size) {
                minusDates.add(datesToFix[i].minusDays(leadDays))
//                Log.d(TAG, "fixing lead days ${minusDates[i]}")
            }
        }
        var fixedDates = ArrayList<LocalDate>()
        when (dayOfWeek) {
            DAY_ANY_DAY -> {
                fixedDates = minusDates
            }

            DAY_WEEK_DAY -> {
                for (i in 0 until minusDates.size) {
                    var newDate = minusDates[i]
                    if (minusDates[i].dayOfWeek == DayOfWeek.SATURDAY) {
                        newDate = minusDates[i].minusDays(1)
                    } else if (minusDates[i].dayOfWeek == DayOfWeek.SUNDAY) {
                        newDate = minusDates[i].minusDays(2)
                    }
                    fixedDates.add(newDate)
                }
            }

            else -> {
                val dayNumber =
                    when (dayOfWeek) {
                        DAY_MONDAY -> {
                            1
                        }

                        DAY_TUESDAY -> {
                            2
                        }

                        DAY_WEDNESDAY -> {
                            3
                        }

                        DAY_THURSDAY -> {
                            4
                        }

                        DAY_FRIDAY -> {
                            5
                        }

                        DAY_SATURDAY -> {
                            6
                        }

                        DAY_SUNDAY -> {
                            7
                        }

                        else -> {
                            0
                        }
                    }
                for (i in 0 until minusDates.size) {
                    val dateToFix = minusDates[i]
                    if (dateToFix.dayOfWeek.value == dayNumber) {
                        fixedDates.add(minusDates[i])
                    } else {
                        var newDate = minusDates[i]
                        newDate = newDate.minusDays(dateToFix.dayOfWeek.value.toLong())
                            .plusDays(dayNumber.toLong())
                        if (newDate.isAfter(minusDates[i])) {
                            newDate = newDate.minusWeeks(1)
//                            Log.d(TAG, "subtracting a week")
                        }
                        while (newDate.isBefore(minusDates[i].minusWeeks(1))) {
                            newDate = newDate.plusWeeks(1)
//                            Log.d(TAG, "Adding a week")
                        }
                        fixedDates.add(newDate)
                    }
                }
            }
        }
//        return fixedDates
        return fixedDates
    }

    private fun projectMonthly(
        startDate: String,
        endDate: String,
        interval: Long,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val datesToFix = ArrayList<LocalDate>()
        if (LocalDate.now() < LocalDate.parse(endDate)) {
            var workingDate: LocalDate
            workingDate = LocalDate.parse(startDate)
            while (workingDate <= LocalDate.parse(endDate)) {
                workingDate = workingDate.plusMonths(interval)
                if (workingDate > LocalDate.now()) {
                    datesToFix.add(workingDate)
                }
            }
        }
        return fixDates(datesToFix, dayOfWeek, leadDays)
    }


    private fun projectWeekly(
        startDate: String,
        endDate: String,
        interval: Long
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        if (LocalDate.now() < LocalDate.parse(endDate)) {
            var workingDate: LocalDate
            workingDate = LocalDate.parse(startDate)
            while (workingDate <= LocalDate.parse(endDate)) {
                workingDate = workingDate.plusWeeks(interval)
                if (workingDate > LocalDate.now()) {
                    dates.add(workingDate)
                    Log.d(
                        TAG,
                        "Testing, in projectWeekly date is $workingDate. -------- End date is ${
                            LocalDate.parse(endDate)
                        } !!!"
                    )
                }
            }
        }
        Log.d(TAG, "dates size = ${dates.size}")
        return dates
    }

    private fun projectYearly(
        startDate: String,
        endDate: String,
        interval: Long,
        dayOfWeek: String,
        leadDays: Long
    ): ArrayList<LocalDate> {
        val datesToFix = ArrayList<LocalDate>()
        if (LocalDate.now() < LocalDate.parse(endDate)) {
            var workingDate: LocalDate
            workingDate = LocalDate.parse(startDate)
            while (workingDate <= LocalDate.parse(endDate)) {
                workingDate = workingDate.plusYears(interval)
                if (workingDate > LocalDate.now()) {
                    datesToFix.add(workingDate)
                }
            }
        }
        return fixDates(datesToFix, dayOfWeek, leadDays)
    }

    //------------To do budget items that fall on a payday -------
    @Suppress("UNUSED_PARAMETER", "UNREACHABLE_CODE", "UnnecessaryVariable")
    private fun projectOnPayDay(
        startDate: String,
        interval: Long
    ): ArrayList<LocalDate> {
        TODO("write the dao function")
        val dates = ArrayList<LocalDate>()
//        val projectionsDb = ProjectionsDb(context)
//        val payDates = projectionsDb.getPayDates(startDate)
//        if (payDates.size > 0) {
//            dates.add(payDates[0])
////            Log.d(TAG, "pay Date number 0 is ${payDates[0]}")
//            if (interval < 2) {
//                for (i in 1 until payDates.size) {
//                    dates.add(payDates[i])
//                }
//            } else {
//                for (i in 1 until payDates.size) {
//                    if ((i.toDouble() / interval.toDouble()) -
//                        (i.toDouble() / interval.toDouble()).toInt() == 0.0
//                    ) {
//                        dates.add(payDates[i])
//                    }
//                }
//            }
//        }
        return dates
    }

    private fun projectOneTime(
        startDate: String,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        dates.add(LocalDate.parse(startDate))
        return fixDates(dates, dayOfWeek, leadDays)
    }
}