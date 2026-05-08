package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_RULE_ADD

@Composable
fun BudgetRuleAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val nf = NumberFunctions()
    val df = DateFunctions()
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

    var budgetNameList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_budget_rule)
        scope.launch(Dispatchers.IO) {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }

        val cached = mainViewModel.getBudgetRuleDetailed()
        if (cached != null) {
            val rule = cached.budgetRule
            if (rule != null) {
                nameState = rule.budgetRuleName
                amountState = nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        rule.budgetAmount
                    }
                )
                isFixedState = rule.budFixedAmount
                isPayDayState = rule.budIsPayDay
                isAutoState = rule.budIsAutoPay
                startDateState = rule.budStartDate
                endDateState = rule.budEndDate ?: df.getCurrentDateAsString()
                frequencyTypeState = rule.budFrequencyTypeId
                frequencyCountState = rule.budFrequencyCount.toString()
                dayOfWeekState = rule.budDayOfWeekId
                leadDaysState = rule.budLeadDays.toString()
                mainViewModel.setTransferNum(0.0)
            }
        } else {
            startDateState = df.getCurrentDateAsString()
            endDateState = df.getCurrentDateAsString()
            amountState = nf.displayDollars(0.0)
        }
    }

    fun getCurrentBudgetRuleForSave(): BudgetRule {
        val cached = mainViewModel.getBudgetRuleDetailed()
        return BudgetRule(
            nf.generateId(),
            nameState.trim(),
            cached?.toAccount?.accountId ?: 0L,
            cached?.fromAccount?.accountId ?: 0L,
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
        val cached = mainViewModel.getBudgetRuleDetailed()
        return BudgetRuleDetailed(
            getCurrentBudgetRuleForSave(),
            cached?.toAccount,
            cached?.fromAccount
        )
    }

    fun validateBudgetRule(): String {
        val name = nameState.trim()
        if (name.isBlank()) return mainActivity.getString(R.string.please_enter_a_name)
        if (budgetNameList.contains(name)) return mainActivity.getString(R.string.this_budget_rule_already_exists)

        val cached = mainViewModel.getBudgetRuleDetailed()
        if (cached?.toAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        if (cached.fromAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        if (amountState.isEmpty()) return mainActivity.getString(R.string.please_enter_a_budgeted_amount_including_zero)
        return ANSWER_OK
    }

    fun saveBudgetRuleIfValid() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            budgetRuleViewModel.insertBudgetRule(getCurrentBudgetRuleForSave())
            mainViewModel.setBudgetRuleDetailed(null)
            mainViewModel.removeCallingFragment(TAG)
            navController.popBackStack()
        } else {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun chooseAccount(requestedAccount: String) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        navController.navigate(Screen.AccountChoose.route)
    }

    fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState))
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        navController.navigate(Screen.Calculator.route)
    }

    val cached = mainViewModel.getBudgetRuleDetailed()

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
        toAccount = cached?.toAccount,
        fromAccount = cached?.fromAccount,
        onChooseAccount = { chooseAccount(it) },
        onGotoCalculator = { gotoCalculator() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { saveBudgetRuleIfValid() },
                containerColor = Color(0xFFB00020)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.White
                )
            }
        }
    )
}