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
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
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

private const val TAG = FRAG_TRANSACTION_SPLIT

@Composable
fun TransactionSplitScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel
    val transactionViewModel = mainActivity.transactionViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.splitting_transaction)
    }

    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var originalAmount by remember { mutableStateOf(0.0) }

    var budgetRule by remember { mutableStateOf<BudgetRule?>(null) }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var fromAccount by remember { mutableStateOf<Account?>(null) }

    var toPending by remember { mutableStateOf(false) }
    var fromPending by remember { mutableStateOf(false) }

    var toAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }
    var fromAccountWithType by remember { mutableStateOf<AccountWithType?>(null) }

    fun updateAmountsDisplay() {
        val amt = nf.getDoubleFromDollars(amount)
        val original = originalAmount
        if (original < amt) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.new_amount_cannot_be_more_than_the_original_amount),
                Toast.LENGTH_LONG
            ).show()
            amount = nf.displayDollars(0.0)
        }
    }

    LaunchedEffect(Unit) {
        val mTransactionDetailed = mainViewModel.getTransactionDetailed()
        if (mTransactionDetailed != null) {
            val transaction = mTransactionDetailed.transaction!!
            originalAmount = transaction.transAmount
            date = transaction.transDate
            fromAccount = mTransactionDetailed.fromAccount!!
            fromPending = transaction.transFromAccountPending

            val accountWithType = accountViewModel.getAccountWithType(fromAccount!!.accountId)
            fromAccountWithType = accountWithType
            updateAmountsDisplay()
        }

        val splitDetailed = mainViewModel.getSplitTransactionDetailed()
        if (splitDetailed != null) {
            val transaction = splitDetailed.transaction!!
            description = transaction.transName
            note = transaction.transNote
            fromPending = transaction.transFromAccountPending

            if (mainViewModel.getTransferNum() != null && mainViewModel.getTransferNum() != 0.0) {
                amount = nf.displayDollars(mainViewModel.getTransferNum()!!)
            } else if (transaction.transAmount != 0.0) {
                amount = nf.displayDollars(transaction.transAmount)
            } else {
                amount = nf.displayDollars(0.0)
            }
            updateAmountsDisplay()

            if (splitDetailed.toAccount != null) {
                toAccount = splitDetailed.toAccount
                val accountWithType = accountViewModel.getAccountWithType(toAccount!!.accountId)
                toAccountWithType = accountWithType
                if (accountWithType.accountType!!.allowPending) {
                    toPending = splitDetailed.transaction!!.transToAccountPending
                }
            }
            if (splitDetailed.budgetRule != null) {
                budgetRule = splitDetailed.budgetRule
                if (description.isBlank()) {
                    description = budgetRule!!.budgetRuleName
                }
                if (toAccount == null) {
                    val ruleFull = budgetRuleViewModel.getBudgetRuleDetailed(budgetRule!!.ruleId)
                    if (ruleFull != null) {
                        toAccount = ruleFull.toAccount
                        val accountWithType =
                            accountViewModel.getAccountWithType(toAccount!!.accountId)
                        toAccountWithType = accountWithType
                        if (accountWithType.accountType!!.allowPending) {
                            toPending = splitDetailed.transaction!!.transToAccountPending
                        }
                    }
                }
            }
        }
    }

    fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            nf.generateId(),
            date,
            description,
            note,
            budgetRule?.ruleId ?: 0L,
            toAccount?.accountId ?: 0L,
            toPending,
            fromAccount?.accountId ?: 0L,
            fromPending,
            nf.getDoubleFromDollars(amount),
            transIsDeleted = false,
            transUpdateTime = df.getCurrentTimeAsString()
        )
    }

    fun getSplitTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRule,
            toAccount,
            fromAccount,
        )
    }

    TransactionSplitScreen(
        date = date,
        onDateChange = { date = it },
        budgetRule = budgetRule,
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        amount = amount,
        onAmountChange = {
            amount = it
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
                )
            }))
            mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        originalAmount = originalAmount,
        toAccount = toAccount,
        onChooseToAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        toPending = toPending,
        onToPendingChange = { toPending = it },
        allowToPending = toAccountWithType?.accountType?.allowPending == true,
        fromAccount = fromAccount,
        fromPending = fromPending,
        onFromPendingChange = { fromPending = it },
        allowFromPending = fromAccountWithType?.accountType?.allowPending == true,
        onFromAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        description = description,
        onDescriptionChange = { description = it },
        note = note,
        onNoteChange = { note = it },
        onSaveClick = {
            val amt = nf.getDoubleFromDollars(amount)
            val answer = if (amount.isBlank()) {
                mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction)
            } else if (amt >= originalAmount) {
                mainActivity.getString(R.string.the_amount_of_a_split_transaction_must_be_less_than_the_original)
            } else if (description.isBlank()) {
                mainActivity.getString(R.string.please_enter_a_name_or_description)
            } else if (toAccount == null) {
                mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            } else if (budgetRule == null) {
                AlertDialog.Builder(mainActivity).apply {
                    setMessage(
                        mainActivity.getString(R.string.there_is_no_budget_rule) + mainActivity.getString(
                            R.string.budget_rules_are_used_to_update_the_budget
                        )
                    )
                    setNegativeButton(mainActivity.getString(R.string.retry), null)
                }.create().show()
                "" // Return empty to indicate not valid but handled
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                var display =
                    mainActivity.getString(R.string.this_will_perform) + description + mainActivity.getString(
                        R.string._for_
                    ) + nf.getDollarsFromDouble(amt) + mainActivity.getString(R.string.__from) + fromAccount!!.accountName
                display += if (fromPending) mainActivity.getString(R.string._pending) else ""
                display += mainActivity.getString(R.string._to) + toAccount!!.accountName
                display += if (toPending) mainActivity.getString(R.string._pending) else ""
                AlertDialog.Builder(mainActivity)
                    .setTitle(mainActivity.getString(R.string.confirm_performing_transaction))
                    .setMessage(display)
                    .setPositiveButton(mainActivity.getString(R.string.confirm)) { _, _ ->
                        val mTransaction = getCurrentTransactionForSave()
                        mainActivity.lifecycleScope.launch {
                            accountUpdateViewModel.performTransaction(mTransaction)
                            val transactionDetailed = mainViewModel.getTransactionDetailed()!!
                            val oldTransaction = transactionDetailed.transaction!!
                            oldTransaction.transAmount = originalAmount - amt
                            if (mainViewModel.getUpdatingTransaction()) {
                                accountUpdateViewModel.updateTransactionWithoutAccountUpdate(
                                    oldTransaction
                                )
                            }
                            mainViewModel.setTransactionDetailed(
                                TransactionDetailed(
                                    oldTransaction,
                                    transactionDetailed.budgetRule,
                                    transactionDetailed.toAccount,
                                    transactionDetailed.fromAccount
                                )
                            )
                            mainViewModel.setSplitTransactionDetailed(null)
                            mainViewModel.removeCallingFragment(TAG)
                            navController.popBackStack()
                        }
                    }.setNegativeButton(mainActivity.getString(R.string.go_back), null).show()
            } else if (answer.isNotEmpty()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
    )
}