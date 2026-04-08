package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_RULE_UPDATE

class BudgetRuleUpdateFragment : Fragment(), MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    private val nameState = mutableStateOf("")
    private val amountState = mutableStateOf("")
    private val isFixedState = mutableStateOf(false)
    private val isPayDayState = mutableStateOf(false)
    private val isAutoState = mutableStateOf(false)
    private val startDateState = mutableStateOf("")
    private val endDateState = mutableStateOf("")
    private val frequencyTypeState = mutableStateOf(0)
    private val frequencyCountState = mutableStateOf("1")
    private val dayOfWeekState = mutableStateOf(0)
    private val leadDaysState = mutableStateOf("0")

    private var budgetNameList: List<String>? = null
    private var ruleId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_budget_rule)

        loadBudgetRule()
        getBudgetRuleNameForValidation()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    BudgetRuleUpdateScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadBudgetRule() {
        lifecycleScope.launch {
            if (mainViewModel.getCallingFragments() != null) {
                if (mainViewModel.getBudgetRuleDetailed() != null) {
                    val mCallingFragment = mainViewModel.getCallingFragments()!!
                    val detailed = if (mCallingFragment.contains(FRAG_TRANSACTION_VIEW) &&
                        !mCallingFragment.contains(ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_CHOOSE)
                    ) {
                        withContext(Dispatchers.IO) {
                            budgetRuleViewModel.getBudgetRuleDetailed(
                                mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.ruleId
                            )
                        }
                    } else {
                        mainViewModel.getBudgetRuleDetailed()
                    }
                    delay(WAIT_250)
                    detailed?.let { populateFromBudgetRuleDetailed(it) }
                }
            } else {
                startDateState.value = df.getCurrentDateAsString()
                endDateState.value = df.getCurrentDateAsString()
            }
        }
    }

    private fun populateFromBudgetRuleDetailed(detailed: BudgetRuleDetailed) {
        detailed.budgetRule?.let { rule ->
            ruleId = rule.ruleId
            nameState.value = rule.budgetRuleName
            amountState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0) {
                    mainViewModel.getTransferNum()!!
                } else {
                    rule.budgetAmount
                }
            )
            mainViewModel.setTransferNum(0.0)
            isFixedState.value = rule.budFixedAmount
            isPayDayState.value = rule.budIsPayDay
            isAutoState.value = rule.budIsAutoPay
            startDateState.value = rule.budStartDate
            endDateState.value = rule.budEndDate ?: ""
            frequencyTypeState.value = rule.budFrequencyTypeId
            frequencyCountState.value = rule.budFrequencyCount.toString()
            dayOfWeekState.value = rule.budDayOfWeekId
            leadDaysState.value = rule.budLeadDays.toString()
        }
    }

    private fun getBudgetRuleNameForValidation() {
        lifecycleScope.launch(Dispatchers.IO) {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BudgetRuleUpdateScreen() {
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

        val detailed = mainViewModel.getBudgetRuleDetailed()
        val toAccount = detailed?.toAccount
        val fromAccount = detailed?.fromAccount

        val budgetItems by budgetItemViewModel.getBudgetItems(ruleId).observeAsState(emptyList())

        val frequencyTypes = stringArrayResource(R.array.frequency_types)
        val daysOfWeek = stringArrayResource(R.array.days_of_week)

        Scaffold(
            floatingActionButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { chooseAddOptionsOrUpdateBudgetRuleToContinue() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Options")
                    }

                    FloatingActionButton(
                        onClick = { updateBudgetRuleIfValid() },
                        containerColor = Color(0xFFB00020)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Update", tint = Color.White)
                    }
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
                ProjectTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.budget_rule_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                ClickableSelectionCard(
                    label = stringResource(R.string.to_this_account),
                    value = toAccount?.accountName ?: stringResource(R.string.choose_an_account),
                    onClick = { chooseToAccount() }
                )

                ClickableSelectionCard(
                    label = stringResource(R.string.from_this_account),
                    value = fromAccount?.accountName ?: stringResource(R.string.choose_an_account),
                    onClick = { chooseFromAccount() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProjectTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.amount)) },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { gotoCalculator() }) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = stringResource(R.string.calculator)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Checkbox(checked = isFixed, onCheckedChange = { isFixed = it })
                        Text(
                            stringResource(R.string.fixed),
                            style = MaterialTheme.typography.labelMedium
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
                    ProjectTextField(
                        value = startDate,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.start_date)) },
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { chooseStartDate() }
                            ),
                        readOnly = true,
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    ProjectTextField(
                        value = endDate,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.end_date)) },
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { chooseEndDate() }
                            ),
                        readOnly = true,
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.projected_date),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                budgetItems.forEach { item ->
                    ProjectedDateItem(item)
                }
            }
        }
    }

    @Composable
    fun ProjectedDateItem(item: BudgetItemDetailed) {
        val actualDate = df.getDisplayDate(item.budgetItem!!.biActualDate)
        val payDay = df.getDisplayDate(item.budgetItem!!.biPayDay)
        val amount = nf.displayDollars(item.budgetItem!!.biProjectedAmount)

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { confirmGotoBudgetItem(item) }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = actualDate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " (pay day $payDay)",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = " for $amount",
                    style = MaterialTheme.typography.bodyMedium
                )
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

    private fun chooseAddOptionsOrUpdateBudgetRuleToContinue() {
        val curBudgetRule = getCurrentBudgetRuleForSaving()
        val detailed = mainViewModel.getBudgetRuleDetailed()
        val cachedBudgetRule = detailed?.budgetRule

        if (cachedBudgetRule != null &&
            curBudgetRule.budgetRuleName == cachedBudgetRule.budgetRuleName &&
            curBudgetRule.budToAccountId == cachedBudgetRule.budToAccountId &&
            curBudgetRule.budFromAccountId == cachedBudgetRule.budFromAccountId &&
            curBudgetRule.budgetAmount == cachedBudgetRule.budgetAmount &&
            curBudgetRule.budFixedAmount == cachedBudgetRule.budFixedAmount &&
            curBudgetRule.budIsPayDay == cachedBudgetRule.budIsPayDay &&
            curBudgetRule.budIsAutoPay == cachedBudgetRule.budIsAutoPay &&
            curBudgetRule.budStartDate == cachedBudgetRule.budStartDate &&
            curBudgetRule.budEndDate == cachedBudgetRule.budEndDate &&
            curBudgetRule.budDayOfWeekId == cachedBudgetRule.budDayOfWeekId &&
            curBudgetRule.budFrequencyTypeId == cachedBudgetRule.budFrequencyTypeId &&
            curBudgetRule.budFrequencyCount == cachedBudgetRule.budFrequencyCount &&
            curBudgetRule.budLeadDays == cachedBudgetRule.budLeadDays
        ) {
            chooseOptions()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.this_budget_rule_has_not_been_saved))
                .setMessage(getString(R.string.would_you_like_to_save_this_budget_rule_and_continue))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    validateBudgetRuleBeforeOptions()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun validateBudgetRuleBeforeOptions() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            updateBudgetRule()
            chooseOptions()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun chooseOptions() {
        val detailed = mainViewModel.getBudgetRuleDetailed()!!
        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(R.string.choose_an_action_for) + " " +
                        detailed.budgetRule!!.budgetRuleName
            )
            .setItems(
                arrayOf(
                    getString(R.string.add_a_new_transaction_based_on_the_budget_rule),
                    getString(R.string.create_a_scheduled_item_with_this_budget_rule),
                    getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> addNewTransaction()
                    1 -> createNewBudgetItem()
                    2 -> gotoAnalysis()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addNewTransaction() {
        val detailed = mainViewModel.getBudgetRuleDetailed()!!
        mainViewModel.setTransactionDetailed(createTransactionDetailed(detailed))
        mainViewModel.addCallingFragment(TAG)
        gotoTransactionAddFragment()
    }

    private fun createTransactionDetailed(detailed: BudgetRuleDetailed): TransactionDetailed {
        val tempTransaction = Transactions(
            nf.generateId(),
            df.getCurrentDateAsString(),
            detailed.budgetRule!!.budgetRuleName,
            "",
            detailed.budgetRule!!.ruleId,
            detailed.budgetRule!!.budToAccountId,
            false,
            detailed.budgetRule!!.budFromAccountId,
            false,
            detailed.budgetRule!!.budgetAmount,
            false,
            df.getCurrentTimeAsString()
        )
        return TransactionDetailed(
            tempTransaction,
            detailed.budgetRule!!,
            toAccount = detailed.toAccount!!,
            fromAccount = detailed.fromAccount!!,
        )
    }

    private fun createNewBudgetItem() {
        lifecycleScope.launch {
            val detailed = mainViewModel.getBudgetRuleDetailed()!!
            mainViewModel.setBudgetItemDetailed(createBudgetItemDetailed(detailed))
            mainViewModel.addCallingFragment(TAG)
            delay(1000L)
            gotoBudgetItemAddFragment()
        }
    }

    private suspend fun createBudgetItemDetailed(detailed: BudgetRuleDetailed): BudgetItemDetailed {
        var curPayday: String
        withContext(Dispatchers.IO) {
            curPayday = budgetItemViewModel.getPayDaysActive().first()
        }
        delay(WAIT_250)
        val tempBudgetItem = BudgetItem(
            detailed.budgetRule!!.ruleId,
            biProjectedDate = df.getCurrentDateAsString(),
            biActualDate = df.getCurrentDateAsString(),
            biPayDay = curPayday,
            biBudgetName = nameState.value,
            biIsPayDayItem = false,
            biToAccountId = detailed.toAccount!!.accountId,
            biFromAccountId = detailed.fromAccount!!.accountId,
            biProjectedAmount = detailed.budgetRule!!.budgetAmount,
            biIsPending = true,
            biIsFixed = isFixedState.value,
            biIsAutomatic = isAutoState.value,
            biManuallyEntered = true,
            biIsCompleted = false,
            biIsCancelled = false,
            biIsDeleted = false,
            biUpdateTime = df.getCurrentTimeAsString(),
            biLocked = true,
        )
        return BudgetItemDetailed(
            tempBudgetItem,
            detailed.budgetRule!!,
            detailed.toAccount!!,
            detailed.fromAccount!!
        )
    }

    private fun gotoAnalysis() {
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.addCallingFragment(TAG)
        gotoTransactionAnalysisFragment()
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
                confirmDeleteBudgetRule()
                true
            }

            else -> false
        }
    }

    private fun chooseEndDate() {
        val curDate = endDateState.value.ifBlank { df.getCurrentDateAsString() }
        val curDateAll = curDate.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                endDateState.value = "$year-${month.toString().padStart(2, '0')}-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_the_final_date))
        datePickerDialog.show()
    }

    private fun chooseStartDate() {
        val curDate = startDateState.value.ifBlank { df.getCurrentDateAsString() }
        val curDateAll = curDate.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                startDateState.value = "$year-${month.toString().padStart(2, '0')}-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_the_first_date))
        datePickerDialog.show()
    }

    private fun getCurrentBudgetRuleForSaving(): BudgetRule {
        val detailed = mainViewModel.getBudgetRuleDetailed()
        return BudgetRule(
            detailed?.budgetRule?.ruleId ?: ruleId,
            nameState.value.trim(),
            detailed?.toAccount?.accountId ?: 0L,
            detailed?.fromAccount?.accountId ?: 0L,
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
        val detailed = mainViewModel.getBudgetRuleDetailed()
        return BudgetRuleDetailed(
            getCurrentBudgetRuleForSaving(),
            detailed?.toAccount,
            detailed?.fromAccount
        )
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        gotoAccountChooseFragment()
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        gotoAccountChooseFragment()
    }

    private fun confirmDeleteBudgetRule() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_budget_rule))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_budget_rule))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteBudgeRule()
                gotoCallingFragment()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteBudgeRule() {
        val detailed = mainViewModel.getBudgetRuleDetailed()
        val rId = detailed?.budgetRule?.ruleId ?: ruleId
        if (rId != 0L) {
            budgetRuleViewModel.deleteBudgetRule(rId, df.getCurrentTimeAsString())
        }
    }

    private fun updateBudgetRuleIfValid() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            updateBudgetRule()
            gotoCallingFragment()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun updateBudgetRule() {
        val newRule = getCurrentBudgetRuleForSaving()
        budgetRuleViewModel.updateBudgetRule(newRule)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
    }

    private fun validateBudgetRule(): String {
        if (nameState.value.isBlank()) {
            return getString(R.string.please_enter_a_name)
        }
        val detailed = mainViewModel.getBudgetRuleDetailed()
        budgetNameList?.let { list ->
            for (name in list) {
                if (name == nameState.value.trim() &&
                    detailed?.budgetRule != null &&
                    name != detailed.budgetRule!!.budgetRuleName
                ) {
                    return getString(R.string.this_budget_rule_already_exists)
                }
            }
        }
        if (detailed?.toAccount == null) {
            return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        }
        if (detailed.fromAccount == null) {
            return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        }
        if (amountState.value.isEmpty()) {
            return getString(R.string.please_enter_a_budgeted_amount_including_zero)
        }
        return ANSWER_OK
    }

    private fun confirmGotoBudgetItem(item: BudgetItemDetailed) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.would_you_like_to_go_to_this_budget_item_on) + " ${
                df.getDisplayDate(item.budgetItem!!.biActualDate)
            }?"
        ).setPositiveButton(getString(R.string.yes)) { _, _ ->
            gotoBudgetItem(item)
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun gotoBudgetItem(item: BudgetItemDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(item)
        gotoBudgetItemUpdateFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().popBackStack()
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                amountState.value.ifBlank { getString(R.string.zero_double) })
        )
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoCalculatorFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetItemAddFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun gotoTransactionAddFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToTransactionAddFragment()
        )
    }

    private fun gotoTransactionAnalysisFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToTransactionAnalysisFragment()
        )
    }

    private fun gotoAccountChooseFragment() {
        findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToAccountChooseFragment()
        )
    }
}