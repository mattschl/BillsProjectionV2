package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANS_PERFORM

class TransactionPerformFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf(df.getCurrentDateAsString())
    private var descriptionState = mutableStateOf("")
    private var descriptionTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var noteState = mutableStateOf("")
    private var noteTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var amountState = mutableStateOf("")
    private var amountTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var budgetedAmountState = mutableStateOf("")
    private var budgetedAmountTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var toAccountState = mutableStateOf<Account?>(null)
    private var fromAccountState = mutableStateOf<Account?>(null)
    private var budgetRuleState = mutableStateOf<BudgetRule?>(null)
    private var toPendingState = mutableStateOf(false)
    private var fromPendingState = mutableStateOf(false)
    private var allowToPendingState = mutableStateOf(false)
    private var allowFromPendingState = mutableStateOf(false)
    private var remainderState = mutableDoubleStateOf(0.0)
    private val refreshKey = mutableStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.value >= 0) {
                        TransactionPerformScreen()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshData()
    }

    override fun refreshData() {
        mainActivity.topMenuBar.title = getString(R.string.perform_a_transaction)
        populateValues()
        refreshKey.value++
    }

    @Composable
    fun TransactionPerformScreen() {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { performTransactionIfValid() },
                    modifier = Modifier
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
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
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date
                ProjectDateField(
                    value = dateState.value,
                    onValueChange = { dateState.value = it },
                    label = stringResource(R.string.date),
                    modifier = Modifier.fillMaxWidth()
                )

                // Budget Rule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.rules),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = budgetRuleState.value?.budgetRuleName
                            ?: stringResource(R.string.choose_a_budget_rule),
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Amount and Split
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.amount), modifier = Modifier.weight(1f))
                    ProjectTextField(
                        value = amountTextFieldValue.value,
                        onValueChange = {
                            amountTextFieldValue.value = it
                            amountState.value = it.text
                            calculateRemainder()
                        },
                        modifier = Modifier.weight(1.5f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Button(
                        onClick = { splitTransaction() },
                        enabled = nf.getDoubleFromDollars(amountState.value) > 0 && fromAccountState.value != null,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = stringResource(R.string.split))
                    }
                }

                // Budgeted Card
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.budgeted),
                                style = MaterialTheme.typography.labelMedium
                            )
                            ProjectTextField(
                                value = budgetedAmountTextFieldValue.value,
                                onValueChange = {
                                    budgetedAmountTextFieldValue.value = it
                                    budgetedAmountState.value = it.text
                                    calculateRemainder()
                                    updateBudgetItemInCache()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = stringResource(R.string.remainder),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = nf.displayDollars(remainderState.doubleValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // To Account
                AccountSelectionRow(
                    label = stringResource(R.string.to_this_account),
                    accountName = toAccountState.value?.accountName
                        ?: stringResource(R.string.choose_an_account),
                    isPending = toPendingState.value,
                    onPendingChange = { toPendingState.value = it },
                    allowPending = allowToPendingState.value,
                    onAccountClick = { chooseToAccount() }
                )

                // From Account
                AccountSelectionRow(
                    label = stringResource(R.string.from_this_account),
                    accountName = fromAccountState.value?.accountName
                        ?: stringResource(R.string.choose_an_account),
                    isPending = fromPendingState.value,
                    onPendingChange = { fromPendingState.value = it },
                    allowPending = allowFromPendingState.value,
                    onAccountClick = { chooseFromAccount() }
                )

                // Description
                ProjectTextField(
                    value = descriptionTextFieldValue.value,
                    onValueChange = {
                        descriptionTextFieldValue.value = it
                        descriptionState.value = it.text
                    },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                ProjectTextField(
                    value = noteTextFieldValue.value,
                    onValueChange = {
                        noteTextFieldValue.value = it
                        noteState.value = it.text
                    },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    fun AccountSelectionRow(
        label: String,
        accountName: String,
        isPending: Boolean,
        onPendingChange: (Boolean) -> Unit,
        allowPending: Boolean,
        onAccountClick: () -> Unit
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = accountName,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAccountClick() }
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (allowPending) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPending, onCheckedChange = onPendingChange)
                        Text(
                            text = stringResource(R.string.pending),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }

    fun populateValues() {
        if (mainViewModel.getTransactionDetailed() != null) {
            populateValuesFromTransactionDetailedInCache()
        } else if (mainViewModel.getBudgetItemDetailed() != null) {
            populateValuesFromBudgetItemDetailedInCache()
        }
    }

    private fun populateValuesFromTransactionDetailedInCache() {
        val detailed = mainViewModel.getTransactionDetailed()!!
        val trans = detailed.transaction ?: return

        mBudgetRule = detailed.budgetRule
        budgetRuleState.value = detailed.budgetRule
        toAccountState.value = detailed.toAccount
        fromAccountState.value = detailed.fromAccount

        descriptionState.value = trans.transName
        descriptionTextFieldValue.value = TextFieldValue(trans.transName)
        noteState.value = trans.transNote
        noteTextFieldValue.value = TextFieldValue(trans.transNote)
        dateState.value = trans.transDate

        val amount = if (mainViewModel.getTransferNum() != 0.0) {
            mainViewModel.getTransferNum()!!
        } else {
            trans.transAmount
        }
        amountState.value = nf.displayDollars(amount)
        amountTextFieldValue.value = TextFieldValue(amountState.value)
        mainViewModel.setTransferNum(0.0)

        val budgetedAmount =
            mainViewModel.getBudgetItemDetailed()?.budgetItem?.biProjectedAmount ?: 0.0
        budgetedAmountState.value = nf.displayDollars(budgetedAmount)
        budgetedAmountTextFieldValue.value = TextFieldValue(budgetedAmountState.value)

        toPendingState.value = trans.transToAccountPending
        fromPendingState.value = trans.transFromAccountPending

        detailed.toAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowToPendingState.value = it.accountType?.allowPending == true
            }
        }

        detailed.fromAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowFromPendingState.value = it.accountType?.allowPending == true
            }
        }

        calculateRemainder()
    }

    private fun populateValuesFromBudgetItemDetailedInCache() {
        val detailed = mainViewModel.getBudgetItemDetailed()!!
        val budgetItem = detailed.budgetItem!!

        toAccountState.value = detailed.toAccount
        fromAccountState.value = detailed.fromAccount
        budgetRuleState.value = detailed.budgetRule

        budgetRuleState.value?.let { mBudgetRule = it }

        descriptionState.value = budgetItem.biBudgetName
        descriptionTextFieldValue.value = TextFieldValue(budgetItem.biBudgetName)
        dateState.value = df.getCurrentDateAsString()
        budgetedAmountState.value = nf.displayDollars(budgetItem.biProjectedAmount)
        budgetedAmountTextFieldValue.value = TextFieldValue(budgetedAmountState.value)
        amountState.value = nf.displayDollars(0.0)
        amountTextFieldValue.value = TextFieldValue(amountState.value)

        detailed.toAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowToPendingState.value = it.accountType?.allowPending == true
                if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                    toPendingState.value = true
                }
            }
        }

        detailed.fromAccount?.let { acc ->
            accountViewModel.getAccountDetailed(acc.accountId).observe(viewLifecycleOwner) {
                allowFromPendingState.value = it.accountType?.allowPending == true
                if (it.accountType?.allowPending == true && it.accountType.tallyOwing) {
                    fromPendingState.value = true
                }
            }
        }

        calculateRemainder()
    }

    private fun updateBudgetItemInCache() {
        val amount = nf.getDoubleFromDollars(budgetedAmountState.value)
        mainViewModel.getBudgetItemDetailed()?.let { detailed ->
            if (amount != detailed.budgetItem?.biProjectedAmount) {
                detailed.budgetItem?.biProjectedAmount = amount
                mainViewModel.setBudgetItemDetailed(detailed)
            }
        }
    }

    private fun splitTransaction() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.setTransferNum(0.0)
        val amount = nf.getDoubleFromDollars(amountState.value)
        if (fromAccountState.value != null && amount > 2.0) {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            gotoTransactionSplitFragment()
        }
    }

    private fun calculateRemainder() {
        val amt = nf.getDoubleFromDollars(amountState.value)
        val budgeted = nf.getDoubleFromDollars(budgetedAmountState.value)
        remainderState.doubleValue = budgeted - amt
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            budgetRuleState.value,
            toAccountState.value,
            fromAccountState.value
        )
    }

    private fun performTransactionIfValid() {
        calculateRemainder()
        val message = validateTransaction()
        if (message == ANSWER_OK) {
            confirmPerformTransaction()
        } else {
            Toast.makeText(requireContext(), getString(R.string.error) + message, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun confirmPerformTransaction() {
        val amount = nf.getDoubleFromDollars(amountState.value)
        var display = getString(R.string.this_will_perform) + " " + descriptionState.value +
                getString(R.string._for_) + " " + nf.displayDollars(amount) +
                getString(R.string.__from) + " " + (fromAccountState.value?.accountName ?: "")
        if (fromPendingState.value) display += " " + getString(R.string.pending)
        display += " " + getString(R.string._to) + " " + (toAccountState.value?.accountName ?: "")
        if (toPendingState.value) display += " " + getString(R.string.pending)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_performing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                performTransaction()
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    private fun validateTransaction(): String {
        val amount = nf.getDoubleFromDollars(amountState.value)
        if (descriptionState.value.isBlank()) {
            return getString(R.string.please_enter_a_name_or_description)
        }
        if (amount == 0.0) {
            return getString(R.string.please_enter_an_amount_for_this_transaction)
        }
        if (fromAccountState.value == null || toAccountState.value == null) {
            return " " + getString(R.string.choose_an_account)
        }
        return ANSWER_OK
    }

    private fun performTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        lifecycleScope.launch {
            accountUpdateViewModel.performTransaction(mTransaction)
            updateBudgetItem()
            gotoCallingFragment()
        }
    }

    private fun updateBudgetItem() {
        val remainder = remainderState.doubleValue
        val completed = remainder < 2.0
        val detailed = mainViewModel.getBudgetItemDetailed() ?: return
        val mBudget = detailed.budgetItem ?: return

        budgetItemViewModel.updateBudgetItem(
            mBudget.copy(
                biProjectedAmount = remainder,
                biIsCompleted = completed,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        )
    }

    private fun getCurrentTransactionForSave(): Transactions {
        return Transactions(
            transId = nf.generateId(),
            transDate = dateState.value,
            transName = descriptionState.value,
            transNote = noteState.value,
            transRuleId = budgetRuleState.value?.ruleId ?: 0L,
            transToAccountId = toAccountState.value?.accountId ?: 0L,
            transToAccountPending = toPendingState.value,
            transFromAccountId = fromAccountState.value?.accountId ?: 0L,
            transFromAccountPending = fromPendingState.value,
            transAmount = nf.getDoubleFromDollars(amountState.value),
            transIsDeleted = false,
            transUpdateTime = df.getCurrentTimeAsString()
        )
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().popBackStack()
    }

    private fun gotoAccountChooseFragment() {
        findNavController().navigate(
            TransactionPerformFragmentDirections.actionTransactionPerformFragmentToAccountChooseFragment()
        )
    }

    private fun gotoTransactionSplitFragment() {
        findNavController().navigate(
            TransactionPerformFragmentDirections.actionTransactionPerformFragmentToTransactionSplitFragment()
        )
    }

    private var mBudgetRule: BudgetRule? = null // For compatibility if needed
}