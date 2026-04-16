package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_ACCOUNT_TYPES

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
            val mCallingFragment = mainViewModel.getCallingFragments() ?: ""
            when {
                mCallingFragment.contains(FRAG_ACCOUNT_UPDATE) -> {
                    navController.navigate(Screen.AccountUpdate.route)
                }

                mCallingFragment.contains(FRAG_ACCOUNT_ADD) -> {
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
            if (type.keepTotals) parts.add(mainActivity.getString(R.string.balance_will_be_updated))
            if (type.tallyOwing) parts.add(mainActivity.getString(R.string.will_calculate_amount_owing))
            if (type.isAsset) parts.add(mainActivity.getString(R.string.this_is_an_asset))
            if (type.displayAsAsset) parts.add(mainActivity.getString(R.string.this_will_be_used_for_the_budget))
            if (type.allowPending) parts.add(mainActivity.getString(R.string.allow_transactions_pending))
            if (type.acctIsDeleted) parts.add(mainActivity.getString(R.string.deleted))
            if (parts.isEmpty()) mainActivity.getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
            else parts.joinToString("\n")
        }
    )
}

@Composable
fun AccountTypeAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val accountViewModel = mainActivity.accountViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_account_type)

    var name by remember { mutableStateOf("") }
    var keepTotals by remember { mutableStateOf(false) }
    var isAsset by remember { mutableStateOf(false) }
    var tallyOwing by remember { mutableStateOf(false) }
    var displayAsAsset by remember { mutableStateOf(false) }
    var allowPending by remember { mutableStateOf(false) }

    val accountTypeNames by accountViewModel.getAccountTypeNames().observeAsState(emptyList())

    AccountTypeFormScreen(
        name = name,
        onNameChange = { name = it },
        keepTotals = keepTotals,
        onKeepTotalsChange = { keepTotals = it },
        isAsset = isAsset,
        onIsAssetChange = { isAsset = it },
        keepOwing = tallyOwing,
        onKeepOwingChange = { tallyOwing = it },
        displayAsAsset = displayAsAsset,
        onDisplayAsAssetChange = { displayAsAsset = it },
        allowPending = allowPending,
        onAllowPendingChange = { allowPending = it },
        onSaveClick = {
            if (name.trim().isEmpty()) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.please_enter_a_name),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountTypeNames.contains(name.trim())) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.this_account_rule_already_exists),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                accountViewModel.addAccountType(
                    AccountType(
                        nf.generateId(),
                        name.trim(),
                        keepTotals,
                        isAsset,
                        tallyOwing,
                        false,
                        displayAsAsset,
                        allowPending,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        fabContentDescription = mainActivity.getString(R.string.add_account_type)
    )
}

@Composable
fun AccountTypeUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val df = remember { DateFunctions() }

    val accountType = mainViewModel.getAccountType() ?: return

    mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_account_type)

    var name by remember { mutableStateOf(accountType.accountType) }
    var keepTotals by remember { mutableStateOf(accountType.keepTotals) }
    var isAsset by remember { mutableStateOf(accountType.isAsset) }
    var tallyOwing by remember { mutableStateOf(accountType.tallyOwing) }
    var displayAsAsset by remember { mutableStateOf(accountType.displayAsAsset) }
    var allowPending by remember { mutableStateOf(accountType.allowPending) }

    val accountTypeNames by accountViewModel.getAccountTypeNames().observeAsState(emptyList())

    AccountTypeFormScreen(
        name = name,
        onNameChange = { name = it },
        keepTotals = keepTotals,
        onKeepTotalsChange = { keepTotals = it },
        isAsset = isAsset,
        onIsAssetChange = { isAsset = it },
        keepOwing = tallyOwing,
        onKeepOwingChange = { tallyOwing = it },
        displayAsAsset = displayAsAsset,
        onDisplayAsAssetChange = { displayAsAsset = it },
        allowPending = allowPending,
        onAllowPendingChange = { allowPending = it },
        onSaveClick = {
            val answer = if (name.trim().isEmpty()) {
                mainActivity.getString(R.string.please_enter_a_name)
            } else if (accountTypeNames.any { it == name.trim() && it != accountType.accountType }) {
                mainActivity.getString(R.string.this_account_rule_already_exists)
            } else {
                ANSWER_OK
            }

            if (answer == ANSWER_OK) {
                val updatedType = accountType.copy(
                    accountType = name.trim(),
                    keepTotals = keepTotals,
                    isAsset = isAsset,
                    tallyOwing = tallyOwing,
                    displayAsAsset = displayAsAsset,
                    allowPending = allowPending,
                    acctUpdateTime = df.getCurrentTimeAsString()
                )

                if (name.trim() == accountType.accountType) {
                    accountViewModel.updateAccountType(updatedType)
                    navController.popBackStack()
                } else {
                    AlertDialog.Builder(mainActivity).apply {
                        setTitle(mainActivity.getString(R.string.rename_account_type))
                        setMessage(
                            mainActivity.getString(R.string.are_you_sure_you_want_to_rename_this_account_type) +
                                    mainActivity.getString(R.string.note) +
                                    mainActivity.getString(R.string.this_will_not_replace_an_existing_account_type)
                        )
                        setPositiveButton(mainActivity.getString(R.string.update_account_type)) { _, _ ->
                            accountViewModel.updateAccountType(updatedType)
                            navController.popBackStack()
                        }
                        setNegativeButton(mainActivity.getString(R.string.cancel), null)
                    }.create().show()
                }
            } else {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        fabContentDescription = mainActivity.getString(R.string.update_account_type)
    )
}