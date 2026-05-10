package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_ACCOUNT_UPDATE

@Composable
fun AccountUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val transactionViewModel = mainActivity.transactionViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    remember { VisualsFunctions() }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_account)
    }

    val accountWithTypeState = remember { mutableStateOf(mainViewModel.getAccountWithType()) }
    val accountNames by accountViewModel.getAccountNameList().observeAsState(emptyList())

    val initialAccount = accountWithTypeState.value?.account
    var name by remember { mutableStateOf(initialAccount?.accountName ?: "") }
    var handle by remember { mutableStateOf(initialAccount?.accountNumber ?: "") }
    var balance by remember {
        mutableStateOf(
            nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                        ?.contains(BALANCE) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    initialAccount?.accountBalance ?: 0.0
                }
            )
        )
    }
    var owing by remember {
        mutableStateOf(
            nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                        ?.contains(OWING) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    initialAccount?.accountOwing ?: 0.0
                }
            )
        )
    }
    var budgeted by remember {
        mutableStateOf(
            nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                        ?.contains(BUDGETED) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    initialAccount?.accBudgetedAmount ?: 0.0
                }
            )
        )
    }
    var limit by remember {
        mutableStateOf(nf.displayDollars(initialAccount?.accountCreditLimit ?: 0.0))
    }

    val accountId = initialAccount?.accountId ?: 0L
    val history by transactionViewModel.getActiveTransactionByAccount(accountId)
        .observeAsState(emptyList())

    val liveAccountWithType by accountViewModel.getAccountWithTypeLive(accountId)
        .observeAsState()

    LaunchedEffect(liveAccountWithType) {
        liveAccountWithType?.let { awt ->
            if (awt.accountType?.keepTotals == true) {
                balance = nf.displayDollars(awt.account.accountBalance)
            } else if (awt.accountType?.tallyOwing == true) {
                owing = nf.displayDollars(awt.account.accountOwing)
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.setTransferNum(0.0)
    }

    fun getUpdatedAccount(): Account {
        return Account(
            mainViewModel.getAccountWithType()!!.account.accountId,
            name.trim(),
            handle.trim(),
            mainViewModel.getAccountWithType()!!.accountType?.typeId ?: 0L,
            nf.getDoubleFromDollars(budgeted),
            nf.getDoubleFromDollars(balance),
            nf.getDoubleFromDollars(owing),
            nf.getDoubleFromDollars(limit),
            false,
            df.getCurrentTimeAsString()
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
                delay(WAIT_500)
            }
        }
    }

    fun confirmCompletePendingTransactions(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        var display = mainActivity.getString(R.string.this_will_apply_the_amount_of) +
                nf.displayDollars(transaction.transAmount)
        display += if (transaction.transToAccountPending) {
            mainActivity.getString(R.string.to_) + (transactionDetailed.toAccount?.accountName
                ?: "")
        } else ""
        display += if (transaction.transToAccountPending && transaction.transFromAccountPending) {
            mainActivity.getString(R.string._and)
        } else ""
        display += if (transaction.transFromAccountPending) {
            mainActivity.getString(R.string.from) + (transactionDetailed.fromAccount?.accountName
                ?: "")
        } else ""

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
            delay(WAIT_500)
        }
    }

    fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        AlertDialog.Builder(mainActivity).setTitle(
            mainActivity.getString(R.string.are_you_sure_you_want_to_delete) + transaction.transName
        ).setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
            deleteTransaction(transaction)
        }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
    }

    fun showTransactionOptions(transactionDetailed: TransactionDetailed) {
        var display = ""
        val transaction = transactionDetailed.transaction ?: return

        if (transaction.transToAccountPending) {
            display += mainActivity.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transaction.transAmount
            ) + mainActivity.getString(R.string._to_) + (transactionDetailed.toAccount?.accountName
                ?: "")
        }
        if (transaction.transToAccountPending) {
            display += mainActivity.getString(R.string._pending)
        }
        if (display != "" && transaction.transFromAccountPending) {
            display += mainActivity.getString(R.string._and)
        }
        if (transaction.transFromAccountPending) {
            display += mainActivity.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transaction.transAmount
            ) + mainActivity.getString(R.string._From_) + (transactionDetailed.fromAccount?.accountName
                ?: "")
        }

        val options = mutableListOf(
            mainActivity.getString(R.string.edit_this_transaction),
            display,
            mainActivity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
            mainActivity.getString(R.string.delete_this_transaction)
        )

        AlertDialog.Builder(mainActivity).setTitle(
            mainActivity.getString(R.string.choose_an_action_for) + transaction.transName
        ).setItems(options.toTypedArray()) { _, pos ->
            when (pos) {
                0 -> {
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
                            delay(WAIT_500)
                            navController.navigate(Screen.TransactionUpdate.route)
                        }
                    }
                }

                1 -> if (transaction.transToAccountPending || transaction.transFromAccountPending) {
                    confirmCompletePendingTransactions(transactionDetailed)
                }

                2 -> {
                    mainViewModel.setCallingFragments(TAG)
                    budgetRuleViewModel.getBudgetRuleFullLive(
                        transaction.transRuleId
                    ).observe(mainActivity) { bRuleDetailed ->
                        mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                        mainViewModel.addCallingFragment(TAG)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    }
                }

                3 -> confirmDeleteTransaction(transactionDetailed)
            }
        }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
    }

    AccountEditScreen(
        name = name,
        onNameChange = { name = it },
        handle = handle,
        onHandleChange = { handle = it },
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
        balance = balance,
        onBalanceChange = { balance = it },
        onBalanceIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(balance.ifBlank {
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
        owing = owing,
        onOwingChange = { owing = it },
        onOwingIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(owing.ifBlank {
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
        budgeted = budgeted,
        onBudgetedChange = { budgeted = it },
        onBudgetedIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(budgeted.ifBlank {
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
        limit = limit,
        onLimitChange = { limit = it },
        accountId = accountId,
        history = history,
        onHistoryItemClick = { showTransactionOptions(it) },
        onSaveClick = {
            val answer = if (name.isBlank()) {
                mainActivity.getString(R.string.please_enter_a_name)
            } else if (accountNames.any { it == name && it != mainViewModel.getAccountWithType()!!.account.accountName }) {
                mainActivity.getString(R.string.this_budget_rule_already_exists)
            } else if (mainViewModel.getAccountWithType()?.accountType == null) {
                mainActivity.getString(R.string.please_choose_an_account_type)
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                val accountWithType = mainViewModel.getAccountWithType()!!
                if (name.trim() == accountWithType.account.accountName.trim()) {
                    accountViewModel.updateAccount(getUpdatedAccount())
                    mainViewModel.removeCallingFragment(TAG)
                    mainViewModel.setAccountWithType(null)
                    navController.popBackStack()
                } else {
                    AlertDialog.Builder(mainActivity).apply {
                        setTitle(mainActivity.getString(R.string.rename_account))
                        setMessage(
                            mainActivity.getString(R.string.are_you_sure_you_want_to_rename_this_account) +
                                    mainActivity.getString(R.string.note) +
                                    mainActivity.getString(R.string.this_will_not_replace_an_existing_account_type)
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
        nf = nf,
        df = df
    )
}