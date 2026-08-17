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

        var display = "${context.getString(R.string.this_will_perform)}${trans.transName}${
            context.getString(R.string._for_)
        }${nf.getDollarsFromDouble(trans.transAmount)}${context.getString(R.string.__from)}${fromAccountName}"

        if (trans.transFromAccountPending) {
            display += context.getString(R.string._pending)
        }

        display += "${context.getString(R.string._to)}${toAccountName}"

        if (trans.transToAccountPending) {
            display += context.getString(R.string._pending)
        }

        return display
    }

    fun buildPendingCompletionMessage(
        context: Context,
        transactionDetailed: TransactionDetailed,
        nf: NumberFunctions
    ): String {
        val trans = transactionDetailed.transaction ?: return ""
        val toAccountName = transactionDetailed.toAccount?.accountName ?: ""
        val fromAccountName = transactionDetailed.fromAccount?.accountName ?: ""

        var display = "${context.getString(R.string.this_will_apply_the_amount_of)}${
            nf.displayDollars(trans.transAmount)
        }"

        if (trans.transToAccountPending) {
            display += "${context.getString(R.string.to_)}${toAccountName}"
        }

        if (trans.transToAccountPending && trans.transFromAccountPending) {
            display += context.getString(R.string._and)
        }

        if (trans.transFromAccountPending) {
            display += "${context.getString(R.string.from)}${fromAccountName}"
        }

        return display
    }
}