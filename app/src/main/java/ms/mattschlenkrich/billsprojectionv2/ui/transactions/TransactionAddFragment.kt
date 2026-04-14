package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
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
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
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

class TransactionAddFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf("")
    private var descriptionState = mutableStateOf("")
    private var noteState = mutableStateOf("")
    private var amountState = mutableStateOf("")
    private var toAccountState = mutableStateOf<Account?>(null)
    private var fromAccountState = mutableStateOf<Account?>(null)
    private var budgetRuleState = mutableStateOf<BudgetRule?>(null)
    private var toPendingState = mutableStateOf(false)
    private var fromPendingState = mutableStateOf(false)

    private var toAccountWithTypeState = mutableStateOf<AccountWithType?>(null)
    private var fromAccountWithTypeState = mutableStateOf<AccountWithType?>(null)
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        updateViewModels()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.intValue >= 0) {
                        TransactionEditScreen(
                            date = dateState.value,
                            onDateChange = { dateState.value = it },
                            description = descriptionState.value,
                            onDescriptionChange = { descriptionState.value = it },
                            note = noteState.value,
                            onNoteChange = { noteState.value = it },
                            amount = amountState.value,
                            onAmountChange = { amountState.value = it },
                            toAccount = toAccountState.value,
                            fromAccount = fromAccountState.value,
                            budgetRule = budgetRuleState.value,
                            toPending = toPendingState.value,
                            onToPendingChange = { toPendingState.value = it },
                            fromPending = fromPendingState.value,
                            onFromPendingChange = { fromPendingState.value = it },
                            allowToPending = toAccountWithTypeState.value?.accountType?.allowPending == true,
                            allowFromPending = fromAccountWithTypeState.value?.accountType?.allowPending == true,
                            onSaveClick = { saveTransactionIfValid() },
                            onChooseBudgetRule = { chooseBudgetRule() },
                            onChooseFromAccount = { chooseFromAccount() },
                            onChooseToAccount = { chooseToAccount() },
                            onSplitClick = { splitTransactions() },
                            onGotoCalculator = { gotoCalculator() },
                            isSplitEnabled = fromAccountState.value != null && nf.getDoubleFromDollars(
                                amountState.value
                            ) > 2.0
                        )
                    }
                }
            }
        }
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        accountViewModel = mainActivity.accountViewModel
    }

    override fun refreshData() {
        mainActivity.topMenuBar.title = getString(R.string.add_a_new_transaction)
        updateViewModels()
        populateValues()
        refreshKey.intValue++
    }

    fun populateValues() {
        updateViewModels()
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        refreshData()
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