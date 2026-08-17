package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose.BudgetSummaryScreen
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

    var sheetTitle by remember { mutableStateOf("") }
    var sheetOptions by remember { mutableStateOf(emptyList<ActionOption>()) }

    BudgetSummaryScreen(
        monthlyRules = monthlyRules,
        occasionalRules = occasionalRules,
        annualRules = annualRules,
        onRuleClick = { curRule ->
            sheetTitle =
                "${mainActivity.getString(R.string.choose_an_action_for)} ${(curRule.budgetRule?.budgetRuleName ?: "")}"
            sheetOptions = listOf(
                ActionOption(
                    mainActivity.getString(R.string.view_or_edit_this_budget_rule),
                    Icons.Default.Edit
                ) {
                    val budgetRule = BudgetRuleDetailed(
                        curRule.budgetRule!!,
                        curRule.toAccount!!.account,
                        curRule.fromAccount!!.account
                    )
                    mainViewModel.setBudgetRuleDetailed(budgetRule)
                    mainViewModel.setCallingFragments(TAG)
                    navController.navigate(Screen.BudgetRuleUpdate.route)
                },
                ActionOption(
                    mainActivity.getString(R.string.delete_this_budget_rule),
                    Icons.Default.Delete
                ) {
                    budgetRuleViewModel.deleteBudgetRule(
                        curRule.budgetRule!!.ruleId, df.getCurrentTimeAsString()
                    )
                },
                ActionOption(
                    mainActivity.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule),
                    Icons.Default.History
                ) {
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
            )
        },
        sheetTitle = sheetTitle,
        sheetOptions = sheetOptions,
        onSheetDismiss = {
            sheetOptions = emptyList()
            sheetTitle = ""
        }
    )
}