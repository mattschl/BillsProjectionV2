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
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountTypeFormScreen

@Composable
fun AccountTypeUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    val df = remember { DateFunctions() }

    val accountType = mainViewModel.getAccountType() ?: return

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
                mainActivity.getString(R.string.msg_prompt_enter_name)
            } else if (accountTypeNames.any { it == name.trim() && it != accountType.accountType }) {
                mainActivity.getString(R.string.msg_error_account_rule_exists)
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
                        setTitle(mainActivity.getString(R.string.title_rename_account_type))
                        setMessage(
                            mainActivity.getString(R.string.prompt_rename_account_type) + "\n\n" +
                                    mainActivity.getString(R.string.label_note_header) +
                                    mainActivity.getString(R.string.msg_wont_replace_account_type)
                        )
                        setPositiveButton(mainActivity.getString(R.string.action_confirm)) { _, _ ->
                            accountViewModel.updateAccountType(updatedType)
                            navController.popBackStack()
                        }
                        setNegativeButton(mainActivity.getString(R.string.action_cancel), null)
                    }.create().show()
                }
            } else {
                Toast.makeText(
                    mainActivity,
                    mainActivity.getString(R.string.label_error) + answer,
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        fabContentDescription = mainActivity.getString(R.string.action_update_account_type)
    )
}