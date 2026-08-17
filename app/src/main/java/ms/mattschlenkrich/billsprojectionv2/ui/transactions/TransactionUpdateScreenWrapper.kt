package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionEditScreen

private const val TAG = FRAG_TRANS_UPDATE

@Composable
fun TransactionUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel

    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val state = rememberTransactionEditState(nf, df)
    val coroutineScope = rememberCoroutineScope()

    fun updateTransaction() {
        val oldTrans = mainViewModel.getOldTransaction()?.transaction
        if (oldTrans != null) {
            coroutineScope.launch {
                accountUpdateViewModel.updateTransaction(
                    oldTrans, state.toTransactions()
                )
                mainViewModel.removeCallingFragment(TAG)
                mainViewModel.setOldTransaction(null)
                mainViewModel.setTransactionDetailed(null)
                navController.popBackStack()
            }
        }
    }

    fun confirmUpdateTransaction() {
        val transactionDetailed = state.toTransactionDetailed()
        val display = TransactionMessageHelper.buildConfirmationMessage(
            mainActivity, transactionDetailed, nf
        )

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
        val valid = state.validate()

        if (state.descriptionError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.please_enter_a_name_or_description),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (state.toAccountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (state.fromAccountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (state.amountError) {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + mainActivity.getString(R.string.please_enter_an_amount_for_this_transaction),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (state.budgetRule == null) {
            updateWithoutBudget()
        } else if (valid) {
            confirmUpdateTransaction()
        }
    }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_this_transaction)
        if (mainViewModel.getOldTransaction() != null && mainViewModel.getTransactionDetailed() == null) {
            val transFull = mainViewModel.getOldTransaction()!!
            state.updateFrom(
                TransactionDetailed(
                    transFull.transaction,
                    transFull.budgetRule,
                    transFull.toAccountAndType.account,
                    transFull.fromAccountAndType.account
                )
            )

            state.toAccountWithType =
                accountViewModel.getAccountWithType(transFull.transaction.transToAccountId)
            state.fromAccountWithType =
                accountViewModel.getAccountWithType(transFull.transaction.transFromAccountId)
        } else if (mainViewModel.getTransactionDetailed() != null) {
            val cached = mainViewModel.getTransactionDetailed()!!
            state.updateFrom(cached, mainViewModel.getTransferNum())
            val ruleChanged =
                cached.budgetRule != null && cached.budgetRule!!.ruleId != cached.transaction?.transRuleId

            state.toAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                state.toAccountWithType = awt
                if (ruleChanged) {
                    state.toPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }
            state.fromAccount?.let {
                val awt = accountViewModel.getAccountWithType(it.accountId)
                state.fromAccountWithType = awt
                if (ruleChanged) {
                    state.fromPending = awt.accountType?.allowPending == true &&
                            awt.accountType.tallyOwing == true
                }
            }
            mainViewModel.setRequestedAccount("")
            mainViewModel.setTransferNum(0.0)
        }

        if (mainViewModel.getUpdatingTransaction()) {
            mainViewModel.setUpdatingTransaction(false)
            updateTransactionIfValid()
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
        onSaveClick = { updateTransactionIfValid() },
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
            mainViewModel.setTransferNum(0.0)
            mainViewModel.setUpdatingTransaction(true)
            if (state.fromAccount != null && nf.getDoubleFromDollars(state.amount) > 2.0) {
                mainViewModel.addCallingFragment(TAG)
                mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
                navController.navigate(Screen.TransactionSplit.route)
            }
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