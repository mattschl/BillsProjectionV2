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

    fun killPredictions(): Boolean {
        try {
            val startDate = LocalDate.now().minusWeeks(2).toString()
            budgetItemViewModel.killFutureBudgetItems(
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
        for (rule in payDayBudgetRuleList) {
            val endDate = if (rule.budEndDate!! > stopDate) {
                stopDate
            } else {
                rule.budEndDate
            }
            val payDates = projectBudgetDates.projectDates(
                rule.budStartDate,
                endDate,
                rule.budFrequencyCount.toLong(),
                rule.budFrequencyTypeId,
                rule.budDayOfWeekId,
                rule.budLeadDays.toLong()
            )
            for (date in payDates) {
                insertOrOverwriteBudgetItemFromBudgetRule(
                    rule, date.toString()
                )
                budgetItemViewModel.rewriteBudgetItem(
                    rule.ruleId,
                    date.toString(),
                    date.toString(),
                    date.toString(),
                    rule.budgetRuleName,
                    rule.budIsPayDay,
                    rule.budToAccountId,
                    rule.budFromAccountId,
                    rule.budgetAmount,
                    rule.budFixedAmount,
                    rule.budIsAutoPay,
                    df.getCurrentTimeAsString()
                )
            }
        }
    }

    private suspend fun updateBudgetItemsFallingOnPaydays(
        rulesOnPayDay: ArrayList<BudgetRule>, stopDate: String, payDays: List<String>
    ) {
        for (rule in rulesOnPayDay) {
            val endDate = if (rule.budEndDate!! > stopDate) {
                stopDate
            } else {
                rule.budEndDate
            }
            val payDates = projectBudgetDates.projectOnPayDay(
                rule.budStartDate, rule.budFrequencyCount.toLong(), payDays, endDate
            )
            for (date in payDates) {
                insertOrOverwriteBudgetItemFromBudgetRule(
                    rule, date.toString()
                )
            }
        }
    }

    private suspend fun updateAllOtherBudgetItems(
        rulesOther: ArrayList<BudgetRule>, stopDate: String
    ) {
        for (rule in rulesOther) {
            val endDate = if (rule.budEndDate!! > stopDate) {
                stopDate
            } else {
                rule.budEndDate
            }
            val payDates = projectBudgetDates.projectDates(
                rule.budStartDate,
                endDate,
                rule.budFrequencyCount.toLong(),
                rule.budFrequencyTypeId,
                rule.budDayOfWeekId,
                rule.budLeadDays.toLong()
            )
            val payDays = budgetItemViewModel.getPayDaysActive()
            if (payDays.isNotEmpty()) {
                for (date in payDates) {
                    for (d in 0 until payDays.size - 1) {
                        if (date >= LocalDate.parse(payDays[d]) && date < LocalDate.parse(
                                payDays[d + 1]
                            )
                        ) {
                            insertBudgetItemWithPayDay(
                                rule, date.toString(), payDays[d]
                            )
                            budgetItemViewModel.rewriteBudgetItem(
                                rule.ruleId,
                                date.toString(),
                                date.toString(),
                                payDays[d],
                                rule.budgetRuleName,
                                rule.budIsPayDay,
                                rule.budToAccountId,
                                rule.budFromAccountId,
                                rule.budgetAmount,
                                rule.budFixedAmount,
                                rule.budIsAutoPay,
                                df.getCurrentTimeAsString()
                            )
                        }
                    }
                }
            }
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

    private fun deleteEligibleFutureItems(): Boolean {
        try {
            budgetItemViewModel.deleteFutureBudgetItems(
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

    private suspend fun insertOrOverwriteBudgetItemFromBudgetRule(
        budgetRule: BudgetRule, projectedDate: String
    ): Boolean {
        val newBudgetItem = BudgetItem(
            budgetRule.ruleId,
            projectedDate,
            projectedDate,
            projectedDate,
            budgetRule.budgetRuleName,
            budgetRule.budIsPayDay,
            budgetRule.budToAccountId,
            budgetRule.budFromAccountId,
            budgetRule.budgetAmount,
            false,
            budgetRule.budFixedAmount,
            budgetRule.budIsAutoPay,
            biManuallyEntered = false,
            biLocked = false,
            biIsCompleted = false,
            biIsCancelled = false,
            biIsDeleted = false,
            biUpdateTime = df.getCurrentTimeAsString()
        )
        return try {
            budgetItemViewModel.insertBudgetItemSync(
                newBudgetItem
            )
            true
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "Try next item", e)
            budgetItemViewModel.insertOrReplaceBudgetItemSync(
                newBudgetItem
            )
            false
        }
    }

    private suspend fun insertBudgetItemWithPayDay(
        budgetRule: BudgetRule, projectedDate: String, payDay: String
    ): Boolean {
        return try {
            budgetItemViewModel.insertBudgetItemSync(
                BudgetItem(
                    budgetRule.ruleId,
                    projectedDate,
                    projectedDate,
                    payDay,
                    budgetRule.budgetRuleName,
                    budgetRule.budIsPayDay,
                    budgetRule.budToAccountId,
                    budgetRule.budFromAccountId,
                    budgetRule.budgetAmount,
                    false,
                    budgetRule.budFixedAmount,
                    budgetRule.budIsAutoPay,
                    biManuallyEntered = false,
                    biLocked = false,
                    biIsCompleted = false,
                    biIsCancelled = false,
                    biIsDeleted = false,
                    biUpdateTime = df.getCurrentTimeAsString()
                )
            )
            true
        } catch (e: SQLiteConstraintException) {
            false
        }
    }
}