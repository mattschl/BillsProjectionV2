package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.BI_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_MANUALLY_ENTERED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.common.BI_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BI_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem

@Dao
interface BudgetItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BI_PAY_DAY = :payDay," +
                "$BI_UPDATE_TIME = :updateTime " +
                "WHERE $BI_BUDGET_RULE_ID = :budgetRuleId " +
                "AND $BI_PROJECTED_DATE = :projectedDate;"
    )
    suspend fun updatePayDay(
        payDay: String,
        updateTime: String,
        budgetRuleId: Long,
        projectedDate: String
    )

    @Query(
        "SELECT DISTINCT $BI_PROJECTED_DATE FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BI_IS_PAY_DAY_ITEM = 1 " +
                "AND $BI_IS_DELETED = 0 " +
                "AND $BI_IS_CANCELLED = 0 " +
                "ORDER BY $BI_PROJECTED_DATE; "
    )
    fun getPayDaysActive(): List<String>

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BI_IS_DELETED = 1, " +
                "$BI_UPDATE_TIME = :updateTime " +
                "WHERE $BI_ACTUAL_DATE > :currentDate " +
                "AND $BI_IS_MANUALLY_ENTERED = 0"
    )
    suspend fun deleteFutureBudgetItems(
        currentDate: String,
        updateTime: String
    )

}