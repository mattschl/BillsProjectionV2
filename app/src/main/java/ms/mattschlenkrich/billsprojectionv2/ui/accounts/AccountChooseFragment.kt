package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_CHOOSE

class AccountChooseFragment : Fragment(), MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel

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
                    AccountChooseScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Composable
    fun AccountChooseScreen() {
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
                        items(accountsWithType) { account ->
                            AccountSelectItem(account)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccountSelectItem(accountWithType: AccountWithType) {
        val account = accountWithType.account
        val isDeleted = account.accIsDeleted
        val bgColor = if (isDeleted) Color(0xFFB00020) else Color.White
        val textColor = if (isDeleted) Color.White else {
            val accountType = accountWithType.accountType
            if (accountType != null &&
                (accountType.tallyOwing || accountType.keepTotals) &&
                accountType.displayAsAsset
            ) Color.Red else Color.Black
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseAccountAndPopulateCache(accountWithType) },
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = account.accountName + if (isDeleted) " " + stringResource(R.string.deleted) else "",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }

    private fun chooseAccountAndPopulateCache(curAccount: AccountWithType) {
        val mCallingFragment = mainViewModel.getCallingFragments() ?: ""
        if (mCallingFragment.contains(FRAG_BUDGET_RULE_ADD) ||
            mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)
        ) {
            populateBudgetRuleDetailed(curAccount)
        } else if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            populateBudgetItemDetailed(curAccount)
        } else if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
            populateSplitTransaction(curAccount)
        } else if (mCallingFragment.contains(FRAG_TRANS_ADD) ||
            mCallingFragment.contains(FRAG_TRANS_PERFORM) ||
            mCallingFragment.contains(FRAG_TRANS_UPDATE)
        ) {
            populateTransactionDetailed(curAccount)
        }
        findNavController().popBackStack()
    }

    private fun populateSplitTransaction(curAccount: AccountWithType) {
        val splitTrans = mainViewModel.getSplitTransactionDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
        val updatedTransaction = splitTrans.transaction?.copy(
            transToAccountPending = curAccount.accountType?.tallyOwing ?: false
        )
        val splitTransactionDetailed = splitTrans.copy(
            transaction = updatedTransaction,
            toAccount = if (isToAccount) curAccount.account else splitTrans.toAccount,
            fromAccount = if (!isToAccount) curAccount.account else splitTrans.fromAccount,
        )
        mainViewModel.setSplitTransactionDetailed(splitTransactionDetailed)
    }

    private fun populateTransactionDetailed(curAccount: AccountWithType) {
        val tempTrans = mainViewModel.getTransactionDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
        val isFromAccount = mainViewModel.getRequestedAccount() == REQUEST_FROM_ACCOUNT

        val accountType = curAccount.accountType
        val updatedTransaction = tempTrans.transaction?.copy(
            transToAccountPending = (accountType?.allowPending ?: false) && (accountType?.tallyOwing
                ?: false) && isToAccount,
            transFromAccountPending = (accountType?.allowPending
                ?: false) && (accountType?.tallyOwing ?: false) && isFromAccount
        )

        val transactionDetailed = tempTrans.copy(
            transaction = updatedTransaction,
            toAccount = if (isToAccount) curAccount.account else tempTrans.toAccount,
            fromAccount = if (isFromAccount) curAccount.account else tempTrans.fromAccount,
        )
        mainViewModel.setTransactionDetailed(transactionDetailed)
    }

    private fun populateBudgetItemDetailed(curAccount: AccountWithType) {
        val tempBudgetItem = mainViewModel.getBudgetItemDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
        mainViewModel.setBudgetItemDetailed(
            tempBudgetItem.copy(
                toAccount = if (isToAccount) curAccount.account else tempBudgetItem.toAccount,
                fromAccount = if (!isToAccount) curAccount.account else tempBudgetItem.fromAccount,
            )
        )
    }

    private fun populateBudgetRuleDetailed(curAccount: AccountWithType) {
        val tempBudgetRule = mainViewModel.getBudgetRuleDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
        mainViewModel.setBudgetRuleDetailed(
            tempBudgetRule.copy(
                toAccount = if (isToAccount) curAccount.account else tempBudgetRule.toAccount,
                fromAccount = if (!isToAccount) curAccount.account else tempBudgetRule.fromAccount,
            )
        )
    }

    private fun gotoAccountAddFragment() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        findNavController().navigate(
            AccountChooseFragmentDirections.actionAccountChooseFragmentToAccountAddFragment()
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