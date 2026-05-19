package ms.mattschlenkrich.billsprojectionv2.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountViewScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAnalysisScreenWrapper
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionViewScreenWrapper

@Composable
fun MainPagerScreen(
    activity: MainActivity,
    navController: NavHostControllerWrapper,
    pagerState: PagerState
) {
    LaunchedEffect(pagerState.currentPage) {
        val titleResId = when (pagerState.currentPage) {
            0 -> R.string.view_the_budget
            1 -> R.string.view_transaction_history
            2 -> R.string.accounts
            3 -> R.string.transaction_analysis
            4 -> R.string.budget_rules
            else -> R.string.view_the_budget
        }
        activity.topMenuBar.setTitle(titleResId)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true // Enable swiping
    ) { page ->
        when (page) {
            0 -> BudgetViewScreenWrapper(activity, navController.controller)
            1 -> TransactionViewScreenWrapper(activity, navController.controller)
            2 -> AccountViewScreenWrapper(activity, navController.controller)
            3 -> TransactionAnalysisScreenWrapper(activity, navController.controller)
            4 -> BudgetRuleScreenWrapper(activity, navController.controller)
        }
    }
}

// Helper to pass the NavHostController without conflicts in wrapper signatures
data class NavHostControllerWrapper(val controller: androidx.navigation.NavHostController)