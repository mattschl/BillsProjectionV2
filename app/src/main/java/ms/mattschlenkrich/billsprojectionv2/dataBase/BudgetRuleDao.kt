package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_NAME
import ms.mattschlenkrich.billsprojectionv2.DAY_ID
import ms.mattschlenkrich.billsprojectionv2.DAY_OF_WEEK_ID
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_ID
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.TABLE_DAYS_OF_WEEK
import ms.mattschlenkrich.billsprojectionv2.TABLE_FREQUENCY_TYPES
import ms.mattschlenkrich.billsprojectionv2.UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.model.DaysOfWeek
import ms.mattschlenkrich.billsprojectionv2.model.FrequencyTypes

@Dao
interface BudgetRuleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudgetRule(budgetRule: BudgetRule)

    @Update
    suspend fun updateBudgetRule(budgetRule: BudgetRule)

    @Query(
        "UPDATE $TABLE_BUDGET_RULES " +
                "SET $IS_DELETED = 1, " +
                "$UPDATE_TIME = :updateTime " +
                "WHERE $RULE_ID = :budgetRuleId"
    )
    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String)

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "$TABLE_FREQUENCY_TYPES.*, " +
                "$TABLE_DAYS_OF_WEEK.* " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOIN $TABLE_FREQUENCY_TYPES ON " +
                "$TABLE_BUDGET_RULES.$FREQUENCY_TYPE_ID = " +
                "$TABLE_FREQUENCY_TYPES.$FREQUENCY_ID " +
                "LEFT JOiN $TABLE_DAYS_OF_WEEK ON " +
                "$TABLE_DAYS_OF_WEEK.$DAY_ID = " +
                "$TABLE_BUDGET_RULES.$DAY_OF_WEEK_ID " +
                "WHERE $TABLE_BUDGET_RULES.$IS_DELETED = 0 " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getActiveBudgetRules(): LiveData<List<BudgetRule>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "$TABLE_FREQUENCY_TYPES.*, " +
                "$TABLE_DAYS_OF_WEEK.* " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOIN $TABLE_FREQUENCY_TYPES ON " +
                "$TABLE_BUDGET_RULES.$FREQUENCY_TYPE_ID = " +
                "$TABLE_FREQUENCY_TYPES.$FREQUENCY_ID " +
                "LEFT JOiN $TABLE_DAYS_OF_WEEK ON " +
                "$TABLE_DAYS_OF_WEEK.$DAY_ID = " +
                "$TABLE_BUDGET_RULES.$DAY_OF_WEEK_ID " +
                "WHERE $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME LIKE :query " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun searchBudgetRules(query: String?): LiveData<List<BudgetRuleDetailed>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFrequencyType(frequencyType: FrequencyTypes)

    @Update
    suspend fun updateFrequencyType(frequencyType: FrequencyTypes)

    @Delete
    suspend fun deleteFrequencyType(frequencyType: FrequencyTypes)

    @Query(
        "SELECT * FROM $TABLE_FREQUENCY_TYPES"
    )
    fun getFrequencyTypes(): LiveData<List<FrequencyTypes>>

    @Query(
        "SELECT * FROM $TABLE_FREQUENCY_TYPES " +
                "WHERE $FREQUENCY_ID = :frequencyId"
    )
    fun findFrequencyType(frequencyId: Long): LiveData<List<FrequencyTypes>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayOfWeek(daysOfWeek: DaysOfWeek)

    @Update
    suspend fun updateDayOfWeek(daysOfWeek: DaysOfWeek)

    @Delete
    suspend fun deleteDayOfWeek(daysOfWeek: DaysOfWeek)

    @Query(
        "SELECT * FROM $TABLE_DAYS_OF_WEEK"
    )
    fun getDaysOfWeek(): LiveData<List<DaysOfWeek>>

    @Query(
        "SELECT * FROM $TABLE_DAYS_OF_WEEK " +
                "WHERE $DAY_ID = :dayOfWeekId"
    )
    fun findDayOfWeek(dayOfWeekId: Long): LiveData<List<DaysOfWeek>>
}