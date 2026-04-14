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
import androidx.compose.ui.res.stringResource
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
    private var noteState = mutableStateOf("")
    private var amountState = mutableStateOf("")
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
                            onSaveClick = { updateTransactionIfValid() },
                            onChooseBudgetRule = { chooseBudgetRule() },
                            onChooseFromAccount = { chooseFromAccount() },
                            onChooseToAccount = { chooseToAccount() },
                            onSplitClick = { gotoSplitTransaction() },
                            onGotoCalculator = { gotoCalculator() },
                            isSplitEnabled = fromAccountState.value != null && nf.getDoubleFromDollars(
                                amountState.value
                            ) > 2.0,
                            splitButtonText = stringResource(R.string.split)
                        )
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
        noteState.value = trans.transNote
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
            descriptionState.value = trans.transName
            noteState.value = trans.transNote
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