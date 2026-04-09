package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNTS

class AccountsFragment : Fragment(), RefreshableFragment {
    
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val cf = NumberFunctions()
    private val vf = VisualsFunctions()
    private val df = DateFunctions()
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.choose_an_account)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountsScreen(refreshKey.intValue)
                }
            }
        }
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.accounts)
        lifecycleScope.launch {
            delay(100)
            refreshKey.intValue++
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun setupRecyclerView() {
        // Since this fragment uses Compose, we can trigger a recomposition by refreshing data
    }

    @Composable
    fun AccountsScreen(refreshKey: Int) {
        var searchQuery by remember(refreshKey) { mutableStateOf("") }
        val accountsWithType by if (searchQuery.isEmpty()) {
            accountViewModel.getAccountsWithType().observeAsState(emptyList())
        } else {
            accountViewModel.searchAccountsWithType("%$searchQuery%").observeAsState(emptyList())
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { gotoAccountAddFragment() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_new_account),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp)
            ) {
                ProjectTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search)) },
                    placeholder = { Text(stringResource(R.string.enter_criteria)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.weight(1f)
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
                            contentPadding = PaddingValues(bottom = 80.dp),
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
    }

    @Composable
    fun AccountItem(accountWithType: AccountWithType) {
        val account = accountWithType.account
        val isDeleted = account.accIsDeleted
        val containerColor = if (isDeleted) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
        val contentColor = if (isDeleted) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptionsForAccount(accountWithType) },
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = account.accountName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
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
                    style = MaterialTheme.typography.labelSmall
                )

                val info = getAccountInfoText(accountWithType)
                if (info.isNotEmpty()) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodySmall,
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
            R.id.action_accountsFragment_to_accountAddFragment
        )
    }

    fun gotoAccountUpdateFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        findNavController().navigate(
            R.id.action_accountsFragment_to_accountUpdateFragment
        )
    }

    fun gotoTransactionAverageFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().navigate(
            R.id.action_accountsFragment_to_transactionAnalysisFragment
        )
    }
}