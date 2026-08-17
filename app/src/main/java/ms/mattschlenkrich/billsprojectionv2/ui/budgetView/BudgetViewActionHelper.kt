package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

object BudgetViewActionHelper {

    fun getAddOptions(
        activity: MainActivity,
        onNewBudgetItem: () -> Unit,
        onUnscheduledTransaction: () -> Unit
    ): List<ActionOption> {
        return listOf(
            ActionOption(
                activity.getString(R.string.schedule_a_new_budget_item),
                Icons.Default.Add
            ) { onNewBudgetItem() },
            ActionOption(
                activity.getString(R.string.add_an_unscheduled_transaction),
                Icons.Default.Receipt
            ) { onUnscheduledTransaction() }
        )
    }

    fun getBudgetItemOptions(
        activity: MainActivity,
        curBudgetDetailed: BudgetItemDetailed,
        nf: NumberFunctions,
        onPerformCustom: () -> Unit,
        onPerformFull: () -> Unit,
        onAdjustProjection: () -> Unit,
        onGoToRule: () -> Unit,
        onCancelItem: () -> Unit
    ): List<ActionOption> {
        val curBudget = curBudgetDetailed.budgetItem!!
        return listOf(
            ActionOption(
                "${activity.getString(R.string.perform_a_transaction_on_)} \"${curBudget.biBudgetName}\" ",
                Icons.Default.Edit
            ) { onPerformCustom() },
            ActionOption(
                if (curBudget.biProjectedAmount == 0.0) ""
                else "${activity.getString(R.string.perform_action)}\"${curBudget.biBudgetName}\" ${
                    activity.getString(
                        R.string.for_amount_of_the_full_amount_
                    )
                }${nf.displayDollars(curBudget.biProjectedAmount)}",
                Icons.Default.Check
            ) { onPerformFull() },
            ActionOption(
                activity.getString(R.string.adjust_the_projections_for_this_item),
                Icons.Default.PlayArrow
            ) { onAdjustProjection() },
            ActionOption(
                activity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                Icons.AutoMirrored.Filled.Rule
            ) { onGoToRule() },
            ActionOption(
                "${activity.getString(R.string.this_will_cancel)}${curBudget.biBudgetName}${
                    activity.getString(
                        R.string.with_the_amount_of
                    )
                }${nf.displayDollars(curBudget.biProjectedAmount)}${activity.getString(R.string._remaining)}",
                Icons.Default.Cancel
            ) { onCancelItem() }
        )
    }

    fun getLockOptions(
        activity: MainActivity,
        budgetItemName: String,
        onLockItem: () -> Unit,
        onUnlockItem: () -> Unit,
        onLockPayDay: () -> Unit,
        onUnlockPayDay: () -> Unit
    ): List<ActionOption> {
        return listOf(
            ActionOption(
                "${activity.getString(R.string.lock)}$budgetItemName",
                Icons.Default.Lock
            ) { onLockItem() },
            ActionOption(
                "${activity.getString(R.string.un_lock)}$budgetItemName",
                Icons.Default.LockOpen
            ) { onUnlockItem() },
            ActionOption(
                activity.getString(R.string.lock_all_items_for_this_payday),
                Icons.Default.Lock
            ) { onLockPayDay() },
            ActionOption(
                activity.getString(R.string.un_lock_all_items_for_this_payday),
                Icons.Default.LockOpen
            ) { onUnlockPayDay() }
        )
    }

    fun getPendingTransactionOptions(
        activity: MainActivity,
        onComplete: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ): List<ActionOption> {
        return listOf(
            ActionOption(
                activity.getString(R.string.complete_this_pending_transaction),
                Icons.Default.Check
            ) { onComplete() },
            ActionOption(
                activity.getString(R.string.open_the_transaction_to_edit_it),
                Icons.Default.Edit
            ) { onEdit() },
            ActionOption(
                activity.getString(R.string.delete_this_pending_transaction),
                Icons.Default.Delete
            ) { onDelete() }
        )
    }
}