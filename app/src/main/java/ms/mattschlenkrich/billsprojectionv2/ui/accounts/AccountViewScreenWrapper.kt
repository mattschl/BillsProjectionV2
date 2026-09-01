package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.material.icons.Icons
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
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.components.ManagedActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.rememberActionSheetState
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountsListScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val accountViewModel = activity.accountViewModel
    val cf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val actionSheetState = rememberActionSheetState()

    LaunchedEffect(Unit) {
        activity.topMenuBar.setTitle(R.string.title_accounts)
    }

    var searchQuery by remember { mutableStateOf("") }
    val accountsWithType by if (searchQuery.isEmpty()) {
        accountViewModel.getAccountsWithType().observeAsState(emptyList())
    } else {
        accountViewModel.searchAccountsWithType("%$searchQuery%").observeAsState(emptyList())
    }

    AccountsListScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        accountsWithType = accountsWithType,
        onAddAccountClick = {
            mainViewModel.addCallingFragment(SCREEN_ACCOUNTS)
            mainViewModel.setAccountWithType(null)
            navController.navigate(Screen.AccountAdd.route)
        },
        onAccountClick = { accountWithType ->
            actionSheetState.show(
                activity.getString(R.string.title_choose_action_for) + accountWithType.account.accountName,
                listOf(
                    ActionOption(
                        activity.getString(R.string.action_edit_account),
                        Icons.Default.Edit
                    ) {
                        mainViewModel.addCallingFragment(SCREEN_ACCOUNTS)
                        mainViewModel.setAccountWithType(accountWithType)
                        navController.navigate(Screen.AccountUpdate.route)
                    },
                    ActionOption(
                        activity.getString(R.string.action_delete_account),
                        Icons.Default.Delete
                    ) {
                        accountViewModel.deleteAccount(
                            accountWithType.account.accountId, df.getCurrentTimeAsString()
                        )
                    },
                    ActionOption(
                        activity.getString(R.string.action_view_account_summary),
                        Icons.Default.History
                    ) {
                        mainViewModel.addCallingFragment(SCREEN_ACCOUNTS)
                        mainViewModel.setAccountWithType(accountWithType)
                        mainViewModel.setBudgetRuleDetailed(null)
                        navController.navigate(Screen.Analysis.route)
                    }
                )
            )
        },
        getAccountInfoText = { accountWithType ->
            val account = accountWithType.account
            val parts = mutableListOf<String>()
            if (account.accountNumber.isNotEmpty()) {
                parts.add("# ${account.accountNumber}")
            }
            if (account.accountBalance != 0.0) {
                parts.add(activity.getString(R.string.label_balance) + cf.displayDollars(account.accountBalance))
            }
            if (account.accountOwing != 0.0) {
                parts.add(activity.getString(R.string.label_owing) + cf.displayDollars(account.accountOwing))
            }
            if (account.accBudgetedAmount != 0.0) {
                parts.add(activity.getString(R.string.label_budgeted) + cf.displayDollars(account.accBudgetedAmount))
            }
            if (account.accountCreditLimit != 0.0) {
                parts.add(
                    activity.getString(R.string.label_credit_limit) + cf.displayDollars(
                        account.accountCreditLimit
                    )
                )
            }
            if (account.accIsDeleted) {
                parts.add(activity.getString(R.string.label_deleted_suffix))
            }
            parts.joinToString("\n")
        },
        sheetTitle = actionSheetState.title,
        sheetOptions = actionSheetState.options,
        onSheetDismiss = {
            actionSheetState.dismiss()
        }
    )
    ManagedActionBottomSheet(actionSheetState)
}