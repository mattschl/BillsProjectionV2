package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
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

private const val TAG = FRAG_TRANS_UPDATE

@Composable
fun TransactionUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel

    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var budgetRule by remember { mutableStateOf<BudgetRule?>(null) }
    var toPending by remember { mutableStateOf(false) }
    var fromPending by remember { mutableStateOf(false) }

    var toAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }
    var fromAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }

    var transactionId by remember { mutableLongStateOf(0L) }

    var descriptionError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var toAccountError by remember { mutableStateOf(false) }
    var fromAccountError by remember { mutableStateOf(false) }

    fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            transactionId,
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
        )
    }

    fun getCurrentTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRule,
            toAccount,
            fromAccount
        )
    }

    fun updateTransaction() {
        val oldTrans = mainViewModel.getOldTransaction()?.transaction
        if (oldTrans != null) {
            coroutineScope.launch {
                accountUpdateViewModel.updateTransaction(
                    oldTrans, getCurrentTransactionForSave()
                )
                mainViewModel.removeCallingFragment(TAG)
                mainViewModel.setOldTransaction(null)
                mainViewModel.setTransactionDetailed(null)
                navController.popBackStack()
            }
        }
    }

    fun confirmUpdateTransaction() {
        val trans = getCurrentTransactionForSave()
        var display = mainActivity.getString(R.string.this_will_perform) + trans.transName +
                mainActivity.getString(R.string._for_) + nf.getDollarsFromDouble(trans.transAmount) +
                mainActivity.getString(R.string.__from) + (fromAccount?.accountName ?: "")
        if (fromPending) display += mainActivity.getString(R.string.pending)
        display += mainActivity.getString(R.string._to) + (toAccount?.accountName ?: "")
        if (toPending) display += mainActivity.getString(R.string.pending)

        AlertDialog.Builder(mainActivity)
            .setTitle(mainActivity.getString(R.string.confirm_performing_transaction))
            .setMessage(display)
            .setPositiveButton(mainActivity.getString(R.string.confirm)) { _, _ ->
                updateTransaction()
            }
            .setNegativeButton(mainActivity.getString(R.string.go_back), null)
            .show()
    }

    fun updateWithoutBudget() {
        AlertDialog.Builder(mainActivity).apply {
            setMessage(
                mainActivity.getString(R.string.there_is_no_budget_rule) + mainActivity.getString(R.string.budget_rules_are_used_to_update_the_budget)
            )
            setPositiveButton(mainActivity.getString(R.string.save_anyway)) { _, _ ->
                confirmUpdateTransaction()
            }
            setNegativeButton(mainActivity.getString(R.string.retry), null)
        }.create().show()
    }

    fun updateTransactionIfValid() {
        descriptionError = description.isBlank()
        toAccountError = toAccount == null
        fromAccountError = fromAccount == null
        amountError = amount.isBlank() || nf.getDoubleFromDollars(amount) == 0.0

        if (descriptionError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.please_enter_a_name_or_description),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (toAccountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (fromAccountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (amountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (budgetRule == null) {
            updateWithoutBudget()
        } else {
            confirmUpdateTransaction()
        }
    }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_this_transaction)
        if (mainViewModel.getOldTransaction() != null && mainViewModel.getTransactionDetailed() == null) {
            val transFull = mainViewModel.getOldTransaction()!!
            val trans = transFull.transaction
            transactionId = trans.transId
            date = trans.transDate
            amount = nf.displayDollars(trans.transAmount)
            description = trans.transName
            note = trans.transNote
            budgetRule = transFull.budgetRule
            toAccount = transFull.toAccountAndType.account
            toPending = trans.transToAccountPending
            fromAccount = transFull.fromAccountAndType.account
            fromPending = trans.transFromAccountPending

            toAccountWithType = accountViewModel.getAccountWithType(trans.transToAccountId)
            fromAccountWithType = accountViewModel.getAccountWithType(trans.transFromAccountId)
        } else if (mainViewModel.getTransactionDetailed() != null) {
            val cached = mainViewModel.getTransactionDetailed()!!
            val trans = cached.transaction
            val rule = cached.budgetRule
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
                        if (mainViewModel.getTransferNum() != 0.0) mainViewModel.getTransferNum()!!
                        else trans.transAmount
                    )
                }
                note = trans.transNote
                toPending = trans.transToAccountPending
                fromPending = trans.transFromAccountPending
            }
            budgetRule = rule
            toAccount = cached.toAccount
            fromAccount = cached.fromAccount

            toAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                toAccountWithType = awt
                if (ruleChanged) {
                    toPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }
            fromAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                fromAccountWithType = awt
                if (ruleChanged) {
                    fromPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }
            mainViewModel.setTransferNum(0.0)
        }

        if (mainViewModel.getUpdatingTransaction()) {
            mainViewModel.setUpdatingTransaction(false)
            updateTransactionIfValid()
        }
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
        descriptionError = descriptionError,
        amountError = amountError,
        toAccountError = toAccountError,
        fromAccountError = fromAccountError,
        onSaveClick = { updateTransactionIfValid() },
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        onChooseFromAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onChooseToAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onSplitClick = {
            mainViewModel.setSplitTransactionDetailed(null)
            mainViewModel.setTransferNum(0.0)
            mainViewModel.setUpdatingTransaction(true)
            if (fromAccount != null && nf.getDoubleFromDollars(amount) > 2.0) {
                mainViewModel.addCallingFragment(TAG)
                mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
                navController.navigate(Screen.TransactionSplit.route)
            }
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount))
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        isSplitEnabled = fromAccount != null && nf.getDoubleFromDollars(amount) > 2.0,
        splitButtonText = stringResource(R.string.split)
    )
}