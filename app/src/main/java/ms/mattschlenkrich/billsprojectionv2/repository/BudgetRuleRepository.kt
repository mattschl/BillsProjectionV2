package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule

class BudgetRuleRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetRule(budgetRule: BudgetRule) =
        db.getBudgetRuleDao().insertBudgetRule(budgetRule)

    suspend fun insertBudgetRule(
        ruleId: Long, budgetRuleName: String, amount: Double,
        toAccount: Long, fromAccount: Long,
        fixedAmount: Boolean, isPayDay: Boolean,
        isAutoPayment: Boolean, startDate: String,
        endDate: String, frequencyTypeId: Int,
        frequencyCount: Int, dayOfWeekId: Int,
        leadDays: Int, isDeleted: Boolean, updateTime: String
    ) =
        db.getBudgetRuleDao().insertBudgetRule(
            ruleId, budgetRuleName, amount, toAccount, fromAccount,
            fixedAmount, isPayDay, isAutoPayment, startDate,
            endDate, frequencyTypeId, frequencyCount, dayOfWeekId,
            leadDays, isDeleted, updateTime
        )

    suspend fun updateBudgetRule(
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
        db.getBudgetRuleDao().updateBudgetRule(
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

    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        db.getBudgetRuleDao().deleteBudgetRule(budgetRuleId, updateTime)

    fun getActiveBudgetRulesDetailed() =
        db.getBudgetRuleDao().getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        db.getBudgetRuleDao().searchBudgetRules(query)

    fun findBudgetRuleByName(query: String?) =
        db.getBudgetRuleDao().findBudgetRuleByName(query)

    fun getBudgetRuleNameList() =
        db.getBudgetRuleDao().getBudgetRuleNameList()
}