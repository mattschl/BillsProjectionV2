package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem

@Dao
interface BudgetItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudgetItem(budgetItem: BudgetItem)

    @Update
    suspend fun updateBudgetItem(budgetItem: BudgetItem)
}