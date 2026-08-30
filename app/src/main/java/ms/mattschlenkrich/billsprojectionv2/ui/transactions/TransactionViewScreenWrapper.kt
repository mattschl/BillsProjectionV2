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
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.TransactionMessageHelper
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose.TransactionViewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController,
) {
    val mainViewModel = activity.mainViewModel
    val transactionViewModel = activity.transactionViewModel
    val accountUpdateViewModel = activity.accountUpdateViewModel
    val budgetRuleViewModel = activity.budgetRuleViewModel
    val nf = LocalNumberFunctions.current
    val actionSheetState = rememberActionSheetState()

    LaunchedEffect(Unit) {
        activity.topMenuBar.title = activity.getString(R.string.view_transaction_history)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf(emptySet<Long>()) }

    LaunchedEffect(searchQuery) {
        selectedItems = emptySet()
    }

    val transactionList by if (searchQuery.isBlank()) {
        transactionViewModel.getActiveTransactionsDetailed()
    } else {
        transactionViewModel.searchActiveTransactionsDetailed("%$searchQuery%")
    }.observeAsState(initial = emptyList())

    val selectedSum = remember(selectedItems, transactionList) {
        if (selectedItems.isEmpty()) 0.0
        else {
            transactionList.asSequence().filter {
                selectedItems.contains(it.transaction?.transId)
            }.sumOf { it.transaction?.transAmount ?: 0.0 }
        }
    }

    TransactionViewScreen(
        transactionList = transactionList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onAddClick = {
            mainViewModel.addCallingFragment(SCREEN_TRANSACTION_VIEW)
            mainViewModel.setTransactionDetailed(null)
            navController.navigate(Screen.TransactionAdd.route)
        },
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
        onTransactionClick = { transactionDetailed ->
            if (selectedItems.isNotEmpty()) {
                selectedItems = emptySet()
            } else {
                transactionDetailed.transaction?.let { trans ->
                    val context = activity
                    val display = TransactionMessageHelper.buildPendingCompletionMessage(
                        activity, transactionDetailed, nf
                    )

                    val options = listOf(
                        ActionOption(
                            context.getString(R.string.edit_this_transaction),
                            Icons.Default.Edit
                        ) {
                            mainViewModel.setCallingFragments(SCREEN_TRANSACTION_VIEW)
                            mainViewModel.setTransactionDetailed(transactionDetailed)
                            activity.lifecycleScope.launch {
                                val oldTransactionFull = async {
                                    transactionViewModel.getTransactionFull(
                                        trans.transId,
                                        trans.transToAccountId,
                                        trans.transFromAccountId
                                    )
                                }.await()
                                mainViewModel.setOldTransaction(oldTransactionFull)
                                navController.navigate(Screen.TransactionUpdate.route)
                            }
                        },
                        ActionOption(display, Icons.Default.Check) {
                            val newTransaction = trans.copy(
                                transToAccountPending = false,
                                transFromAccountPending = false
                            )
                            activity.lifecycleScope.launch {
                                accountUpdateViewModel.updateTransaction(trans, newTransaction)
                            }
                        },
                        ActionOption(
                            context.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                            Icons.AutoMirrored.Filled.Rule
                        ) {
                            mainViewModel.setCallingFragments(SCREEN_TRANSACTION_VIEW)
                            budgetRuleViewModel.getBudgetRuleFullLive(
                                trans.transRuleId
                            ).observe(activity) { bRuleDetailed ->
                                if (bRuleDetailed != null) {
                                    mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                                    navController.navigate(Screen.BudgetRuleUpdate.route)
                                }
                            }
                        },
                        ActionOption(
                            context.getString(R.string.delete_this_transaction),
                            Icons.Default.Delete
                        ) {
                            AlertDialog.Builder(activity)
                                .setTitle("${activity.getString(R.string.are_you_sure_you_want_to_delete)} ${trans.transName}")
                                .setPositiveButton(activity.getString(R.string.delete)) { _, _ ->
                                    activity.lifecycleScope.launch {
                                        accountUpdateViewModel.deleteTransaction(trans)
                                    }
                                }
                                .setNegativeButton(activity.getString(R.string.cancel), null)
                                .show()
                        }
                    )

                    actionSheetState.show(
                        "${context.getString(R.string.choose_an_action_for)} ${trans.transName}",
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