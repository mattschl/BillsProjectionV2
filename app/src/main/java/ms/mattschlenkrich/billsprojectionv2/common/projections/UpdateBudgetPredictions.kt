package ms.mattschlenkrich.billsprojectionv2.common.projections

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import java.time.LocalDate

private const val TAG = "UpdateBudgetItems"

class UpdateBudgetPredictions(
    mainActivity: MainActivity,
) {
    private var budgetRuleViewModel: BudgetRuleViewModel =
        mainActivity.budgetRuleViewModel
    private var budgetItemViewModel: BudgetItemViewModel =
        mainActivity.budgetItemViewModel
    private val df = DateFunctions()
    private val projectBudgetDates = ProjectBudgetDates(mainActivity)

    fun killPredictions(): Boolean {
        val startDate = LocalDate.now().minusWeeks(2).toString()
        budgetItemViewModel.killFutureBudgetItems(
            startDate,
            df.getCurrentTimeAsString()
        )
        return true
    }

    fun updatePredictions(stopDate: String) {
        //1. Delete the future dates not already locked
        val waitTime = 200L
        runBlocking {
            deleteFutureItems()
        }
        runBlocking {
            val budgetRules =
                budgetRuleViewModel.getBudgetRulesActive()
            if (budgetRules.isNotEmpty()) {
                val payDayBudgetRuleList =
                    getBudgetRuleListThatIsPayday(budgetRules)
                if (payDayBudgetRuleList.isNotEmpty()) {
                    for (rule in payDayBudgetRuleList) {
                        val endDate =
                            if (rule.budEndDate!! > stopDate) {
                                stopDate
                            } else {
                                rule.budEndDate
                            }
                        val payDates =
                            projectBudgetDates.projectDates(
                                rule.budStartDate,
                                endDate,
                                rule.budFrequencyCount.toLong(),
                                rule.budFrequencyTypeId,
                                rule.budDayOfWeekId,
                                rule.budLeadDays.toLong()
                            )
                        for (date in payDates) {
                            Log.d(TAG, "adding ${rule.budgetRuleName} date is $date")
                            runBlocking {
                                insertOrOverwriteBudgetItemFromBudgetRule(
                                    rule,
                                    date.toString()
                                )
                            }
                            runBlocking {
                                delay(waitTime)
                            }
                            runBlocking {
                                budgetItemViewModel.rewriteBudgetItem(
                                    rule.ruleId, date.toString(), date.toString(), date.toString(),
                                    rule.budgetRuleName, rule.budIsPayDay, rule.budToAccountId,
                                    rule.budFromAccountId, rule.budgetAmount, rule.budFixedAmount,
                                    rule.budIsAutoPay, df.getCurrentTimeAsString()
                                )
                            }
                        }
                    }
                }
                val rulesOnPayDay =
                    getBudgetRulesFallingOnPayDay(budgetRules)
                if (rulesOnPayDay.isNotEmpty()) {
                    Log.d(TAG, "before the delay ........")
                    delay(10000)
                    Log.d(TAG, "Delay is done")
                    val payDayList =
                        async {
                            budgetItemViewModel.getPayDaysActive()
                        }
                    val payDays =
                        payDayList.await()
                    if (payDayList.await().isNotEmpty()) {
                        for (rule in rulesOnPayDay) {
                            val endDate =
                                if (rule.budEndDate!! > stopDate) {
                                    stopDate
                                } else {
                                    rule.budEndDate
                                }
                            val payDates =
                                projectBudgetDates.projectOnPayDay(
                                    rule.budStartDate,
                                    rule.budFrequencyCount.toLong(),
                                    payDays,
                                    endDate
                                )
                            for (date in payDates) {
                                Log.d(TAG, "adding ${rule.budgetRuleName} date is $date")
                                runBlocking {
                                    insertOrOverwriteBudgetItemFromBudgetRule(
                                        rule,
                                        date.toString()
                                    )
                                }
                                runBlocking {
                                    delay(waitTime)
                                }
//                                runBlocking {
//                                    budgetItemViewModel.rewriteBudgetItem(
//                                        rule.ruleId,
//                                        date.toString(),
//                                        date.toString(),
//                                        date.toString(),
//                                        rule.budgetRuleName,
//                                        rule.budIsPayDay,
//                                        rule.budToAccountId,
//                                        rule.budFromAccountId,
//                                        rule.budgetAmount,
//                                        rule.budFixedAmount,
//                                        rule.budIsAutoPay,
//                                        df.getCurrentTimeAsString()
//                                    )
//                                }
                            }
                        }
                    }

                }
                val rulesOther =
                    getBudgetRulesOther(budgetRules)
                if (rulesOther.isNotEmpty()) {
                    for (rule in rulesOther) {
                        val endDate =
                            if (rule.budEndDate!! > stopDate) {
                                stopDate
                            } else {
                                rule.budEndDate
                            }
                        val payDates =
                            projectBudgetDates.projectDates(
                                rule.budStartDate,
                                endDate,
                                rule.budFrequencyCount.toLong(),
                                rule.budFrequencyTypeId,
                                rule.budDayOfWeekId,
                                rule.budLeadDays.toLong()
                            )
                        val payDayList =
                            async {
                                budgetItemViewModel.getPayDaysActive()
                            }
                        val payDays =
                            payDayList.await()
                        if (payDays.isNotEmpty()) {
                            for (date in payDates) {
                                for (d in 0 until payDays.size - 1) {
                                    if (date >= LocalDate.parse(payDays[d]) &&
                                        date < LocalDate.parse(payDays[d + 1])
                                    ) {
                                        Log.d(
                                            TAG,
                                            "adding ${rule.budgetRuleName} date is $date"
                                        )
                                        runBlocking {
                                            insertBudgetItemWithPayDay(
                                                rule,
                                                date.toString(),
                                                payDays[d]
                                            )
                                        }
                                        runBlocking {
                                            delay(waitTime)
                                        }
                                        runBlocking {
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
                }
            }
        }
    }

    private fun getBudgetRulesOther(
        budgetRules: List<BudgetRule>
    ): ArrayList<BudgetRule> {
        val ruleList = ArrayList<BudgetRule>()
        for (rule in budgetRules) {
            if (!rule.budIsPayDay &&
                rule.budFrequencyTypeId != 3
            ) {
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
//        val payDayRuleList = ArrayList<BudgetRule>()
//        for (rule in budgetRules) {
//            if (rule.budIsPayDay) {
//                payDayRuleList.add(rule)
//            }
//        }
//        return payDayRuleList
        return budgetRules.filter { budgetRule ->
            budgetRule.budIsPayDay
        }
    }

    private fun deleteFutureItems(): Boolean {
        budgetItemViewModel.deleteFutureBudgetItems(
            df.getCurrentDateAsString(),
            df.getCurrentTimeAsString()
        )
        return true
    }

    private fun insertOrOverwriteBudgetItemFromBudgetRule(
        budgetRule: BudgetRule,
        projectedDate: String
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
        try {
            budgetItemViewModel.insertBudgetItem(
                newBudgetItem
            )
            return true
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "exception was called - Exception is ${e.message}")
            budgetItemViewModel.overWriteBudgetItem(
                newBudgetItem
            )
            return false
        }
    }

    private fun insertBudgetItemWithPayDay(
        budgetRule: BudgetRule,
        projectedDate: String,
        payDay: String
    ): Boolean {
        try {
            budgetItemViewModel.insertBudgetItem(
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
        } catch (e: SQLiteConstraintException) {

            return false
        }
        return true
    }
}
