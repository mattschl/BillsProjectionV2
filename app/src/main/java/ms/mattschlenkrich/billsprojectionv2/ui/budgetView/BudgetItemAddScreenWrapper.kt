package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_ITEM_ADD

@Composable
fun BudgetItemAddScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val budgetItemViewModel = mainActivity.budgetItemViewModel
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    val dateState = remember { mutableStateOf("") }
    val nameState = remember { mutableStateOf("") }
    val payDayState = remember { mutableStateOf("") }
    val amountState = remember { mutableStateOf("") }
    val isFixedState = remember { mutableStateOf(false) }
    val isPayDayItemState = remember { mutableStateOf(false) }
    val isAutoState = remember { mutableStateOf(false) }
    val isLockedState = remember { mutableStateOf(true) }

    val payDays by budgetItemViewModel.getPayDays().observeAsState(emptyList())
    val budgetItemDetailedCached = mainViewModel.getBudgetItemDetailed()

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.add_a_new_budget_item)
        if (budgetItemDetailedCached != null) {
            dateState.value =
                budgetItemDetailedCached.budgetItem?.biProjectedDate ?: df.getCurrentDateAsString()
            nameState.value = budgetItemDetailedCached.budgetItem?.biBudgetName ?: ""
            payDayState.value = budgetItemDetailedCached.budgetItem?.biPayDay ?: ""
            amountState.value = nf.displayDollars(
                if (mainViewModel.getTransferNum() != 0.0) {
                    mainViewModel.getTransferNum()!!
                } else {
                    budgetItemDetailedCached.budgetItem?.biProjectedAmount ?: 0.0
                }
            )
            isFixedState.value = budgetItemDetailedCached.budgetItem?.biIsFixed ?: false
            isPayDayItemState.value = budgetItemDetailedCached.budgetItem?.biIsPayDayItem ?: false
            isAutoState.value = budgetItemDetailedCached.budgetItem?.biIsAutomatic ?: false
            isLockedState.value = budgetItemDetailedCached.budgetItem?.biLocked ?: true
            mainViewModel.setTransferNum(0.0)
        } else {
            dateState.value = df.getCurrentDateAsString()
            amountState.value = nf.displayDollars(0.0)
        }
    }

    fun getCurrentBudgetItemForSaving(): BudgetItem {
        val cached = mainViewModel.getBudgetItemDetailed()
        return BudgetItem(
            cached?.budgetRule?.ruleId ?: nf.generateId(),
            dateState.value,
            dateState.value,
            payDayState.value,
            nameState.value,
            isPayDayItemState.value,
            cached?.toAccount?.accountId ?: 0L,
            cached?.fromAccount?.accountId ?: 0L,
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

    fun getCurrentBudgetItemDetailed(): BudgetItemDetailed {
        val cached = mainViewModel.getBudgetItemDetailed()
        return BudgetItemDetailed(
            getCurrentBudgetItemForSaving(),
            cached?.budgetRule,
            cached?.toAccount,
            cached?.fromAccount
        )
    }

    fun validateBudgetItem(): String {
        if (nameState.value.isBlank()) return mainActivity.getString(R.string.please_enter_a_name_or_description)
        val cached = mainViewModel.getBudgetItemDetailed()
        if (cached?.toAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_go_to)
        if (cached.fromAccount == null) return mainActivity.getString(R.string.there_needs_to_be_an_account_money_will_come_from)
        if (amountState.value.isEmpty()) return mainActivity.getString(R.string.please_enter_a_budgeted_amount_including_zero)
        return ANSWER_OK
    }

    fun gotoCallingFragment() {
        mainViewModel.setBudgetItemDetailed(null)
        mainViewModel.removeCallingFragment(TAG)
        navController.popBackStack()
    }

    fun saveBudgetItemIfValid() {
        val error = validateBudgetItem()
        if (error == ANSWER_OK) {
            budgetItemViewModel.insertBudgetItem(getCurrentBudgetItemForSaving())
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        navController.navigate(Screen.BudgetRuleChoose.route)
    }

    fun chooseAccount(requestedAccount: String) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        navController.navigate(Screen.AccountChoose.route)
    }

    fun gotoCalculator() {
        mainViewModel.setTransferNum(nf.getDoubleFromDollars(amountState.value))
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        navController.navigate(Screen.Calculator.route)
    }

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
        budgetItemDetailed = budgetItemDetailedCached,
        payDays = payDays,
        onSaveClick = { saveBudgetItemIfValid() },
        onChooseBudgetRule = { chooseBudgetRule() },
        onChooseAccount = { chooseAccount(it) },
        onGotoCalculator = { gotoCalculator() }
    )
}