package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule

class BudgetRuleRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetRule(
        budgetRuleName: String, amount: Double,
        toAccount: String, fromAccount: String,
        fixedAmount: Int, isPayDay: Int,
        isAutoPayment: Int, startDate: String,
        endDate: String, frequencyTypeId: Long,
        frequencyCount: Int, dayOfWeekId: Long,
        leadDays: Int, updateTime: String
    ) =
        db.getBudgetRuleDao().insertBudgetRule(
            budgetRuleName, amount, toAccount, fromAccount,
            fixedAmount, isPayDay, isAutoPayment, startDate,
            endDate, frequencyTypeId, frequencyCount, dayOfWeekId,
            leadDays, updateTime
        )

    suspend fun updateBudgetRule(budgetRule: BudgetRule) =
        db.getBudgetRuleDao().updateBudgetRule(budgetRule)

    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        db.getBudgetRuleDao().deleteBudgetRule(budgetRuleId, updateTime)

    fun getActiveBudgetRulesDetailed() =
        db.getBudgetRuleDao().getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        db.getBudgetRuleDao().searchBudgetRules(query)
}