package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed

class BudgetRuleEditState(
    val nf: NumberFunctions,
    val df: DateFunctions
) {
    var name by mutableStateOf("")
    var amount by mutableStateOf("")
    var isFixed by mutableStateOf(false)
    var isPayDay by mutableStateOf(false)
    var isAuto by mutableStateOf(false)
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var frequencyType by mutableIntStateOf(0)
    var frequencyCount by mutableStateOf("1")
    var dayOfWeek by mutableIntStateOf(0)
    var leadDays by mutableStateOf("0")
    var ruleId by mutableLongStateOf(0L)

    fun updateFrom(rule: BudgetRule, transferNum: Double? = null) {
        ruleId = rule.ruleId
        name = rule.budgetRuleName
        amount = nf.displayDollars(
            if (transferNum != null && transferNum != 0.0) transferNum else rule.budgetAmount
        )
        isFixed = rule.budFixedAmount
        isPayDay = rule.budIsPayDay
        isAuto = rule.budIsAutoPay
        startDate = rule.budStartDate
        endDate = rule.budEndDate ?: ""
        frequencyType = rule.budFrequencyTypeId
        frequencyCount = rule.budFrequencyCount.toString()
        dayOfWeek = rule.budDayOfWeekId
        leadDays = rule.budLeadDays.toString()
    }

    fun toBudgetRule(toAccountId: Long, fromAccountId: Long): BudgetRule {
        return BudgetRule(
            ruleId,
            name.trim(),
            toAccountId,
            fromAccountId,
            nf.getDoubleFromDollars(amount),
            isFixed,
            isPayDay,
            isAuto,
            startDate,
            endDate,
            dayOfWeek,
            frequencyType,
            frequencyCount.toIntOrNull() ?: 1,
            leadDays.toIntOrNull() ?: 0,
            false,
            df.getCurrentTimeAsString()
        )
    }

    fun toBudgetRuleDetailed(detailed: BudgetRuleDetailed?): BudgetRuleDetailed {
        return BudgetRuleDetailed(
            toBudgetRule(
                detailed?.toAccount?.accountId ?: 0L,
                detailed?.fromAccount?.accountId ?: 0L
            ),
            detailed?.toAccount,
            detailed?.fromAccount
        )
    }
}

@Composable
fun rememberBudgetRuleEditState(
    nf: NumberFunctions,
    df: DateFunctions
): BudgetRuleEditState {
    return remember { BudgetRuleEditState(nf, df) }
}