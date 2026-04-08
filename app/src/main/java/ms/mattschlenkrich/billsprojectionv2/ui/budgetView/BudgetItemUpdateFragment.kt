package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_ITEM_UPDATE

class BudgetItemUpdateFragment : Fragment(), MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var dateState = mutableStateOf("")
    private var nameState = mutableStateOf("")
    private var nameTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var payDayState = mutableStateOf("")
    private var amountState = mutableStateOf("")
    private var amountTextFieldValue = mutableStateOf(TextFieldValue(""))
    private var isFixedState = mutableStateOf(false)
    private var isPayDayItemState = mutableStateOf(false)
    private var isAutoState = mutableStateOf(false)
    private var isLockedState = mutableStateOf(true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_this_budget_item)

        initializeValues()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    BudgetItemUpdateScreen()
                }
            }
        }
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

            // Update TextFieldValues
            nameTextFieldValue.value = TextFieldValue(nameState.value)
            amountTextFieldValue.value = TextFieldValue(amountState.value)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BudgetItemUpdateScreen() {
        var date by dateState
        var name by nameState
        var payDay by payDayState
        var amount by amountState
        var isFixed by isFixedState
        var isPayDayItem by isPayDayItemState
        var isAuto by isAutoState
        var isLocked by isLockedState

        val cached = mainViewModel.getBudgetItemDetailed()
        val toAccount = cached?.toAccount
        val fromAccount = cached?.fromAccount
        val budgetRule = cached?.budgetRule

        val payDays by budgetItemViewModel.getPayDays().observeAsState(emptyList())

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { updateBudgetItemIfValid() },
                    containerColor = Color(0xFFB00020)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Done", tint = Color.White)
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    ProjectTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text(stringResource(R.string.projected_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = true,
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { chooseDate() }
                    )
                }

                ProjectTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProjectTextField(
                        value = payDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.pay_day)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        payDays.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    payDay = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                ClickableSelectionCard(
                    label = stringResource(R.string.budget_rule),
                    value = budgetRule?.budgetRuleName
                        ?: stringResource(R.string.choose_a_budget_rule),
                    onClick = { chooseBudgetRule() }
                )

                ClickableSelectionCard(
                    label = stringResource(R.string.to_this_account),
                    value = toAccount?.accountName ?: stringResource(R.string.choose_an_account),
                    onClick = { chooseAccount(REQUEST_TO_ACCOUNT) }
                )

                ClickableSelectionCard(
                    label = stringResource(R.string.from_this_account),
                    value = fromAccount?.accountName ?: stringResource(R.string.choose_an_account),
                    onClick = { chooseAccount(REQUEST_FROM_ACCOUNT) }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProjectTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                        },
                        label = { Text(stringResource(R.string.projected_amount)) },
                        modifier = Modifier
                            .weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { gotoCalculator() }) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = stringResource(R.string.calculator)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Checkbox(checked = isFixed, onCheckedChange = { isFixed = it })
                        Text(
                            stringResource(R.string.fixed),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LabeledCheckbox(
                        label = stringResource(R.string.pay_day),
                        checked = isPayDayItem,
                        onCheckedChange = { isPayDayItem = it }
                    )
                    LabeledCheckbox(
                        label = stringResource(R.string.automatic),
                        checked = isAuto,
                        onCheckedChange = { isAuto = it }
                    )
                    LabeledCheckbox(
                        label = stringResource(R.string.lock),
                        checked = isLocked,
                        onCheckedChange = { isLocked = it }
                    )
                }
            }
        }
    }

    @Composable
    fun ClickableSelectionCard(label: String, value: String, onClick: () -> Unit) {
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    fun LabeledCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
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

    private fun chooseDate() {
        val curDate =
            if (dateState.value.contains("-")) dateState.value else df.getCurrentDateAsString()
        val curDateAll = curDate.split("-")
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                dateState.value = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_the_projected_date))
        datePickerDialog.show()
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