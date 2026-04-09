package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    private var nameState = mutableStateOf("")
    private var keepTotalsState = mutableStateOf(false)
    private var isAssetState = mutableStateOf(false)
    private var keepOwingState = mutableStateOf(false)
    private var displayAsAssetState = mutableStateOf(false)
    private var allowPendingState = mutableStateOf(false)

    private var accountTypeList = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountTypeAddScreen()
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_a_new_account_type)

        lifecycleScope.launch(Dispatchers.IO) {
            accountTypeList = accountViewModel.getAccountTypeNames()
        }
    }

    @Composable
    fun AccountTypeAddScreen() {
        var name by nameState
        var keepTotals by keepTotalsState
        var isAsset by isAssetState
        var keepOwing by keepOwingState
        var displayAsAsset by displayAsAssetState
        var allowPending by allowPendingState

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { saveAccountIfValid() },
                    containerColor = Color(0xFFB00020)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(R.string.save),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_type)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                CheckboxRow(
                    text = stringResource(R.string.this_account_does_not_keep_a_balance_owing_amount),
                    checked = keepTotals,
                    onCheckedChange = { keepTotals = it }
                )
                CheckboxRow(
                    text = stringResource(R.string.this_is_an_asset),
                    checked = isAsset,
                    onCheckedChange = { isAsset = it }
                )
                CheckboxRow(
                    text = stringResource(R.string.balance_owing_will_be_calculated),
                    checked = keepOwing,
                    onCheckedChange = { keepOwing = it }
                )
                CheckboxRow(
                    text = stringResource(R.string.this_will_be_used_for_the_budget),
                    checked = displayAsAsset,
                    onCheckedChange = { displayAsAsset = it }
                )
                CheckboxRow(
                    text = stringResource(R.string.transactions_may_be_postponed),
                    checked = allowPending,
                    onCheckedChange = { allowPending = it }
                )
            }
        }
    }

    @Composable
    fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = text, modifier = Modifier.padding(start = 8.dp))
        }
    }

    private fun saveAccountIfValid() {
        val name = nameState.value.trim()
        if (name.isEmpty()) {
            showMessage(getString(R.string.enter_a_name_for_this_account_type))
            return
        }
        if (accountTypeList.contains(name)) {
            showMessage(getString(R.string.enter_a_unique_name_for_this_account_type))
            return
        }

        val accountType = AccountType(
            nf.generateId(),
            name,
            keepTotalsState.value,
            isAssetState.value,
            keepOwingState.value,
            false,
            displayAsAssetState.value,
            allowPendingState.value,
            false,
            df.getCurrentTimeAsString()
        )
        accountViewModel.addAccountType(accountType)
        val accountWithType = mainViewModel.getAccountWithType()
        if (accountWithType != null) {
            mainViewModel.setAccountWithType(
                AccountWithType(accountWithType.account, accountType)
            )
        }
        gotoCallingFragment()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        findNavController().popBackStack()
    }
}