package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_ITEM_UPDATE

class BudgetItemUpdateFragment : Fragment(), MenuProvider, RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private val dateState = mutableStateOf("")
    private val nameState = mutableStateOf("")
    private val payDayState = mutableStateOf("")
    private val amountState = mutableStateOf("")
    private val isFixedState = mutableStateOf(false)
    private val isPayDayItemState = mutableStateOf(false)
    private val isAutoState = mutableStateOf(false)
    private val isLockedState = mutableStateOf(true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    val payDays by budgetItemViewModel.getPayDays().observeAsState(emptyList())

                    BudgetItemScreen(
                        date = dateState.value,
                        onDateChange = { dateState.value = it },
                        name = nameState.value,
                        onNameChange = { nameState.value = it },
                        payDay = payDayState.value,
                        onPayDayChange = { payDayState.value = it },
                        amount = amountState.value,
                        onAmountChange = { amountState.value = it },
                        isFixed = isFixedState.value,
                        onIsFixedChange = { isFixedState.value = it },
                        isPayDayItem = isPayDayItemState.value,
                        onIsPayDayItemChange = { isPayDayItemState.value = it },
                        isAuto = isAutoState.value,
                        onIsAutoChange = { isAutoState.value = it },
                        isLocked = isLockedState.value,
                        onIsLockedChange = { isLockedState.value = it },
                        budgetItemDetailed = mainViewModel.getBudgetItemDetailed(),
                        payDays = payDays,
                        onSaveClick = { updateBudgetItemIfValid() },
                        onChooseBudgetRule = { chooseBudgetRule() },
                        onChooseAccount = { chooseAccount(it) },
                        onGotoCalculator = { gotoCalculator() }
                    )
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_this_budget_item)

        initializeValues()
    }

    private fun initializeValues() {
        val cached = mainViewModel.getBudgetItemDetailed()
        if (cached != null) {
            dateState.value = cached.budgetItem?.biActualDate ?: ""
            nameState.value = cached.budgetItem?.biBudgetName ?: ""
            payDayState.value = cached.budgetItem?.biPayDay ?: ""
            amountState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0) {
                    mainViewModel.getTransferNum()!!
                } else {
                    cached.budgetItem?.biProjectedAmount ?: 0.0
                }
            )
            isFixedState.value = cached.budgetItem?.biIsFixed ?: false
            isPayDayItemState.value = cached.budgetItem?.biIsPayDayItem ?: false
            isAutoState.value = cached.budgetItem?.biIsAutomatic ?: false
            isLockedState.value = cached.budgetItem?.biLocked ?: true
            mainViewModel.setTransferNum(0.0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun chooseAccount(requestedAccount: String) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToAccountChooseFragment()
        )
    }

    private fun updateBudgetItemIfValid() {
        val error = validateBudgetItem()
        if (error == ANSWER_OK) {
            budgetItemViewModel.updateBudgetItem(getCurrentBudgetItemForUpdating())
            gotoCallingFragment()
        } else {
            showMessage(getString(R.string.error) + error)
        }
    }

    private fun validateBudgetItem(): String {
        if (nameState.value.isBlank()) return getString(R.string.please_enter_a_name_or_description)
        val cached = mainViewModel.getBudgetItemDetailed()
        if (cached?.toAccount == null) return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        if (cached?.fromAccount == null) return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        if (amountState.value.isEmpty()) return getString(R.string.please_enter_a_budgeted_amount_including_zero)
        return ANSWER_OK
    }

    private fun getCurrentBudgetItemDetailed(): BudgetItemDetailed {
        val cached = mainViewModel.getBudgetItemDetailed()
        return BudgetItemDetailed(
            getCurrentBudgetItemForUpdating(),
            cached?.budgetRule,
            cached?.toAccount,
            cached?.fromAccount
        )
    }

    private fun getCurrentBudgetItemForUpdating(): BudgetItem {
        val cached = mainViewModel.getBudgetItemDetailed()!!
        return BudgetItem(
            cached.budgetRule?.ruleId ?: 0L,
            cached.budgetItem!!.biProjectedDate,
            dateState.value,
            payDayState.value,
            nameState.value,
            isPayDayItemState.value,
            cached.toAccount?.accountId ?: 0L,
            cached.fromAccount?.accountId ?: 0L,
            nf.getDoubleFromDollars(amountState.value),
            biIsPending = false,
            biIsFixed = isFixedState.value,
            biIsAutomatic = isAutoState.value,
            biManuallyEntered = true,
            biIsCompleted = false,
            biIsCancelled = false,
            biIsDeleted = false,
            biUpdateTime = df.getCurrentTimeAsString(),
            biLocked = isLockedState.value,
        )
    }

    private fun confirmDeleteBudgetItem() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.delete_budget_item))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_budget_item))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                val cached = mainViewModel.getBudgetItemDetailed()!!
                budgetItemViewModel.deleteBudgetItem(
                    cached.budgetItem!!.biRuleId,
                    cached.budgetItem!!.biProjectedDate,
                    df.getCurrentTimeAsString()
                )
                gotoCallingFragment()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setBudgetItemDetailed(null)
        mainViewModel.removeCallingFragment(TAG)
        findNavController().popBackStack()
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState.value))
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToCalcFragment()
        )
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG)
            .show()
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
                confirmDeleteBudgetItem()
                true
            }

            else -> false
        }
    }
}