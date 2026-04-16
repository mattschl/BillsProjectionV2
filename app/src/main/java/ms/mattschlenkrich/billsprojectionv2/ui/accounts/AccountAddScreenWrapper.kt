package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_ACCOUNT_ADD

@Composable
fun AccountAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_a_new_account)

    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var owing by remember { mutableStateOf("") }
    var budgeted by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    // Initialize values from cache if they exist
    val cached = mainViewModel.getAccountWithType()
    if (cached != null) {
        name = cached.account.accountName
        handle = cached.account.accountNumber
        balance = nf.displayDollars(
            if (mainViewModel.getTransferNum() != 0.0 &&
                mainViewModel.getReturnTo()?.contains(BALANCE) == true
            ) {
                mainViewModel.getTransferNum()!!
            } else {
                cached.account.accountBalance
            }
        )
        owing = nf.displayDollars(
            if (mainViewModel.getTransferNum() != 0.0 &&
                mainViewModel.getReturnTo()?.contains(OWING) == true
            ) {
                mainViewModel.getTransferNum()!!
            } else {
                cached.account.accountOwing
            }
        )
        budgeted = nf.displayDollars(
            if (mainViewModel.getTransferNum() != 0.0 &&
                mainViewModel.getReturnTo()?.contains(BUDGETED) == true
            ) {
                mainViewModel.getTransferNum()!!
            } else {
                cached.account.accBudgetedAmount
            }
        )
        limit = nf.displayDollars(cached.account.accountCreditLimit)
        mainViewModel.setTransferNum(0.0)
    } else {
        balance = nf.displayDollars(0.0)
        owing = nf.displayDollars(0.0)
        budgeted = nf.displayDollars(0.0)
        limit = nf.displayDollars(0.0)
    }

    val accountWithType = mainViewModel.getAccountWithType()
    val accountType = accountWithType?.accountType

    fun getCurrentAccount(): Account {
        return Account(
            nf.generateId(),
            name.trim(),
            handle.trim(),
            mainViewModel.getAccountWithType()?.accountType?.typeId ?: 0L,
            nf.getDoubleFromDollars(budgeted),
            nf.getDoubleFromDollars(balance),
            nf.getDoubleFromDollars(owing),
            nf.getDoubleFromDollars(limit),
            false,
            df.getCurrentTimeAsString()
        )
    }

    AccountEditScreen(
        name = name,
        onNameChange = { name = it },
        handle = handle,
        onHandleChange = { handle = it },
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
            if (accountType.keepTotals) details.add(mainActivity.getString(R.string.this_account_does_not_keep_a_balance_owing_amount))
            if (accountType.isAsset) details.add(mainActivity.getString(R.string.this_is_an_asset))
            if (accountType.displayAsAsset) details.add(mainActivity.getString(R.string.this_will_be_used_for_the_budget))
            if (accountType.tallyOwing) details.add(mainActivity.getString(R.string.balance_owing_will_be_calculated))
            if (accountType.allowPending) details.add(mainActivity.getString(R.string.transactions_may_be_postponed))
            if (details.isEmpty()) mainActivity.getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
            else details.joinToString("\n")
        } else "",
        balance = balance,
        onBalanceChange = { balance = it },
        onBalanceIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(balance.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
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
        owing = owing,
        onOwingChange = { owing = it },
        onOwingIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(owing.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
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
        budgeted = budgeted,
        onBudgetedChange = { budgeted = it },
        onBudgetedIconClick = {
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(budgeted.ifBlank {
                mainActivity.getString(
                    R.string.zero_double
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
        limit = limit,
        onLimitChange = { limit = it },
        onSaveClick = {
            val accountNames = accountViewModel.getAccountNameList().value ?: emptyList()
            val curName = name.trim()

            if (curName.isEmpty()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_enter_a_name_for_this_account),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountNames.contains(curName)) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.this_account_already_exists),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountType == null) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.this_account_must_have_an_account_type),
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