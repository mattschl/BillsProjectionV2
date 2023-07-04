package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.BUDGET_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_NAME
import ms.mattschlenkrich.billsprojectionv2.DAY_OF_WEEK_ID
import ms.mattschlenkrich.billsprojectionv2.END_DATE
import ms.mattschlenkrich.billsprojectionv2.FIXED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_COUNT
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.IS_AUTO_PAY
import ms.mattschlenkrich.billsprojectionv2.IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.IS_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.LEAD_DAYS
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.START_DATE
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed

@Dao
interface BudgetRuleDao {
    @Insert
    suspend fun insertBudgetRule(budgetRule: BudgetRule)

    @Query(
        "SELECT * FROM $TABLE_BUDGET_RULES " +
                "WHERE $BUDGET_RULE_NAME = :query"
    )
    fun findBudgetRuleByName(query: String?): List<BudgetRule>

    @Query(
        "SELECT $BUDGET_RULE_NAME FROM $TABLE_BUDGET_RULES"
    )
    fun getBudgetRuleNameList(): List<String>

    @Transaction
    @Query(
        "INSERT INTO $TABLE_BUDGET_RULES " +
                "($RULE_ID, $BUDGET_RULE_NAME, $TO_ACCOUNT_ID, " +
                "$FROM_ACCOUNT_ID, $BUDGET_AMOUNT, $FIXED_AMOUNT, " +
                "$IS_PAY_DAY, $IS_AUTO_PAY, $START_DATE, $END_DATE, " +
                "$DAY_OF_WEEK_ID, $FREQUENCY_TYPE_ID, $FREQUENCY_COUNT, $LEAD_DAYS," +
                "$IS_DELETED, $UPDATE_TIME) " +
                "VALUES (" +
                ":ruleId," +
                ":budgetRuleName, " +
                ":toAccountId, " +
                ":fromAccountId, " +
                ":budgetAmount, :fixedAmount, :isPayDay, :isAutoPayment, " +
                ":startDate, :endDate, :dayOfWeekId, :frequencyTypeId, " +
                ":frequencyCount, :leadDays, :isDeleted, :updateTime" +
                ")"

    )
    suspend fun insertBudgetRule(
        ruleId: Long,
        budgetRuleName: String,
        budgetAmount: Double,
        toAccountId: Long,
        fromAccountId: Long,
        fixedAmount: Boolean,
        isPayDay: Boolean,
        isAutoPayment: Boolean,
        startDate: String,
        endDate: String,
        frequencyTypeId: Int,
        frequencyCount: Int,
        dayOfWeekId: Int,
        leadDays: Int,
        isDeleted: Boolean,
        updateTime: String
    )

    @Query(
        "UPDATE $TABLE_BUDGET_RULES " +
                "SET $BUDGET_RULE_NAME = :budgetRuleName, " +
                "$BUDGET_AMOUNT = :budgetAmount, " +
                "$TO_ACCOUNT_ID = :toAccountId, " +
                "$FROM_ACCOUNT_ID = :fromAccountId, " +
                "$FIXED_AMOUNT = :fixedAmount, " +
                "$IS_PAY_DAY = :isPayDay," +
                "$IS_AUTO_PAY = :isAutoPayment, " +
                "$START_DATE = :startDate, " +
                "$END_DATE = :endDate, " +
                "$FREQUENCY_TYPE_ID = :frequencyTypeId, " +
                "$FREQUENCY_COUNT = :frequencyCount, " +
                "$DAY_OF_WEEK_ID = :dayOfWeekId, " +
                "$LEAD_DAYS = :leadDays, " +
                "$IS_DELETED = :isDeleted, " +
                "$UPDATE_TIME = :updateTime " +
                "WHERE $RULE_ID = :ruleId"
    )
    suspend fun updateBudgetRule(
        ruleId: Long,
        budgetRuleName: String,
        budgetAmount: Double,
        toAccountId: Long,
        fromAccountId: Long,
        fixedAmount: Boolean,
        isPayDay: Boolean,
        isAutoPayment: Boolean,
        startDate: String,
        endDate: String,
        frequencyTypeId: Int,
        frequencyCount: Int,
        dayOfWeekId: Int,
        leadDays: Int,
        isDeleted: Boolean,
        updateTime: String
    )

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
                "toAccount.* , " +
                "fromAccount.*  " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.$IS_DELETED = 0 " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getActiveBudgetRulesDetailed():
            LiveData<List<BudgetRuleDetailed>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*  " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME LIKE :query " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun searchBudgetRules(query: String?): LiveData<List<BudgetRuleDetailed>>

}