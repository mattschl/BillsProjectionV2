package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@Composable
fun BudgetRuleScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val budgetRuleViewModel = activity.budgetRuleViewModel
    val nf = NumberFunctions()
    val df = DateFunctions()

    LaunchedEffect(Unit) {
        activity.topMenuBar.setTitle(R.string.budget_rules)
    }

    var searchQuery by remember { mutableStateOf("") }
    val budgetRulesDetailed by if (searchQuery.isEmpty()) {
        budgetRuleViewModel.getActiveBudgetRulesDetailed()
            .observeAsState(emptyList())
    } else {
        budgetRuleViewModel.searchBudgetRules("%$searchQuery%")
            .observeAsState(emptyList())
    }

    BudgetRulesListScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        budgetRulesDetailed = budgetRulesDetailed,
        onAddClick = {
            mainViewModel.setBudgetRuleDetailed(null)
            mainViewModel.addCallingFragment(FRAG_BUDGET_RULES)
            navController.navigate(Screen.BudgetRuleAdd.route)
        },
        onItemClick = { budgetRuleDetailed ->
            AlertDialog.Builder(activity).setTitle(
                activity.getString(R.string.choose_an_action_for) + budgetRuleDetailed.budgetRule!!.budgetRuleName
            ).setItems(
                arrayOf(
                    activity.getString(R.string.view_or_edit_this_budget_rule),
                    activity.getString(R.string.add_a_new_transaction_based_on_the_budget_rule),
                    activity.getString(R.string.create_a_scheduled_item_with_this_budget_rule),
                    activity.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule),
                    activity.getString(R.string.delete_this_budget_rule),
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        mainViewModel.addCallingFragment(FRAG_BUDGET_RULES)
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    }

                    1 -> {
                        val mTransaction = Transactions(
                            nf.generateId(),
                            df.getCurrentDateAsString(),
                            budgetRuleDetailed.budgetRule!!.budgetRuleName,
                            "",
                            budgetRuleDetailed.budgetRule!!.ruleId,
                            0L,
                            false,
                            0L,
                            false,
                            budgetRuleDetailed.budgetRule!!.budgetAmount,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        mainViewModel.setTransactionDetailed(
                            TransactionDetailed(
                                mTransaction,
                                budgetRuleDetailed.budgetRule,
                                null,
                                null,
                            )
                        )
                        mainViewModel.addCallingFragment(FRAG_BUDGET_RULES)
                        navController.navigate(Screen.TransactionAdd.route)
                    }

                    2 -> {
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        mainViewModel.addCallingFragment(FRAG_BUDGET_RULES)
                        mainViewModel.setBudgetItemDetailed(
                            BudgetItemDetailed(
                                BudgetItem(
                                    budgetRuleDetailed.budgetRule!!.ruleId,
                                    df.getCurrentDateAsString(),
                                    df.getCurrentDateAsString(),
                                    "",
                                    budgetRuleDetailed.budgetRule!!.budgetRuleName,
                                    budgetRuleDetailed.budgetRule!!.budIsPayDay,
                                    budgetRuleDetailed.toAccount!!.accountId,
                                    budgetRuleDetailed.fromAccount!!.accountId,
                                    budgetRuleDetailed.budgetRule!!.budgetAmount,
                                    false,
                                    budgetRuleDetailed.budgetRule!!.budFixedAmount,
                                    budgetRuleDetailed.budgetRule!!.budIsAutoPay,
                                    biManuallyEntered = true,
                                    biIsCompleted = false,
                                    biIsCancelled = false,
                                    biIsDeleted = false,
                                    biUpdateTime = df.getCurrentTimeAsString(),
                                    biLocked = true
                                ),
                                budgetRuleDetailed.budgetRule!!,
                                budgetRuleDetailed.toAccount!!,
                                budgetRuleDetailed.fromAccount!!,
                            )
                        )
                        navController.navigate(Screen.BudgetItemAdd.route)
                    }

                    3 -> {
                        mainViewModel.addCallingFragment(FRAG_BUDGET_RULES)
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        mainViewModel.setAccountWithType(null)
                        navController.navigate(Screen.Analysis.route)
                    }

                    4 -> {
                        budgetRuleViewModel.deleteBudgetRule(
                            budgetRuleDetailed.budgetRule!!.ruleId, df.getCurrentTimeAsString()
                        )
                    }
                }
            }.setNegativeButton(activity.getString(R.string.cancel), null).show()
        }
    )
}