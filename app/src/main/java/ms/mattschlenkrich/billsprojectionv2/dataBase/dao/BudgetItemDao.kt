package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_DISPLAY_AS_ASSET
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_AUTOMATIC
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_COMPLETED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_FIXED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_MANUALLY_ENTERED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_LOCKED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_NAME
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PROJECTED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.IS_ASSET
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed

@Dao
interface BudgetItemDao {

    @Insert
    suspend fun insertBudgetItem(budgetItem: BudgetItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceBudgetItem(budgetItem: BudgetItem)

    @Update
    suspend fun updateBudgetItem(budgetItem: BudgetItem)

    @Query(
        "SELECT * FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BUDGET_ITEM_RULE_ID = :ruleId " +
                "AND $BUDGET_ITEM_PROJECTED_DATE = :projectedDate"
    )
    fun getBudgetItem(ruleId: Long, projectedDate: String): BudgetItem?

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_IS_DELETED = 1, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_RULE_ID = :budgetRulId " +
                "AND $BUDGET_ITEM_PROJECTED_DATE = :projectedDate"
    )
    suspend fun deleteBudgetItem(
        budgetRulId: Long, projectedDate: String,
        updateTime: String
    )

    @Query(
        "SELECT DISTINCT $BUDGET_ITEM_PROJECTED_DATE FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BUDGET_ITEM_IS_PAY_DAY_ITEM = 1 " +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "ORDER BY $BUDGET_ITEM_PROJECTED_DATE; "
    )
    fun getPayDaysActive(): List<String>

    @Query(
        "SELECT DISTINCT $BUDGET_ITEM_PAY_DAY FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "ORDER BY $BUDGET_ITEM_PAY_DAY;"
    )
    fun getPayDays(): LiveData<List<String>>

