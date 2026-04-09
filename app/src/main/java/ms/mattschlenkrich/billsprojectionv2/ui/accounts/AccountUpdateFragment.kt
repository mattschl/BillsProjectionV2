package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_ACCOUNT_UPDATE

class AccountUpdateFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val vf = VisualsFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    AccountUpdateScreen()
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        transactionViewModel = mainActivity.transactionViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel

        mainActivity.topMenuBar.title = getString(R.string.update_account)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Composable
    fun AccountUpdateScreen() {
        val accountWithTypeState = remember { mutableStateOf(mainViewModel.getAccountWithType()) }
        val accountNames by accountViewModel.getAccountNameList().observeAsState(emptyList())

        val initialAccount = accountWithTypeState.value?.account
        var name by remember { mutableStateOf(initialAccount?.accountName ?: "") }
        var handle by remember { mutableStateOf(initialAccount?.accountNumber ?: "") }
        var balance by remember {
            mutableStateOf(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                            ?.contains(BALANCE) == true
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        initialAccount?.accountBalance ?: 0.0
                    }
                )
            )
        }
        var owing by remember {
            mutableStateOf(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                            ?.contains(OWING) == true
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        initialAccount?.accountOwing ?: 0.0
                    }
                )
            )
        }
        var budgeted by remember {
            mutableStateOf(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0 && mainViewModel.getReturnTo()
                            ?.contains(BUDGETED) == true
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        initialAccount?.accBudgetedAmount ?: 0.0
                    }
                )
            )
        }
        var limit by remember {
            mutableStateOf(nf.displayDollars(initialAccount?.accountCreditLimit ?: 0.0))
        }

        val accountId = initialAccount?.accountId ?: 0L
        val history by transactionViewModel.getActiveTransactionByAccount(accountId)
            .observeAsState(emptyList())

        // Update balances if they change in DB
        val liveAccountWithType by accountViewModel.getAccountWithTypeLive(accountId)
            .observeAsState()

        LaunchedEffect(liveAccountWithType) {
            liveAccountWithType?.let { awt ->
                if (awt.accountType?.keepTotals == true) {
                    balance = nf.displayDollars(awt.account.accountBalance)
                } else if (awt.accountType?.tallyOwing == true) {
                    owing = nf.displayDollars(awt.account.accountOwing)
                }
            }
        }

        LaunchedEffect(Unit) {
            mainViewModel.setTransferNum(0.0)
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        updateAccountIfValid(
                            name,
                            handle,
                            balance,
                            owing,
                            budgeted,
                            limit,
                            accountNames
                        )
                    },
                    containerColor = Color(0xFFB00020) // Deep red
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedCard(
                    onClick = {
                        gotoAccountTypes(
                            name, handle, balance, owing, budgeted, limit
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.account_type) + ":",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = accountWithTypeState.value?.accountType?.accountType
                                ?: stringResource(R.string.account_type),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    BalanceField(
                        label = stringResource(R.string.balance),
                        value = balance,
                        onValueChange = { balance = it },
                        onLongClick = {
                            gotoCalculator(
                                BALANCE,
                                name,
                                handle,
                                balance,
                                owing,
                                budgeted,
                                limit
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BalanceField(
                        label = stringResource(R.string.owing),
                        value = owing,
                        onValueChange = { owing = it },
                        onLongClick = {
                            gotoCalculator(
                                OWING,
                                name,
                                handle,
                                balance,
                                owing,
                                budgeted,
                                limit
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    BalanceField(
                        label = stringResource(R.string.budgeted),
                        value = budgeted,
                        onValueChange = { budgeted = it },
                        onLongClick = {
                            gotoCalculator(
                                BUDGETED,
                                name,
                                handle,
                                balance,
                                owing,
                                budgeted,
                                limit
                            )
                        },
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: " + if (accountId == 0L) stringResource(R.string.id_not_created_yet) else accountId.toString(),
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { transaction ->
                        HistoryItem(transaction)
                    }
                }
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
                fontWeight = FontWeight.Bold
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

    @Composable
    fun HistoryItem(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .clickable {
                    showTransactionOptions(transactionDetailed)
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color(vf.getRandomColorInt()))
                )
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.transName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = nf.displayDollars(transaction.transAmount),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = df.getDisplayDateWithYear(transaction.transDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        val toFromText = (transactionDetailed.fromAccount?.accountName ?: "") +
                                " -> " + (transactionDetailed.toAccount?.accountName ?: "")
                        Text(
                            text = toFromText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    if (transaction.transNote.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.note_) + transaction.transNote,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }

    private fun showTransactionOptions(transactionDetailed: TransactionDetailed) {
        var display = ""
        val transaction = transactionDetailed.transaction ?: return

        if (transaction.transToAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transaction.transAmount
            ) + getString(R.string._to_) + (transactionDetailed.toAccount?.accountName ?: "")
        }
        if (transaction.transToAccountPending) {
            display += getString(R.string._pending)
        }
        if (display != "" && transaction.transFromAccountPending) {
            display += getString(R.string._and)
        }
        if (transaction.transFromAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transaction.transAmount
            ) + getString(R.string._From_) + (transactionDetailed.fromAccount?.accountName ?: "")
        }

        val options = mutableListOf(
            getString(R.string.edit_this_transaction),
            display,
            getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
            getString(R.string.delete_this_transaction)
        )

        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + transaction.transName
        ).setItems(options.toTypedArray()) { _, pos ->
            when (pos) {
                0 -> gotoTransactionUpdate(transactionDetailed)
                1 -> if (transaction.transToAccountPending || transaction.transFromAccountPending) {
                    confirmCompletePendingTransactions(transactionDetailed)
                }

                2 -> gotoBudgetRuleUpdate(transactionDetailed)
                3 -> confirmDeleteTransaction(transactionDetailed)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setTransactionDetailed(transactionDetailed)
        lifecycleScope.launch(Dispatchers.IO) {
            val oldTransactionFull = transactionViewModel.getTransactionFull(
                transaction.transId,
                transaction.transToAccountId,
                transaction.transFromAccountId
            )
            mainViewModel.setOldTransaction(oldTransactionFull)
            withContext(Dispatchers.Main) {
                delay(WAIT_500)
                gotoTransactionUpdateFragment()
            }
        }
    }

    private fun confirmCompletePendingTransactions(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        var display = getString(R.string.this_will_apply_the_amount_of) +
                nf.displayDollars(transaction.transAmount)
        display += if (transaction.transToAccountPending) {
            getString(R.string.to_) + (transactionDetailed.toAccount?.accountName ?: "")
        } else ""
        display += if (transaction.transToAccountPending && transaction.transFromAccountPending) {
            getString(R.string._and)
        } else ""
        display += if (transaction.transFromAccountPending) {
            getString(R.string.from) + (transactionDetailed.fromAccount?.accountName ?: "")
        } else ""

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                completePendingTransactions(transactionDetailed)
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            val newTransaction = Transactions(
                transaction.transId,
                transaction.transDate,
                transaction.transName,
                transaction.transNote,
                transaction.transRuleId,
                transaction.transToAccountId,
                false,
                transaction.transFromAccountId,
                false,
                transaction.transAmount,
                transaction.transIsDeleted,
                df.getCurrentTimeAsString()
            )
            lifecycleScope.launch {
                mainActivity.accountUpdateViewModel.updateTransaction(
                    transaction, newTransaction
                )
                delay(WAIT_500)
            }
        }
    }

    private fun gotoBudgetRuleUpdate(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        mainViewModel.setCallingFragments(TAG)
        budgetRuleViewModel.getBudgetRuleFullLive(
            transaction.transRuleId
        ).observe(viewLifecycleOwner) { bRuleDetailed ->
            mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
            gotoBudgetRuleUpdateFragment()
        }
    }

    private fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        val transaction = transactionDetailed.transaction ?: return
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.are_you_sure_you_want_to_delete) + transaction.transName
        ).setPositiveButton(getString(R.string.delete)) { _, _ ->
            deleteTransaction(transaction)
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun deleteTransaction(transaction: Transactions) {
        lifecycleScope.launch {
            mainActivity.accountUpdateViewModel.deleteTransaction(transaction)
            delay(WAIT_500)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.apply {
            add(Menu.NONE, R.id.action_delete, Menu.NONE, R.string.delete).apply {
                setIcon(android.R.drawable.ic_menu_delete)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                confirmDeleteAccount()
                true
            }

            else -> false
        }
    }

    private fun getUpdatedAccount(
        name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String
    ): Account {
        return Account(
            mainViewModel.getAccountWithType()!!.account.accountId,
            name.trim(),
            handle.trim(),
            mainViewModel.getAccountWithType()!!.accountType?.typeId ?: 0L,
            nf.getDoubleFromDollars(budgeted),
            nf.getDoubleFromDollars(balance),
            nf.getDoubleFromDollars(owing),
            nf.getDoubleFromDollars(limit),
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun validateAccount(name: String, accountNames: List<String>): String {
        if (name.isBlank()) {
            return getString(R.string.please_enter_a_name)
        }
        for (existingName in accountNames) {
            if (existingName == name &&
                existingName != mainViewModel.getAccountWithType()!!.account.accountName
            ) {
                return getString(R.string.this_budget_rule_already_exists)
            }
        }
        if (mainViewModel.getAccountWithType()?.accountType == null) {
            return getString(R.string.please_choose_an_account_type)
        }
        return ANSWER_OK
    }

    private fun updateAccountIfValid(
        name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String,
        accountNames: List<String>
    ) {
        val answer = validateAccount(name, accountNames)
        if (answer == ANSWER_OK) {
            confirmUpdateAccount(name, handle, balance, owing, budgeted, limit)
        } else {
            Toast.makeText(requireContext(), getString(R.string.error) + answer, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun confirmUpdateAccount(
        name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String
    ) {
        val accountWithType = mainViewModel.getAccountWithType()!!
        if (name.trim() == accountWithType.account.accountName.trim()) {
            accountViewModel.updateAccount(
                getUpdatedAccount(
                    name,
                    handle,
                    balance,
                    owing,
                    budgeted,
                    limit
                )
            )
            gotoCallingFragment()
        } else {
            confirmRenameAccount(name, handle, balance, owing, budgeted, limit)
        }
    }

    private fun confirmRenameAccount(
        name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.rename_account))
            setMessage(
                getString(R.string.are_you_sure_you_want_to_rename_this_account) +
                        getString(R.string.note) +
                        getString(R.string.this_will_not_replace_an_existing_account_type)
            )
            setPositiveButton(getString(R.string.update_account)) { _, _ ->
                accountViewModel.updateAccount(
                    getUpdatedAccount(
                        name,
                        handle,
                        balance,
                        owing,
                        budgeted,
                        limit
                    )
                )
                gotoCallingFragment()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_account))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_account))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccount()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteAccount() {
        accountViewModel.deleteAccount(
            mainViewModel.getAccountWithType()!!.account.accountId,
            df.getCurrentTimeAsString()
        )
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        gotoCallingFragment()
    }

    private fun gotoCalculator(
        type: String, name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String
    ) {
        val currentValue = when (type) {
            BALANCE -> balance
            OWING -> owing
            BUDGETED -> budgeted
            else -> "0.00"
        }
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(currentValue.ifBlank { getString(R.string.zero_double) }))
        mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(name, handle, balance, owing, budgeted, limit),
                mainViewModel.getAccountWithType()!!.accountType
            )
        )
        gotoCalculatorFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        findNavController().popBackStack()
    }

    private fun gotoCalculatorFragment() {
        findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoAccountTypes(
        name: String, handle: String, balance: String,
        owing: String, budgeted: String, limit: String
    ) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(name, handle, balance, owing, budgeted, limit),
                mainViewModel.getAccountWithType()!!.accountType
            )
        )
        gotoAccountTypesFragment()
    }

    private fun gotoAccountTypesFragment() {
        findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToAccountTypesFragment()
        )
    }

    private fun gotoTransactionUpdateFragment() {
        findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToTransactionUpdateFragment()
        )
    }

    private fun gotoBudgetRuleUpdateFragment() {
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToBudgetRuleUpdateFragment()
        )
    }
}