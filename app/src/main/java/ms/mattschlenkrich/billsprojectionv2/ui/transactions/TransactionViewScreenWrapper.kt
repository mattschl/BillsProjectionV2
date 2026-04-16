package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@Composable
fun TransactionViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val transactionViewModel = activity.transactionViewModel
    val accountUpdateViewModel = activity.accountUpdateViewModel
    val budgetRuleViewModel = activity.budgetRuleViewModel
    val nf = NumberFunctions()

    activity.topMenuBar.title = stringResource(R.string.view_transaction_history)

    val searchQueryState = remember { mutableStateOf("") }
    val query by searchQueryState

    val transactionList by if (query.isBlank()) {
        transactionViewModel.getActiveTransactionsDetailed()
    } else {
        transactionViewModel.searchActiveTransactionsDetailed("%$query%")
    }.observeAsState(initial = emptyList())

    TransactionViewScreen(
        transactionList = transactionList,
        onAddClick = {
            mainViewModel.addCallingFragment(FRAG_TRANSACTION_VIEW)
            mainViewModel.setTransactionDetailed(null)
            navController.navigate(Screen.TransactionAdd.route)
        },
        onTransactionClick = { transactionDetailed ->
            val context = activity
            var display = ""
            val trans = transactionDetailed.transaction!!
            if (trans.transToAccountPending) {
                display += context.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                    trans.transAmount
                ) + context.getString(R.string._to_) + transactionDetailed.toAccount!!.accountName + " " + context.getString(
                    R.string.pending
                )
            }
            if (display.isNotEmpty() && trans.transFromAccountPending) {
                display += " " + context.getString(R.string._and) + " "
            }
            if (trans.transFromAccountPending) {
                display += context.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                    trans.transAmount
                ) + context.getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName + " " + context.getString(
                    R.string.pending
                )
            }

            val items = mutableListOf(
                context.getString(R.string.edit_this_transaction),
                display,
                context.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                context.getString(R.string.delete_this_transaction)
            )

            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.choose_an_action_for) + " " + trans.transName)
                .setItems(items.toTypedArray()) { _, pos ->
                    when (pos) {
                        0 -> {
                            mainViewModel.setCallingFragments(FRAG_TRANSACTION_VIEW)
                            mainViewModel.setTransactionDetailed(transactionDetailed)
                            activity.lifecycleScope.launch {
                                val oldTransactionFull = async {
                                    transactionViewModel.getTransactionFull(
                                        transactionDetailed.transaction.transId,
                                        transactionDetailed.transaction.transToAccountId,
                                        transactionDetailed.transaction.transFromAccountId
                                    )
                                }.await()
                                mainViewModel.setOldTransaction(oldTransactionFull)
                                navController.navigate(Screen.TransactionUpdate.route)
                            }
                        }

                        1 -> if (trans.transToAccountPending || trans.transFromAccountPending) {
                            val newTransaction = trans.copy(
                                transToAccountPending = false,
                                transFromAccountPending = false
                            )
                            activity.lifecycleScope.launch {
                                accountUpdateViewModel.updateTransaction(trans, newTransaction)
                            }
                        }

                        2 -> {
                            mainViewModel.setCallingFragments(FRAG_TRANSACTION_VIEW)
                            budgetRuleViewModel.getBudgetRuleFullLive(
                                transactionDetailed.transaction.transRuleId
                            ).observe(activity) { bRuleDetailed ->
                                if (bRuleDetailed != null) {
                                    mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                                    navController.navigate(Screen.BudgetRuleUpdate.route)
                                }
                            }
                        }

                        3 -> {
                            AlertDialog.Builder(activity)
                                .setTitle(activity.getString(R.string.are_you_sure_you_want_to_delete) + " " + trans.transName)
                                .setPositiveButton(activity.getString(R.string.delete)) { _, _ ->
                                    activity.lifecycleScope.launch {
                                        accountUpdateViewModel.deleteTransaction(trans)
                                    }
                                }
                                .setNegativeButton(activity.getString(R.string.cancel), null)
                                .show()
                        }
                    }
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }
    )
}