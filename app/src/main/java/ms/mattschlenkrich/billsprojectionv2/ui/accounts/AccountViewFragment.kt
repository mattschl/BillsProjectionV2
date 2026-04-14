package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
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

class AccountViewFragment : Fragment(), RefreshableFragment {

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
                    AccountViewFragmentScreen(refreshKey.intValue)
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

    @Composable
    fun AccountViewFragmentScreen(refreshKey: Int) {
        var searchQuery by remember(refreshKey) { mutableStateOf("") }
        val accountsWithType by if (searchQuery.isEmpty()) {
            accountViewModel.getAccountsWithType().observeAsState(emptyList())
        } else {
            accountViewModel.searchAccountsWithType("%$searchQuery%").observeAsState(emptyList())
        }

        AccountsListScreen(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            accountsWithType = accountsWithType,
            onAddAccountClick = { gotoAccountAddFragment() },
            onAccountClick = { chooseOptionsForAccount(it) },
            getAccountInfoText = { getAccountInfoText(it) },
            vf = vf
        )
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
            R.id.action_accountViewFragment_to_accountAddFragment
        )
    }

    fun gotoAccountUpdateFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        findNavController().navigate(
            R.id.action_accountViewFragment_to_accountUpdateFragment
        )
    }

    fun gotoTransactionAverageFragment(accountWithType: AccountWithType) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(accountWithType)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().navigate(
            R.id.action_accountViewFragment_to_transactionAnalysisFragment
        )
    }
}