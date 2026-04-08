package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNTS

class AccountsFragment : Fragment(), MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val cf = NumberFunctions()
    private val vf = VisualsFunctions()
    private val df = DateFunctions()

    private var searchQueryState = mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_an_account)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountsScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun setupRecyclerView() {
        // Since this fragment uses Compose, we can trigger a recomposition by refreshing data
        // or just let it be if it's already observing LiveData.
        // However, if we want to force a refresh when called from MainActivity:
        searchQueryState.value = searchQueryState.value // Trigger recomposition if needed
    }

    @Composable
    fun AccountsScreen() {
        val query by searchQueryState
        val accountsWithType by if (query.isEmpty()) {
            accountViewModel.getAccountsWithType().observeAsState(emptyList())
        } else {
            accountViewModel.searchAccountsWithType("%$query%").observeAsState(emptyList())
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { gotoAccountAddFragment() },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_new_account),
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
                if (accountsWithType.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_accounts_to_view),
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.headlineSmall,
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
                        items(accountsWithType) { account ->
                            AccountItem(account)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccountItem(accountWithType: AccountWithType) {
        val account = accountWithType.account
        val isDeleted = account.accIsDeleted
        val bgColor = if (isDeleted) Color(0xFFB00020) else Color.White
        val textColor = if (isDeleted) Color.White else Color.Black

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptionsForAccount(accountWithType) },
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
                        text = account.accountName,
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
                Text(
                    text = accountWithType.accountType?.accountType ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )

                val info = getAccountInfoText(accountWithType)
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

    private fun getAccountInfoText(accountWithType: AccountWithType): String {
        val account = accountWithType.account
        val parts = mutableListOf<String>()
        if (account.accountNumber.isNotEmpty()) {
            parts.add("# ${account.accountNumber}")
        }
        if (account.accountBalance != 0.0) {
            parts.add(getString(R.string.balance) + cf.displayDollars(account.accountBalance))
        }
        if (account.accountOwing != 0.0) {
            parts.add(getString(R.string.owing) + cf.displayDollars(account.accountOwing))
        }
        if (account.accBudgetedAmount != 0.0) {
            parts.add(getString(R.string.budgeted) + cf.displayDollars(account.accBudgetedAmount))
        }
        if (account.accountCreditLimit != 0.0) {
            parts.add(getString(R.string.credit_limit) + cf.displayDollars(account.accountCreditLimit))
        }
        if (account.accIsDeleted) {
            parts.add(getString(R.string.deleted))
        }
        return parts.joinToString("\n")
    }

    private fun chooseOptionsForAccount(accountWithType: AccountWithType) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + accountWithType.account.accountName
        ).setItems(
            arrayOf(
                getString(R.string.edit_this_account),
                getString(R.string.delete_this_account),
                getString(R.string.view_a_summary_of_transactions_using_this_account)
            )
        ) { _, pos ->
            when (pos) {
                0 -> gotoAccountUpdateFragment(accountWithType)
                1 -> deleteAccount(accountWithType)
                2 -> gotoTransactionAverageFragment(accountWithType)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun deleteAccount(accountWithType: AccountWithType) {
        accountViewModel.deleteAccount(
            accountWithType.account.accountId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoAccountAddFragment() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        findNavController().navigate(
            AccountsFragmentDirections.actionAccountsFragmentToAccountAddFragment()
        )
    }

    fun gotoAccountUpdateFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        findNavController().navigate(
            AccountsFragmentDirections.actionAccountsFragmentToAccountUpdateFragment()
        )
    }

    fun gotoTransactionAverageFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().navigate(
            AccountsFragmentDirections.actionAccountsFragmentToTransactionAnalysisFragment()
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val searchItem = menu.add(Menu.NONE, R.id.action_search, Menu.NONE, R.string.search).apply {
            setIcon(android.R.drawable.ic_menu_search)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        }
        val searchView = SearchView(requireContext())
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQueryState.value = newText ?: ""
                return true
            }
        })
        searchItem.actionView = searchView
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
}