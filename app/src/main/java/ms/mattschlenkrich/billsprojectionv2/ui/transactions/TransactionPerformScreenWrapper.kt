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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_TRANS_PERFORM

@Composable
fun TransactionPerformScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel
    val budgetItemViewModel = mainActivity.budgetItemViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.perform_a_transaction)
    }

    var date by remember { mutableStateOf(df.getCurrentDateAsString()) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var budgetedAmount by remember { mutableStateOf("") }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var budgetRule by remember { mutableStateOf<BudgetRule?>(null) }
    var toPending by remember { mutableStateOf(false) }
    var fromPending by remember { mutableStateOf(false) }
    var allowToPending by remember { mutableStateOf(false) }
    var allowFromPending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (mainViewModel.getTransactionDetailed() != null) {
            val detailed = mainViewModel.getTransactionDetailed()!!
            val trans = detailed.transaction
            if (trans != null) {
                budgetRule = detailed.budgetRule
                toAccount = detailed.toAccount
                fromAccount = detailed.fromAccount

                description = trans.transName
                note = trans.transNote
                date = trans.transDate

                val curAmount = if (mainViewModel.getTransferNum() != 0.0) {
                    mainViewModel.getTransferNum()!!
                } else {
                    trans.transAmount
                }
                amount = nf.displayDollars(curAmount)
                mainViewModel.setTransferNum(0.0)

                val bAmount =
                    mainViewModel.getBudgetItemDetailed()?.budgetItem?.biProjectedAmount ?: 0.0
                budgetedAmount = nf.displayDollars(bAmount)

                toPending = trans.transToAccountPending
                fromPending = trans.transFromAccountPending

                detailed.toAccount?.let { acc ->
                    accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                        allowToPending = it.accountType?.allowPending == true
                    }
                }

                detailed.fromAccount?.let { acc ->
                    accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                        allowFromPending = it.accountType?.allowPending == true
                    }
                }
            }
        } else if (mainViewModel.getBudgetItemDetailed() != null) {
            val detailed = mainViewModel.getBudgetItemDetailed()!!
            val budgetItem = detailed.budgetItem!!

            toAccount = detailed.toAccount
            fromAccount = detailed.fromAccount
            budgetRule = detailed.budgetRule

            description = budgetItem.biBudgetName
            date = df.getCurrentDateAsString()
            budgetedAmount = nf.displayDollars(budgetItem.biProjectedAmount)
            amount = nf.displayDollars(0.0)

            detailed.toAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    allowToPending = it.accountType?.allowPending == true
                    if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                        toPending = true
                    }
                }
            }

            detailed.fromAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    allowFromPending = it.accountType?.allowPending == true
                    if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                        fromPending = true
                    }
                }
            }
        }
    }

    fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            transId = nf.generateId(),
            transDate = date,
            transName = description,
            transNote = note,
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

    fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRule,
            toAccount,
            fromAccount
        )
    }

    TransactionPerformScreen(
        date = date,
        onDateChange = { date = it },
        budgetRule = budgetRule,
        amount = amount,
        onAmountChange = {
            amount = it
        },
        onSplitClick = {
            mainViewModel.setSplitTransactionDetailed(null)
            mainViewModel.setTransferNum(0.0)
            val amt = nf.getDoubleFromDollars(amount)
            if (fromAccount != null && amt > 2.0) {
                mainViewModel.addCallingFragment(TAG)
                mainViewModel.setTransactionDetailed(getTransactionDetailed())
                navController.navigate(Screen.TransactionSplit.route)
            }
        },
        budgetedAmount = budgetedAmount,
        onBudgetedAmountChange = {
            budgetedAmount = it
            val amt = nf.getDoubleFromDollars(budgetedAmount)
            mainViewModel.getBudgetItemDetailed()?.let { detailed ->
                if (amt != detailed.budgetItem?.biProjectedAmount) {
                    detailed.budgetItem?.biProjectedAmount = amt
                    mainViewModel.setBudgetItemDetailed(detailed)
                }
            }
        },
        toAccount = toAccount,
        toPending = toPending,
        onToPendingChange = { toPending = it },
        allowToPending = allowToPending,
        onToAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        fromAccount = fromAccount,
        fromPending = fromPending,
        onFromPendingChange = { fromPending = it },
        allowFromPending = allowFromPending,
        onFromAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        description = description,
        onDescriptionChange = { description = it },
        note = note,
        onNoteChange = { note = it },
        onSaveClick = {
            val amt = nf.getDoubleFromDollars(amount)
            val answer = if (description.isBlank()) {
                mainActivity.getString(R.string.please_enter_a_name_or_description)
            } else if (amt == 0.0) {
                mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction)
            } else if (fromAccount == null || toAccount == null) {
                " " + mainActivity.getString(R.string.choose_an_account)
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                var display =
                    mainActivity.getString(R.string.this_will_perform) + " " + description +
                            mainActivity.getString(R.string._for_) + " " + nf.displayDollars(amt) +
                            mainActivity.getString(R.string.__from) + " " + (fromAccount?.accountName
                        ?: "")
                if (fromPending) display += " " + mainActivity.getString(R.string.pending)
                display += " " + mainActivity.getString(R.string._to) + " " + (toAccount?.accountName
                    ?: "")
                if (toPending) display += " " + mainActivity.getString(R.string.pending)

                AlertDialog.Builder(mainActivity)
                    .setTitle(mainActivity.getString(R.string.confirm_performing_transaction))
                    .setMessage(display)
                    .setPositiveButton(mainActivity.getString(R.string.confirm)) { _, _ ->
                        val mTransaction = getCurrentTransactionForSave()
                        mainActivity.lifecycleScope.launch {
                            accountUpdateViewModel.performTransaction(mTransaction)
                            val rem = nf.getDoubleFromDollars(budgetedAmount) - amt
                            val completed = rem < 2.0
                            val detailed = mainViewModel.getBudgetItemDetailed()
                            if (detailed != null) {
                                val mBudget = detailed.budgetItem
                                if (mBudget != null) {
                                    budgetItemViewModel.updateBudgetItem(
                                        mBudget.copy(
                                            biProjectedAmount = rem,
                                            biIsCompleted = completed,
                                            biUpdateTime = df.getCurrentTimeAsString()
                                        )
                                    )
                                }
                            }
                            mainViewModel.removeCallingFragment(TAG)
                            mainViewModel.setTransactionDetailed(null)
                            mainViewModel.setBudgetRuleDetailed(null)
                            navController.popBackStack()
                        }
                    }
                    .setNegativeButton(mainActivity.getString(R.string.go_back), null)
                    .show()
            } else {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount))
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        isSplitEnabled = nf.getDoubleFromDollars(amount) > 2.0 && fromAccount != null,
    )
}