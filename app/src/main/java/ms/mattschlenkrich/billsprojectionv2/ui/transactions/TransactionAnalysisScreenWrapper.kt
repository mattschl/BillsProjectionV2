package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_TRANSACTION_ANALYSIS

@Composable
fun TransactionAnalysisScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val transactionViewModel = mainActivity.transactionViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel

    val nf = NumberFunctions()
    val df = DateFunctions()
    val coroutineScope = rememberCoroutineScope()

    var timeRange by remember { mutableStateOf(TimeRange.SHOW_ALL) }
    var isSearchEnabled by remember { mutableStateOf(false) }
    var searchQueryInput by remember { mutableStateOf("") }
    var searchQueryActual by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(df.getFirstOfMonth(df.getCurrentDateAsString())) }
    var endDate by remember { mutableStateOf(df.getCurrentDateAsString()) }

    val budgetRuleDetailed = mainViewModel.getBudgetRuleDetailed()
    val accountWithType = mainViewModel.getAccountWithType()

    val mode = when {
        isSearchEnabled -> AnalysisMode.SEARCH
        budgetRuleDetailed != null -> AnalysisMode.BUDGET_RULE
        accountWithType != null -> AnalysisMode.ACCOUNT
        else -> AnalysisMode.NONE
    }

    val effectiveStartDate = when (timeRange) {
        TimeRange.LAST_MONTH -> df.getFirstOfPreviousMonth(df.getCurrentDateAsString())
        TimeRange.DATE_RANGE -> startDate
        else -> ""
    }
    val effectiveEndDate = when (timeRange) {
        TimeRange.LAST_MONTH -> df.getLastOfPreviousMonth(df.getCurrentDateAsString())
        TimeRange.DATE_RANGE -> endDate
        else -> ""
    }

    val budgetRuleId = budgetRuleDetailed?.budgetRule?.ruleId ?: -1L
    val accountId = accountWithType?.account?.accountId ?: -1L
    val query = if (isSearchEnabled) "%$searchQueryActual%" else ""

    val transactionListResult by remember(
        budgetRuleId,
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        transactionViewModel.getTransactionsFiltered(
            budgetRuleId,
            accountId,
            query,
            effectiveStartDate,
            effectiveEndDate
        )
    }.observeAsState(emptyList())
    val transactionList = transactionListResult ?: emptyList()

    val sumToAccount by remember(
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        if (accountId != -1L) {
            transactionViewModel.getSumToAccountFiltered(
                accountId,
                query,
                effectiveStartDate,
                effectiveEndDate
            )
        } else {
            MutableLiveData(null)
        }
    }.observeAsState(null)

    val sumFromAccount by remember(
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        if (accountId != -1L) {
            transactionViewModel.getSumFromAccountFiltered(
                accountId,
                query,
                effectiveStartDate,
                effectiveEndDate
            )
        } else {
            MutableLiveData(null)
        }
    }.observeAsState(null)

    val sumCredits by remember(
        budgetRuleId,
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        transactionViewModel.getSumFiltered(
            budgetRuleId,
            accountId,
            query,
            effectiveStartDate,
            effectiveEndDate
        )
    }.observeAsState(null)

    val maxVal by remember(
        budgetRuleId,
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        transactionViewModel.getMaxFiltered(
            budgetRuleId,
            accountId,
            query,
            effectiveStartDate,
            effectiveEndDate
        )
    }.observeAsState(null)

    val minVal by remember(
        budgetRuleId,
        accountId,
        query,
        effectiveStartDate,
        effectiveEndDate
    ) {
        transactionViewModel.getMinFiltered(
            budgetRuleId,
            accountId,
            query,
            effectiveStartDate,
            effectiveEndDate
        )
    }.observeAsState(null)

    TransactionAnalysisScreen(
        timeRange = timeRange,
        onTimeRangeChange = { timeRange = it },
        isSearchEnabled = isSearchEnabled,
        onSearchToggle = { isSearchEnabled = it },
        searchQueryInput = searchQueryInput,
        onSearchQueryChange = { searchQueryInput = it },
        onSearchGo = { searchQueryActual = searchQueryInput },
        startDate = startDate,
        onStartDateChange = { startDate = it },
        endDate = endDate,
        onEndDateChange = { endDate = it },
        onDateRangeGo = { /* State update triggers re-observation */ },
        budgetRuleName = budgetRuleDetailed?.budgetRule?.budgetRuleName
            ?: mainActivity.getString(R.string.no_budget_rule_selected),
        accountName = accountWithType?.account?.accountName
            ?: mainActivity.getString(R.string.no_account_selected),
        mode = mode,
        transactionList = transactionList,
        sumToAccount = sumToAccount,
        sumFromAccount = sumFromAccount,
        sumCredits = sumCredits,
        maxVal = maxVal,
        minVal = minVal,
        effectiveEndDate = effectiveEndDate.ifBlank { df.getCurrentDateAsString() },
        onBudgetRuleClick = {
            mainViewModel.eraseAll()
            mainViewModel.setCallingFragments(TAG)
            navController.navigate(Screen.BudgetRuleChoose.route)
        },
        onAccountClick = {
            mainViewModel.eraseAll()
            mainViewModel.setCallingFragments(TAG)
            navController.navigate(Screen.AccountChoose.route)
        },
        onTransactionClick = { transactionDetailed ->
            var display = ""
            if (transactionDetailed.transaction!!.transToAccountPending) {
                display += mainActivity.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                    transactionDetailed.transaction.transAmount
                ) + mainActivity.getString(R.string._to_) + (transactionDetailed.toAccount?.accountName
                    ?: "")
            }
            if (transactionDetailed.transaction.transToAccountPending) {
                display += mainActivity.getString(R.string._pending)
            }
            if (display != "" && transactionDetailed.transaction.transFromAccountPending) {
                display += mainActivity.getString(R.string._and)
            }
            if (transactionDetailed.transaction.transFromAccountPending) {
                display += mainActivity.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                    transactionDetailed.transaction.transAmount
                ) + mainActivity.getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName
            }
            AlertDialog.Builder(mainActivity).setTitle(
                mainActivity.getString(R.string.choose_an_action_for) + transactionDetailed.transaction.transName
            ).setItems(
                arrayOf(
                    mainActivity.getString(R.string.edit_this_transaction),
                    display,
                    mainActivity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                    mainActivity.getString(R.string.delete_this_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        mainViewModel.addCallingFragment(TAG)
                        mainViewModel.setTransactionDetailed(transactionDetailed)
                        coroutineScope.launch(Dispatchers.IO) {
                            val oldTransactionFull = async {
                                transactionViewModel.getTransactionFull(
                                    transactionDetailed.transaction.transId,
                                    transactionDetailed.transaction.transToAccountId,
                                    transactionDetailed.transaction.transFromAccountId
                                )
                            }
                            mainViewModel.setOldTransaction(oldTransactionFull.await())
                            launch(Dispatchers.Main) {
                                delay(WAIT_250)
                                navController.navigate(Screen.TransactionUpdate.route)
                            }
                        }
                    }

                    1 -> {
                        if (transactionDetailed.transaction.transToAccountPending || transactionDetailed.transaction.transFromAccountPending) {
                            transactionDetailed.transaction.apply {
                                val newTransaction = Transactions(
                                    transId,
                                    transDate,
                                    transName,
                                    transNote,
                                    transRuleId,
                                    transToAccountId,
                                    false,
                                    transFromAccountId,
                                    false,
                                    transAmount,
                                    transIsDeleted,
                                    transUpdateTime
                                )
                                coroutineScope.launch(Dispatchers.IO) {
                                    accountUpdateViewModel.updateTransaction(
                                        transactionDetailed.transaction, newTransaction
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        mainViewModel.setCallingFragments(TAG)
                        budgetRuleViewModel.getBudgetRuleFullLive(
                            transactionDetailed.transaction.transRuleId
                        ).observe(mainActivity) { bRuleDetailed ->
                            mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                            navController.navigate(Screen.BudgetRuleUpdate.route)
                        }
                    }

                    3 -> {
                        AlertDialog.Builder(mainActivity).setTitle(
                            mainActivity.getString(R.string.are_you_sure_you_want_to_delete) + transactionDetailed.transaction.transName
                        ).setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
                            coroutineScope.launch(Dispatchers.IO) {
                                accountUpdateViewModel.deleteTransaction(
                                    transactionDetailed.transaction
                                )
                            }
                        }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
                    }
                }
            }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
        }
    )
}