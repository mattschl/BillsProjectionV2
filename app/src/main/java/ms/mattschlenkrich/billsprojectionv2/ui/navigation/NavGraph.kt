package ms.mattschlenkrich.billsprojectionv2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountAddScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountChooseScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountTypeAddScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountTypeUpdateScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountTypesScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountUpdateScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountViewScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleAddScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleChooseScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleUpdateScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetItemAddScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetItemUpdateScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetListScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.calculator.CalculatorScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.help.HelpScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.settings.SettingsScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAddScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAnalysisScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionPerformScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionSplitScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionUpdateScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionViewScreenWrapper

@Composable
fun NavGraph(
    navController: NavHostController,
    activity: MainActivity,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BudgetView.route,
        modifier = modifier
    ) {
        composable(Screen.BudgetView.route) {
            BudgetViewScreenWrapper(activity, navController)
        }
        composable(Screen.Transactions.route) {
            TransactionViewScreenWrapper(activity, navController)
        }
        composable(Screen.Accounts.route) {
            AccountViewScreenWrapper(activity, navController)
        }
        composable(Screen.Analysis.route) {
            TransactionAnalysisScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetRules.route) {
            BudgetRuleScreenWrapper(activity, navController)
        }
        composable(Screen.Help.route) {
            HelpScreenWrapper()
        }
        composable(Screen.Settings.route) {
            SettingsScreenWrapper(activity)
        }
        composable(Screen.BudgetList.route) {
            BudgetListScreenWrapper(activity, navController)
        }
        composable(Screen.AccountAdd.route) {
            AccountAddScreenWrapper(activity, navController)
        }
        composable(Screen.AccountUpdate.route) {
            AccountUpdateScreenWrapper(activity, navController)
        }
        composable(Screen.TransactionAdd.route) {
            TransactionAddScreenWrapper(activity, navController)
        }
        composable(Screen.TransactionUpdate.route) {
            TransactionUpdateScreenWrapper(activity, navController)
        }
        composable(Screen.TransactionPerform.route) {
            TransactionPerformScreenWrapper(activity, navController)
        }
        composable(Screen.TransactionSplit.route) {
            TransactionSplitScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetItemAdd.route) {
            BudgetItemAddScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetItemUpdate.route) {
            BudgetItemUpdateScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetRuleAdd.route) {
            BudgetRuleAddScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetRuleUpdate.route) {
            BudgetRuleUpdateScreenWrapper(activity, navController)
        }
        composable(Screen.AccountTypes.route) {
            AccountTypesScreenWrapper(activity, navController)
        }
        composable(Screen.AccountTypeAdd.route) {
            AccountTypeAddScreenWrapper(activity, navController)
        }
        composable(Screen.AccountTypeUpdate.route) {
            AccountTypeUpdateScreenWrapper(activity, navController)
        }
        composable(Screen.AccountChoose.route) {
            AccountChooseScreenWrapper(activity, navController)
        }
        composable(Screen.BudgetRuleChoose.route) {
            BudgetRuleChooseScreenWrapper(activity, navController)
        }
        composable(Screen.Calculator.route) {
            CalculatorScreenWrapper(activity, navController)
        }
    }
}