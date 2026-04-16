package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_TRANS_ADD

@Composable
fun TransactionAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_a_new_transaction)

    var date by remember { mutableStateOf(df.getCurrentDateAsString()) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(nf.displayDollars(0.0)) }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var budgetRule by remember { mutableStateOf<BudgetRule?>(null) }
    var toPending by remember { mutableStateOf(false) }
    var fromPending by remember { mutableStateOf(false) }

    var toAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }
    var fromAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }

    LaunchedEffect(Unit) {
        val cached = mainViewModel.getTransactionDetailed()
        if (cached != null) {
            val trans = cached.transaction
            val rule = cached.budgetRule
            if (trans != null) {
                date = trans.transDate
                description = if (rule != null && trans.transName.isBlank())
                    rule.budgetRuleName
                else trans.transName
                note = trans.transNote
                toPending = trans.transToAccountPending
                fromPending = trans.transFromAccountPending
                amount = nf.displayDollars(
                    if ((mainViewModel.getTransferNum()
                            ?: 0.0) != 0.0
                    ) mainViewModel.getTransferNum()!!
                    else trans.transAmount
                )
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
            toAccount = cached.toAccount
            fromAccount = cached.fromAccount

            cached.toAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                toAccountWithType = awt
                if (trans == null) {
                    toPending = awt.accountType?.allowPending == true &&
                            awt.accountType?.tallyOwing == true
                }
            }
            cached.fromAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                fromAccountWithType = awt
                if (trans == null) {
                    fromPending = awt.accountType?.allowPending == true &&
                            awt.accountType?.tallyOwing == true
                }
            }

            if (toAccount == null && rule?.budToAccountId != 0L && rule != null) {
                val acc = accountViewModel.getAccount(rule.budToAccountId)
                toAccount = acc
                val awt = accountViewModel.getAccountWithType(acc.accountId)
                toAccountWithType = awt
                if (trans == null) {
                    toPending = awt.accountType?.allowPending == true &&
                            awt.accountType?.tallyOwing == true
                }
            }
            if (fromAccount == null && rule?.budFromAccountId != 0L && rule != null) {
                val acc = accountViewModel.getAccount(rule.budFromAccountId)
                fromAccount = acc
                val awt = accountViewModel.getAccountWithType(acc.accountId)
                fromAccountWithType = awt
                if (trans == null) {
                    fromPending = awt.accountType?.allowPending == true &&
                            awt.accountType?.tallyOwing == true
                }
            }
            mainViewModel.setTransferNum(0.0)
        }
    }

    fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            Transactions(
                nf.generateId(),
                date,
                description.trim(),
                note.trim(),
                budgetRule?.ruleId ?: 0L,
                toAccount?.accountId ?: 0L,
                toPending,
                fromAccount?.accountId ?: 0L,
                fromPending,
                nf.getDoubleFromDollars(amount),
                false,
                df.getCurrentTimeAsString()
            ),
            budgetRule,
            toAccount,
            fromAccount
        )
    }

    TransactionEditScreen(
        date = date,
        onDateChange = { date = it },
        description = description,
        onDescriptionChange = { description = it },
        note = note,
        onNoteChange = { note = it },
        amount = amount,
        onAmountChange = { amount = it },
        toAccount = toAccount,
        fromAccount = fromAccount,
        budgetRule = budgetRule,
        toPending = toPending,
        onToPendingChange = { toPending = it },
        fromPending = fromPending,
        onFromPendingChange = { fromPending = it },
        allowToPending = toAccountWithType?.accountType?.allowPending == true,
        allowFromPending = fromAccountWithType?.accountType?.allowPending == true,
        onSaveClick = {
            val trans = getTransactionDetailed().transaction!!
            if (trans.transName.isBlank()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_enter_a_name_or_description),
                    Toast.LENGTH_LONG
                ).show()
            } else if (trans.transToAccountId == 0L) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to),
                    Toast.LENGTH_LONG
                ).show()
            } else if (trans.transFromAccountId == 0L) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from),
                    Toast.LENGTH_LONG
                ).show()
            } else if (trans.transAmount == 0.0) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                var display = mainActivity.getString(R.string.this_will_perform) + trans.transName +
                        mainActivity.getString(R.string._for_) + nf.getDollarsFromDouble(trans.transAmount) +
                        mainActivity.getString(R.string.__from) + "${fromAccount?.accountName} "
                if (trans.transFromAccountPending) display += mainActivity.getString(R.string._pending)
                display += mainActivity.getString(R.string._to) + " ${toAccount?.accountName}"
                if (trans.transToAccountPending) display += mainActivity.getString(R.string._pending)

                AlertDialog.Builder(mainActivity)
                    .setTitle(mainActivity.getString(R.string.confirm_performing_transaction))
                    .setMessage(display)
                    .setPositiveButton(mainActivity.getString(R.string.confirm)) { _, _ ->
                        mainActivity.lifecycleScope.launch {
                            mainActivity.accountUpdateViewModel.performTransaction(trans)
                            mainViewModel.removeCallingFragment(TAG)
                            mainViewModel.setTransactionDetailed(null)
                            mainViewModel.setBudgetRuleDetailed(null)
                            navController.popBackStack()
                        }
                    }
                    .setNegativeButton(mainActivity.getString(R.string.go_back), null)
                    .show()
            }
        },
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        onChooseFromAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onChooseToAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onSplitClick = {
            mainViewModel.setSplitTransactionDetailed(null)
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.TransactionSplit.route)
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount))
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        isSplitEnabled = fromAccount != null && nf.getDoubleFromDollars(amount) > 2.0
    )
}