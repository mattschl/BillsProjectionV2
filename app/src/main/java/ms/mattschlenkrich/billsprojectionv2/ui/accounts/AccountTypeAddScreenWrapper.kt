package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountTypeFormScreen

@Composable
fun AccountTypeAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val accountViewModel = mainActivity.accountViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

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
                    mainActivity.getString(R.string.msg_prompt_enter_name),
                    Toast.LENGTH_LONG
                ).show()
            } else if (accountTypeNames.contains(name.trim())) {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.msg_error_account_rule_exists),
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
        fabContentDescription = mainActivity.getString(R.string.action_add_account_type)
    )
}