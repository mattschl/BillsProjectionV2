package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.AnalysisMode
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.TimeRange
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionAnalysisScreen

private const val TAG = FRAG_TRANSACTION_ANALYSIS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionAnalysisScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController,
) {
    val mainViewModel = mainActivity.mainViewModel
    val transactionViewModel = mainActivity.transactionViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val accountUpdateViewModel = mainActivity.accountUpdateViewModel

    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val coroutineScope = rememberCoroutineScope()
    val actionSheetState = rememberActionSheetState()
    val state = rememberTransactionEditState(nf, df)

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.transaction_analysis)
    }

    var timeRange by remember { mutableStateOf(TimeRange.LAST_YEAR) }
    var isSearchEnabled by remember { mutableStateOf(value = false) }
    var searchQueryInput by remember { mutableStateOf("") }
    var searchQueryActual by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(df.getFirstOfMonth(df.getCurrentDateAsString())) }
    var endDate by remember { mutableStateOf(df.getCurrentDateAsString()) }
    var selectedItems by remember { mutableStateOf(emptySet<Long>()) }

    LaunchedEffect(timeRange, isSearchEnabled, searchQueryActual, startDate, endDate) {
        selectedItems = emptySet()
    }

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
        TimeRange.LAST_YEAR -> df.getOneYearAgo(df.getCurrentDateAsString())
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
    val transactionList = transactionListResult

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

    val selectedSum = remember(selectedItems, transactionList) {
        if (selectedItems.isEmpty()) 0.0
        else {
            transactionList.asSequence().filter {
                selectedItems.contains(it.transaction?.transId)
            }.sumOf { it.transaction?.transAmount ?: 0.0 }
        }
    }

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
        onTransactionLongClick = { transactionDetailed ->
            transactionDetailed.transaction?.transId?.let { id ->
                selectedItems = if (selectedItems.contains(id)) {
                    selectedItems - id
                } else {
                    selectedItems + id
                }
            }
        },
        selectedItems = selectedItems,
        selectedSum = selectedSum,
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
            if (selectedItems.isNotEmpty()) {
                selectedItems = emptySet()
            } else {
                transactionDetailed.transaction?.let { trans ->
                    state.updateFrom(transactionDetailed)
                    val display = TransactionMessageHelper.buildPendingCompletionMessage(
                        mainActivity, transactionDetailed, nf
                    )

                    val options = listOf(
                        ActionOption(
                            mainActivity.getString(R.string.edit_this_transaction),
                            Icons.Default.Edit
                        ) {
                            mainViewModel.addCallingFragment(TAG)
                            mainViewModel.setTransactionDetailed(transactionDetailed)
                            coroutineScope.launch(Dispatchers.IO) {
                                val oldTransactionFull = async {
                                    transactionViewModel.getTransactionFull(
                                        trans.transId,
                                        trans.transToAccountId,
                                        trans.transFromAccountId
                                    )
                                }
                                mainViewModel.setOldTransaction(oldTransactionFull.await())
                                launch(Dispatchers.Main) {
                                    navController.navigate(Screen.TransactionUpdate.route)
                                }
                            }
                        },
                        ActionOption(display, Icons.Default.Check) {
                            val newTransaction = trans.copy(
                                transToAccountPending = false,
                                transFromAccountPending = false
                            )
                            coroutineScope.launch(Dispatchers.IO) {
                                accountUpdateViewModel.updateTransaction(
                                    trans, newTransaction
                                )
                            }
                        },
                        ActionOption(
                            mainActivity.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                            Icons.AutoMirrored.Filled.Rule
                        ) {
                            mainViewModel.setCallingFragments(TAG)
                            budgetRuleViewModel.getBudgetRuleFullLive(
                                trans.transRuleId
                            ).observe(mainActivity) { bRuleDetailed ->
                                mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                                navController.navigate(Screen.BudgetRuleUpdate.route)
                            }
                        },
                        ActionOption(
                            mainActivity.getString(R.string.delete_this_transaction),
                            Icons.Default.Delete
                        ) {
                            AlertDialog.Builder(mainActivity).setTitle(
                                "${mainActivity.getString(R.string.are_you_sure_you_want_to_delete)}${trans.transName}"
                            ).setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    accountUpdateViewModel.deleteTransaction(
                                        trans
                                    )
                                }
                            }.setNegativeButton(mainActivity.getString(R.string.cancel), null)
                                .show()
                        }
                    )

                    actionSheetState.show(
                        "${mainActivity.getString(R.string.choose_an_action_for)}${trans.transName}",
                        options
                    )
                }
            }
        },
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
        onSheetDismiss = {
            actionSheetState.dismiss()
        },
    )
    ManagedActionBottomSheet(actionSheetState)
}