package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_CHOOSE

class AccountChooseFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountChooseFragmentScreen()
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_an_account)
    }

    @Composable
    fun AccountChooseFragmentScreen() {
        var searchQuery by remember { mutableStateOf("") }
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
            onAccountClick = { chooseAccountAndPopulateCache(it) },
            getAccountInfoText = { "" }, // Choose screen doesn't show info text
        )
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
        } else if (mCallingFragment.contains(FRAG_TRANSACTION_ANALYSIS)) {
            mainViewModel.setAccountWithType(curAccount)
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
            R.id.action_accountChooseFragment_to_accountAddFragment
        )
    }
}