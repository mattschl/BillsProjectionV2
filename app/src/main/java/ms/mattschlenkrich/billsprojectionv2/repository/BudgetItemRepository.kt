package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem

class BudgetItemRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetItem(budgetItem: BudgetItem) =
        db.getBudgetItemDao().insertBudgetItem(budgetItem)

    suspend fun updateBudgetItem(budgetItem: BudgetItem) =
        db.getBudgetItemDao().updateBudgetItem(budgetItem)

    suspend fun deleteBudgetItem(
        budgetRulId: Long,
        projectedDate: String,
        updateTime: String
    ) =
        db.getBudgetItemDao().deleteBudgetItem(
            budgetRulId,
            projectedDate,
            updateTime
        )

    suspend fun updatePayDay(
        payDay: String,
        updateTime: String,
        budgetRuleId: Long,
        projectedDate: String
    ) = db.getBudgetItemDao().updatePayDay(
        payDay, updateTime, budgetRuleId, projectedDate
    )

    fun getPayDaysActive() =
        db.getBudgetItemDao().getPayDaysActive()

    suspend fun deleteFutureBudgetItems(
        currentDate: String,
        updateTime: String
    ) = db.getBudgetItemDao().deleteFutureBudgetItems(
        currentDate, updateTime
    )

    fun getAssetsForBudget() =
        db.getBudgetItemDao().getAssetsForBudget()

    fun getPayDays(asset: String) =
        db.getBudgetItemDao().getPayDays(asset)

    fun getBudgetItems(asset: String, payDay: String) =
        db.getBudgetItemDao().getBudgetItems(asset, payDay)
}