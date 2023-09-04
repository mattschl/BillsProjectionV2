package ms.mattschlenkrich.billsprojectionv2.projections

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        CoroutineScope(Dispatchers.IO) {
            val
        }
        //2. get a list of budget rules
//        var budgetRuleList: List<BudgetRule>
//        CoroutineScope(Dispatchers.IO).launch {
//            val budgetRules =
//                async {
//                    budgetRuleViewModel.getBudgetRulesActive()
//                }
//            budgetRuleList = budgetRules.await()
//        }
//        if (budgetRuleList.isNotEmpty())
        //3. find only those rules that are paydays and process them first


        //4. get a list of paydays

        //5. find only those rules that fall on a payday and process them

        //6. process the rest of the rules and assign paydays
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
