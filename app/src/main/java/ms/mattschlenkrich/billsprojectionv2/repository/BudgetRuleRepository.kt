package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.DaysOfWeek
import ms.mattschlenkrich.billsprojectionv2.model.FrequencyTypes

class BudgetRuleRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetRule(
        budgetRuleName: String, amount: Double,
        toAccount: String, fromAccount: String,
        fixedAmount: Int, isPayDay: Int,
        isAutoPayment: Int, startDate: String,
        endDate: String, frequencyType: String,
        frequencyCount: Int, dayOfWeek: String,
        leadDays: Int, updateTime: String
    ) =
        db.getBudgetRuleDao().insertBudgetRule(
            budgetRuleName, amount, toAccount, fromAccount,
            fixedAmount, isPayDay, isAutoPayment, startDate,
            endDate, frequencyType, frequencyCount, dayOfWeek,
            leadDays, updateTime
        )

    suspend fun updateBudgetRule(budgetRule: BudgetRule) =
        db.getBudgetRuleDao().updateBudgetRule(budgetRule)

    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String) =
        db.getBudgetRuleDao().deleteBudgetRule(budgetRuleId, updateTime)

    fun getActiveBudgetRules() =
        db.getBudgetRuleDao().getActiveBudgetRules()

    fun searchBudgetRules(query: String?) =
        db.getBudgetRuleDao().searchBudgetRules(query)

    suspend fun insertFrequencyType(frequencyType: FrequencyTypes) =
        db.getBudgetRuleDao().insertFrequencyType(frequencyType)

    suspend fun updateFrequencyType(frequencyType: FrequencyTypes) =
        db.getBudgetRuleDao().updateFrequencyType(frequencyType)

    suspend fun deleteFrequencyType(frequencyType: FrequencyTypes) =
        db.getBudgetRuleDao().deleteFrequencyType(frequencyType)

    fun getFrequencyTypes() =
        db.getBudgetRuleDao().getFrequencyTypes()

    fun findFrequencyType(frequencyId: Long) =
        db.getBudgetRuleDao().findFrequencyType(frequencyId)

    suspend fun insertDayOfWeek(daysOfWeek: DaysOfWeek) =
        db.getBudgetRuleDao().insertDayOfWeek(daysOfWeek)

    suspend fun updateDayOfWeek(daysOfWeek: DaysOfWeek) =
        db.getBudgetRuleDao().updateDayOfWeek(daysOfWeek)

    suspend fun deleteDayOfWeek(daysOfWeek: DaysOfWeek) =
        db.getBudgetRuleDao().deleteDayOfWeek(daysOfWeek)

    fun getDaysOfWeek() =
        db.getBudgetRuleDao().getDaysOfWeek()

    fun findDayOfWeek(dayOfWeekId: Long) =
        db.getBudgetRuleDao().findDayOfWeek(dayOfWeekId)
}