package ms.mattschlenkrich.billsprojectionv2.dataBase.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem

class BudgetItemRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetItem(budgetItem: BudgetItem) =
        db.getBudgetItemDao().insertBudgetItem(budgetItem)

    suspend fun updateBudgetItem(budgetItem: BudgetItem) =
        db.getBudgetItemDao().updateBudgetItem(budgetItem)

    suspend fun insertOrReplaceBudgetItem(budgetItem: BudgetItem) =
        db.getBudgetItemDao().insertOrReplaceBudgetItem(budgetItem)

    suspend fun deleteBudgetItem(
        budgetRulId: Long, projectedDate: String, updateTime: String
    ) = db.getBudgetItemDao().deleteBudgetItem(
        budgetRulId, projectedDate, updateTime
    )

    suspend fun killFutureBudgetItems(currentDate: String, updateTime: String) =
        db.getBudgetItemDao().killFutureBudgetItems(currentDate, updateTime)

    fun getPayDaysActive() = db.getBudgetItemDao().getPayDaysActive()

    suspend fun deleteFutureBudgetItems(
        currentDate: String, updateTime: String
    ) = db.getBudgetItemDao().deleteFutureBudgetItems(
        currentDate, updateTime
    )

    fun getAssetsForBudget() = db.getBudgetItemDao().getAssetsForBudget()

    fun getPayDays(asset: String) = db.getBudgetItemDao().getPayDays(asset)

    fun getBudgetItems(asset: String, payDay: String) =
        db.getBudgetItemDao().getBudgetItems(asset, payDay)

    fun getBudgetItems(budgetRuleId: Long) = db.getBudgetItemDao().getBudgetItems(budgetRuleId)

    fun getPayDays() = db.getBudgetItemDao().getPayDays()

    suspend fun cancelBudgetItem(
        budgetRuleId: Long, projectedDate: String, updateTime: String
    ) = db.getBudgetItemDao().cancelBudgetItem(
        budgetRuleId, projectedDate, updateTime
    )

    fun rewriteBudgetItem(
        budgetRuleId: Long,
        projectedDate: String,
        actualDate: String,
        payDay: String,
        budgetName: String,
        isPayDay: Boolean,
        toAccountId: Long,
        fromAccountId: Long,
        projectedAmount: Double,
        isFixed: Boolean,
        isAutomatic: Boolean,
        updateTime: String
    ) = db.getBudgetItemDao().rewriteBudgetItem(
        budgetRuleId,
        projectedDate,
        actualDate,
        payDay,
        budgetName,
        isPayDay,
        toAccountId,
        fromAccountId,
        projectedAmount,
        isFixed,
        isAutomatic,
        updateTime
    )

    suspend fun lockUnlockBudgetItem(
        lock: Boolean, budgetRuleId: Long, payDay: String, updateTime: String
    ) = db.getBudgetItemDao().lockUnlockBudgetItem(
        lock, budgetRuleId, payDay, updateTime
    )

    suspend fun lockUnlockBudgetItem(
        lock: Boolean, payDay: String, updateTime: String
    ) = db.getBudgetItemDao().lockUnlockBudgetItem(
        lock, payDay, updateTime
    )
}