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
}