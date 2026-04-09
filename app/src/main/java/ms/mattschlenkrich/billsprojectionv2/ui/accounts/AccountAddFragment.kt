package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
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
                    AccountAddScreen()
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
    fun AccountAddScreen() {
        var name by nameState
        var handle by handleState
        var balance by balanceState
        var owing by owingState
        var budgeted by budgetedState
        var limit by limitState

        val accountWithType = mainViewModel.getAccountWithType()
        val accountType = accountWithType?.accountType

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ProjectTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            stringResource(R.string.account_name),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                ProjectTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = {
                        Text(
                            stringResource(R.string.number),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            }

            OutlinedCard(
                onClick = { gotoAccountTypes() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.account_type) + ":",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = accountType?.accountType
                                ?: stringResource(R.string.choose_an_account_type),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (accountType == null) MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    }
                    if (accountType != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getAccountTypeDetails(accountType),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceField(
                    label = stringResource(R.string.balance),
                    value = balance,
                    onValueChange = { balance = it },
                    onLongClick = { gotoCalculator(BALANCE) },
                    modifier = Modifier.weight(1f)
                )
                BalanceField(
                    label = stringResource(R.string.owing),
                    value = owing,
                    onValueChange = { owing = it },
                    onLongClick = { gotoCalculator(OWING) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceField(
                    label = stringResource(R.string.budgeted),
                    value = budgeted,
                    onValueChange = { budgeted = it },
                    onLongClick = { gotoCalculator(BUDGETED) },
                    modifier = Modifier.weight(1f)
                )
                ProjectTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = {
                        Text(
                            stringResource(R.string.credit_limit),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 2.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true
                )
            }
        }
    }

    @Composable
    fun BalanceField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        onLongClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        ProjectTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            modifier = modifier
                .padding(horizontal = 2.dp)
                .height(56.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onLongClick) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = stringResource(R.string.calculator)
                    )
                }
            }
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