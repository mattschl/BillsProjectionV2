package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.BI_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.BI_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.BI_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.BI_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem

@Dao
interface BudgetItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudgetItem(budgetItem: BudgetItem)

    @Update
    suspend fun updateBudgetItem(budgetItem: BudgetItem)

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BI_IS_DELETED = 1, " +
                "$BI_UPDATE_TIME = :updateTime, " +
                "$BI_BUDGET_RULE_ID = :budgetRulId " +
                "AND $BI_PROJECTED_DATE = :projectedDate"
    )
    suspend fun deleteBudgetItem(
        budgetRulId: Long,
        projectedDate: String,
        updateTime: String
    )
}