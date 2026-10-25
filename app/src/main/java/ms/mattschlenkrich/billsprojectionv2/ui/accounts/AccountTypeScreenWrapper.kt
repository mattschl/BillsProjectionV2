package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountTypeListScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = SCREEN_ACCOUNT_TYPES

@Composable
fun AccountTypesScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel

    var searchQuery by remember { mutableStateOf("") }

    val accountTypes by if (searchQuery.isEmpty()) {
        accountViewModel.getActiveAccountTypes().observeAsState(emptyList())
    } else {
        accountViewModel.searchAccountTypes("%$searchQuery%").observeAsState(emptyList())
    }

    AccountTypeListScreen(
        accountTypes = accountTypes,
        onAddClick = {
            mainViewModel.addCallingFragment(TAG)
            navController.navigate(Screen.AccountTypeAdd.route)
        },
        onAccountTypeClick = { accountType ->
            val accountWithType = mainViewModel.getAccountWithType()
            if (accountWithType != null) {
                mainViewModel.setAccountType(accountType)
                mainViewModel.setAccountWithType(
                    AccountWithType(accountWithType.account, accountType)
                )
            }
            val callingFragments = mainViewModel.getCallingFragments() ?: ""
            val callerTags = callingFragments.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != TAG }
            val lastCaller = callerTags.lastOrNull() ?: ""

            when (lastCaller) {
                SCREEN_ACCOUNT_UPDATE -> {
                    navController.navigate(Screen.AccountUpdate.route)
                }

                SCREEN_ACCOUNT_ADD -> {
                    navController.navigate(Screen.AccountAdd.route)
                }

                else -> {
                    mainViewModel.setAccountType(accountType)
                    navController.navigate(Screen.AccountTypeUpdate.route)
                }
            }
        },
        onAccountTypeLongClick = { accountType ->
            mainViewModel.setAccountType(accountType)
            navController.navigate(Screen.AccountTypeUpdate.route)
        },
        getAccountTypeInfo = { type ->
            val parts = mutableListOf<String>()
            if (type.keepTotals) parts.add(mainActivity.getString(R.string.msg_balance_updated))
            if (type.tallyOwing) parts.add(mainActivity.getString(R.string.msg_will_calc_owing))
            if (type.isAsset) parts.add(mainActivity.getString(R.string.msg_is_asset))
            if (type.displayAsAsset) parts.add(mainActivity.getString(R.string.msg_used_for_budget))
            if (type.allowPending) parts.add(mainActivity.getString(R.string.label_allow_pending))
            if (type.acctIsDeleted) parts.add(mainActivity.getString(R.string.label_deleted_suffix))
            if (parts.isEmpty()) mainActivity.getString(R.string.msg_account_no_balance)
            else parts.joinToString("\n")
        }
    )
}