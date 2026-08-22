package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.common.functions.BudgetLogic
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetRuleRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

//    fun getBudgetRulesMonthly(today: String) =
//        budgetRuleRepository.getBudgetRulesMonthly(today)

    fun getBudgetRulesCompleteMonthly(today: String) =
        budgetRuleRepository.getBudgetRulesCompleteMonthly(today)

    fun getBudgetRulesCompletedOccasional(today: String) =
        budgetRuleRepository.getBudgetRulesCompletedOccasional(today)

    fun getBudgetRulesCompletedAnnually(today: String) =
        budgetRuleRepository.getBudgetRulesCompletedAnnually(today)

    fun calculateSuggestedAmount(
        rule: BudgetRule,
        transactionViewModel: TransactionViewModel
    ): Double? {
        val today = LocalDate.now()
        val yearAgo = today.minusYears(1)
        val calcStart =
            if (rule.budStartDate > yearAgo.toString()) rule.budStartDate else yearAgo.toString()
        val totalSum =
            transactionViewModel.getSumTransactionByBudgetRuleSync(
                rule.ruleId, calcStart, today.toString()
            ) ?: 0.0
        val totalCount =
            transactionViewModel.getCountTransactionByBudgetRuleSync(
                rule.ruleId, calcStart, today.toString()
            )
        val startDate = try {
            LocalDate.parse(calcStart)
        } catch (e: Exception) {
            today
        }
        val daysElapsed = ChronoUnit.DAYS.between(
            startDate,
            today
        )

        return BudgetLogic.calculateSuggestedAmount(
            totalSum = totalSum,
            totalCount = totalCount,
            daysElapsed = daysElapsed,
            frequencyTypeId = rule.budFrequencyTypeId,
            frequencyCount = rule.budFrequencyCount
        )
    }
}