package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose.BudgetSummaryScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_LIST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val df = LocalDateFunctions.current
    val actionSheetState = rememberActionSheetState()

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
            actionSheetState.show(
                "${mainActivity.getString(R.string.choose_an_action_for)} ${(curRule.budgetRule?.budgetRuleName ?: "")}",
                listOf(
                    ActionOption(
                        mainActivity.getString(R.string.view_or_edit_this_budget_rule),
                        Icons.Default.Edit
                    ) {
                        val budgetRule = BudgetRuleDetailed(
                            curRule.budgetRule,
                            curRule.toAccount?.account,
                            curRule.fromAccount?.account
                        )
                        mainViewModel.setBudgetRuleDetailed(budgetRule)
                        mainViewModel.setCallingFragments(TAG)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    },
                    ActionOption(
                        mainActivity.getString(R.string.delete_this_budget_rule),
                        Icons.Default.Delete
                    ) {
                        curRule.budgetRule?.let {
                            budgetRuleViewModel.deleteBudgetRule(
                                it.ruleId, df.getCurrentTimeAsString()
                            )
                        }
                    },
                    ActionOption(
                        mainActivity.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule),
                        Icons.Default.History
                    ) {
                        mainViewModel.addCallingFragment(TAG)
                        mainViewModel.setBudgetRuleDetailed(
                            BudgetRuleDetailed(
                                curRule.budgetRule,
                                curRule.toAccount?.account,
                                curRule.fromAccount?.account
                            )
                        )
                        mainViewModel.setAccountWithType(null)
                        navController.navigate(Screen.Analysis.route)
                    }
                )
            )
        },
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
        onSheetDismiss = {
            actionSheetState.dismiss()
        }
    )
    ManagedActionBottomSheet(actionSheetState)
}