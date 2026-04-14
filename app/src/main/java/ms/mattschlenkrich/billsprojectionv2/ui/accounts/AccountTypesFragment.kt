package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_TYPES

class AccountTypesFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val vf = VisualsFunctions()

    private var searchQueryState = mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    val query by searchQueryState
                    val accountTypes by if (query.isEmpty()) {
                        accountViewModel.getActiveAccountTypes().observeAsState(emptyList())
                    } else {
                        accountViewModel.searchAccountTypes("%$query%").observeAsState(emptyList())
                    }
                    AccountTypeListScreen(
                        accountTypes = accountTypes,
                        onAddClick = { gotoAccountTypeAdd() },
                        onAccountTypeClick = { chooseAccountType(it) },
                        onAccountTypeLongClick = { updateAccountType(it) },
                        getAccountTypeInfo = { getAccountTypeInfoText(it) }
                    )
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_an_account_type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getAccountTypeInfoText(type: AccountType): String {
        val parts = mutableListOf<String>()
        if (type.keepTotals) parts.add(getString(R.string.balance_will_be_updated))
        if (type.tallyOwing) parts.add(getString(R.string.will_calculate_amount_owing))
        if (type.isAsset) parts.add(getString(R.string.this_is_an_asset))
        if (type.displayAsAsset) parts.add(getString(R.string.this_will_be_used_for_the_budget))
        if (type.allowPending) parts.add(getString(R.string.allow_transactions_pending))
        if (type.acctIsDeleted) parts.add(getString(R.string.deleted))
        return if (parts.isEmpty()) getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
        else parts.joinToString("\n")
    }

    private fun chooseAccountType(accountType: AccountType) {
        val accountWithType = mainViewModel.getAccountWithType()
        if (accountWithType != null) {
            mainViewModel.setAccountType(accountType)
            mainViewModel.setAccountWithType(
                AccountWithType(accountWithType.account, accountType)
            )
        }
        val mCallingFragment = mainViewModel.getCallingFragments() ?: ""
        if (mCallingFragment.contains(FRAG_ACCOUNT_UPDATE)) {
            gotoAccountUpdateFragment()
        } else if (mCallingFragment.contains(FRAG_ACCOUNT_ADD)) {
            gotoAccountAddFragment()
        } else {
            updateAccountType(accountType)
        }
    }

    private fun updateAccountType(accountType: AccountType) {
        mainViewModel.setAccountType(accountType)
        findNavController().navigate(
            AccountTypesFragmentDirections.actionAccountTypesFragmentToAccountTypeUpdateFragment()
        )
    }

    private fun gotoAccountTypeAdd() {
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            AccountTypesFragmentDirections.actionAccountTypesFragmentToAccountTypeAddFragment()
        )
    }

    fun gotoAccountAddFragment() {
        findNavController().navigate(
            AccountTypesFragmentDirections.actionAccountTypesFragmentToAccountAddFragment()
        )
    }

    fun gotoAccountUpdateFragment() {
        findNavController().navigate(
            AccountTypesFragmentDirections.actionAccountTypesFragmentToAccountUpdateFragment()
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val searchItem = menu.add(Menu.NONE, R.id.action_search, Menu.NONE, R.string.search).apply {
            setIcon(android.R.drawable.ic_menu_search)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        }
        val searchView = SearchView(mainActivity)
        searchItem.actionView = searchView
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQueryState.value = newText ?: ""
                return true
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
}