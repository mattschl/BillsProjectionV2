package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_TYPE_UPDATE

class AccountTypeUpdateFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel

    private lateinit var currentAccountType: AccountType
    private var accountTypeList = listOf<String>()
    private val df = DateFunctions()

    private var nameState = mutableStateOf("")
    private var keepTotalsState = mutableStateOf(false)
    private var isAssetState = mutableStateOf(false)
    private var keepOwingState = mutableStateOf(false)
    private var displayAsAssetState = mutableStateOf(false)
    private var allowPendingState = mutableStateOf(false)

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
                        onSaveClick = { isAccountTypeReadyToUpdate() },
                        fabContentDescription = stringResource(R.string.update_account_type)
                    )
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        currentAccountType = mainViewModel.getAccountType()!!
        mainActivity.topMenuBar.title = getString(R.string.update_account_type)

        nameState.value = currentAccountType.accountType
        keepTotalsState.value = currentAccountType.keepTotals
        isAssetState.value = currentAccountType.isAsset
        keepOwingState.value = currentAccountType.tallyOwing
        displayAsAssetState.value = currentAccountType.displayAsAsset
        allowPendingState.value = currentAccountType.allowPending

        lifecycleScope.launch(Dispatchers.IO) {
            accountTypeList = accountViewModel.getAccountTypeNames()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun isAccountTypeReadyToUpdate() {
        val newName = nameState.value.trim()
        if (newName == currentAccountType.accountType) {
            val answer = validateAccountType()
            if (answer == ANSWER_OK) {
                updateAccountType()
            } else {
                showMessage("${getString(R.string.error)}: $answer")
            }
        } else {
            val answer = validateAccountType()
            if (answer == ANSWER_OK) {
                confirmRenameAccountType()
            } else {
                showMessage("${getString(R.string.error)}: $answer")
            }
        }
    }

    private fun confirmRenameAccountType() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.rename_account_type))
            setMessage(
                getString(R.string.are_you_sure_you_want_to_rename_this_account_type) + "\n\n" +
                        getString(R.string.note) + ": " +
                        getString(R.string.this_will_not_replace_an_existing_account_type)
            )
            setPositiveButton(
                getString(R.string.update_account_type)
            ) { _, _ ->
                updateAccountType()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun validateAccountType(): String {
        val name = nameState.value.trim()
        if (name.isBlank()) {
            return getString(R.string.enter_a_name_for_this_account_type)
        }
        if (name != currentAccountType.accountType && accountTypeList.contains(name)) {
            return getString(R.string.enter_a_unique_name_for_this_account_type)
        }
        return ANSWER_OK
    }

    private fun updateAccountType() {
        accountViewModel.updateAccountType(
            AccountType(
                currentAccountType.typeId,
                nameState.value.trim(),
                keepTotalsState.value,
                isAssetState.value,
                keepOwingState.value,
                false,
                displayAsAssetState.value,
                allowPendingState.value,
                false,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun confirmDeleteAccountType() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_account_type))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_account_type))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccountType()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteAccountType() {
        accountViewModel.deleteAccountType(
            currentAccountType.typeId,
            df.getCurrentTimeAsString()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        findNavController().popBackStack()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(Menu.NONE, R.id.action_delete, Menu.NONE, R.string.delete).apply {
            setIcon(android.R.drawable.ic_menu_delete)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                confirmDeleteAccountType()
                true
            }

            else -> false
        }
    }
}