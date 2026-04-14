package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_ADD

class AccountAddFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var nameState = mutableStateOf("")
    private var handleState = mutableStateOf("")
    private var balanceState = mutableStateOf("")
    private var owingState = mutableStateOf("")
    private var budgetedState = mutableStateOf("")
    private var limitState = mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()
        mainActivity.topMenuBar.title = getString(R.string.add_a_new_account)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountAddFragmentScreen()
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainActivity.topMenuBar.title = getString(R.string.add_account)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel

        // Initialize values from cache if they exist
        val cached = mainViewModel.getAccountWithType()
        if (cached != null) {
            nameState.value = cached.account.accountName
            handleState.value = cached.account.accountNumber
            balanceState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 &&
                    mainViewModel.getReturnTo()?.contains(BALANCE) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    cached.account.accountBalance
                }
            )
            owingState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 &&
                    mainViewModel.getReturnTo()?.contains(OWING) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    cached.account.accountOwing
                }
            )
            budgetedState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0 &&
                    mainViewModel.getReturnTo()?.contains(BUDGETED) == true
                ) {
                    mainViewModel.getTransferNum()!!
                } else {
                    cached.account.accBudgetedAmount
                }
            )
            limitState.value = nf.displayDollars(cached.account.accountCreditLimit)
            mainViewModel.setTransferNum(0.0)
        } else {
            balanceState.value = nf.displayDollars(0.0)
            owingState.value = nf.displayDollars(0.0)
            budgetedState.value = nf.displayDollars(0.0)
            limitState.value = nf.displayDollars(0.0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Composable
    fun AccountAddFragmentScreen() {
        var name by nameState
        var handle by handleState
        var balance by balanceState
        var owing by owingState
        var budgeted by budgetedState
        var limit by limitState

        val accountWithType = mainViewModel.getAccountWithType()
        val accountType = accountWithType?.accountType

        AccountEditScreen(
            name = name,
            onNameChange = { name = it },
            handle = handle,
            onHandleChange = { handle = it },
            accountType = accountType,
            onAccountTypeClick = { gotoAccountTypes() },
            accountTypeDetails = if (accountType != null) getAccountTypeDetails(accountType) else "",
            balance = balance,
            onBalanceChange = { balance = it },
            onBalanceIconClick = { gotoCalculator(BALANCE) },
            owing = owing,
            onOwingChange = { owing = it },
            onOwingIconClick = { gotoCalculator(OWING) },
            budgeted = budgeted,
            onBudgetedChange = { budgeted = it },
            onBudgetedIconClick = { gotoCalculator(BUDGETED) },
            limit = limit,
            onLimitChange = { limit = it }
        )
    }

    private fun getAccountTypeDetails(accountType: ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType): String {
        val details = mutableListOf<String>()
        if (accountType.keepTotals) details.add(getString(R.string.this_account_does_not_keep_a_balance_owing_amount))
        if (accountType.isAsset) details.add(getString(R.string.this_is_an_asset))
        if (accountType.displayAsAsset) details.add(getString(R.string.this_will_be_used_for_the_budget))
        if (accountType.tallyOwing) details.add(getString(R.string.balance_owing_will_be_calculated))
        if (accountType.allowPending) details.add(getString(R.string.transactions_may_be_postponed))
        return if (details.isEmpty()) getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
        else details.joinToString("\n")
    }

    private fun getCurrentAccount(): Account {
        return Account(
            nf.generateId(),
            nameState.value.trim(),
            handleState.value.trim(),
            mainViewModel.getAccountWithType()?.accountType?.typeId ?: 0L,
            nf.getDoubleFromDollars(budgetedState.value),
            nf.getDoubleFromDollars(balanceState.value),
            nf.getDoubleFromDollars(owingState.value),
            nf.getDoubleFromDollars(limitState.value),
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun saveAccountIfValid() {
        val accountNames = accountViewModel.getAccountNameList().value ?: emptyList()
        val name = nameState.value.trim()

        if (name.isEmpty()) {
            showMessage(getString(R.string.please_enter_a_name_for_this_account))
            return
        }
        if (accountNames.contains(name)) {
            showMessage(getString(R.string.this_account_already_exists))
            return
        }
        val accountType = mainViewModel.getAccountWithType()?.accountType
        if (accountType == null) {
            showMessage(getString(R.string.this_account_must_have_an_account_type))
            return
        }

        val curAccount = getCurrentAccount()
        accountViewModel.addAccount(curAccount)
        mainViewModel.setAccountWithType(AccountWithType(curAccount, accountType))
        gotoCallingFragment()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoAccountTypes() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(
            AccountWithType(getCurrentAccount(), mainViewModel.getAccountWithType()?.accountType)
        )
        findNavController().navigate(
            AccountAddFragmentDirections.actionAccountAddFragmentToAccountTypesFragment()
        )
    }

    private fun gotoCalculator(type: String) {
        val currentValue = when (type) {
            BALANCE -> balanceState.value
            OWING -> owingState.value
            BUDGETED -> budgetedState.value
            else -> "0.00"
        }
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(currentValue.ifBlank { getString(R.string.zero_double) }))
        mainViewModel.setAccountWithType(
            AccountWithType(getCurrentAccount(), mainViewModel.getAccountWithType()?.accountType)
        )
        findNavController().navigate(
            AccountAddFragmentDirections.actionAccountAddFragmentToCalcFragment()
        )
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        findNavController().popBackStack()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.apply {
            add(Menu.NONE, R.id.action_save, Menu.NONE, R.string.save).apply {
                setIcon(android.R.drawable.ic_menu_save)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_save -> {
                saveAccountIfValid()
                true
            }

            else -> false
        }
    }
}