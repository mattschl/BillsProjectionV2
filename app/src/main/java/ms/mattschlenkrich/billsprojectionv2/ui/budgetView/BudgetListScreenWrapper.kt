package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_LIST

@Composable
fun BudgetListScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val df = DateFunctions()

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.setTitle(R.string.view_budget_summary)
    }

    val budgetDate = df.getCurrentDateAsString()
    val monthlyRules by budgetRuleViewModel.getBudgetRulesCompleteMonthly(budgetDate)
        .observeAsState(emptyList())
    val occasionalRules by budgetRuleViewModel.getBudgetRulesCompletedOccasional(budgetDate)
        .observeAsState(emptyList())
    val annualRules by budgetRuleViewModel.getBudgetRulesCompletedAnnually(budgetDate)
        .observeAsState(emptyList())

    BudgetSummaryScreen(
        monthlyRules = monthlyRules,
        occasionalRules = occasionalRules,
        annualRules = annualRules,
        onRuleClick = { curRule ->
            AlertDialog.Builder(mainActivity).setTitle(
                mainActivity.getString(R.string.choose_an_action_for) + (curRule.budgetRule?.budgetRuleName
                    ?: "")
            ).setItems(
                arrayOf(
                    mainActivity.getString(R.string.view_or_edit_this_budget_rule),
                    mainActivity.getString(R.string.delete_this_budget_rule),
                    mainActivity.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        val budgetRule = BudgetRuleDetailed(
                            curRule.budgetRule!!,
                            curRule.toAccount!!.account,
                            curRule.fromAccount!!.account
                        )
                        mainViewModel.setBudgetRuleDetailed(budgetRule)
                        mainViewModel.setCallingFragments(TAG)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    }

                    1 -> {
                        budgetRuleViewModel.deleteBudgetRule(
                            curRule.budgetRule!!.ruleId, df.getCurrentTimeAsString()
                        )
                    }

                    2 -> {
                        mainViewModel.addCallingFragment(TAG)
                        mainViewModel.setBudgetRuleDetailed(
                            BudgetRuleDetailed(
                                curRule.budgetRule!!,
                                curRule.toAccount!!.account,
                                curRule.fromAccount!!.account
                            )
                        )
                        mainViewModel.setAccountWithType(null)
                        navController.navigate(Screen.Analysis.route)
                    }
                }
            }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
        }
    )
}