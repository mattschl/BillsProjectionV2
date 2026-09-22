package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_RULE_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose.BudgetRuleChooseScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = SCREEN_BUDGET_RULE_CHOOSE

@Composable
fun BudgetRuleChooseScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel

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
            val callingFragments = mainViewModel.getCallingFragments() ?: ""
            val callerTags = callingFragments.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != TAG }
            val lastCaller = callerTags.lastOrNull() ?: ""

            mainViewModel.removeCallingFragment(TAG)
            mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)

            when (lastCaller) {
                SCREEN_TRANSACTION_ANALYSIS -> {
                    navController.popBackStack()
                }

                SCREEN_TRANSACTION_SPLIT -> {
                    val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
                    mainViewModel.setSplitTransactionDetailed(
                        mTransactionSplit?.copy(
                            budgetRule = budgetRuleDetailed.budgetRule,
                            toAccount = budgetRuleDetailed.toAccount,
                            fromAccount = budgetRuleDetailed.fromAccount
                        )
                    )
                    navController.popBackStack()
                }

                SCREEN_BUDGET_ITEM_ADD,
                SCREEN_BUDGET_ITEM_UPDATE -> {
                    val mBudgetDetailed = mainViewModel.getBudgetItemDetailed()
                    mainViewModel.setBudgetItemDetailed(
                        mBudgetDetailed?.copy(
                            budgetRule = budgetRuleDetailed.budgetRule,
                            toAccount = budgetRuleDetailed.toAccount,
                            fromAccount = budgetRuleDetailed.fromAccount
                        )
                    )
                    navController.popBackStack()
                }

                else -> {
                    val mTransaction = mainViewModel.getTransactionDetailed()
                    mainViewModel.setTransactionDetailed(
                        mTransaction?.copy(
                            budgetRule = budgetRuleDetailed.budgetRule,
                            toAccount = budgetRuleDetailed.toAccount,
                            fromAccount = budgetRuleDetailed.fromAccount
                        )
                    )
                    navController.popBackStack()
                }
            }
        }
    )
}