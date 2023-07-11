package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.model.BudgetView

@Dao
interface BudgetViewDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudgetView(budgetView: BudgetView)

    @Update
    suspend fun updateBudgetView(budgetView: BudgetView)
}