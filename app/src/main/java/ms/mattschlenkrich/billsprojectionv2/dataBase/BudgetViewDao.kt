package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Insert
import ms.mattschlenkrich.billsprojectionv2.model.BudgetView

interface BudgetViewDao {
    @Insert
    suspend fun insertBudgetView(budgetView: BudgetView)
}