    @Query(
        "SELECT DISTINCT $BUDGET_ITEM_PAY_DAY FROM $TABLE_BUDGET_ITEMS " +
                "WHERE (:asset = 'All Items' OR " +
                "($BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "OR $BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset)" +
                "))" +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "ORDER BY $BUDGET_ITEM_PAY_DAY ASC"
    )
    fun getPayDays(asset: String): LiveData<List<String>>


    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_IS_DELETED = 1, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_ACTUAL_DATE > :currentDate " +
                "AND $BUDGET_ITEM_IS_MANUALLY_ENTERED = 0 " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "AND $BUDGET_ITEM_LOCKED = 0"
    )
    suspend fun deleteFutureItems(currentDate: String, updateTime: String)


    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_IS_DELETED = 1, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime, " +
                "$BUDGET_ITEM_IS_FIXED = 0, " +
                "$BUDGET_ITEM_LOCKED = 0, " +
                "$BUDGET_ITEM_IS_MANUALLY_ENTERED = 0 " +
                "WHERE $BUDGET_ITEM_ACTUAL_DATE >= :currentDate " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "AND $BUDGET_ITEM_LOCKED = 0"
    )
    suspend fun killFutureBudgetItems(currentDate: String, updateTime: String)

    @Query(
        "SELECT $ACCOUNT_NAME FROM $TABLE_ACCOUNTS " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES ON " +
                "$TABLE_ACCOUNTS.accountTypeId = " +
                "$TABLE_ACCOUNT_TYPES.typeId " +
                "WHERE $TABLE_ACCOUNT_TYPES.$ACCT_DISPLAY_AS_ASSET = 1 " +
                "ORDER BY $TABLE_ACCOUNT_TYPES.$IS_ASSET DESC, " +
                "$TABLE_ACCOUNTS.$ACCOUNT_NAME COLLATE NOCASE;"
    )
    fun getAssetsForBudget(): LiveData<List<String>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * " +
                "FROM $TABLE_BUDGET_ITEMS " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_RULE_ID = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "fromAccount.accountId " +
                "WHERE $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_PAY_DAY = :payDay " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND (:asset = 'All Items' OR " +
                "($TABLE_BUDGET_ITEMS.$BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "OR $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS  " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                " ))" +
                "ORDER BY $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_IS_PAY_DAY_ITEM DESC, " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_ACTUAL_DATE , " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_NAME ;"
    )
    fun getBudgetItems(asset: String, payDay: String)
            : LiveData<List<BudgetItemDetailed>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * " +
                "FROM $TABLE_BUDGET_ITEMS " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_RULE_ID = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "fromAccount.accountId " +
                "WHERE $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_PAY_DAY = :payDay " +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND (:asset = 'All Items' OR " +
                "($TABLE_BUDGET_ITEMS.$BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                "OR $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "(SELECT $ACCOUNT_ID FROM $TABLE_ACCOUNTS  " +
                "WHERE $ACCOUNT_NAME = :asset) " +
                " ))" +
                "ORDER BY $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_IS_PAY_DAY_ITEM DESC, " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_ACTUAL_DATE , " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_NAME ;"
    )
    fun getBudgetItemsAll(asset: String, payDay: String)
            : LiveData<List<BudgetItemDetailed>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT $TABLE_BUDGET_ITEMS.*, budgetRule.*, " +
                "toAccount.*, fromAccount.* " +
                "FROM $TABLE_BUDGET_ITEMS " +
                "LEFT JOIN $TABLE_BUDGET_RULES as budgetRule ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_RULE_ID = " +
                "budgetRule.ruleId " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount ON " +
                "$TABLE_BUDGET_ITEMS.$BUDGET_ITEM_FROM_ACCOUNT_ID = " +
                "fromAccount.accountId " +
                "WHERE $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_RULE_ID = :budgetRuleId " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "AND $BUDGET_ITEM_IS_DELETED = 0 " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "ORDER BY $TABLE_BUDGET_ITEMS.$BUDGET_ITEM_ACTUAL_DATE;"
    )
    fun getBudgetItems(budgetRuleId: Long)
            : LiveData<List<BudgetItemDetailed>>

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_IS_CANCELLED = 1, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_PROJECTED_DATE = :projectedDate " +
                "AND $BUDGET_ITEM_RULE_ID = :budgetRuleId"
    )
    suspend fun cancelBudgetItem(
        budgetRuleId: Long, projectedDate: String, updateTime: String
    )

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_ACTUAL_DATE = :actualDate ," +
                "$BUDGET_ITEM_PAY_DAY = :payDay," +
                "$BUDGET_ITEM_NAME = :budgetName, " +
                "$BUDGET_ITEM_IS_PAY_DAY_ITEM = :isPayDay," +
                "$BUDGET_ITEM_TO_ACCOUNT_ID = :toAccountId, " +
                "$BUDGET_ITEM_FROM_ACCOUNT_ID = :fromAccountId, " +
                "$BUDGET_ITEM_PROJECTED_AMOUNT = :projectedAmount, " +
                "$BUDGET_ITEM_IS_FIXED = :isFixed, " +
                "$BUDGET_ITEM_IS_AUTOMATIC = :isAutomatic, " +
                "$BUDGET_ITEM_IS_DELETED = 0, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_RULE_ID = :budgetRuleId " +
                "AND $BUDGET_ITEM_PROJECTED_DATE = :projectedDate " +
                "AND $BUDGET_ITEM_IS_MANUALLY_ENTERED = 0 " +
                "AND $BUDGET_ITEM_IS_CANCELLED = 0 " +
                "AND $BUDGET_ITEM_IS_COMPLETED = 0 " +
                "AND $BUDGET_ITEM_LOCKED = 0;"
    )
    suspend fun rewriteBudgetItem(
        budgetRuleId: Long, projectedDate: String, actualDate: String, payDay: String,
        budgetName: String, isPayDay: Boolean, toAccountId: Long, fromAccountId: Long,
        projectedAmount: Double, isFixed: Boolean, isAutomatic: Boolean, updateTime: String
    )

    @Query(
        "DELETE FROM $TABLE_BUDGET_ITEMS " +
                "WHERE $BUDGET_ITEM_PAY_DAY < :cutoffDate " +
                "AND ($BUDGET_ITEM_IS_COMPLETED = 1 OR $BUDGET_ITEM_IS_CANCELLED = 1 OR $BUDGET_ITEM_IS_DELETED = 1)"
    )
    suspend fun purgeOldBudgetItems(cutoffDate: String)

    @Query("SELECT * FROM $TABLE_BUDGET_ITEMS")
    fun getAllBudgetItemsSync(): List<BudgetItem>

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_LOCKED = :lock, " +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_RULE_ID = :budgetRuleId " +
                "AND $BUDGET_ITEM_PAY_DAY = :payDay"
    )
    suspend fun lockUnlockBudgetItem(
        lock: Boolean, budgetRuleId: Long, payDay: String, updateTime: String
    )

    @Query(
        "UPDATE $TABLE_BUDGET_ITEMS " +
                "SET $BUDGET_ITEM_LOCKED = :lock," +
                "$BUDGET_ITEM_UPDATE_TIME = :updateTime " +
                "WHERE $BUDGET_ITEM_PAY_DAY = :payDay"
    )
    suspend fun lockUnlockBudgetItem(
        lock: Boolean, payDay: String, updateTime: String
    )
}