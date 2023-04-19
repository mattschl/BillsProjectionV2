package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.DaysOfWeek
import ms.mattschlenkrich.billsprojectionv2.model.FrequencyTypes
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
        endDate: String, frequencyType: String,
        frequencyCount: Int, dayOfWeek: String,
        leadDays: Int, updateTime: String
    ) =
        viewModelScope.launch {
            budgetRuleRepository.insertBudgetRule(
                budgetRuleName, amount, toAccount, fromAccount,
                fixedAmount, isPayDay, isAutoPayment, startDate,
                endDate, frequencyType, frequencyCount, dayOfWeek,
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

    fun getActiveBudgetRules() =
        budgetRuleRepository.getActiveBudgetRules()

    fun searchBudgetRules(query: String?) =
        budgetRuleRepository.searchBudgetRules(query)

    fun insertFrequencyType(frequencyType: FrequencyTypes) =
        viewModelScope.launch {
            budgetRuleRepository.insertFrequencyType(frequencyType)
        }

    fun updateFrequencyType(frequencyType: FrequencyTypes) =
        viewModelScope.launch {
            budgetRuleRepository.updateFrequencyType(frequencyType)
        }

    fun deleteFrequencyType(frequencyType: FrequencyTypes) =
        viewModelScope.launch {
            budgetRuleRepository.deleteFrequencyType(frequencyType)
        }

    fun getFrequencyTypes() =
        budgetRuleRepository.getFrequencyTypes()

    fun findFrequencyType(frequencyId: Long) =
        budgetRuleRepository.findFrequencyType(frequencyId)

    fun insertDayOfWeek(daysOfWeek: DaysOfWeek) =
        viewModelScope.launch {
            budgetRuleRepository.insertDayOfWeek(daysOfWeek)
        }

    fun updateDayOfWeek(daysOfWeek: DaysOfWeek) =
        viewModelScope.launch {
            budgetRuleRepository.updateDayOfWeek(daysOfWeek)
        }

    fun deleteDayOfWeek(daysOfWeek: DaysOfWeek) =
        viewModelScope.launch {
            budgetRuleRepository.deleteDayOfWeek(daysOfWeek)
        }

    fun getDaysOfWeek() =
        budgetRuleRepository.getDaysOfWeek()

    fun findDayOfWeek(dayOfWeekId: Long) =
        budgetRuleRepository.findDayOfWeek(dayOfWeekId)
}