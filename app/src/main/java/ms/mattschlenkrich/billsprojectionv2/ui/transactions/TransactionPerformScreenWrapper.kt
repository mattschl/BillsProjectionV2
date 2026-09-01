package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionPerformScreen

private const val TAG = SCREEN_TRANSACTION_PERFORM

@Composable
fun TransactionPerformScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel
    val budgetItemViewModel = mainActivity.budgetItemViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val state = rememberTransactionEditState(nf, df)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.title_perform_transaction)

        val cachedTrans = mainViewModel.getTransactionDetailed()
        val cachedBudgetItem = mainViewModel.getBudgetItemDetailed()

        if (cachedTrans != null) {
            state.updateFrom(cachedTrans, mainViewModel.getTransferNum())
            mainViewModel.setTransferNum(0.0)

            state.toAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    state.toAccountWithType = it
                }
            }

            state.fromAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    state.fromAccountWithType = it
                }
            }
        } else if (cachedBudgetItem != null) {
            val budgetItem = cachedBudgetItem.budgetItem ?: return@LaunchedEffect
            state.date = df.getCurrentDateAsString()
            state.description = budgetItem.biBudgetName
            state.amount = nf.displayDollars(0.0)
            state.budgetRule = cachedBudgetItem.budgetRule
            state.toAccount = cachedBudgetItem.toAccount
            state.fromAccount = cachedBudgetItem.fromAccount

            state.toAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    state.toAccountWithType = it
                    if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                        state.toPending = true
                    }
                }
            }

            state.fromAccount?.let { acc ->
                accountViewModel.getAccountDetailed(acc.accountId).observe(mainActivity) {
                    state.fromAccountWithType = it
                    if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                        state.fromPending = true
                    }
                }
            }
        }
    }

    TransactionPerformScreen(
        date = state.date,
        onDateChange = { state.date = it },
        budgetRule = state.budgetRule,
        amount = state.amount,
        onAmountChange = { state.amount = it },
        onSplitClick = {
            mainViewModel.setSplitTransactionDetailed(null)
            mainViewModel.setTransferNum(0.0)
            if (state.fromAccount != null && nf.getDoubleFromDollars(state.amount) > 2.0) {
                mainViewModel.addCallingFragment(TAG)
                mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
                navController.navigate(Screen.TransactionSplit.route)
            }
        },
        budgetedAmount = nf.displayDollars(
            mainViewModel.getBudgetItemDetailed()?.budgetItem?.biProjectedAmount ?: 0.0
        ),
        onBudgetedAmountChange = {
            val amt = nf.getDoubleFromDollars(it)
            mainViewModel.getBudgetItemDetailed()?.let { detailed ->
                if (amt != detailed.budgetItem?.biProjectedAmount) {
                    detailed.budgetItem?.biProjectedAmount = amt
                    mainViewModel.setBudgetItemDetailed(detailed)
                }
            }
        },
        toAccount = state.toAccount,
        toPending = state.toPending,
        onToPendingChange = { state.toPending = it },
        allowToPending = state.toAccountWithType?.accountType?.allowPending == true,
        onToAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        fromAccount = state.fromAccount,
        fromPending = state.fromPending,
        onFromPendingChange = { state.fromPending = it },
        allowFromPending = state.fromAccountWithType?.accountType?.allowPending == true,
        onFromAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.AccountChoose.route)
        },
        onChooseBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        description = state.description,
        onDescriptionChange = { state.description = it },
        note = state.note,
        onNoteChange = { state.note = it },
        onSaveClick = {
            if (state.validate()) {
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
                            val budgetedAmount =
                                mainViewModel.getBudgetItemDetailed()?.budgetItem?.biProjectedAmount
                                    ?: 0.0
                            val rem = budgetedAmount - mTransaction.transAmount
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
                    .setNegativeButton(mainActivity.getString(R.string.action_go_back), null)
                    .show()
            } else {
                val errorMsg = when {
                    state.dateError -> mainActivity.getString(R.string.msg_prompt_choose_date)
                    state.descriptionError -> mainActivity.getString(R.string.msg_prompt_enter_description)
                    state.toAccountError -> mainActivity.getString(R.string.msg_error_no_dest_account)
                    state.fromAccountError -> mainActivity.getString(R.string.msg_error_no_source_account)
                    state.amountError -> mainActivity.getString(R.string.msg_prompt_enter_trans_amount)
                    else -> mainActivity.getString(R.string.label_error)
                }
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.label_error) + errorMsg,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onGotoCalculator = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.amount))
            mainViewModel.setTransactionDetailed(state.toTransactionDetailed())
            navController.navigate(Screen.Calculator.route)
        },
        isSplitEnabled = nf.getDoubleFromDollars(state.amount) > 2.0 && state.fromAccount != null,
        descriptionError = state.descriptionError,
        amountError = state.amountError,
        toAccountError = state.toAccountError,
        fromAccountError = state.fromAccountError,
    )
}