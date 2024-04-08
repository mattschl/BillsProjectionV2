package ms.mattschlenkrich.billsprojectionv2.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_NAME
import ms.mattschlenkrich.billsprojectionv2.common.BUD_FIXED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.BUD_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BUD_IS_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BUD_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_YEARLY
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleDetailed

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


    @Update
    suspend fun updateBudgetRule(budgetRule: BudgetRule)

    @Query(
        "UPDATE $TABLE_BUDGET_RULES " +
                "SET budIsDeleted = 1 , " +
                "$BUD_UPDATE_TIME = :updateTime " +
                "WHERE $RULE_ID = :budgetRuleId"
    )
    suspend fun deleteBudgetRule(budgetRuleId: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_BUDGET_RULES " +
                "WHERE $BUD_IS_DELETED = 0 " +
                "ORDER BY $BUD_IS_PAY_DAY DESC, " +
                "$BUDGET_RULE_NAME;"
    )
    suspend fun getBudgetRulesActive():
            List<BudgetRule>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*  " +
                "FROM $TABLE_BUDGET_RULES  " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.budIsDeleted = 0 " +
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
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME LIKE :query " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun searchBudgetRules(query: String?): LiveData<List<BudgetRuleDetailed>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*," +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOiN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.ruleId = :ruleId " +
                "AND $TABLE_BUDGET_RULES.$BUD_IS_DELETED = 0;"
    )
    fun getBudgetRuleDetailed(ruleId: Long): BudgetRuleDetailed

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*," +
                "toAccount.*, " +
                "fromAccount.* " +
                "FROM $TABLE_BUDGET_RULES " +
                "LEFT JOiN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.accountId " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.ruleId = :ruleId " +
                "AND $TABLE_BUDGET_RULES.$BUD_IS_DELETED = 0;"
    )
    fun getBudgetRuleFullLive(ruleId: Long): LiveData<BudgetRuleDetailed>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*  " +
                "FROM $TABLE_BUDGET_RULES  " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "WHERE $TABLE_BUDGET_RULES.budIsDeleted = 0 " +
                "AND (" +
                "($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_WEEKLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount <= 4) " +
                "OR ($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_MONTHLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount == 1)" +
                ") AND $TABLE_BUDGET_RULES.budEndDate >= :today " +
                "AND $TABLE_BUDGET_RULES.budStartDate <= :today " +
                "ORDER BY $TABLE_BUDGET_RULES.budFrequencyCount DESC, " +
                "$TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getBudgetRulesMonthly(today: String):
            LiveData<List<BudgetRuleDetailed>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*," +
                "toAccountType.*," +
                "fromAccountType.* " +
                "FROM $TABLE_BUDGET_RULES  " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as toAccountType on " +
                "toAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID) " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as fromAccountType on " +
                "fromAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID) " +
                "WHERE $TABLE_BUDGET_RULES.budIsDeleted = 0 " +
                "AND (" +
                "($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_WEEKLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount <= 4) " +
                "OR ($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_MONTHLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount == 1)" +
                ") AND $TABLE_BUDGET_RULES.budEndDate >= :today " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUD_IS_PAY_DAY DESC, " +
                "$TABLE_BUDGET_RULES.$BUD_FIXED_AMOUNT DESC, " +
                "$TABLE_BUDGET_RULES.budFrequencyTypeId DESC, " +
                "$TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getBudgetRulesCompletedMonthly(today: String):
            LiveData<List<BudgetRuleComplete>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*," +
                "toAccountType.*," +
                "fromAccountType.* " +
                "FROM $TABLE_BUDGET_RULES  " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as toAccountType on " +
                "toAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID) " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as fromAccountType on " +
                "fromAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID) " +
                "WHERE $TABLE_BUDGET_RULES.budIsDeleted = 0 " +
                "AND (" +
                "($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_WEEKLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount > 4) " +
                "OR ($TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_MONTHLY AND " +
                "$TABLE_BUDGET_RULES.budFrequencyCount > 1)" +
                ") AND $TABLE_BUDGET_RULES.budEndDate >= :today " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUD_IS_PAY_DAY DESC, " +
                "$TABLE_BUDGET_RULES.$BUD_FIXED_AMOUNT DESC, " +
                "$TABLE_BUDGET_RULES.budFrequencyCount DESC, " +
                "$TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getBudgetRulesCompletedOccasional(today: String):
            LiveData<List<BudgetRuleComplete>>

    @Transaction
    @Query(
        "SELECT $TABLE_BUDGET_RULES.*, " +
                "toAccount.* , " +
                "fromAccount.*," +
                "toAccountType.*," +
                "fromAccountType.* " +
                "FROM $TABLE_BUDGET_RULES  " +
                "LEFT JOIN $TABLE_ACCOUNTS as toAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID = " +
                "toAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as toAccountType on " +
                "toAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_TO_ACCOUNT_ID) " +
                "LEFT JOIN $TABLE_ACCOUNTS as fromAccount on " +
                "$TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID = " +
                "fromAccount.$ACCOUNT_ID " +
                "LEFT JOIN $TABLE_ACCOUNT_TYPES as fromAccountType on " +
                "fromAccountType.$TYPE_ID = " +
                "(SELECT $ACCOUNT_TYPE_ID FROM $TABLE_ACCOUNTS " +
                "WHERE $TABLE_ACCOUNTS.$ACCOUNT_ID = $TABLE_BUDGET_RULES.$BUD_FROM_ACCOUNT_ID) " +
                "WHERE $TABLE_BUDGET_RULES.budIsDeleted = 0 " +
                "AND $TABLE_BUDGET_RULES.budFrequencyTypeId == $FREQ_YEARLY " +
                "AND $TABLE_BUDGET_RULES.budEndDate >= :today " +
                "ORDER BY $TABLE_BUDGET_RULES.$BUD_IS_PAY_DAY DESC, " +
                "$TABLE_BUDGET_RULES.budFrequencyCount ASC, " +
                "$TABLE_BUDGET_RULES.$BUDGET_RULE_NAME " +
                "COLLATE NOCASE ASC"
    )
    fun getBudgetRulesCompletedAnnually(today: String):
            LiveData<List<BudgetRuleComplete>>
}