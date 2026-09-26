package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.content.Context
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType

object AccountActionHelper {

    fun validateAccountUpdate(
        context: Context,
        name: String,
        accountNames: List<String>,
        currentAwt: AccountWithType?,
    ): String {
        if (name.isBlank()) {
            return context.getString(R.string.msg_prompt_enter_name)
        }
        if (accountNames.any { accName ->
                (accName == name) && (currentAwt == null || accName != currentAwt.account.accountName)
            }) {
            return context.getString(R.string.msg_error_budget_rule_exists)
        }
        if (currentAwt?.accountType == null) {
            return context.getString(R.string.msg_prompt_choose_account_type)
        }
        return ANSWER_OK
    }

    fun showRenameAccountDialog(
        context: Context,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.title_rename_account))
            setMessage(
                context.getString(R.string.prompt_rename_account) + "\n\n" +
                        context.getString(R.string.label_note_header) +
                        context.getString(R.string.msg_wont_replace_account_type)
            )
            setPositiveButton(context.getString(R.string.action_confirm)) { _, _ -> onConfirm() }
            setNegativeButton(context.getString(R.string.action_cancel), null)
        }.create().show()
    }
}