package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose.BudgetRuleScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_RULE_ADD

@Composable
fun BudgetRuleAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetRuleViewModel = mainActivity.budgetRuleViewModel
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val scope = rememberCoroutineScope()
    val state = rememberBudgetRuleEditState(nf, df)

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
                state.updateFrom(rule, mainViewModel.getTransferNum())
                mainViewModel.setTransferNum(0.0)
            }
        } else {
            state.startDate = df.getCurrentDateAsString()
            state.endDate = df.getCurrentDateAsString()
            state.amount = nf.displayDollars(0.0)
        }
    }

    fun getCurrentBudgetRuleForSave(): BudgetRule {
        val cached = mainViewModel.getBudgetRuleDetailed()
        return state.toBudgetRule(
            cached?.toAccount?.accountId ?: 0L,
            cached?.fromAccount?.accountId ?: 0L
        )
    }

    fun getBudgetRuleDetailed(): BudgetRuleDetailed {
        return state.toBudgetRuleDetailed(mainViewModel.getBudgetRuleDetailed())
    }

    fun validateBudgetRule(): String {
        val name = state.name.trim()
        if (name.isBlank()) return mainActivity.getString(R.string.please_enter_a_name)
        if (budgetNameList.contains(name)) return mainActivity.getString(R.string.this_budget_rule_already_exists)

        val cached = mainViewModel.getBudgetRuleDetailed()
        if (cached?.toAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        if (cached.fromAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        if (state.amount.isEmpty()) return mainActivity.getString(R.string.please_enter_a_budgeted_amount_including_zero)
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
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(state.amount))
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        navController.navigate(Screen.Calculator.route)
    }

    val cached = mainViewModel.getBudgetRuleDetailed()

    BudgetRuleScreen(
        name = state.name,
        onNameChange = { state.name = it },
        amount = state.amount,
        onAmountChange = { state.amount = it },
        isFixed = state.isFixed,
        onIsFixedChange = { state.isFixed = it },
        isPayDay = state.isPayDay,
        onIsPayDayChange = { state.isPayDay = it },
        isAuto = state.isAuto,
        onIsAutoChange = { state.isAuto = it },
        startDate = state.startDate,
        onStartDateChange = { state.startDate = it },
        endDate = state.endDate,
        onEndDateChange = { state.endDate = it },
        frequencyType = state.frequencyType,
        onFrequencyTypeChange = { state.frequencyType = it },
        frequencyCount = state.frequencyCount,
        onFrequencyCountChange = { state.frequencyCount = it },
        dayOfWeek = state.dayOfWeek,
        onDayOfWeekChange = { state.dayOfWeek = it },
        leadDays = state.leadDays,
        onLeadDaysChange = { state.leadDays = it },
        toAccount = cached?.toAccount,
        fromAccount = cached?.fromAccount,
        onChooseAccount = { chooseAccount(it) },
        onGotoCalculator = { gotoCalculator() },
        suggestedAmount = null,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { saveBudgetRuleIfValid() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save"
                )
            }
        }
    )
}