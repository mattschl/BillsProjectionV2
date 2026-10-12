package ms.mattschlenkrich.billsprojectionv2.common.projections

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.PredictionUpdateHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val TAG = "UpdateBudgetPredictions"

class UpdateBudgetPredictions(
    private val mainActivity: MainActivity,
) {
    private val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    private val budgetItemViewModel = mainActivity.budgetItemViewModel
    private val df = DateFunctions()
    private val projectBudgetDates = ProjectBudgetDates(mainActivity)

    suspend fun killPredictions(): Boolean {
        try {
            val startDate = LocalDate.now().minusWeeks(2).toString()
            budgetItemViewModel.killFutureBudgetItemsSync(
                startDate, df.getCurrentTimeAsString(),
            )
        } catch (_: Exception) {
            Log.e(TAG, "An unknown error occurred")
            return false
        }
        return true
    }

    suspend fun updatePredictions(stopDate: String) {
        purgeOldItems()
        deleteEligibleFutureItems()
        val budgetRules = budgetRuleViewModel.getBudgetRulesActive()
        if (budgetRules.isNotEmpty()) {
            val payDayBudgetRuleList = getBudgetRuleListThatIsPayday(budgetRules)
            if (payDayBudgetRuleList.isNotEmpty()) {
                updatePayDayBudgetItemPredictions(payDayBudgetRuleList, stopDate)
            }
            val rulesOnPayDay = getBudgetRulesFallingOnPayDay(budgetRules)
            if (rulesOnPayDay.isNotEmpty()) {
                val payDays = budgetItemViewModel.getPayDaysActive()
                if (payDays.isNotEmpty()) {
                    updateBudgetItemsFallingOnPaydays(rulesOnPayDay, stopDate, payDays)
                }
            }
            val rulesOther = getBudgetRulesOther(budgetRules)
            if (rulesOther.isNotEmpty()) {
                updateAllOtherBudgetItems(rulesOther, stopDate)
            }
        }

        try {
            val settings = SettingsManager(mainActivity).getSettings()
            val today = LocalDate.now()
            val stopLocalDate = try {
                LocalDate.parse(stopDate)
            } catch (_: Exception) {
                today.plusMonths(3)
            }
            val monthsLength =
                (ChronoUnit.MONTHS.between(today, stopLocalDate)).toInt().coerceAtLeast(1)

            val history = PredictionUpdateHistory(
                predictionUpdateId = NumberFunctions().generateId(),
                puhTime = df.getCurrentTimeAsString(),
                puhLastPredictionDate = stopDate,
                puhPredictionLength = monthsLength,
                puhDeviceId = settings.deviceId,
            )
            budgetItemViewModel.insertPredictionUpdateHistory(history)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording prediction update history", e)
        }
    }

    private fun isNoDate(dateStr: String?): Boolean {
        return dateStr.isNullOrBlank() || dateStr == "0" || dateStr == "0000-00-00" || dateStr.trim() == "0"
    }

    private fun getEffectiveEndDate(ruleEndDate: String?, stopDate: String): String {
        return if (isNoDate(ruleEndDate) || ruleEndDate!! > stopDate) {
            stopDate
        } else {
            ruleEndDate
        }
    }

    private suspend fun updatePayDayBudgetItemPredictions(
        payDayBudgetRuleList: List<BudgetRule>,
        stopDate: String,
    ) {
        val updateTime = df.getCurrentTimeAsString()
        for (rule in payDayBudgetRuleList) {
            val endDate = getEffectiveEndDate(rule.budEndDate, stopDate)
            val payDates = projectBudgetDates.projectDates(
                startDate = rule.budStartDate,
                endDate = endDate,
                interval = rule.budFrequencyCount.toLong(),
                intervalTypeId = rule.budFrequencyTypeId,
                dayOfWeekId = rule.budDayOfWeekId,
                leadDays = rule.budLeadDays.toLong(),
            )
            for (date in payDates) {
                insertOrRewriteBudgetItem(
                    rule,
                    date.toString(),
                    date.toString(),
                    date.toString(),
                    updateTime,
                )
            }
        }
    }

    private suspend fun updateBudgetItemsFallingOnPaydays(
        rulesOnPayDay: ArrayList<BudgetRule>, stopDate: String, payDays: List<String>,
    ) {
        val updateTime = df.getCurrentTimeAsString()
        for (rule in rulesOnPayDay) {
            val endDate = getEffectiveEndDate(rule.budEndDate, stopDate)
            val payDates = projectBudgetDates.projectOnPayDay(
                rule.budStartDate, rule.budFrequencyCount.toLong(), payDays, endDate,
            )
            for (date in payDates) {
                insertOrRewriteBudgetItem(
                    rule, date.toString(), date.toString(), date.toString(), updateTime,
                )
            }
        }
    }

    private suspend fun updateAllOtherBudgetItems(
        rulesOther: ArrayList<BudgetRule>, stopDate: String,
    ) {
        val updateTime = df.getCurrentTimeAsString()
        val payDays = budgetItemViewModel.getPayDaysActive()
        if (payDays.isEmpty()) return

        for (rule in rulesOther) {
            val endDate = getEffectiveEndDate(rule.budEndDate, stopDate)
            val payDates = projectBudgetDates.projectDates(
                startDate = rule.budStartDate,
                endDate = endDate,
                interval = rule.budFrequencyCount.toLong(),
                intervalTypeId = rule.budFrequencyTypeId,
                dayOfWeekId = rule.budDayOfWeekId,
                leadDays = rule.budLeadDays.toLong(),
            )
            for (date in payDates) {
                val assignedPayDay = findPayDayForDate(date, payDays)
                assignedPayDay?.let {
                    insertOrRewriteBudgetItem(
                        rule,
                        date.toString(),
                        date.toString(),
                        it,
                        updateTime,
                    )
                }
            }
        }
    }

    private fun findPayDayForDate(date: LocalDate, payDays: List<String>): String? {
        var lastPayDay: String? = null
        for (payDayStr in payDays) {
            val payDay = try {
                LocalDate.parse(payDayStr)
            } catch (_: Exception) {
                null
            } ?: continue
            if (date.isBefore(payDay)) {
                return lastPayDay
            }
            lastPayDay = payDayStr
        }
        return lastPayDay
    }

    private suspend fun insertOrRewriteBudgetItem(
        rule: BudgetRule,
        projectedDate: String,
        actualDate: String,
        payDay: String,
        updateTime: String
    ) {
        val newBudgetItem = BudgetItem(
            biRuleId = rule.ruleId,
            biProjectedDate = projectedDate,
            biActualDate = actualDate,
            biPayDay = payDay,
            biBudgetName = rule.budgetRuleName,
            biIsPayDayItem = rule.budIsPayDay,
            biToAccountId = rule.budToAccountId,
            biFromAccountId = rule.budFromAccountId,
            biProjectedAmount = rule.budgetAmount,
            biIsPending = false,
            biIsFixed = rule.budFixedAmount,
            biIsAutomatic = rule.budIsAutoPay,
            biManuallyEntered = false,
            biLocked = false,
            biIsCompleted = false,
            biIsCancelled = false,
            biIsDeleted = false,
            biUpdateTime = updateTime
        )
        try {
            budgetItemViewModel.insertBudgetItemSync(newBudgetItem)
            Log.d(TAG, "ADDING BUDGET ITEM ${newBudgetItem.biBudgetName}")
        } catch (e: SQLiteConstraintException) {
            // Row exists, rewrite it
            Log.d(
                TAG,
                "THE BUDGET ITEM WAS NOT ADDED - rewriting ${newBudgetItem.biBudgetName}.\n $e"
            )
            budgetItemViewModel.rewriteBudgetItem(
                budgetRuleId = rule.ruleId,
                projectedDate = projectedDate,
                actualDate = actualDate,
                payDay = payDay,
                budgetName = rule.budgetRuleName,
                isPayDay = rule.budIsPayDay,
                toAccountId = rule.budToAccountId,
                fromAccountId = rule.budFromAccountId,
                projectedAmount = rule.budgetAmount,
                isFixed = rule.budFixedAmount,
                isAutomatic = rule.budIsAutoPay,
                updateTime = updateTime
            )
        }
    }

    private fun getBudgetRulesOther(
        budgetRules: List<BudgetRule>
    ): ArrayList<BudgetRule> {
        val ruleList = ArrayList<BudgetRule>()
        for (rule in budgetRules) {
            if ((!rule.budIsPayDay) && (rule.budFrequencyTypeId != 3)) {
                ruleList.add(rule)
            }
        }
        return ruleList
    }

    private fun getBudgetRulesFallingOnPayDay(
        budgetRules: List<BudgetRule>
    ): ArrayList<BudgetRule> {
        val ruleList = ArrayList<BudgetRule>()
        for (rule in budgetRules) {
            if (rule.budFrequencyTypeId == 3) {
                ruleList.add(rule)
            }
        }
        return ruleList
    }

    private fun getBudgetRuleListThatIsPayday(
        budgetRules: List<BudgetRule>
    ): List<BudgetRule> {
        return budgetRules.filter { budgetRule ->
            budgetRule.budIsPayDay
        }
    }

    private suspend fun deleteEligibleFutureItems(): Boolean {
        try {
            budgetItemViewModel.deleteFutureItemsSync(
                df.getCurrentDateAsString(), df.getCurrentTimeAsString()
            )
        } catch (_: Exception) {
            Log.e(TAG, "An unknown error occurred")
            return false
        }
        return true
    }

    private fun purgeOldItems(): Boolean {
        try {
            val cutoffDate = LocalDate.now().minusMonths(2).toString()
            budgetItemViewModel.purgeOldBudgetItems(cutoffDate)
        } catch (_: Exception) {
            Log.e(TAG, "An unknown error occurred")
            return false
        }
        return true
    }
}