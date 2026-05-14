package ms.mattschlenkrich.billsprojectionv2.common.projections

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import java.time.LocalDate

private const val TAG = "UpdateBudgetPredictions"

class UpdateBudgetPredictions(
    mainActivity: MainActivity,
) {
    private val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    private val budgetItemViewModel = mainActivity.budgetItemViewModel
    private val df = DateFunctions()
    private val projectBudgetDates = ProjectBudgetDates(mainActivity)

    suspend fun killPredictions(): Boolean {
        try {
            val startDate = LocalDate.now().minusWeeks(2).toString()
            budgetItemViewModel.killFutureBudgetItemsSync(
                startDate, df.getCurrentTimeAsString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "An unknown error occurred", e)
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
    }

    private suspend fun updatePayDayBudgetItemPredictions(
        payDayBudgetRuleList: List<BudgetRule>,
        stopDate: String,
    ) {
        val updateTime = df.getCurrentTimeAsString()
        for (rule in payDayBudgetRuleList) {
            val endDate = if (rule.budEndDate!! > stopDate) stopDate else rule.budEndDate
            val payDates = projectBudgetDates.projectDates(
                startDate = rule.budStartDate,
                endDate = endDate,
                interval = rule.budFrequencyCount.toLong(),
                intervalTypeId = rule.budFrequencyTypeId,
                dayOfWeekId = rule.budDayOfWeekId,
                leadDays = rule.budLeadDays.toLong()
            )
            for (date in payDates) {
                insertOrRewriteBudgetItem(
                    rule, date.toString(), date.toString(), date.toString(), updateTime
                )
            }
        }
    }

    private suspend fun updateBudgetItemsFallingOnPaydays(
        rulesOnPayDay: ArrayList<BudgetRule>, stopDate: String, payDays: List<String>
    ) {
        val updateTime = df.getCurrentTimeAsString()
        for (rule in rulesOnPayDay) {
            val endDate = if (rule.budEndDate!! > stopDate) stopDate else rule.budEndDate
            val payDates = projectBudgetDates.projectOnPayDay(
                rule.budStartDate, rule.budFrequencyCount.toLong(), payDays, endDate
            )
            for (date in payDates) {
                insertOrRewriteBudgetItem(
                    rule, date.toString(), date.toString(), date.toString(), updateTime
                )
            }
        }
    }

    private suspend fun updateAllOtherBudgetItems(
        rulesOther: ArrayList<BudgetRule>, stopDate: String
    ) {
        val updateTime = df.getCurrentTimeAsString()
        val payDays = budgetItemViewModel.getPayDaysActive()
        if (payDays.isEmpty()) return

        for (rule in rulesOther) {
            val endDate = if (rule.budEndDate!! > stopDate) stopDate else rule.budEndDate
            val payDates = projectBudgetDates.projectDates(
                startDate = rule.budStartDate,
                endDate = endDate,
                interval = rule.budFrequencyCount.toLong(),
                intervalTypeId = rule.budFrequencyTypeId,
                dayOfWeekId = rule.budDayOfWeekId,
                leadDays = rule.budLeadDays.toLong()
            )
            for (date in payDates) {
                val assignedPayDay = findPayDayForDate(date, payDays)
                if (assignedPayDay != null) {
                    insertOrRewriteBudgetItem(
                        rule, date.toString(), date.toString(), assignedPayDay, updateTime
                    )
                }
            }
        }
    }

    private fun findPayDayForDate(date: LocalDate, payDays: List<String>): String? {
        var lastPayDay: String? = null
        for (payDayStr in payDays) {
            val payDay = LocalDate.parse(payDayStr)
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
        } catch (e: SQLiteConstraintException) {
            // Row exists, rewrite it
            Log.d(TAG, "THE BUDGET ITEM WAS NOT ADDED - rewriting now.\n $e")
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
            if (!rule.budIsPayDay && rule.budFrequencyTypeId != 3) {
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
        } catch (e: Exception) {
            Log.e(TAG, "An unknown error occurred", e)
            return false
        }
        return true
    }

    private fun purgeOldItems(): Boolean {
        try {
            val cutoffDate = LocalDate.now().minusMonths(2).toString()
            budgetItemViewModel.purgeOldBudgetItems(cutoffDate)
        } catch (e: Exception) {
            Log.e(TAG, "An unknown error occurred", e)
            return false
        }
        return true
    }
}