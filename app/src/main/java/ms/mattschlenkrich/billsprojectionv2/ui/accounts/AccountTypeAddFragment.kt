package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
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
                    AccountTypeFormScreen(
                        name = nameState.value,
                        onNameChange = { nameState.value = it },
                        keepTotals = keepTotalsState.value,
                        onKeepTotalsChange = { keepTotalsState.value = it },
                        isAsset = isAssetState.value,
                        onIsAssetChange = { isAssetState.value = it },
                        keepOwing = keepOwingState.value,
                        onKeepOwingChange = { keepOwingState.value = it },
                        displayAsAsset = displayAsAssetState.value,
                        onDisplayAsAssetChange = { displayAsAssetState.value = it },
                        allowPending = allowPendingState.value,
                        onAllowPendingChange = { allowPendingState.value = it },
                        onSaveClick = { saveAccountIfValid() },
                        fabContentDescription = stringResource(R.string.save)
                    )
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