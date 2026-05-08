package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
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
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_ACCOUNT_CHOOSE

@Composable
fun AccountChooseScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    mainActivity.topMenuBar.title = mainActivity.getString(R.string.choose_an_account)

    var searchQuery by remember { mutableStateOf("") }
    val accountsWithType by if (searchQuery.isEmpty()) {
        accountViewModel.getAccountsWithTypeBudgetFirst().observeAsState(emptyList())
    } else {
        accountViewModel.searchAccountsWithTypeBudgetFirst("%$searchQuery%")
            .observeAsState(emptyList())
    }

    AccountChooseScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        accountsWithType = accountsWithType,
        onAccountClick = { curAccount ->
            val mCallingFragment = mainViewModel.getCallingFragments() ?: ""
            if (mCallingFragment.contains(FRAG_BUDGET_RULE_ADD) ||
                mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)
            ) {
                populateBudgetRuleDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
                mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
            ) {
                populateBudgetItemDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                populateSplitTransaction(mainActivity, curAccount)
            } else if (mCallingFragment.contains(FRAG_TRANS_ADD) ||
                mCallingFragment.contains(FRAG_TRANS_PERFORM) ||
                mCallingFragment.contains(FRAG_TRANS_UPDATE)
            ) {
                populateTransactionDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(FRAG_TRANSACTION_ANALYSIS)) {
                mainViewModel.setAccountWithType(curAccount)
            }
            navController.popBackStack()
        },
        onAddAccountClick = {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setAccountWithType(null)
            navController.navigate(Screen.AccountAdd.route)
        },
    )
}

private fun populateSplitTransaction(mainActivity: MainActivity, curAccount: AccountWithType) {
    val mainViewModel = mainActivity.mainViewModel
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

private fun populateTransactionDetailed(mainActivity: MainActivity, curAccount: AccountWithType) {
    val mainViewModel = mainActivity.mainViewModel
    val tempTrans = mainViewModel.getTransactionDetailed()!!
    val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
    val isFromAccount = mainViewModel.getRequestedAccount() == REQUEST_FROM_ACCOUNT

    val accountType = curAccount.accountType!!
    val updatedTransaction = tempTrans.transaction?.copy(
        transToAccountPending = (accountType.allowPending || accountType.tallyOwing) && !isToAccount,
        transFromAccountPending = (accountType.allowPending || accountType.tallyOwing) && isFromAccount
    )

    val transactionDetailed = tempTrans.copy(
        transaction = updatedTransaction,
        toAccount = if (isToAccount) curAccount.account else tempTrans.toAccount,
        fromAccount = if (isFromAccount) curAccount.account else tempTrans.fromAccount,
    )
    mainViewModel.setTransactionDetailed(transactionDetailed)
}

private fun populateBudgetItemDetailed(mainActivity: MainActivity, curAccount: AccountWithType) {
    val mainViewModel = mainActivity.mainViewModel
    val tempBudgetItem = mainViewModel.getBudgetItemDetailed()!!
    val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
    mainViewModel.setBudgetItemDetailed(
        tempBudgetItem.copy(
            toAccount = if (isToAccount) curAccount.account else tempBudgetItem.toAccount,
            fromAccount = if (!isToAccount) curAccount.account else tempBudgetItem.fromAccount,
        )
    )
}

private fun populateBudgetRuleDetailed(mainActivity: MainActivity, curAccount: AccountWithType) {
    val mainViewModel = mainActivity.mainViewModel
    val tempBudgetRule = mainViewModel.getBudgetRuleDetailed()!!
    val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
    mainViewModel.setBudgetRuleDetailed(
        tempBudgetRule.copy(
            toAccount = if (isToAccount) curAccount.account else tempBudgetRule.toAccount,
            fromAccount = if (!isToAccount) curAccount.account else tempBudgetRule.fromAccount,
        )
    )
}