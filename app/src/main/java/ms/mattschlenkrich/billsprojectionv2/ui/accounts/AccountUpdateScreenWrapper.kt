package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountEditScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_ACCOUNT_UPDATE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val transactionViewModel = mainActivity.transactionViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val actionSheetState = rememberActionSheetState()
    val state = rememberAccountEditState(nf, df)

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_account)
    }

    val accountWithTypeState = remember { mutableStateOf(mainViewModel.getAccountWithType()) }
    val accountNames by accountViewModel.getAccountNameList().observeAsState(emptyList())

    val initialAccount = accountWithTypeState.value?.account
    val accountId = initialAccount?.accountId ?: 0L

    val history by transactionViewModel.getActiveTransactionByAccount(accountId)
        .observeAsState(emptyList())

    val liveAccountWithType by accountViewModel.getAccountWithTypeLive(accountId)
        .observeAsState()

    LaunchedEffect(liveAccountWithType) {
        liveAccountWithType?.let { awt ->
            if (awt.accountType?.keepTotals == true) {
                state.balance = nf.displayDollars(awt.account.accountBalance)
            } else if (awt.accountType?.tallyOwing == true) {
                state.owing = nf.displayDollars(awt.account.accountOwing)
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.setTransferNum(0.0)
        state.updateFrom(
            accountWithTypeState.value,
            mainViewModel.getTransferNum(),
            mainViewModel.getReturnTo()
        )
    }

    fun getUpdatedAccount(): Account {
        val currentAwt = mainViewModel.getAccountWithType()!!
        return state.toAccount(
            currentAwt.account.accountId,
            currentAwt.accountType?.typeId ?: 0L
        )
    }

    fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        mainActivity.lifecycleScope.launch(Dispatchers.Main) {
            val newTransaction = Transactions(
                transaction.transId,
                transaction.transDate,
                transaction.transName,
                transaction.transNote,
                transaction.transRuleId,
                transaction.transToAccountId,
                false,
                transaction.transFromAccountId,
                false,
                transaction.transAmount,
                transaction.transIsDeleted,
                df.getCurrentTimeAsString()
            )
            mainActivity.lifecycleScope.launch {
                mainActivity.accountUpdateViewModel.updateTransaction(
                    transaction, newTransaction
                )
            }
        }
    }

    fun confirmCompletePendingTransactions(transactionDetailed: TransactionDetailed) {
        val display = TransactionMessageHelper.buildPendingCompletionMessage(
            mainActivity, transactionDetailed, nf
        )

        AlertDialog.Builder(mainActivity)
            .setTitle(mainActivity.getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(mainActivity.getString(R.string.confirm)) { _, _ ->
                completePendingTransactions(transactionDetailed)
            }
            .setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
    }

    fun deleteTransaction(transaction: Transactions) {
        mainActivity.lifecycleScope.launch {
            mainActivity.accountUpdateViewModel.deleteTransaction(transaction)
        }
    }

    fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        AlertDialog.Builder(mainActivity).setTitle(
            "${mainActivity.getString(R.string.are_you_sure_you_want_to_delete)}${transaction.transName}"
        ).setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
            deleteTransaction(transaction)
        }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
    }

    fun showTransactionOptions(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        val display = TransactionMessageHelper.buildPendingCompletionMessage(
            mainActivity, transactionDetailed, nf
        )

        val options = mutableListOf(
            ActionOption(
                mainActivity.getString(R.string.edit_this_transaction),
                Icons.Default.Edit
            ) {
                mainViewModel.setCallingFragments(TAG)
                mainViewModel.setTransactionDetailed(transactionDetailed)
                mainActivity.lifecycleScope.launch(Dispatchers.IO) {
                    val oldTransactionFull = transactionViewModel.getTransactionFull(
                        transaction.transId,
                        transaction.transToAccountId,
                        transaction.transFromAccountId
                    )
                    mainViewModel.setOldTransaction(oldTransactionFull)
                    withContext(Dispatchers.Main) {
                        navController.navigate(Screen.TransactionUpdate.route)
                    }
                }
            },
            ActionOption(display, Icons.Default.Check) {
                if (transaction.transToAccountPending || transaction.transFromAccountPending) {
                    confirmCompletePendingTransactions(transactionDetailed)
                }
            },
            ActionOption(
                mainActivity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                Icons.AutoMirrored.Filled.Rule
            ) {
                mainViewModel.setCallingFragments(TAG)
                budgetRuleViewModel.getBudgetRuleFullLive(
                    transaction.transRuleId
                ).observe(mainActivity) { bRuleDetailed ->
                    mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                    mainViewModel.addCallingFragment(TAG)
                    navController.navigate(Screen.BudgetRuleUpdate.route)
                }
            },
            ActionOption(
                mainActivity.getString(R.string.delete_this_transaction),
                Icons.Default.Delete
            ) {
                confirmDeleteTransaction(transactionDetailed)
            }
        )

        actionSheetState.show(
            "${mainActivity.getString(R.string.choose_an_action_for)}${transaction.transName}",
            options
        )
    }

    AccountEditScreen(
        name = state.name,
        onNameChange = { state.name = it },
        handle = state.handle,
        onHandleChange = { state.handle = it },
        accountType = accountWithTypeState.value?.accountType,
        onAccountTypeClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getUpdatedAccount(),
                    mainViewModel.getAccountWithType()!!.accountType
                )
            )
            navController.navigate(Screen.AccountTypes.route)
        },
        accountTypeDetails = "",
        balance = state.balance,
        onBalanceChange = { state.balance = it },
        onBalanceIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.balance.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getUpdatedAccount(),
                    mainViewModel.getAccountWithType()!!.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        owing = state.owing,
        onOwingChange = { state.owing = it },
        onOwingIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.owing.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getUpdatedAccount(),
                    mainViewModel.getAccountWithType()!!.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        budgeted = state.budgeted,
        onBudgetedChange = { state.budgeted = it },
        onBudgetedIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.budgeted.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getUpdatedAccount(),
                    mainViewModel.getAccountWithType()!!.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        limit = state.limit,
        onLimitChange = { state.limit = it },
        accountId = accountId,
        history = history,
        onHistoryItemClick = { showTransactionOptions(it) },
        onSaveClick = {
            val answer = if (state.name.isBlank()) {
                mainActivity.getString(R.string.please_enter_a_name)
            } else if (accountNames.any { it == state.name && it != mainViewModel.getAccountWithType()!!.account.accountName }) {
                mainActivity.getString(R.string.this_budget_rule_already_exists)
            } else if (mainViewModel.getAccountWithType()?.accountType == null) {
                mainActivity.getString(R.string.please_choose_an_account_type)
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                val accountWithType = mainViewModel.getAccountWithType()!!
                if (state.name.trim() == accountWithType.account.accountName.trim()) {
                    accountViewModel.updateAccount(getUpdatedAccount())
                    mainViewModel.removeCallingFragment(TAG)
                    mainViewModel.setAccountWithType(null)
                    navController.popBackStack()
                } else {
                    AlertDialog.Builder(mainActivity).apply {
                        setTitle(mainActivity.getString(R.string.rename_account))
                        setMessage(
                            "${mainActivity.getString(R.string.are_you_sure_you_want_to_rename_this_account)}${
                                mainActivity.getString(
                                    R.string.note
                                )
                            }${mainActivity.getString(R.string.this_will_not_replace_an_existing_account_type)}"
                        )
                        setPositiveButton(mainActivity.getString(R.string.update_account)) { _, _ ->
                            accountViewModel.updateAccount(getUpdatedAccount())
                            mainViewModel.removeCallingFragment(TAG)
                            mainViewModel.setAccountWithType(null)
                            navController.popBackStack()
                        }
                        setNegativeButton(mainActivity.getString(R.string.cancel), null)
                    }.create().show()
                }
            } else {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
        onSheetDismiss = {
            actionSheetState.dismiss()
        }
    )
    ManagedActionBottomSheet(actionSheetState)
}