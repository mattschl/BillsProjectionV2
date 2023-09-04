package ms.mattschlenkrich.billsprojectionv2.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel

private const val TAG = "UpdateBudgetItems"

class UpdateBudgetPredictions(
    mainActivity: MainActivity,
) {
    private var budgetRuleViewModel: BudgetRuleViewModel = mainActivity.budgetRuleViewModel
    private var budgetItemViewModel: BudgetItemViewModel = mainActivity.budgetItemViewModel
    private val df = DateFunctions()
    private val projectBudgetDates = ProjectBudgetDates(mainActivity)


    fun updatePredictions(stopDate: String) {
        //1. Delete the future dates not already locked
        runBlocking(Dispatchers.IO) {
            launch {
                budgetItemViewModel.deleteFutureBudgetItems(
                    df.getCurrentDateAsString(),
                    df.getCurrentTimeAsString()
                )
            }
        }

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
}
