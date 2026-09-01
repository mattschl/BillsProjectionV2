package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_ACCOUNT_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_ADD
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.SCREEN_TRANSACTION_UPDATE
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.compose.AccountChooseScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = SCREEN_ACCOUNT_CHOOSE

@Composable
fun AccountChooseScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val accountViewModel = mainActivity.accountViewModel
    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.title_choose_account)
    }

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
            if (mCallingFragment.contains(SCREEN_BUDGET_RULE_ADD) ||
                mCallingFragment.contains(SCREEN_BUDGET_RULE_UPDATE)
            ) {
                populateBudgetRuleDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(SCREEN_BUDGET_ITEM_ADD) ||
                mCallingFragment.contains(SCREEN_BUDGET_ITEM_UPDATE)
            ) {
                populateBudgetItemDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(SCREEN_TRANSACTION_SPLIT)) {
                populateSplitTransaction(mainActivity, curAccount)
            } else if (mCallingFragment.contains(SCREEN_TRANSACTION_ADD) ||
                mCallingFragment.contains(SCREEN_TRANSACTION_PERFORM) ||
                mCallingFragment.contains(SCREEN_TRANSACTION_UPDATE)
            ) {
                populateTransactionDetailed(mainActivity, curAccount)
            } else if (mCallingFragment.contains(SCREEN_TRANSACTION_ANALYSIS)) {
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
    val splitTrans = mainViewModel.getSplitTransactionDetailed() ?: return
    val requestedAccount = mainViewModel.getRequestedAccount()
    val isToAccount = requestedAccount == REQUEST_TO_ACCOUNT
    val isFromAccount = requestedAccount == REQUEST_FROM_ACCOUNT

    val accountType = curAccount.accountType
    val updatedTransaction = splitTrans.transaction?.copy(
        transToAccountPending = if (isToAccount) (accountType?.allowPending == true && accountType.tallyOwing)
        else splitTrans.transaction.transToAccountPending,
        transFromAccountPending = if (isFromAccount) (accountType?.allowPending == true && accountType.tallyOwing)
        else splitTrans.transaction.transFromAccountPending
    )
    val splitTransactionDetailed = splitTrans.copy(
        transaction = updatedTransaction,
        toAccount = if (isToAccount) curAccount.account else splitTrans.toAccount,
        fromAccount = if (isFromAccount) curAccount.account else splitTrans.fromAccount,
    )
    mainViewModel.setSplitTransactionDetailed(splitTransactionDetailed)
}

private fun populateTransactionDetailed(mainActivity: MainActivity, curAccount: AccountWithType) {
    val mainViewModel = mainActivity.mainViewModel
    val tempTrans = mainViewModel.getTransactionDetailed() ?: return
    val requestedAccount = mainViewModel.getRequestedAccount()
    val isToAccount = requestedAccount == REQUEST_TO_ACCOUNT
    val isFromAccount = requestedAccount == REQUEST_FROM_ACCOUNT

    val accountType = curAccount.accountType
    val updatedTransaction = tempTrans.transaction?.copy(
        transToAccountPending = if (isToAccount) (accountType?.allowPending == true && accountType.tallyOwing)
        else tempTrans.transaction.transToAccountPending,
        transFromAccountPending = if (isFromAccount) (accountType?.allowPending == true && accountType.tallyOwing)
        else tempTrans.transaction.transFromAccountPending
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
    val tempBudgetItem = mainViewModel.getBudgetItemDetailed() ?: return
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
    val tempBudgetRule = mainViewModel.getBudgetRuleDetailed() ?: return
    val isToAccount = mainViewModel.getRequestedAccount() == REQUEST_TO_ACCOUNT
    mainViewModel.setBudgetRuleDetailed(
        tempBudgetRule.copy(
            toAccount = if (isToAccount) curAccount.account else tempBudgetRule.toAccount,
            fromAccount = if (!isToAccount) curAccount.account else tempBudgetRule.fromAccount,
        )
    )
}