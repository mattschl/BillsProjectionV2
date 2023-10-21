package ms.mattschlenkrich.billsprojectionv2.projections

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
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
                val payDayRuleList =
                    getPayDayRules(budgetRules)
                if (payDayRuleList.isNotEmpty()) {
                    for (rule in payDayRuleList) {
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
                                insertRule(
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
                    getBudgetRulesOnPayDay(budgetRules)
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
                                    insertRule(
                                        rule,
                                        date.toString()
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
                                            insertRule(
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

    private fun getBudgetRulesOnPayDay(
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

    private fun getPayDayRules(
        budgetRules: List<BudgetRule>
    ): List<BudgetRule> {
        val payDayRuleList = ArrayList<BudgetRule>()
        for (rule in budgetRules) {
            if (rule.budIsPayDay) {
                payDayRuleList.add(rule)
            }
        }
        return payDayRuleList
    }

    private fun deleteFutureItems(): Boolean {
        budgetItemViewModel.deleteFutureBudgetItems(
            df.getCurrentDateAsString(),
            df.getCurrentTimeAsString()
        )
        return true
    }

    private fun insertRule(rule: BudgetRule, projectedDate: String): Boolean {
        budgetItemViewModel.insertBudgetItem(
            BudgetItem(
                rule.ruleId,
                projectedDate,
                projectedDate,
                projectedDate,
                rule.budgetRuleName,
                rule.budIsPayDay,
                rule.budToAccountId,
                rule.budFromAccountId,
                rule.budgetAmount,
                false,
                rule.budFixedAmount,
                rule.budIsAutoPay,
                biManuallyEntered = false,
                biLocked = false,
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        )
        return true
    }

    private fun insertRule(
        rule: BudgetRule,
        projectedDate: String,
        payDay: String
    ): Boolean {
        budgetItemViewModel.insertBudgetItem(
            BudgetItem(
                rule.ruleId,
                projectedDate,
                projectedDate,
                payDay,
                rule.budgetRuleName,
                rule.budIsPayDay,
                rule.budToAccountId,
                rule.budFromAccountId,
                rule.budgetAmount,
                false,
                rule.budFixedAmount,
                rule.budIsAutoPay,
                biManuallyEntered = false,
                biLocked = false,
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        )
        return true
    }
}
