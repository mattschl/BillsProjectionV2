package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectDateField
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_RULE_ADD

class BudgetRuleAddFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private var nameState = mutableStateOf("")
    private var amountState = mutableStateOf("")
    private var isFixedState = mutableStateOf(false)
    private var isPayDayState = mutableStateOf(false)
    private var isAutoState = mutableStateOf(false)
    private var startDateState = mutableStateOf("")
    private var endDateState = mutableStateOf("")
    private var frequencyTypeState = mutableStateOf(0)
    private var frequencyCountState = mutableStateOf("1")
    private var dayOfWeekState = mutableStateOf(0)
    private var leadDaysState = mutableStateOf("0")

    private var budgetNameList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    BudgetRuleAddScreen()
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_budget_rule)

        initializeValues()

        lifecycleScope.launch(Dispatchers.IO) {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    private fun initializeValues() {
        val cached = mainViewModel.getBudgetRuleDetailed()
        if (cached != null) {
            val rule = cached.budgetRule
            if (rule != null) {
                nameState.value = rule.budgetRuleName
                amountState.value = nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        rule.budgetAmount
                    }
                )
                isFixedState.value = rule.budFixedAmount
                isPayDayState.value = rule.budIsPayDay
                isAutoState.value = rule.budIsAutoPay
                startDateState.value = rule.budStartDate
                endDateState.value = rule.budEndDate ?: df.getCurrentDateAsString()
                frequencyTypeState.value = rule.budFrequencyTypeId
                frequencyCountState.value = rule.budFrequencyCount.toString()
                dayOfWeekState.value = rule.budDayOfWeekId
                leadDaysState.value = rule.budLeadDays.toString()
                mainViewModel.setTransferNum(0.0)
            }
        } else {
            startDateState.value = df.getCurrentDateAsString()
            endDateState.value = df.getCurrentDateAsString()
            amountState.value = nf.displayDollars(0.0)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BudgetRuleAddScreen() {
        var name by nameState
        var amount by amountState
        var isFixed by isFixedState
        var isPayDay by isPayDayState
        var isAuto by isAutoState
        var startDate by startDateState
        var endDate by endDateState
        var frequencyType by frequencyTypeState
        var frequencyCount by frequencyCountState
        var dayOfWeek by dayOfWeekState
        var leadDays by leadDaysState

        val cached = mainViewModel.getBudgetRuleDetailed()
        val toAccount = cached?.toAccount
        val fromAccount = cached?.fromAccount

        val frequencyTypes = stringArrayResource(R.array.frequency_types)
        val daysOfWeek = stringArrayResource(R.array.days_of_week)

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { saveBudgetRuleIfValid() },
                    containerColor = Color(0xFFB00020)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.budget_rule_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClickableSelectionCard(
                        label = stringResource(R.string.to_this_account),
                        value = toAccount?.accountName
                            ?: stringResource(R.string.choose_an_account),
                        onClick = { chooseAccount(REQUEST_TO_ACCOUNT) },
                        modifier = Modifier.weight(1f)
                    )

                    ClickableSelectionCard(
                        label = stringResource(R.string.from_this_account),
                        value = fromAccount?.accountName
                            ?: stringResource(R.string.choose_an_account),
                        onClick = { chooseAccount(REQUEST_FROM_ACCOUNT) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.amount)) },
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { gotoCalculator() }
                            ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Checkbox(
                            checked = isFixed,
                            onCheckedChange = { isFixed = it },
                            modifier = Modifier.padding(0.dp)
                        )
                        Text(
                            stringResource(R.string.fixed),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    LabeledCheckbox(
                        label = stringResource(R.string.make_a_pay_day),
                        checked = isPayDay,
                        onCheckedChange = { isPayDay = it },
                        modifier = Modifier.weight(1f)
                    )
                    LabeledCheckbox(
                        label = stringResource(R.string.automatic_payment),
                        checked = isAuto,
                        onCheckedChange = { isAuto = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectDateField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = stringResource(R.string.start_date),
                        modifier = Modifier.weight(1f)
                    )
                    ProjectDateField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = stringResource(R.string.end_date),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = stringResource(R.string.scheduling_rules),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdown(
                        label = stringResource(R.string.budget_rules),
                        options = frequencyTypes.toList(),
                        selectedIndex = frequencyType,
                        onItemSelected = { frequencyType = it },
                        modifier = Modifier.weight(1.5f)
                    )

                    ProjectTextField(
                        value = frequencyCount,
                        onValueChange = { frequencyCount = it },
                        label = { Text(stringResource(R.string.times)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdown(
                        label = stringResource(R.string.on_day),
                        options = daysOfWeek.toList(),
                        selectedIndex = dayOfWeek,
                        onItemSelected = { dayOfWeek = it },
                        modifier = Modifier.weight(1.5f)
                    )

                    ProjectTextField(
                        value = leadDays,
                        onValueChange = { leadDays = it },
                        label = { Text(stringResource(R.string.lead_days)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ExposedDropdown(
        label: String,
        options: List<String>,
        selectedIndex: Int,
        onItemSelected: (Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
        ) {
            ProjectTextField(
                value = options.getOrElse(selectedIndex) { "" },
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onItemSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun ClickableSelectionCard(
        label: String,
        value: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    fun LabeledCheckbox(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }

    private fun chooseAccount(requestedAccount: String) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        findNavController().navigate(
            BudgetRuleAddFragmentDirections.actionBudgetRuleAddFragmentToAccountChooseFragment()
        )
    }

    private fun saveBudgetRuleIfValid() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            budgetRuleViewModel.insertBudgetRule(getCurrentBudgetRuleForSave())
            mainViewModel.setBudgetRuleDetailed(null)
            mainViewModel.removeCallingFragment(TAG)
            findNavController().popBackStack()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun validateBudgetRule(): String {
        val name = nameState.value.trim()
        if (name.isBlank()) return getString(R.string.please_enter_a_name)
        if (budgetNameList.contains(name)) return getString(R.string.this_budget_rule_already_exists)

        val cached = mainViewModel.getBudgetRuleDetailed()
        if (cached?.toAccount == null) return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        if (cached.fromAccount == null) return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        if (amountState.value.isEmpty()) return getString(R.string.please_enter_a_budgeted_amount_including_zero)
        return ANSWER_OK
    }

    private fun getCurrentBudgetRuleForSave(): BudgetRule {
        val cached = mainViewModel.getBudgetRuleDetailed()
        return BudgetRule(
            nf.generateId(),
            nameState.value.trim(),
            cached?.toAccount?.accountId ?: 0L,
            cached?.fromAccount?.accountId ?: 0L,
            nf.getDoubleFromDollars(amountState.value),
            isFixedState.value,
            isPayDayState.value,
            isAutoState.value,
            startDateState.value,
            endDateState.value,
            dayOfWeekState.value,
            frequencyTypeState.value,
            frequencyCountState.value.toIntOrNull() ?: 1,
            leadDaysState.value.toIntOrNull() ?: 0,
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun getBudgetRuleDetailed(): BudgetRuleDetailed {
        val cached = mainViewModel.getBudgetRuleDetailed()
        return BudgetRuleDetailed(
            getCurrentBudgetRuleForSave(),
            cached?.toAccount,
            cached?.fromAccount
        )
    }


    private fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState.value))
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        findNavController().navigate(
            BudgetRuleAddFragmentDirections.actionBudgetRuleAddFragmentToCalcFragment()
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}