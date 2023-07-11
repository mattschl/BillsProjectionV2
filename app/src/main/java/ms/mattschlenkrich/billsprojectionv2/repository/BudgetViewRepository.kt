package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.BudgetView

class BudgetViewRepository(private val db: BillsDatabase) {

    suspend fun insertBudgetView(budgetView: BudgetView) =
        db.getBudgetViewDao().insertBudgetView(budgetView)

    suspend fun updateBudgetView(budgetView: BudgetView) =
        db.getBudgetViewDao().updateBudgetView(budgetView)
}