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
                activity.getString(R.string.action_schedule_budget_item),
                Icons.Default.Add
            ) { onNewBudgetItem() },
            ActionOption(
                activity.getString(R.string.action_add_unscheduled_transaction),
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
        val curBudget = curBudgetDetailed.budgetItem ?: return emptyList()
        return listOf(
            ActionOption(
                "${activity.getString(R.string.msg_perform_transaction_on)} \"${curBudget.biBudgetName}\" ",
                Icons.Default.Edit
            ) { onPerformCustom() },
            ActionOption(
                if (curBudget.biProjectedAmount == 0.0) ""
                else "${activity.getString(R.string.action_perform)}\"${curBudget.biBudgetName}\" ${
                    activity.getString(
                        R.string.msg_for_full_amount
                    )
                }${nf.displayDollars(curBudget.biProjectedAmount)}",
                Icons.Default.Check
            ) { onPerformFull() },
            ActionOption(
                activity.getString(R.string.action_adjust_projection),
                Icons.Default.PlayArrow
            ) { onAdjustProjection() },
            ActionOption(
                activity.getString(R.string.action_go_to_rules),
                Icons.AutoMirrored.Filled.Rule
            ) { onGoToRule() },
            ActionOption(
                "${activity.getString(R.string.msg_will_cancel)}${curBudget.biBudgetName}${
                    activity.getString(
                        R.string.msg_with_amount
                    )
                }${nf.displayDollars(curBudget.biProjectedAmount)}${activity.getString(R.string.text_remaining_suffix)}",
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
                "${activity.getString(R.string.action_lock)}$budgetItemName",
                Icons.Default.Lock
            ) { onLockItem() },
            ActionOption(
                "${activity.getString(R.string.action_unlock)}$budgetItemName",
                Icons.Default.LockOpen
            ) { onUnlockItem() },
            ActionOption(
                activity.getString(R.string.action_lock_payday_items),
                Icons.Default.Lock
            ) { onLockPayDay() },
            ActionOption(
                activity.getString(R.string.action_unlock_payday_items),
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
                activity.getString(R.string.action_complete_pending_transaction),
                Icons.Default.Check
            ) { onComplete() },
            ActionOption(
                activity.getString(R.string.action_open_transaction_to_edit),
                Icons.Default.Edit
            ) { onEdit() },
            ActionOption(
                activity.getString(R.string.action_delete_pending_transaction),
                Icons.Default.Delete
            ) { onDelete() }
        )
    }
}