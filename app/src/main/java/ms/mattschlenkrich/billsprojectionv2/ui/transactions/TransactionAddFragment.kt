package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANS_ADD

class TransactionAddFragment : Fragment(), MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf("")
    private var descriptionState = mutableStateOf("")
    private var descriptionTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var noteState = mutableStateOf("")
    private var noteTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var amountState = mutableStateOf("")
    private var amountTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var toAccountState = mutableStateOf<Account?>(null)
    private var fromAccountState = mutableStateOf<Account?>(null)
    private var budgetRuleState = mutableStateOf<BudgetRule?>(null)
    private var toPendingState = mutableStateOf(false)
    private var fromPendingState = mutableStateOf(false)

    private var toAccountWithTypeState = mutableStateOf<AccountWithType?>(null)
    private var fromAccountWithTypeState = mutableStateOf<AccountWithType?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_a_new_transaction)

        populateValues()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    TransactionAddScreen()
                }
            }
        }
    }

    fun populateValues() {
        val cached = mainViewModel.getTransactionDetailed()
        if (cached != null) {
            val trans = cached.transaction
            val rule = cached.budgetRule
            if (trans != null) {
                dateState.value = trans.transDate
                descriptionState.value =
                    if (rule != null && trans.transName.isBlank())
                        rule.budgetRuleName
                    else trans.transName
                noteState.value = trans.transNote
                toPendingState.value = trans.transToAccountPending
                fromPendingState.value = trans.transFromAccountPending
                amountState.value = nf.displayDollars(
                    if ((mainViewModel.getTransferNum()
                            ?: 0.0) != 0.0
                    ) mainViewModel.getTransferNum()!!
                    else trans.transAmount
                )
            } else {
                dateState.value = df.getCurrentDateAsString()
                if (rule != null) {
                    descriptionState.value = rule.budgetRuleName
                    amountState.value = nf.displayDollars(rule.budgetAmount)
                } else {
                    amountState.value = nf.displayDollars(0.0)
                }
            }
            budgetRuleState.value = rule
            toAccountState.value = cached.toAccount
            fromAccountState.value = cached.fromAccount

            // Update TextFieldValues
            descriptionTextFieldValue.value = TextFieldValue(descriptionState.value)
            noteTextFieldValue.value = TextFieldValue(noteState.value)
            amountTextFieldValue.value = TextFieldValue(amountState.value)

            // Load extra account info
            lifecycleScope.launch {
                cached.toAccount?.let {
                    val awt = accountViewModel.getAccountWithType(it.accountId)
                    toAccountWithTypeState.value = awt
                    if (trans == null) {
                        toPendingState.value = awt.accountType?.allowPending == true &&
                                awt.accountType?.tallyOwing == true
                    }
                }
                cached.fromAccount?.let {
                    val awt = accountViewModel.getAccountWithType(it.accountId)
                    fromAccountWithTypeState.value = awt
                    if (trans == null) {
                        fromPendingState.value = awt.accountType?.allowPending == true &&
                                awt.accountType?.tallyOwing == true
                    }
                }

                // If budget rule set but accounts not, try loading from budget rule
                if (toAccountState.value == null && rule?.budToAccountId != 0L) {
                    val acc = accountViewModel.getAccount(rule!!.budToAccountId)
                    toAccountState.value = acc
                    val awt = accountViewModel.getAccountWithType(acc.accountId)
                    toAccountWithTypeState.value = awt
                    if (trans == null) {
                        toPendingState.value = awt.accountType?.allowPending == true &&
                                awt.accountType?.tallyOwing == true
                    }
                }
                if (fromAccountState.value == null && rule?.budFromAccountId != 0L) {
                    val acc = accountViewModel.getAccount(rule!!.budFromAccountId)
                    fromAccountState.value = acc
                    val awt = accountViewModel.getAccountWithType(acc.accountId)
                    fromAccountWithTypeState.value = awt
                    if (trans == null) {
                        fromPendingState.value = awt.accountType?.allowPending == true &&
                                awt.accountType?.tallyOwing == true
                    }
                }
            }
            mainViewModel.setTransferNum(0.0)
        } else {
            dateState.value = df.getCurrentDateAsString()
            amountState.value = nf.displayDollars(0.0)
        }
        descriptionTextFieldValue.value = TextFieldValue(descriptionState.value)
        noteTextFieldValue.value = TextFieldValue(noteState.value)
        amountTextFieldValue.value = TextFieldValue(amountState.value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Composable
    fun TransactionAddScreen() {
        var date by dateState
        var description by descriptionState
        var note by noteState
        var amount by amountState

        val cached = mainViewModel.getTransactionDetailed()
        val toAccount = cached?.toAccount
        val fromAccount = cached?.fromAccount
        val budgetRule = cached?.budgetRule
        var toPending by toPendingState
        var fromPending by fromPendingState

        val toAccountWithType = toAccountWithTypeState.value
        val fromAccountWithType = fromAccountWithTypeState.value

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { saveTransactionIfValid() },
                    modifier = Modifier
                        .padding(16.dp),
                    containerColor = Color(0xFFB00020) // Deep Red
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = stringResource(R.string.save),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var descriptionValue by descriptionTextFieldValue
                ProjectTextField(
                    value = descriptionValue,
                    onValueChange = {
                        descriptionValue = it
                        description = it.text
                    },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    ProjectTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.date)) },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { chooseDate() },
                        readOnly = true,
                        enabled = false,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BalanceField(
                        label = stringResource(R.string.amount),
                        value = amountTextFieldValue.value,
                        onValueChange = {
                            amountTextFieldValue.value = it
                            amount = it.text
                        },
                        onLongClick = { gotoCalculator() },
                        modifier = Modifier.weight(1f)
                    )
                }

                SelectorCard(
                    label = stringResource(R.string.budget_rule),
                    value = budgetRule?.budgetRuleName
                        ?: stringResource(R.string.choose_a_budget_rule),
                    onClick = { chooseBudgetRule() }
                )

                AccountSelectorCard(
                    label = stringResource(R.string.from_account_name),
                    account = fromAccount,
                    isPending = fromPending,
                    onPendingChange = { fromPending = it },
                    allowPending = fromAccountWithType?.accountType?.allowPending == true,
                    onClick = { chooseFromAccount() }
                )

                AccountSelectorCard(
                    label = stringResource(R.string.to_account_name),
                    account = toAccount,
                    isPending = toPending,
                    onPendingChange = { toPending = it },
                    allowPending = toAccountWithType?.accountType?.allowPending == true,
                    onClick = { chooseToAccount() }
                )

                var noteValue by noteTextFieldValue
                ProjectTextField(
                    value = noteValue,
                    onValueChange = {
                        noteValue = it
                        note = it.text
                    },
                    label = { Text(stringResource(R.string.note)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { splitTransactions() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fromAccount != null && nf.getDoubleFromDollars(amount) > 2.0
                ) {
                    Text(stringResource(R.string.splitting_transaction))
                }
            }
        }
    }

    @Composable
    fun SelectorCard(label: String, value: String, onClick: () -> Unit) {
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    @Composable
    fun AccountSelectorCard(
        label: String,
        account: Account?,
        isPending: Boolean,
        onPendingChange: (Boolean) -> Unit,
        allowPending: Boolean,
        onClick: () -> Unit
    ) {
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = account?.accountName ?: stringResource(R.string.choose_an_account),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (allowPending) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onPendingChange(!isPending) }
                    ) {
                        Checkbox(checked = isPending, onCheckedChange = onPendingChange)
                        Text(
                            text = stringResource(R.string.pending),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun BalanceField(
        label: String,
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit,
        onLongClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        ProjectTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

    private fun chooseDate() {
        val curDateAll = dateState.value.split("-")
        DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                dateState.value = "$year-${month.toString().padStart(2, '0')}-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        ).apply {
            setTitle(getString(R.string.choose_transaction_date))
            show()
        }
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToAccountChooseFragment()
        )
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToAccountChooseFragment()
        )
    }

    private fun splitTransactions() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToTransactionSplitFragment()
        )
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState.value))
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToCalcFragment()
        )
    }

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            Transactions(
                nf.generateId(),
                dateState.value,
                descriptionState.value.trim(),
                noteState.value.trim(),
                budgetRuleState.value?.ruleId ?: 0L,
                toAccountState.value?.accountId ?: 0L,
                toPendingState.value,
                fromAccountState.value?.accountId ?: 0L,
                fromPendingState.value,
                nf.getDoubleFromDollars(amountState.value),
                false,
                df.getCurrentTimeAsString()
            ),
            budgetRuleState.value,
            toAccountState.value,
            fromAccountState.value
        )
    }

    private fun saveTransactionIfValid() {
        val trans = getTransactionDetailed().transaction!!
        if (trans.transName.isBlank()) {
            showMessage(getString(R.string.please_enter_a_name_or_description))
            return
        }
        if (trans.transToAccountId == 0L) {
            showMessage(getString(R.string.there_needs_to_be_an_account_money_will_go_to))
            return
        }
        if (trans.transFromAccountId == 0L) {
            showMessage(getString(R.string.there_needs_to_be_an_account_money_will_come_from))
            return
        }
        if (trans.transAmount == 0.0) {
            showMessage(getString(R.string.please_enter_an_amount_for_this_transaction))
            return
        }

        var display = getString(R.string.this_will_perform) + trans.transName +
                getString(R.string._for_) + nf.getDollarsFromDouble(trans.transAmount) +
                getString(R.string.__from) + "${fromAccountState.value?.accountName} "
        if (trans.transFromAccountPending) display += getString(R.string._pending)
        display += getString(R.string._to) + " ${toAccountState.value?.accountName}"
        if (trans.transToAccountPending) display += getString(R.string._pending)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_performing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                lifecycleScope.launch {
                    mainActivity.accountUpdateViewModel.performTransaction(trans)
                    mainViewModel.removeCallingFragment(TAG)
                    mainViewModel.setTransactionDetailed(null)
                    mainViewModel.setBudgetRuleDetailed(null)
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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
                saveTransactionIfValid()
                true
            }

            else -> false
        }
    }
}