package ms.mattschlenkrich.billsprojectionv2.projections

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    private var budgetRuleViewModel: BudgetRuleViewModel
    private var budgetItemViewModel: BudgetItemViewModel
    private val df = DateFunctions()
    private val projectBudgetDates = ProjectBudgetDates(mainActivity)

    init {
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
    }

    fun updatePredictions(stopDate: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val deleted = async {
                budgetItemViewModel.deleteFutureBudgetItems(
                    df.getCurrentDateAsString(),
                    df.getCurrentTimeAsString()
                )
            }
            if (deleted.await().isCompleted) {
                val budgetRulesList = async {
                    budgetRuleViewModel.getBudgetRulesActive(
                        df.getCurrentDateAsString()
                    )
                }
                val budgetRules = budgetRulesList.await()
                Log.d(TAG, "budgetRules size is ${budgetRules.size}")
                for (i in budgetRules.indices) {
                    if (budgetRules[i].budIsPayDay) {
                        fillPayDays(
                            budgetRules[i],
                            stopDate
                        )
                    } else {
                        break
                    }
                }
                val payDays = async {
                    budgetItemViewModel.getPayDaysActive(
                        df.getCurrentDateAsString()
                    )
                }
                val payDayDates = payDays.await()
                for (i in budgetRules.indices) {
                    val endDate =
                        if (stopDate < budgetRules[i].budEndDate!!) {
                            stopDate
                        } else {
                            budgetRules[i].budEndDate!!
                        }
                    if (!budgetRules[i].budIsPayDay) {
                        val projectedDates =
                            projectBudgetDates.projectDates(
                                budgetRules[i].budStartDate,
                                endDate,
                                budgetRules[i].budFrequencyCount.toLong(),
                                budgetRules[i].budFrequencyTypeId,
                                budgetRules[i].budDayOfWeekId,
                                budgetRules[i].budLeadDays.toLong()
                            )
                        for (a in 0 until projectedDates.size) {
                            for (p in 0 until payDayDates.size - 1) {
                                if (projectedDates[a] >= LocalDate.parse(payDayDates[p]) &&
                                    projectedDates[a] < LocalDate.parse(payDayDates[p + 1])
                                ) {
                                    fillOthers(
                                        budgetRules[i],
                                        projectedDates[a].toString(),
                                        payDayDates[p]
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fillOthers(
        budgetRule: BudgetRule,
        projectedDate: String,
        payDay: String
    ) {
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
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        )
    }

    private fun fillPayDays(budgetRule: BudgetRule, stopDate: String) {
        val endDate =
            if (stopDate < budgetRule.budEndDate!!) {
                stopDate
            } else {
                budgetRule.budEndDate
            }
        Log.d(
            TAG,
            "Attempting to update the budgetItems StartDate is ${budgetRule.budStartDate}, endDate is $endDate"
        )
        val payDates =
            projectBudgetDates.projectDates(
                budgetRule.budStartDate,
                endDate,
                budgetRule.budFrequencyTypeId.toLong(),
                budgetRule.budFrequencyCount,
                budgetRule.budDayOfWeekId,
                budgetRule.budLeadDays.toLong()
            )
        Log.d(TAG, "payDates size is ${payDates.size}")
        for (a in 0 until payDates.size) {
            Log.d(TAG, "in fillPayDays, payDate is ${payDates[a]}")
            budgetItemViewModel.insertBudgetItem(
                BudgetItem(
                    budgetRule.ruleId,
                    payDates[a].toString(),
                    payDates[a].toString(),
                    payDates[a].toString(),
                    budgetRule.budgetRuleName,
                    true,
                    budgetRule.budToAccountId,
                    budgetRule.budFromAccountId,
                    budgetRule.budgetAmount,
                    false,
                    budgetRule.budFixedAmount,
                    budgetRule.budFixedAmount,
                    biManuallyEntered = false,
                    biIsCompleted = false,
                    biIsCancelled = false,
                    biIsDeleted = false,
                    biUpdateTime = df.getCurrentTimeAsString()
                )
            )
        }
    }
}
