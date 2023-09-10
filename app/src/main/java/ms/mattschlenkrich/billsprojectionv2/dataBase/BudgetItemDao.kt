package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_DISPLAY_AS_ASSET
import ms.mattschlenkrich.billsprojectionv2.common.BI_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_BUDGET_NAME
import ms.mattschlenkrich.billsprojectionv2.common.BI_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_COMPLETED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_MANUALLY_ENTERED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.common.BI_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BI_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
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

    @Query(
        "SELECT $ACCOUNT_NAME FROM $TABLE_ACCOUNTS " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES ON " +
                "$TABLE_ACCOUNTS.accountTypeId = " +
                "$TABLE_ACCOUNT_TYPES.typeId " +
                "WHERE $TABLE_ACCOUNT_TYPES.$ACCT_DISPLAY_AS_ASSET = 1 " +
                "ORDER BY $TABLE_ACCOUNTS.$ACCOUNT_NAME COLLATE NOCASE;"
    )
    fun getAssetsForBudget(): LiveData<List<String>>

    @Query(
        "SELECT DISTINCT $BI_PAY_DAY FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BI_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "AND $BI_IS_DELETED = 0 " +
                "AND $BI_IS_COMPLETED = 0 " +
                "AND $BI_IS_CANCELLED = 0 " +
                "ORDER BY $BI_PAY_DAY ASC"
    )
    fun getPayDays(asset: String): LiveData<List<String>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_ITEMS.*, budgetRule.*, " +
                "toAccount.*, fromAccount.* " +
                "FROM $TABLE_BUDGET_ITEMS " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule ON " +
                "$TABLE_BUDGET_ITEMS.$BI_BUDGET_RULE_ID = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BI_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BI_FROM_ACCOUNT_ID = " +
                "fromAccount.accountId " +
                "WHERE $TABLE_BUDGET_ITEMS.$BI_PAY_DAY = :payDay " +
                "AND ($TABLE_BUDGET_ITEMS.$BI_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "OR $TABLE_BUDGET_ITEMS.$BI_TO_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS  " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                " )" +
                "ORDER BY $TABLE_BUDGET_ITEMS.$BI_ACTUAL_DATE ASC, " +
                "$TABLE_BUDGET_ITEMS.$BI_BUDGET_NAME ASC;"
    )
    fun getBudgetItems(
        asset: String,
        payDay: String
    )
            : LiveData<List<BudgetDetailed>>
}