package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose.BudgetRulesListScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRuleScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val budgetRuleViewModel = activity.budgetRuleViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val actionSheetState = rememberActionSheetState()

    LaunchedEffect(Unit) {
        activity.topMenuBar.setTitle(R.string.title_budget_rules)
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
            mainViewModel.addCallingFragment(SCREEN_BUDGET_RULES)
            navController.navigate(Screen.BudgetRuleAdd.route)
        },
        onItemClick = { budgetRuleDetailed ->
            val rule = budgetRuleDetailed.budgetRule!!
            actionSheetState.show(
                "${activity.getString(R.string.title_choose_action_for)} ${rule.budgetRuleName}",
                listOf(
                    ActionOption(
                        activity.getString(R.string.action_view_edit_rule),
                        Icons.Default.Edit
                    ) {
                        mainViewModel.addCallingFragment(SCREEN_BUDGET_RULES)
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        navController.navigate(Screen.BudgetRuleUpdate.route)
                    },
                    ActionOption(
                        activity.getString(R.string.action_add_transaction_from_rule),
                        Icons.Default.Add
                    ) {
                        val mTransaction = Transactions(
                            nf.generateId(),
                            df.getCurrentDateAsString(),
                            rule.budgetRuleName,
                            "",
                            rule.ruleId,
                            0L,
                            false,
                            0L,
                            false,
                            rule.budgetAmount,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        mainViewModel.setTransactionDetailed(
                            TransactionDetailed(
                                mTransaction,
                                rule,
                                null,
                                null,
                            )
                        )
                        mainViewModel.addCallingFragment(SCREEN_BUDGET_RULES)
                        navController.navigate(Screen.TransactionAdd.route)
                    },
                    ActionOption(
                        activity.getString(R.string.action_create_scheduled_item),
                        Icons.Default.Add
                    ) {
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        mainViewModel.addCallingFragment(SCREEN_BUDGET_RULES)
                        mainViewModel.setBudgetItemDetailed(
                            BudgetItemDetailed(
                                BudgetItem(
                                    rule.ruleId,
                                    df.getCurrentDateAsString(),
                                    df.getCurrentDateAsString(),
                                    "",
                                    rule.budgetRuleName,
                                    rule.budIsPayDay,
                                    budgetRuleDetailed.toAccount!!.accountId,
                                    budgetRuleDetailed.fromAccount!!.accountId,
                                    rule.budgetAmount,
                                    false,
                                    rule.budFixedAmount,
                                    rule.budIsAutoPay,
                                    biManuallyEntered = true,
                                    biIsCompleted = false,
                                    biIsCancelled = false,
                                    biIsDeleted = false,
                                    biUpdateTime = df.getCurrentTimeAsString(),
                                    biLocked = true
                                ),
                                rule,
                                budgetRuleDetailed.toAccount!!,
                                budgetRuleDetailed.fromAccount!!,
                            )
                        )
                        navController.navigate(Screen.BudgetItemAdd.route)
                    },
                    ActionOption(
                        activity.getString(R.string.action_view_rule_summary),
                        Icons.Default.History
                    ) {
                        mainViewModel.addCallingFragment(SCREEN_BUDGET_RULES)
                        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                        mainViewModel.setAccountWithType(null)
                        navController.navigate(Screen.Analysis.route)
                    },
                    ActionOption(
                        activity.getString(R.string.action_delete_budget_rule),
                        Icons.Default.Delete
                    ) {
                        budgetRuleViewModel.deleteBudgetRule(
                            rule.ruleId, df.getCurrentTimeAsString()
                        )
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