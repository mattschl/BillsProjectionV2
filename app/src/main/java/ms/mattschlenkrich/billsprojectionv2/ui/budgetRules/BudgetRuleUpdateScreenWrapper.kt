package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.components.BudgetItemDisplay
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_RULE_UPDATE

@Composable
fun BudgetRuleUpdateScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val budgetItemViewModel = mainActivity.budgetItemViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val scope = rememberCoroutineScope()

    var nameState by remember { mutableStateOf("") }
    var amountState by remember { mutableStateOf("") }
    var isFixedState by remember { mutableStateOf(false) }
    var isPayDayState by remember { mutableStateOf(false) }
    var isAutoState by remember { mutableStateOf(false) }
    var startDateState by remember { mutableStateOf("") }
    var endDateState by remember { mutableStateOf("") }
    var frequencyTypeState by remember { mutableIntStateOf(0) }
    var frequencyCountState by remember { mutableStateOf("1") }
    var dayOfWeekState by remember { mutableIntStateOf(0) }
    var leadDaysState by remember { mutableStateOf("0") }
    var ruleIdState by remember { mutableLongStateOf(0L) }

    var budgetNameList by remember { mutableStateOf<List<String>?>(null) }

    val detailedCached = mainViewModel.getBudgetRuleDetailed()

    val budgetItems by remember(ruleIdState) {
        budgetItemViewModel.getBudgetItems(ruleIdState)
    }.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_budget_rule)
        scope.launch(Dispatchers.IO) {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }

        if (detailedCached != null) {
            val rule = detailedCached.budgetRule
            if (rule != null) {
                ruleIdState = rule.ruleId
                nameState = rule.budgetRuleName
                amountState = nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        rule.budgetAmount
                    }
                )
                mainViewModel.setTransferNum(0.0)
                isFixedState = rule.budFixedAmount
                isPayDayState = rule.budIsPayDay
                isAutoState = rule.budIsAutoPay
                startDateState = rule.budStartDate
                endDateState = rule.budEndDate ?: ""
                frequencyTypeState = rule.budFrequencyTypeId
                frequencyCountState = rule.budFrequencyCount.toString()
                dayOfWeekState = rule.budDayOfWeekId
                leadDaysState = rule.budLeadDays.toString()
            }
        }
    }

    fun getCurrentBudgetRuleForSaving(): BudgetRule {
        val detailed = mainViewModel.getBudgetRuleDetailed()
        return BudgetRule(
            detailed?.budgetRule?.ruleId ?: ruleIdState,
            nameState.trim(),
            detailed?.toAccount?.accountId ?: 0L,
            detailed?.fromAccount?.accountId ?: 0L,
            nf.getDoubleFromDollars(amountState),
            isFixedState,
            isPayDayState,
            isAutoState,
            startDateState,
            endDateState,
            dayOfWeekState,
            frequencyTypeState,
            frequencyCountState.toIntOrNull() ?: 1,
            leadDaysState.toIntOrNull() ?: 0,
            false,
            df.getCurrentTimeAsString()
        )
    }

    fun getBudgetRuleDetailed(): BudgetRuleDetailed {
        val detailed = mainViewModel.getBudgetRuleDetailed()
        return BudgetRuleDetailed(
            getCurrentBudgetRuleForSaving(),
            detailed?.toAccount,
            detailed?.fromAccount
        )
    }

    fun updateBudgetRule() {
        val newRule = getCurrentBudgetRuleForSaving()
        budgetRuleViewModel.updateBudgetRule(newRule)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
    }

    fun validateBudgetRule(): String {
        if (nameState.isBlank()) {
            return mainActivity.getString(R.string.please_enter_a_name)
        }
        val detailed = mainViewModel.getBudgetRuleDetailed()
        budgetNameList?.let { list ->
            for (name in list) {
                if (name == nameState.trim() &&
                    detailed?.budgetRule != null &&
                    name != detailed.budgetRule!!.budgetRuleName
                ) {
                    return mainActivity.getString(R.string.this_budget_rule_already_exists)
                }
            }
        }
        if (detailed?.toAccount == null) {
            return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        }
        if (detailed.fromAccount == null) {
            return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        }
        if (amountState.isEmpty()) {
            return mainActivity.getString(R.string.please_enter_a_budgeted_amount_including_zero)
        }
        return ANSWER_OK
    }

    fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(null)
        navController.popBackStack()
    }

    fun updateBudgetRuleIfValid() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            updateBudgetRule()
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun deleteBudgeRule() {
        val detailed = mainViewModel.getBudgetRuleDetailed()
        val rId = detailed?.budgetRule?.ruleId ?: ruleIdState
        if (rId != 0L) {
            budgetRuleViewModel.deleteBudgetRule(rId, df.getCurrentTimeAsString())
        }
    }

    fun confirmDeleteBudgetRule() {
        AlertDialog.Builder(mainActivity).apply {
            setTitle(mainActivity.getString(R.string.delete_budget_rule))
            setMessage(mainActivity.getString(R.string.are_you_sure_you_want_to_delete_this_budget_rule))
            setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
                deleteBudgeRule()
                gotoCallingFragment()
            }
            setNegativeButton(mainActivity.getString(R.string.cancel), null)
        }.create().show()
    }

    fun createTransactionDetailed(detailed: BudgetRuleDetailed): TransactionDetailed {
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

    fun addNewTransaction() {
        val detailed = mainViewModel.getBudgetRuleDetailed()!!
        mainViewModel.setTransactionDetailed(createTransactionDetailed(detailed))
        mainViewModel.addCallingFragment(TAG)
        navController.navigate(Screen.TransactionAdd.route)
    }

    suspend fun createBudgetItemDetailed(detailed: BudgetRuleDetailed): BudgetItemDetailed {
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
            biBudgetName = nameState,
            biIsPayDayItem = false,
            biToAccountId = detailed.toAccount!!.accountId,
            biFromAccountId = detailed.fromAccount!!.accountId,
            biProjectedAmount = detailed.budgetRule!!.budgetAmount,
            biIsPending = true,
            biIsFixed = isFixedState,
            biIsAutomatic = isAutoState,
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

    fun createNewBudgetItem() {
        scope.launch {
            val detailed = mainViewModel.getBudgetRuleDetailed()!!
            mainViewModel.setBudgetItemDetailed(createBudgetItemDetailed(detailed))
            mainViewModel.addCallingFragment(TAG)
            delay(1000L)
            navController.navigate(Screen.BudgetItemAdd.route)
        }
    }

    fun gotoAnalysis() {
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.addCallingFragment(TAG)
        navController.navigate(Screen.Analysis.route)
    }

    fun chooseOptions() {
        val detailed = mainViewModel.getBudgetRuleDetailed()!!
        AlertDialog.Builder(mainActivity)
            .setTitle(
                mainActivity.getString(R.string.choose_an_action_for) + " " +
                        detailed.budgetRule!!.budgetRuleName
            )
            .setItems(
                arrayOf(
                    mainActivity.getString(R.string.add_a_new_transaction_based_on_the_budget_rule),
                    mainActivity.getString(R.string.create_a_scheduled_item_with_this_budget_rule),
                    mainActivity.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> addNewTransaction()
                    1 -> createNewBudgetItem()
                    2 -> gotoAnalysis()
                }
            }
            .setNegativeButton(mainActivity.getString(R.string.cancel), null)
            .show()
    }

    fun chooseAddOptionsOrUpdateBudgetRuleToContinue() {
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
            AlertDialog.Builder(mainActivity)
                .setTitle(mainActivity.getString(R.string.this_budget_rule_has_not_been_saved))
                .setMessage(mainActivity.getString(R.string.would_you_like_to_save_this_budget_rule_and_continue))
                .setPositiveButton(mainActivity.getString(R.string.yes)) { _, _ ->
                    val message = validateBudgetRule()
                    if (message == ANSWER_OK) {
                        updateBudgetRule()
                        chooseOptions()
                    } else {
                        Toast.makeText(
                            mainActivity,
                            mainActivity.getString(R.string.error) + message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton(mainActivity.getString(R.string.cancel), null)
                .show()
        }
    }

    fun chooseAccount(requestedAccount: String) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.setRequestedAccount(requestedAccount)
        navController.navigate(Screen.AccountChoose.route)
    }

    fun gotoBudgetItem(item: BudgetItemDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(item)
        navController.navigate(Screen.BudgetItemUpdate.route)
    }

    fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                amountState.ifBlank { mainActivity.getString(R.string.zero_double) })
        )
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        navController.navigate(Screen.Calculator.route)
    }

    BudgetRuleScreen(
        name = nameState,
        onNameChange = { nameState = it },
        amount = amountState,
        onAmountChange = { amountState = it },
        isFixed = isFixedState,
        onIsFixedChange = { isFixedState = it },
        isPayDay = isPayDayState,
        onIsPayDayChange = { isPayDayState = it },
        isAuto = isAutoState,
        onIsAutoChange = { isAutoState = it },
        startDate = startDateState,
        onStartDateChange = { startDateState = it },
        endDate = endDateState,
        onEndDateChange = { endDateState = it },
        frequencyType = frequencyTypeState,
        onFrequencyTypeChange = { frequencyTypeState = it },
        frequencyCount = frequencyCountState,
        onFrequencyCountChange = { frequencyCountState = it },
        dayOfWeek = dayOfWeekState,
        onDayOfWeekChange = { dayOfWeekState = it },
        leadDays = leadDaysState,
        onLeadDaysChange = { leadDaysState = it },
        toAccount = detailedCached?.toAccount,
        fromAccount = detailedCached?.fromAccount,
        onChooseAccount = { chooseAccount(it) },
        onGotoCalculator = { gotoCalculator() },
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
                    Icon(
                        Icons.Default.Done,
                        contentDescription = "Update",
                        tint = Color.White
                    )
                }
            }
        },
        bottomContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { confirmDeleteBudgetRule() }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val pendingItems =
                budgetItems.filter { it.budgetItem?.biIsPending == true }
            if (pendingItems.isNotEmpty()) {
                val pendingTotal =
                    pendingItems.sumOf { it.budgetItem?.biProjectedAmount ?: 0.0 }
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pending),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = nf.displayDollars(pendingTotal) + " (${pendingItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            Text(
                text = stringResource(R.string.projected_date),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            budgetItems.forEach { item ->
                BudgetItemDisplay(
                    budgetItemDetailed = item,
                    isCredit = item.toAccount?.accountId == detailedCached?.toAccount?.accountId,
                    onClick = {
                        AlertDialog.Builder(mainActivity).setTitle(
                            mainActivity.getString(R.string.would_you_like_to_go_to_this_budget_item_on) + " ${
                                df.getDisplayDate(item.budgetItem!!.biActualDate)
                            }?"
                        ).setPositiveButton(mainActivity.getString(R.string.yes)) { _, _ ->
                            gotoBudgetItem(item)
                        }.setNegativeButton(mainActivity.getString(R.string.cancel), null).show()
                    }
                )
            }
        }
    )
}