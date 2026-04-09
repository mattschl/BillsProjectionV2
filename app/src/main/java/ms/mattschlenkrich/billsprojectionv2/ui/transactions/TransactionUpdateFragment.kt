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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableIntStateOf
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
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANS_UPDATE

class TransactionUpdateFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel

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

    private var mTransactionId: Long = 0

    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_this_transaction)

        populateValues()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.intValue >= 0) {
                        TransactionUpdateScreen()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.update_transaction)
        populateValues()
        refreshKey.intValue++
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
    }

    fun populateValues() {
        if (mainViewModel.getOldTransaction() != null && mainViewModel.getTransactionDetailed() == null) {
            populateValuesFromOldTransaction()
        } else if (mainViewModel.getTransactionDetailed() != null) {
            populateValuesFromCache()
        }

        if (mainViewModel.getUpdatingTransaction()) {
            mainViewModel.setUpdatingTransaction(false)
            updateTransactionIfValid()
        }
    }

    private fun populateValuesFromOldTransaction() {
        val transFull = mainViewModel.getOldTransaction()!!
        val trans = transFull.transaction
        mTransactionId = trans.transId
        dateState.value = trans.transDate
        amountState.value = nf.displayDollars(trans.transAmount)
        descriptionState.value = trans.transName
        descriptionTextFieldValue.value = TextFieldValue(trans.transName)
        noteState.value = trans.transNote
        noteTextFieldValue.value = TextFieldValue(trans.transNote)
        budgetRuleState.value = transFull.budgetRule
        toAccountState.value = transFull.toAccountAndType.account
        toPendingState.value = trans.transToAccountPending
        fromAccountState.value = transFull.fromAccountAndType.account
        fromPendingState.value = trans.transFromAccountPending

        lifecycleScope.launch {
            toAccountWithTypeState.value =
                accountViewModel.getAccountWithType(trans.transToAccountId)
            fromAccountWithTypeState.value =
                accountViewModel.getAccountWithType(trans.transFromAccountId)
        }
    }

    private fun populateValuesFromCache() {
        val cached = mainViewModel.getTransactionDetailed()!!
        val trans = cached.transaction
        if (trans != null) {
            mTransactionId = trans.transId
            dateState.value = trans.transDate
            amountState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0) mainViewModel.getTransferNum()!!
                else trans.transAmount
            )
            amountTextFieldValue.value = TextFieldValue(amountState.value)
            descriptionState.value = trans.transName
            descriptionTextFieldValue.value = TextFieldValue(trans.transName)
            noteState.value = trans.transNote
            noteTextFieldValue.value = TextFieldValue(trans.transNote)
            toPendingState.value = trans.transToAccountPending
            fromPendingState.value = trans.transFromAccountPending
        }
        budgetRuleState.value = cached.budgetRule
        toAccountState.value = cached.toAccount
        fromAccountState.value = cached.fromAccount

        lifecycleScope.launch {
            cached.toAccount?.let {
                toAccountWithTypeState.value = accountViewModel.getAccountWithType(it.accountId)
            }
            cached.fromAccount?.let {
                fromAccountWithTypeState.value = accountViewModel.getAccountWithType(it.accountId)
            }
        }
        mainViewModel.setTransferNum(0.0)
    }

    @Composable
    fun TransactionUpdateScreen() {
        var date by dateState
        var description by descriptionState
        var note by noteState
        var amount by amountState
        val toAccount by toAccountState
        val fromAccount by fromAccountState
        val budgetRule by budgetRuleState
        var toPending by toPendingState
        var fromPending by fromPendingState

        val toAccountWithType = toAccountWithTypeState.value
        val fromAccountWithType = fromAccountWithTypeState.value

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { updateTransactionIfValid() },
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
                    Box(modifier = Modifier.weight(1f)) {
                        ProjectTextField(
                            value = date,
                            onValueChange = { },
                            label = { Text(stringResource(R.string.date)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { chooseDate() }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    var amountValue by amountTextFieldValue
                    BalanceField(
                        label = stringResource(R.string.amount),
                        value = amountValue,
                        onValueChange = {
                            amountValue = it
                            amount = it.text
                        },
                        onLongClick = { gotoCalculator() },
                        modifier = Modifier.weight(1f)
                    )
                }

                SelectorCard(
                    label = stringResource(R.string.rules),
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
                    onClick = { gotoSplitTransaction() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fromAccount != null && nf.getDoubleFromDollars(amount) > 2.0
                ) {
                    Text(stringResource(R.string.split))
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
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        findNavController().navigate(
            TransactionUpdateFragmentDirections.actionTransactionUpdateFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        findNavController().navigate(
            TransactionUpdateFragmentDirections.actionTransactionUpdateFragmentToAccountChooseFragment()
        )
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        findNavController().navigate(
            TransactionUpdateFragmentDirections.actionTransactionUpdateFragmentToAccountChooseFragment()
        )
    }

    private fun gotoSplitTransaction() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.setTransferNum(0.0)
        mainViewModel.setUpdatingTransaction(true)
        if (fromAccountState.value != null && nf.getDoubleFromDollars(amountState.value) > 2.0) {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            findNavController().navigate(
                TransactionUpdateFragmentDirections.actionTransactionUpdateFragmentToTransactionSplitFragment()
            )
        }
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState.value))
        mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        findNavController().navigate(
            TransactionUpdateFragmentDirections.actionTransactionUpdateFragmentToCalcFragment()
        )
    }

    private fun updateTransactionIfValid() {
        val message = validateTransactionForUpdate()
        if (message == ANSWER_OK) {
            confirmUpdateTransaction()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun validateTransactionForUpdate(): String {
        if (descriptionState.value.isBlank()) {
            return getString(R.string.please_enter_a_name_or_description)
        }
        if (toAccountState.value == null) {
            return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        }
        if (fromAccountState.value == null) {
            return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        }
        if (amountState.value.isBlank()) {
            return getString(R.string.please_enter_an_amount_for_this_transaction)
        }
        if (budgetRuleState.value == null) {
            updateWithoutBudget()
            return "WAIT_FOR_CONFIRM"
        }
        return ANSWER_OK
    }

    private fun confirmUpdateTransaction() {
        val trans = getCurrentTransactionForSave()
        var display = getString(R.string.this_will_perform) + trans.transName +
                getString(R.string._for_) + nf.getDollarsFromDouble(trans.transAmount) +
                getString(R.string.__from) + fromAccountState.value!!.accountName
        if (fromPendingState.value) display += getString(R.string.pending)
        display += getString(R.string._to) + toAccountState.value!!.accountName
        if (toPendingState.value) display += getString(R.string.pending)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_performing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                updateTransaction()
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    private fun updateWithoutBudget() {
        AlertDialog.Builder(requireContext()).apply {
            setMessage(
                getString(R.string.there_is_no_budget_rule) + getString(R.string.budget_rules_are_used_to_update_the_budget)
            )
            setPositiveButton(getString(R.string.save_anyway)) { _, _ ->
                confirmUpdateTransaction()
            }
            setNegativeButton(getString(R.string.retry), null)
        }.create().show()
    }

    private fun updateTransaction() {
        val oldTrans = mainViewModel.getOldTransaction()?.transaction
        if (oldTrans != null) {
            lifecycleScope.launch {
                accountUpdateViewModel.updateTransaction(
                    oldTrans, getCurrentTransactionForSave()
                )
                mainViewModel.removeCallingFragment(TAG)
                gotoCallingFragment()
            }
        }
    }

    private fun deleteTransaction() {
        val trans = mainViewModel.getOldTransaction()?.transaction
        if (trans != null) {
            lifecycleScope.launch {
                accountUpdateViewModel.deleteTransaction(trans)
                mainViewModel.setTransactionDetailed(null)
                mainViewModel.removeCallingFragment(TAG)
                gotoCallingFragment()
            }
        }
    }

    private fun confirmDeleteTransaction() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_this_transaction))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_transaction))
            setPositiveButton(getString(R.string.delete_this_transaction)) { _, _ ->
                deleteTransaction()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun getCurrentTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRuleState.value,
            toAccountState.value,
            fromAccountState.value
        )
    }

    private fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            mTransactionId,
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
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setOldTransaction(null)
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.removeCallingFragment(TAG)
        findNavController().popBackStack()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(Menu.NONE, R.id.action_delete, Menu.NONE, R.string.delete).apply {
            setIcon(android.R.drawable.ic_menu_delete)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                confirmDeleteTransaction()
                true
            }

            else -> false
        }
    }
}