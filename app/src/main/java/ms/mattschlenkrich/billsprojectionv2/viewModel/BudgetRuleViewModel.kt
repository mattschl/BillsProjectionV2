package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository

class BudgetRuleViewModel(
    app: Application,
    private val budgetRuleRepository: BudgetRuleRepository
) : AndroidViewModel(app) {

    fun insertBudgetRule(
        budgetRuleName: String, amount: Double,
        toAccount: String, fromAccount: String,
        fixedAmount: Int, isPayDay: Int,
        isAutoPayment: Int, startDate: String,
        endDate: String, frequencyTypeId: Long,
        frequencyCount: Int, dayOfWeekId: Long,
        leadDays: Int, updateTime: String
    ) =
        viewModelScope.launch {
            budgetRuleRepository.insertBudgetRule(
                budgetRuleName, amount, toAccount, fromAccount,
                fixedAmount, isPayDay, isAutoPayment, startDate,
                endDate, frequencyTypeId, frequencyCount, dayOfWeekId,
                leadDays, updateTime
            )
        }

    fun updateBudgetRule(budgetRule: BudgetRule) =
        viewModelScope.launch {
            budgetRuleRepository.updateBudgetRule(budgetRule)
        }

    fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        viewModelScope.launch {
            budgetRuleRepository.deleteBudgetRule(budgetRuleId, updateTime)
        }

    fun getActiveBudgetRulesDetailed() =
        budgetRuleRepository.getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        budgetRuleRepository.searchBudgetRules(query)
}