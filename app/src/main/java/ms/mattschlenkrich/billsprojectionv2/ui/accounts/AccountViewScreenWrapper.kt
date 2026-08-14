package ms.mattschlenkrich.billsprojectionv2.ui.accounts

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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountsListScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

@Composable
fun AccountViewScreenWrapper(
    activity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = activity.mainViewModel
    val accountViewModel = activity.accountViewModel
    val cf = NumberFunctions()
    val vf = VisualsFunctions()
    val df = DateFunctions()

    LaunchedEffect(Unit) {
        activity.topMenuBar.setTitle(R.string.accounts)
    }

    var searchQuery by remember { mutableStateOf("") }
    val accountsWithType by if (searchQuery.isEmpty()) {
        accountViewModel.getAccountsWithType().observeAsState(emptyList())
    } else {
        accountViewModel.searchAccountsWithType("%$searchQuery%").observeAsState(emptyList())
    }

    var sheetTitle by remember { mutableStateOf("") }
    var sheetOptions by remember { mutableStateOf(emptyList<ActionOption>()) }

    AccountsListScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        accountsWithType = accountsWithType,
        onAddAccountClick = {
            mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
            mainViewModel.setAccountWithType(null)
            navController.navigate(Screen.AccountAdd.route)
        },
        onAccountClick = { accountWithType ->
            sheetTitle =
                activity.getString(R.string.choose_an_action_for) + accountWithType.account.accountName
            sheetOptions = listOf(
                ActionOption(
                    activity.getString(R.string.edit_this_account),
                    Icons.Default.Edit
                ) {
                    mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
                    mainViewModel.setAccountWithType(accountWithType)
                    navController.navigate(Screen.AccountUpdate.route)
                },
                ActionOption(
                    activity.getString(R.string.delete_this_account),
                    Icons.Default.Delete
                ) {
                    accountViewModel.deleteAccount(
                        accountWithType.account.accountId, df.getCurrentTimeAsString()
                    )
                },
                ActionOption(
                    activity.getString(R.string.view_a_summary_of_transactions_using_this_account),
                    Icons.Default.History
                ) {
                    mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
                    mainViewModel.setAccountWithType(accountWithType)
                    mainViewModel.setBudgetRuleDetailed(null)
                    navController.navigate(Screen.Analysis.route)
                }
            )
        },
        getAccountInfoText = { accountWithType ->
            val account = accountWithType.account
            val parts = mutableListOf<String>()
            if (account.accountNumber.isNotEmpty()) {
                parts.add("# ${account.accountNumber}")
            }
            if (account.accountBalance != 0.0) {
                parts.add(activity.getString(R.string.balance) + cf.displayDollars(account.accountBalance))
            }
            if (account.accountOwing != 0.0) {
                parts.add(activity.getString(R.string.owing) + cf.displayDollars(account.accountOwing))
            }
            if (account.accBudgetedAmount != 0.0) {
                parts.add(activity.getString(R.string.budgeted) + cf.displayDollars(account.accBudgetedAmount))
            }
            if (account.accountCreditLimit != 0.0) {
                parts.add(activity.getString(R.string.credit_limit) + cf.displayDollars(account.accountCreditLimit))
            }
            if (account.accIsDeleted) {
                parts.add(activity.getString(R.string.deleted))
            }
            parts.joinToString("\n")
        },
        vf = vf,
        sheetTitle = sheetTitle,
        sheetOptions = sheetOptions,
        onSheetDismiss = {
            sheetOptions = emptyList()
            sheetTitle = ""
        }
    )
}