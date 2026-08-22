package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

class TransactionEditState(
    val nf: NumberFunctions,
    val df: DateFunctions
) {
    var date by mutableStateOf("")
    var description by mutableStateOf("")
    var note by mutableStateOf("")
    var amount by mutableStateOf("")
    var toAccount by mutableStateOf<Account?>(null)
    var fromAccount by mutableStateOf<Account?>(null)
    var budgetRule by mutableStateOf<BudgetRule?>(null)
    var toPending by mutableStateOf(false)
    var fromPending by mutableStateOf(false)

    var toAccountWithType by mutableStateOf<AccountWithType?>(null)
    var fromAccountWithType by mutableStateOf<AccountWithType?>(null)

    var transactionId by mutableLongStateOf(0L)

    var dateError by mutableStateOf(false)
    var descriptionError by mutableStateOf(false)
    var amountError by mutableStateOf(false)
    var toAccountError by mutableStateOf(false)
    var fromAccountError by mutableStateOf(false)

    fun updateFrom(detailed: TransactionDetailed, transferNum: Double? = null) {
        val trans = detailed.transaction
        val rule = detailed.budgetRule
        val ruleChanged = rule != null && rule.ruleId != trans?.transRuleId

        if (trans != null) {
            transactionId = trans.transId
            date = trans.transDate
            if (ruleChanged || trans.transName.isBlank()) {
                description = rule?.budgetRuleName ?: ""
                amount = nf.displayDollars(rule?.budgetAmount ?: 0.0)
            } else {
                description = trans.transName
                amount = nf.displayDollars(
                    if (transferNum != null && transferNum != 0.0) transferNum else trans.transAmount
                )
            }
            note = trans.transNote
            toPending = trans.transToAccountPending
            fromPending = trans.transFromAccountPending
        } else {
            date = df.getCurrentDateAsString()
            if (rule != null) {
                description = rule.budgetRuleName
                amount = nf.displayDollars(rule.budgetAmount)
            } else {
                amount = nf.displayDollars(0.0)
            }
        }
        budgetRule = rule
        toAccount = detailed.toAccount
        fromAccount = detailed.fromAccount
    }

    fun toTransactions(): Transactions {
        return Transactions(
            transId = if (transactionId == 0L) nf.generateId() else transactionId,
            transDate = date,
            transName = description.trim(),
            transNote = note.trim(),
            transRuleId = budgetRule?.ruleId ?: 0L,
            transToAccountId = toAccount?.accountId ?: 0L,
            transToAccountPending = toPending,
            transFromAccountId = fromAccount?.accountId ?: 0L,
            transFromAccountPending = fromPending,
            transAmount = nf.getDoubleFromDollars(amount),
            transIsDeleted = false,
            transUpdateTime = df.getCurrentTimeAsString()
        )
    }

    fun toTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            toTransactions(),
            budgetRule,
            toAccount,
            fromAccount
        )
    }

    fun validate(): Boolean {
        dateError = date.isBlank()
        descriptionError = description.isBlank()
        toAccountError = toAccount == null
        fromAccountError = fromAccount == null
        amountError = amount.isBlank() || nf.getDoubleFromDollars(amount) == 0.0

        return !dateError && !descriptionError && !toAccountError && !fromAccountError && !amountError
    }
}

@Composable
fun rememberTransactionEditState(
    nf: NumberFunctions,
    df: DateFunctions
): TransactionEditState {
    return remember { TransactionEditState(nf, df) }
}