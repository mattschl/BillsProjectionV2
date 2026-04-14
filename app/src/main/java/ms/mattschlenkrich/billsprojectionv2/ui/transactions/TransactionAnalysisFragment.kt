package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.transaction_analysis)

        return ComposeView(requireContext()).apply {
            setContent {
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

                BillsProjectionTheme {
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
                            ?: getString(R.string.no_budget_rule_selected),
                        accountName = accountWithType?.account?.accountName
                            ?: getString(R.string.no_account_selected),
                        mode = mode,
                        transactionList = transactionList,
                        sumToAccount = sumToAccount,
                        sumFromAccount = sumFromAccount,
                        sumCredits = sumCredits,
                        maxVal = maxVal,
                        minVal = minVal,
                        effectiveEndDate = effectiveEndDate.ifBlank { df.getCurrentDateAsString() },
                        onBudgetRuleClick = { gotoBudgetRule() },
                        onAccountClick = { gotoAccount() },
                        onTransactionClick = { chooseOptions(it) }
                    )
                }
            }
        }
    }

    private fun chooseOptions(transactionDetailed: TransactionDetailed) {
        var display = ""
        if (transactionDetailed.transaction!!.transToAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + getString(R.string._to_) + (transactionDetailed.toAccount?.accountName ?: "")
        }
        if (transactionDetailed.transaction.transToAccountPending) {
            display += getString(R.string._pending)
        }
        if (display != "" && transactionDetailed.transaction.transFromAccountPending) {
            display += getString(R.string._and)
        }
        if (transactionDetailed.transaction.transFromAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName
        }
        android.app.AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + transactionDetailed.transaction.transName
        ).setItems(
            arrayOf(
                getString(R.string.edit_this_transaction),
                display,
                getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                getString(R.string.delete_this_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    gotoTransactionUpdate(transactionDetailed)
                }

                1 -> {
                    if (transactionDetailed.transaction.transToAccountPending || transactionDetailed.transaction.transFromAccountPending) {
                        completePendingTransactions(transactionDetailed)
                    }
                }

                2 -> {
                    gotoBudgetRuleUpdate(transactionDetailed)
                }

                3 -> {
                    confirmDeleteTransaction(transactionDetailed)
                }
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun gotoBudgetRuleUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(TAG)
        budgetRuleViewModel.getBudgetRuleFullLive(
            transactionDetailed.transaction!!.transRuleId
        ).observe(viewLifecycleOwner) { bRuleDetailed ->
            mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
            gotoBudgetRuleUpdateFragment()
        }
    }

    private fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        transactionDetailed.transaction!!.apply {
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
            lifecycleScope.launch(Dispatchers.IO) {
                accountUpdateViewModel.updateTransaction(
                    transactionDetailed.transaction, newTransaction
                )
            }
        }
    }

    private fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        android.app.AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.are_you_sure_you_want_to_delete) + transactionDetailed.transaction!!.transName
        ).setPositiveButton(getString(R.string.delete)) { _, _ ->
            deleteTransaction(transactionDetailed.transaction!!)
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun deleteTransaction(transaction: Transactions) {
        lifecycleScope.launch(Dispatchers.IO) {
            accountUpdateViewModel.deleteTransaction(
                transaction
            )
        }
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(transactionDetailed)
        lifecycleScope.launch(Dispatchers.IO) {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transactionDetailed.transaction!!.transId,
                    transactionDetailed.transaction.transToAccountId,
                    transactionDetailed.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(oldTransactionFull.await())
            launch(Dispatchers.Main) {
                delay(ms.mattschlenkrich.billsprojectionv2.common.WAIT_250)
                gotoTransactionUpdateFragment()
            }
        }
    }

    private fun gotoAccount() {
        mainViewModel.eraseAll()
        mainViewModel.setCallingFragments(TAG)

        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_accountChooseFragment
        )
    }

    private fun gotoBudgetRule() {
        mainViewModel.eraseAll()
        mainViewModel.setCallingFragments(TAG)
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_budgetRuleChooseFragment
        )
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_transactionUpdateFragment
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(
            R.id.action_transactionAnalysisFragment_to_budgetRuleUpdateFragment
        )
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.transaction_analysis)
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
    }
}