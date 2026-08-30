package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionEditScreen

private const val TAG = SCREEN_TRANS_ADD

@Composable
fun TransactionAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val state = rememberTransactionEditState(nf, df)

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_a_new_transaction)
    }

    LaunchedEffect(Unit) {
        val cached = mainViewModel.getTransactionDetailed()
        if (cached != null) {
            state.updateFrom(cached, mainViewModel.getTransferNum())
            val ruleChanged =
                cached.budgetRule?.ruleId != cached.transaction?.transRuleId

            cached.toAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                state.toAccountWithType = awt
                if (ruleChanged) {
                    state.toPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }
            cached.fromAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                state.fromAccountWithType = awt
                if (ruleChanged) {
                    state.fromPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }

            val rule = cached.budgetRule
            if (state.toAccount == null && rule?.budToAccountId != 0L && rule != null) {
                val acc = accountViewModel.getAccount(rule.budToAccountId)
                state.toAccount = acc
                val awt = accountViewModel.getAccountWithType(acc.accountId)
                state.toAccountWithType = awt
                state.toPending = awt.accountType?.allowPending == true &&
                        awt.accountType.tallyOwing == true
            }
            if (state.fromAccount == null && rule?.budFromAccountId != 0L && rule != null) {
                val acc = accountViewModel.getAccount(rule.budFromAccountId)
                state.fromAccount = acc
                val awt = accountViewModel.getAccountWithType(acc.accountId)
                state.fromAccountWithType = awt
                state.fromPending = awt.accountType?.allowPending == true &&
                        awt.accountType.tallyOwing == true
            }
            mainViewModel.setRequestedAccount("")
            mainViewModel.setTransferNum(0.0)
        }
    }

    TransactionEditScreen(
        date = state.date,
        onDateChange = { state.date = it },
        description = state.description,
        onDescriptionChange = { state.description = it },
        note = state.note,
        onNoteChange = { state.note = it },
        amount = state.amount,
        onAmountChange = { state.amount = it },
        toAccount = state.toAccount,
        fromAccount = state.fromAccount,
        budgetRule = state.budgetRule,
        toPending = state.toPending,
        onToPendingChange = { state.toPending = it },
        fromPending = state.fromPending,
        onFromPendingChange = { state.fromPending = it },
        allowToPending = state.toAccountWithType?.accountType?.allowPending == true,
        allowFromPending = state.fromAccountWithType?.accountType?.allowPending == true,
        descriptionError = state.descriptionError,
        amountError = state.amountError,
        toAccountError = state.toAccountError,
        fromAccountError = state.fromAccountError,
        onSaveClick = {
            val valid = state.validate()
            val trans = state.toTransactions()

            if (state.dateError) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_choose_a_date),
                    Toast.LENGTH_LONG
                ).show()
            } else if (state.descriptionError) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to),
                    Toast.LENGTH_LONG
                ).show()
            } else if (state.fromAccountError) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from),
                    Toast.LENGTH_LONG
                ).show()
            } else if (state.amountError) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction),
                    Toast.LENGTH_LONG
                ).show()
            } else if (valid) {
                val transactionDetailed = state.toTransactionDetailed()
                val display = TransactionMessageHelper.buildConfirmationMessage(
                    mainActivity, transactionDetailed, nf
                )

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
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        onChooseFromAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onChooseToAccount = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onSplitClick = {
            mainViewModel.setSplitTransactionDetailed(null)
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.TransactionSplit.route)
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.amount))
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        isSplitEnabled = state.fromAccount != null && nf.getDoubleFromDollars(state.amount) > 2.0,
        splitButtonText = stringResource(R.string.split)
    )
}