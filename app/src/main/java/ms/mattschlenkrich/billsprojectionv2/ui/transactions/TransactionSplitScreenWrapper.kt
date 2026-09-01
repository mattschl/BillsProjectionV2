package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionSplitScreen

private const val TAG = SCREEN_TRANSACTION_SPLIT

@Composable
fun TransactionSplitScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val state = rememberTransactionEditState(nf, df)

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.title_splitting_transaction)
    }

    var originalAmount by remember { mutableDoubleStateOf(0.0) }

    fun updateAmountsDisplay() {
        val amt = nf.getDoubleFromDollars(state.amount)
        val original = originalAmount
        if (original < amt) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.label_error) + mainActivity.getString(R.string.msg_error_amount_too_high),
                Toast.LENGTH_LONG
            ).show()
            state.amount = nf.displayDollars(0.0)
        }
    }

    LaunchedEffect(Unit) {
        val mTransactionDetailed = mainViewModel.getTransactionDetailed()
        if (mTransactionDetailed != null) {
            val transaction = mTransactionDetailed.transaction ?: return@LaunchedEffect
            originalAmount = transaction.transAmount
            state.date = transaction.transDate
            state.fromAccount = mTransactionDetailed.fromAccount
            state.fromPending = transaction.transFromAccountPending

            val fromAccount = state.fromAccount ?: return@LaunchedEffect
            val accountWithType = accountViewModel.getAccountWithType(fromAccount.accountId)
            state.fromAccountWithType = accountWithType
            updateAmountsDisplay()
        }

        val splitDetailed = mainViewModel.getSplitTransactionDetailed()
        if (splitDetailed != null) {
            state.updateFrom(splitDetailed, mainViewModel.getTransferNum())
            updateAmountsDisplay()

            if (state.toAccount != null) {
                val toAccount = state.toAccount ?: return@LaunchedEffect
                val accountWithType =
                    accountViewModel.getAccountWithType(toAccount.accountId)
                state.toAccountWithType = accountWithType
            }
            state.budgetRule?.let { rule ->
                if (state.description.isBlank()) {
                    state.description = rule.budgetRuleName
                }
                if (state.toAccount == null) {
                    val ruleFull =
                        budgetRuleViewModel.getBudgetRuleDetailed(rule.ruleId)
                    if (ruleFull != null) {
                        state.toAccount = ruleFull.toAccount
                        state.toAccount?.let { toAcc ->
                            val accountWithType =
                                accountViewModel.getAccountWithType(toAcc.accountId)
                            state.toAccountWithType = accountWithType
                            if (accountWithType.accountType?.allowPending == true) {
                                state.toPending =
                                    splitDetailed.transaction?.transToAccountPending ?: false
                            }
                        }
                    }
                }
            }
        }
    }

    TransactionSplitScreen(
        date = state.date,
        onDateChange = { state.date = it },
        budgetRule = state.budgetRule,
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setSplitTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        amount = state.amount,
        onAmountChange = {
            state.amount = it
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.amount.ifBlank {
                mainActivity.getString(
                    R.string.val_zero_double
                )
            }))
            mainViewModel.setSplitTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        originalAmount = originalAmount,
        toAccount = state.toAccount,
        onChooseToAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setSplitTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        toPending = state.toPending,
        onToPendingChange = { state.toPending = it },
        allowToPending = state.toAccountWithType?.accountType?.allowPending == true,
        fromAccount = state.fromAccount,
        fromPending = state.fromPending,
        onFromPendingChange = { state.fromPending = it },
        allowFromPending = state.fromAccountWithType?.accountType?.allowPending == true,
        onFromAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setSplitTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        description = state.description,
        onDescriptionChange = { state.description = it },
        note = state.note,
        onNoteChange = { state.note = it },
        descriptionError = state.descriptionError,
        amountError = state.amountError,
        toAccountError = state.toAccountError,
        fromAccountError = state.fromAccountError,
        onSaveClick = {
            val amt = nf.getDoubleFromDollars(state.amount)
            val valid = state.validate()

            val answer = if (state.date.isBlank()) {
                mainActivity.getString(R.string.msg_prompt_choose_date)
            } else if (state.amount.isBlank()) {
                mainActivity.getString(R.string.msg_error_split_amount_high)
            } else if (state.description.isBlank()) {
                mainActivity.getString(R.string.msg_prompt_enter_description)
            } else if (state.toAccount == null) {
                mainActivity.getString(R.string.msg_error_no_dest_account)
            } else if (state.budgetRule == null) {
                AlertDialog.Builder(mainActivity).apply {
                    setMessage(
                        mainActivity.getString(R.string.msg_no_budget_rule) + mainActivity.getString(
                            R.string.msg_budget_rules_purpose
                        )
                    )
                    setNegativeButton(mainActivity.getString(R.string.action_retry), null)
                }.create().show()
                "" // Return empty to indicate not valid but handled
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                val transactionDetailed = state.toTransactionDetailed()
                val display = TransactionMessageHelper.buildConfirmationMessage(
                    mainActivity, transactionDetailed, nf
                )
                AlertDialog.Builder(mainActivity)
                    .setTitle(mainActivity.getString(R.string.title_confirm_transaction))
                    .setMessage(display)
                    .setPositiveButton(mainActivity.getString(R.string.action_confirm)) { _, _ ->
                        val mTransaction = state.toTransactions()
                        mainActivity.lifecycleScope.launch {
                            accountUpdateViewModel.performTransaction(mTransaction)
                            val cachedDetailed =
                                mainViewModel.getTransactionDetailed() ?: return@launch
                            val oldTransaction = cachedDetailed.transaction ?: return@launch
                            oldTransaction.transAmount = originalAmount - amt
                            if (mainViewModel.getUpdatingTransaction()) {
                                accountUpdateViewModel.updateTransactionWithoutAccountUpdate(
                                    oldTransaction
                                )
                            }
                            mainViewModel.setTransactionDetailed(
                                TransactionDetailed(
                                    oldTransaction,
                                    cachedDetailed.budgetRule,
                                    cachedDetailed.toAccount,
                                    cachedDetailed.fromAccount
                                )
                            )
                            mainViewModel.setSplitTransactionDetailed(null)
                            mainViewModel.removeCallingFragment(TAG)
                            navController.popBackStack()
                        }
                    }.setNegativeButton(mainActivity.getString(R.string.action_go_back), null)
                    .show()
            } else if (answer.isNotEmpty()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.label_error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
    )
}