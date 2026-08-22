package ms.mattschlenkrich.billsprojectionv2.common.projections

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
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import java.time.DayOfWeek
import java.time.LocalDate

class ProjectBudgetDates(
    private val frequencyTypes: Array<String>,
    private val daysOfWeek: Array<String>
) {

    constructor(mainActivity: MainActivity) : this(
        mainActivity.baseContext.resources.getStringArray(R.array.frequency_types),
        mainActivity.baseContext.resources.getStringArray(R.array.days_of_week)
    )

    fun projectDates(
        startDate: String,
        endDate: String,
        interval: Long,
        intervalTypeId: Int,
        dayOfWeekId: Int,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val intervalType = frequencyTypes[intervalTypeId]
        val dayOfWeek = daysOfWeek[dayOfWeekId]
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
                return ArrayList()
            }

            INTERVAL_SPECIAL -> {
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

    internal fun fixDates(
        datesToFix: ArrayList<LocalDate>,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        var minusDates = ArrayList<LocalDate>()
        if (leadDays == 0L) {
            minusDates = datesToFix
        } else {
            for (i in datesToFix.indices) {
                minusDates.add(datesToFix[i].minusDays(leadDays))
            }
        }
        val fixedDates = ArrayList<LocalDate>()
        when (dayOfWeek) {
            DAY_ANY_DAY -> {
                fixedDates.addAll(minusDates)
            }

            DAY_WEEK_DAY -> {
                for (i in minusDates.indices) {
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
                val dayNumber = when (dayOfWeek) {
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
                for (i in minusDates.indices) {
                    val dateToFix = minusDates[i]
                    if (dateToFix.dayOfWeek.value == dayNumber) {
                        fixedDates.add(minusDates[i])
                    } else {
                        var newDate = minusDates[i]
                        newDate = newDate.minusDays(dateToFix.dayOfWeek.value.toLong())
                            .plusDays(dayNumber.toLong())
                        if (newDate.isAfter(minusDates[i])) {
                            newDate = newDate.minusWeeks(1)
                        }
                        while (newDate.isBefore(minusDates[i].minusWeeks(1))) {
                            newDate = newDate.plusWeeks(1)
                        }
                        fixedDates.add(newDate)
                    }
                }
            }
        }
        return fixedDates
    }

    internal fun projectMonthly(
        startDate: String,
        endDate: String,
        interval: Long,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val datesToFix = ArrayList<LocalDate>()
        val start = parseDateSafely(startDate) ?: return datesToFix
        val end = parseDateSafely(endDate) ?: return datesToFix
        if (LocalDate.now() < end) {
            var workingDate = start
            do {
                if (workingDate > LocalDate.now().minusMonths(interval).plusDays(1)) {
                    datesToFix.add(workingDate)
                }
                workingDate = workingDate.plusMonths(interval)
            } while (workingDate <= end)
        }
        return fixDates(datesToFix, dayOfWeek, leadDays)
    }


    internal fun projectWeekly(
        startDate: String, endDate: String, interval: Long
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        val start = parseDateSafely(startDate) ?: return dates
        val end = parseDateSafely(endDate) ?: return dates
        if (LocalDate.now() < end) {
            var workingDate = start
            while (workingDate <= end) {
                if (workingDate > LocalDate.now().minusWeeks(interval)) {
                    dates.add(workingDate)
                }
                workingDate = workingDate.plusWeeks(interval)
            }
        }
        return dates
    }

    internal fun projectYearly(
        startDate: String, endDate: String, interval: Long, dayOfWeek: String, leadDays: Long
    ): ArrayList<LocalDate> {
        val datesToFix = ArrayList<LocalDate>()
        val start = parseDateSafely(startDate) ?: return datesToFix
        val end = parseDateSafely(endDate) ?: return datesToFix
        if (LocalDate.now() < end) {
            var workingDate = start
            while (workingDate <= end) {
                if (workingDate > LocalDate.now().minusWeeks(1)) {
                    datesToFix.add(workingDate)
                }
                workingDate = workingDate.plusYears(interval)
            }
        }
        return fixDates(datesToFix, dayOfWeek, leadDays)
    }

    internal fun projectOneTime(
        startDate: String,
        dayOfWeek: String,
        leadDays: Long,
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        parseDateSafely(startDate)?.let { dates.add(it) }
        return fixDates(dates, dayOfWeek, leadDays)
    }

    fun projectOnPayDay(
        startDate: String, interval: Long, payDayList: List<String>, endDate: String
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        for (d in payDayList.indices) {
            if (payDayList[d] in startDate..endDate && (d + 1) % interval.toInt() == 0 && payDayList[d] >= LocalDate.now()
                    .toString()
            ) {
                parseDateSafely(payDayList[d])?.let { dates.add(it) }
            }
        }
        return dates
    }

    private fun parseDateSafely(dateString: String): LocalDate? {
        return try {
            if (dateString.isBlank()) null
            else LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}