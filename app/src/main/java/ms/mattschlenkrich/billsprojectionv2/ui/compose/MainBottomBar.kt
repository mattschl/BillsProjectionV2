package ms.mattschlenkrich.billsprojectionv2.ui.compose

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@Composable
fun MainBottomBar(
    pagerState: PagerState,
    currentRoute: String?,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple(R.string.title_budget_view, R.drawable.ic_budget_view, Screen.BudgetView.route),
        Triple(R.string.title_transactions, R.drawable.ic_transactions, Screen.Transactions.route),
        Triple(R.string.title_accounts, R.drawable.ic_accounts, Screen.Accounts.route),
        Triple(R.string.title_analysis, R.drawable.ic_analysis, Screen.Analysis.route),
        Triple(R.string.title_budget_rules, R.drawable.ic_budget_rules, Screen.BudgetRules.route),
    )

    NavigationBar {
        items.forEachIndexed { index, (labelRes, iconRes, route) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(iconRes),
                        contentDescription = stringResource(labelRes),
                        modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                    )
                },
                label = { Text(stringResource(labelRes), softWrap = false) },
                selected = (currentRoute == route) || (currentRoute == Screen.MainPager.route && pagerState.currentPage == index),
                onClick = { onItemSelected(index) }
            )
        }
    }
}