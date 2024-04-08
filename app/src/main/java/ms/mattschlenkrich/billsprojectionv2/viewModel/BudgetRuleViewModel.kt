package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository

class BudgetRuleViewModel(
    app: Application,
    private val budgetRuleRepository: BudgetRuleRepository
) : AndroidViewModel(app) {

    fun insertBudgetRule(budgetRule: BudgetRule) =
        viewModelScope.launch {
            budgetRuleRepository.insertBudgetRule(budgetRule)
        }

    fun updateBudgetRule(budgetRule: BudgetRule) =
        viewModelScope.launch {
            budgetRuleRepository.updateBudgetRule(budgetRule)
        }

    fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        viewModelScope.launch {
            budgetRuleRepository.deleteBudgetRule(budgetRuleId, updateTime)
        }

    suspend fun getBudgetRulesActive() =
        budgetRuleRepository.getBudgetRulesActive()

    fun getActiveBudgetRulesDetailed() =
        budgetRuleRepository.getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        budgetRuleRepository.searchBudgetRules(query)

    fun getBudgetRuleNameList() =
        budgetRuleRepository.getBudgetRuleNameList()

    fun getBudgetRuleDetailed(ruleId: Long) =
        budgetRuleRepository.getBudgetRuleDetailed(ruleId)

    fun getBudgetRuleFullLive(ruleId: Long) =
        budgetRuleRepository.getBudgetRuleFullLive(ruleId)

    fun getBudgetRulesMonthly(today: String) =
        budgetRuleRepository.getBudgetRulesMonthly(today)

    fun getBudgetRulesCompleteMonthly(today: String) =
        budgetRuleRepository.getBudgetRulesCompleteMonthly(today)

    fun getBudgetRulesCompletedOccasional(today: String) =
        budgetRuleRepository.getBudgetRulesCompletedOccasional(today)

    fun getBudgetRulesCompletedAnnually(today: String) =
        budgetRuleRepository.getBudgetRulesCompletedAnnually(today)
}