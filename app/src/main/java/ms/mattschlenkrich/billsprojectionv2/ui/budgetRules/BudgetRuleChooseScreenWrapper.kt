package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_RULE_CHOOSE

@Composable
fun BudgetRuleChooseScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.choose_a_budget_rule)
    }

    var searchQuery by remember { mutableStateOf("") }
    val budgetRulesDetailed by if (searchQuery.isEmpty()) {
        budgetRuleViewModel.getActiveBudgetRulesDetailed()
            .observeAsState(emptyList())
    } else {
        budgetRuleViewModel.searchBudgetRules("%$searchQuery%")
            .observeAsState(emptyList())
    }

    BudgetRuleChooseScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        budgetRulesDetailed = budgetRulesDetailed,
        onAddBudgetRule = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setBudgetRuleDetailed(null)
            navController.navigate(Screen.BudgetRuleAdd.route)
        },
        onBudgetRuleClick = { budgetRuleDetailed ->
            val callingFragments = mainViewModel.getCallingFragments()
            if (callingFragments != null) {
                if (callingFragments.contains(FRAG_TRANSACTION_ANALYSIS)) {
                    mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                    navController.popBackStack()
                } else {
                    mainViewModel.removeCallingFragment(TAG)
                    mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                    if (callingFragments.contains(FRAG_TRANSACTION_SPLIT)) {
                        val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
                        mainViewModel.setSplitTransactionDetailed(
                            mTransactionSplit?.copy(
                                budgetRule = budgetRuleDetailed.budgetRule,
                                toAccount = budgetRuleDetailed.toAccount,
                                fromAccount = budgetRuleDetailed.fromAccount
                            )
                        )
                    } else {
                        val mTransaction = mainViewModel.getTransactionDetailed()
                        mainViewModel.setTransactionDetailed(
                            mTransaction?.copy(
                                budgetRule = budgetRuleDetailed.budgetRule,
                                toAccount = budgetRuleDetailed.toAccount,
                                fromAccount = budgetRuleDetailed.fromAccount
                            )
                        )
                    }
                    if (callingFragments.contains(FRAG_BUDGET_ITEM_ADD) || callingFragments.contains(
                            FRAG_BUDGET_ITEM_UPDATE
                        )
                    ) {
                        val mBudgetDetailed = mainViewModel.getBudgetItemDetailed()
                        mainViewModel.setBudgetItemDetailed(
                            mBudgetDetailed?.copy(
                                budgetRule = budgetRuleDetailed.budgetRule,
                                toAccount = budgetRuleDetailed.toAccount,
                                fromAccount = budgetRuleDetailed.fromAccount
                            )
                        )
                    }
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
        }
    )
}