package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountEditScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = SCREEN_ACCOUNT_ADD

@Composable
fun AccountAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val state = rememberAccountEditState(nf, df)

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.title_add_account)
    }

    // Initialize values from cache if they exist
    val cached = mainViewModel.getAccountWithType()
    LaunchedEffect(Unit) {
        if (cached != null) {
            state.updateFrom(cached, mainViewModel.getTransferNum(), mainViewModel.getReturnTo())
            mainViewModel.setTransferNum(0.0)
        } else {
            state.balance = nf.displayDollars(0.0)
            state.owing = nf.displayDollars(0.0)
            state.budgeted = nf.displayDollars(0.0)
            state.limit = nf.displayDollars(0.0)
        }
    }

    val accountType = cached?.accountType

    fun getCurrentAccount(): Account {
        return state.toAccount(
            nf.generateId(),
            mainViewModel.getAccountWithType()?.accountType?.typeId ?: 0L
        )
    }

    AccountEditScreen(
        name = state.name,
        onNameChange = { state.name = it },
        handle = state.handle,
        onHandleChange = { state.handle = it },
        accountType = accountType,
        onAccountTypeClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getCurrentAccount(),
                    mainViewModel.getAccountWithType()?.accountType
                )
            )
            navController.navigate(Screen.AccountTypes.route)
        },
        accountTypeDetails = if (accountType != null) {
            val details = mutableListOf<String>()
            if (accountType.keepTotals) details.add(mainActivity.getString(R.string.msg_account_no_balance))
            if (accountType.isAsset) details.add(mainActivity.getString(R.string.msg_is_asset))
            if (accountType.displayAsAsset) details.add(mainActivity.getString(R.string.msg_used_for_budget))
            if (accountType.tallyOwing) details.add(mainActivity.getString(R.string.msg_balance_owing_calc))
            if (accountType.allowPending) details.add(mainActivity.getString(R.string.msg_transactions_postponed))
            if (details.isEmpty()) mainActivity.getString(R.string.msg_account_no_balance)
            else details.joinToString("\n")
        } else "",
        balance = state.balance,
        onBalanceChange = { state.balance = it },
        onBalanceIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.balance.ifBlank {
                mainActivity.getString(
                    R.string.val_zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getCurrentAccount(),
                    mainViewModel.getAccountWithType()?.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        owing = state.owing,
        onOwingChange = { state.owing = it },
        onOwingIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.owing.ifBlank {
                mainActivity.getString(
                    R.string.val_zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getCurrentAccount(),
                    mainViewModel.getAccountWithType()?.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        budgeted = state.budgeted,
        onBudgetedChange = { state.budgeted = it },
        onBudgetedIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.budgeted.ifBlank {
                mainActivity.getString(
                    R.string.val_zero_double
                )
            }))
            mainViewModel.setAccountWithType(
                AccountWithType(
                    getCurrentAccount(),
                    mainViewModel.getAccountWithType()?.accountType
                )
            )
            navController.navigate(Screen.Calculator.route)
        },
        limit = state.limit,
        onLimitChange = { state.limit = it },
        onSaveClick = {
            val accountNames = accountViewModel.getAccountNameList().value ?: emptyList()
            val curName = state.name.trim()

            if (curName.isEmpty()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.msg_prompt_enter_account_name),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountNames.contains(curName)) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.msg_error_account_exists),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountType == null) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.msg_error_account_type_required),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val curAccount = getCurrentAccount()
                accountViewModel.addAccount(curAccount)
                mainViewModel.setAccountWithType(AccountWithType(curAccount, accountType))
                mainViewModel.removeCallingFragment(TAG)
                mainViewModel.setAccountWithType(null)
                navController.popBackStack()
            }
        }
    )
}