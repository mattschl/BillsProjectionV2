package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType

class AccountEditState(
    val nf: NumberFunctions,
    val df: DateFunctions
) {
    var name by mutableStateOf("")
    var handle by mutableStateOf("")
    var balance by mutableStateOf("")
    var owing by mutableStateOf("")
    var budgeted by mutableStateOf("")
    var limit by mutableStateOf("")

    fun updateFrom(
        accountWithType: AccountWithType?,
        transferNum: Double? = null,
        returnTo: String? = null
    ) {
        val account = accountWithType?.account
        name = account?.accountName ?: ""
        handle = account?.accountNumber ?: ""
        balance = nf.displayDollars(
            if (transferNum != null && transferNum != 0.0 && returnTo?.contains(BALANCE) == true) transferNum
            else account?.accountBalance ?: 0.0
        )
        owing = nf.displayDollars(
            if (transferNum != null && transferNum != 0.0 && returnTo?.contains(OWING) == true) transferNum
            else account?.accountOwing ?: 0.0
        )
        budgeted = nf.displayDollars(
            if (transferNum != null && transferNum != 0.0 && returnTo?.contains(BUDGETED) == true) transferNum
            else account?.accBudgetedAmount ?: 0.0
        )
        limit = nf.displayDollars(account?.accountCreditLimit ?: 0.0)
    }

    fun toAccount(accountId: Long, typeId: Long): Account {
        return Account(
            accountId = accountId,
            accountName = name.trim(),
            accountNumber = handle.trim(),
            accountTypeId = typeId,
            accBudgetedAmount = nf.getDoubleFromDollars(budgeted),
            accountBalance = nf.getDoubleFromDollars(balance),
            accountOwing = nf.getDoubleFromDollars(owing),
            accountCreditLimit = nf.getDoubleFromDollars(limit),
            accIsDeleted = false,
            accUpdateTime = df.getCurrentTimeAsString()
        )
    }
}

@Composable
fun rememberAccountEditState(
    nf: NumberFunctions,
    df: DateFunctions
): AccountEditState {
    return remember { AccountEditState(nf, df) }
}