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

    fun insertBudgetRule(budgetRule: BudgetRule) =
        viewModelScope.launch {
            budgetRuleRepository.insertBudgetRule(
                budgetRule
            )
        }

    fun insertBudgetRule(
        ruleId: Long, budgetRuleName: String, amount: Double,
        toAccount: Long, fromAccount: Long,
        fixedAmount: Boolean, isPayDay: Boolean,
        isAutoPayment: Boolean, startDate: String,
        endDate: String, frequencyTypeId: Int,
        frequencyCount: Int, dayOfWeekId: Int,
        leadDays: Int, isDeleted: Boolean, updateTime: String
    ) =
        viewModelScope.launch {
            budgetRuleRepository.insertBudgetRule(
                ruleId, budgetRuleName, amount, toAccount, fromAccount,
                fixedAmount, isPayDay, isAutoPayment, startDate,
                endDate, frequencyTypeId, frequencyCount, dayOfWeekId,
                leadDays, isDeleted, updateTime
            )
        }

    fun updateBudgetRule(
        ruleId: Long,
        budgetRuleName: String,
        budgetAmount: Double,
        toAccountId: Long,
        fromAccountId: Long,
        fixedAmount: Boolean,
        isPayDay: Boolean,
        isAutoPayment: Boolean,
        startDate: String,
        endDate: String,
        frequencyTypeId: Int,
        frequencyCount: Int,
        dayOfWeekId: Int,
        leadDays: Int,
        isDeleted: Boolean,
        updateTime: String
    ) =
        viewModelScope.launch {
            budgetRuleRepository.updateBudgetRule(
                ruleId,
                budgetRuleName,
                budgetAmount,
                toAccountId,
                fromAccountId,
                fixedAmount,
                isPayDay,
                isAutoPayment,
                startDate,
                endDate,
                frequencyTypeId,
                frequencyCount,
                dayOfWeekId,
                leadDays,
                isDeleted,
                updateTime
            )
        }

    fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        viewModelScope.launch {
            budgetRuleRepository.deleteBudgetRule(budgetRuleId, updateTime)
        }

    fun getActiveBudgetRulesDetailed() =
        budgetRuleRepository.getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        budgetRuleRepository.searchBudgetRules(query)

    fun findBudgetRuleByName(query: String?) =
        budgetRuleRepository.findBudgetRuleByName(query)

    fun getBudgetRuleNameList() =
        budgetRuleRepository.getBudgetRuleNameList()
}