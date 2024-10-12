package ms.mattschlenkrich.billsprojectionv2.dataBase.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

class BudgetRuleRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetRule(budgetRule: BudgetRule) =
        db.getBudgetRuleDao().insertBudgetRule(budgetRule)

    suspend fun updateBudgetRule(budgetRule: BudgetRule) =
        db.getBudgetRuleDao().updateBudgetRule(budgetRule)

    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        db.getBudgetRuleDao().deleteBudgetRule(budgetRuleId, updateTime)

    suspend fun getBudgetRulesActive() =
        db.getBudgetRuleDao().getBudgetRulesActive()

    fun getActiveBudgetRulesDetailed() =
        db.getBudgetRuleDao().getActiveBudgetRulesDetailed()

    fun searchBudgetRules(query: String?) =
        db.getBudgetRuleDao().searchBudgetRules(query)

    fun getBudgetRuleNameList() =
        db.getBudgetRuleDao().getBudgetRuleNameList()

    fun getBudgetRuleDetailed(ruleId: Long) =
        db.getBudgetRuleDao().getBudgetRuleDetailed(ruleId)

    fun getBudgetRuleFullLive(ruleId: Long) =
        db.getBudgetRuleDao().getBudgetRuleFullLive(ruleId)

    fun getBudgetRulesMonthly(today: String) =
        db.getBudgetRuleDao().getBudgetRulesMonthly(today)

    fun getBudgetRulesCompleteMonthly(today: String) =
        db.getBudgetRuleDao().getBudgetRulesCompletedMonthly(today)

    fun getBudgetRulesCompletedOccasional(today: String) =
        db.getBudgetRuleDao().getBudgetRulesCompletedOccasional(today)

    fun getBudgetRulesCompletedAnnually(today: String) =
        db.getBudgetRuleDao().getBudgetRulesCompletedAnnually(today)
}