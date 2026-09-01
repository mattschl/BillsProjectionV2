package ms.mattschlenkrich.billsprojectionv2.common.functions

import android.content.Context
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

object TransactionMessageHelper {
    fun buildConfirmationMessage(
        context: Context,
        transactionDetailed: TransactionDetailed,
        nf: NumberFunctions
    ): String {
        val trans = transactionDetailed.transaction ?: return ""
        val toAccountName = transactionDetailed.toAccount?.accountName ?: ""
        val fromAccountName = transactionDetailed.fromAccount?.accountName ?: ""

        var display = "${context.getString(R.string.msg_will_perform)}${trans.transName}${
            context.getString(R.string.text_for_padded)
        }${nf.getDollarsFromDouble(trans.transAmount)}${context.getString(R.string.text_from_header)}${fromAccountName}"

        if (trans.transFromAccountPending) {
            display += context.getString(R.string.text_pending_suffix)
        }

        display += "${context.getString(R.string.text_to_header)}${toAccountName}"

        if (trans.transToAccountPending) {
            display += context.getString(R.string.text_pending_suffix)
        }

        return display
    }

    fun buildPendingCompletionMessage(
        context: Context,
        transactionDetailed: TransactionDetailed,
        nf: NumberFunctions
    ): String {
        val trans = transactionDetailed.transaction ?: return ""
        if (!trans.transToAccountPending && !trans.transFromAccountPending) return ""

        val toAccountName = transactionDetailed.toAccount?.accountName ?: ""
        val fromAccountName = transactionDetailed.fromAccount?.accountName ?: ""

        var display = "${context.getString(R.string.msg_will_apply_amount)}${
            nf.displayDollars(trans.transAmount)
        }"

        if (trans.transToAccountPending) {
            display += "${context.getString(R.string.label_to_colon)}${toAccountName}"
        }

        if (trans.transToAccountPending && trans.transFromAccountPending) {
            display += context.getString(R.string.text_and_header)
        }

        if (trans.transFromAccountPending) {
            display += "${context.getString(R.string.label_from_header)}${fromAccountName}"
        }

        return display
    }
}