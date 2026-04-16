package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
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

    activity.topMenuBar.title = stringResource(R.string.choose_an_account)

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
            mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
            mainViewModel.setAccountWithType(null)
            navController.navigate(Screen.AccountAdd.route)
        },
        onAccountClick = { accountWithType ->
            AlertDialog.Builder(activity).setTitle(
                activity.getString(R.string.choose_an_action_for) + accountWithType.account.accountName
            ).setItems(
                arrayOf(
                    activity.getString(R.string.edit_this_account),
                    activity.getString(R.string.delete_this_account),
                    activity.getString(R.string.view_a_summary_of_transactions_using_this_account)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
                        mainViewModel.setAccountWithType(accountWithType)
                        navController.navigate(Screen.AccountUpdate.route)
                    }

                    1 -> {
                        accountViewModel.deleteAccount(
                            accountWithType.account.accountId, df.getCurrentTimeAsString()
                        )
                    }

                    2 -> {
                        mainViewModel.addCallingFragment(FRAG_ACCOUNTS)
                        mainViewModel.setAccountWithType(accountWithType)
                        mainViewModel.setBudgetRuleDetailed(null)
                        navController.navigate(Screen.Analysis.route)
                    }
                }
            }.setNegativeButton(activity.getString(R.string.cancel), null).show()
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
        vf = vf
    )
}