package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.app.AlertDialog
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

object BudgetRuleActionHelper {

    fun getRuleOptions(
        context: Context,
        ruleName: String,
        onAddTransaction: () -> Unit,
        onCreateBudgetItem: () -> Unit,
        onGotoAnalysis: () -> Unit,
    ): Pair<String, List<ActionOption>> {
        val title = "${context.getString(R.string.title_choose_action_for)} $ruleName"
        val options = listOf(
            ActionOption(
                context.getString(R.string.action_add_transaction_from_rule),
                Icons.Default.Add,
            ) { onAddTransaction() },
            ActionOption(
                context.getString(R.string.action_create_scheduled_item),
                Icons.Default.Add,
            ) { onCreateBudgetItem() },
            ActionOption(
                context.getString(R.string.action_view_rule_summary),
                Icons.Default.History,
            ) { onGotoAnalysis() },
        )
        return title to options
    }

    fun isRuleUnchanged(
        currentRule: BudgetRule,
        cachedRule: BudgetRule?,
    ): Boolean {
        if (cachedRule == null) return false
        return (currentRule.budgetRuleName == cachedRule.budgetRuleName) &&
                (currentRule.budToAccountId == cachedRule.budToAccountId) &&
                (currentRule.budFromAccountId == cachedRule.budFromAccountId) &&
                (currentRule.budgetAmount == cachedRule.budgetAmount) &&
                (currentRule.budFixedAmount == cachedRule.budFixedAmount) &&
                (currentRule.budIsPayDay == cachedRule.budIsPayDay) &&
                (currentRule.budIsAutoPay == cachedRule.budIsAutoPay) &&
                (currentRule.budStartDate == cachedRule.budStartDate) &&
                (currentRule.budEndDate == cachedRule.budEndDate) &&
                (currentRule.budDayOfWeekId == cachedRule.budDayOfWeekId) &&
                (currentRule.budFrequencyTypeId == cachedRule.budFrequencyTypeId) &&
                (currentRule.budFrequencyCount == cachedRule.budFrequencyCount) &&
                (currentRule.budLeadDays == cachedRule.budLeadDays)
    }

    fun showUnsavedWarningDialog(
        context: Context,
        onSaveAndProceed: () -> Unit,
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.msg_warning_rule_not_saved))
            .setMessage(context.getString(R.string.prompt_save_and_continue))
            .setPositiveButton(context.getString(R.string.action_yes)) { _, _ -> onSaveAndProceed() }
            .setNegativeButton(context.getString(R.string.action_cancel), null)
            .show()
    }
}