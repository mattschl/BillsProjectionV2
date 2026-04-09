package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    AccountTypesScreen()
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

    @Composable
    fun AccountTypesScreen() {
        val query by searchQueryState
        val accountTypes by if (query.isEmpty()) {
            accountViewModel.getActiveAccountTypes().observeAsState(emptyList())
        } else {
            accountViewModel.searchAccountTypes("%$query%").observeAsState(emptyList())
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { gotoAccountTypeAdd() },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_new_account_type),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (accountTypes.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_types_found), // Assuming this string exists or similar
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(accountTypes) { type ->
                            AccountTypeItem(type)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AccountTypeItem(accountType: AccountType) {
        val isDeleted = accountType.acctIsDeleted
        val bgColor =
            if (isDeleted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        val textColor =
            if (isDeleted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { chooseAccountType(accountType) },
                    onLongClick = { updateAccountType(accountType) }
                ),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = accountType.accountType,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(vf.getRandomColorInt()))
                    )
                }

                val info = getAccountTypeInfoText(accountType)
                if (info.isNotEmpty()) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
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