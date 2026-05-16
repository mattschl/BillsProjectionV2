package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

//import android.app.AlertDialog
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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen

private const val TAG = FRAG_BUDGET_ITEM_UPDATE

@Composable
fun BudgetItemUpdateScreenWrapper(
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
        mainActivity.topMenuBar.title = mainActivity.getString(R.string.update_this_budget_item)
        if (budgetItemDetailedCached != null) {
            val item = budgetItemDetailedCached.budgetItem
            val rule = budgetItemDetailedCached.budgetRule
            val ruleChanged = rule != null && rule.ruleId != item?.biRuleId

            dateState.value = item?.biActualDate ?: ""

            if (ruleChanged || item?.biBudgetName.isNullOrBlank()) {
                nameState.value = rule?.budgetRuleName ?: ""
                amountState.value = nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0) mainViewModel.getTransferNum()!!
                    else rule?.budgetAmount ?: 0.0
                )
                isFixedState.value = rule?.budFixedAmount ?: false
                isPayDayItemState.value = rule?.budIsPayDay ?: false
                isAutoState.value = rule?.budIsAutoPay ?: false
            } else {
                nameState.value = item?.biBudgetName ?: ""
                amountState.value = nf.displayDollars(
                    if (mainViewModel.getTransferNum() != 0.0) mainViewModel.getTransferNum()!!
                    else item?.biProjectedAmount ?: 0.0
                )
                isFixedState.value = item?.biIsFixed ?: false
                isPayDayItemState.value = item?.biIsPayDayItem ?: false
                isAutoState.value = item?.biIsAutomatic ?: false
            }

            payDayState.value = item?.biPayDay ?: ""
            isLockedState.value = item?.biLocked ?: true
            mainViewModel.setTransferNum(0.0)
        }
    }

    fun getCurrentBudgetItemForUpdating(): BudgetItem {
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

    fun getCurrentBudgetItemDetailed(): BudgetItemDetailed {
        val cached = mainViewModel.getBudgetItemDetailed()
        return BudgetItemDetailed(
            getCurrentBudgetItemForUpdating(),
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

    fun updateBudgetItemIfValid() {
        val error = validateBudgetItem()
        if (error == ANSWER_OK) {
            budgetItemViewModel.updateBudgetItem(getCurrentBudgetItemForUpdating())
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mainActivity,
                mainActivity.getString(R.string.error) + error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*fun confirmDeleteBudgetItem() {
        AlertDialog.Builder(mainActivity).apply {
            setTitle(mainActivity.getString(R.string.delete_budget_item))
            setMessage(mainActivity.getString(R.string.are_you_sure_you_want_to_delete_this_budget_item))
            setPositiveButton(mainActivity.getString(R.string.delete)) { _, _ ->
                val cached = mainViewModel.getBudgetItemDetailed()!!
                budgetItemViewModel.deleteBudgetItem(
                    cached.budgetItem!!.biRuleId,
                    cached.budgetItem!!.biProjectedDate,
                    df.getCurrentTimeAsString()
                )
                gotoCallingFragment()
            }
            setNegativeButton(mainActivity.getString(R.string.cancel), null)
        }.create().show()
    }*/

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
        onSaveClick = { updateBudgetItemIfValid() },
        onChooseBudgetRule = { chooseBudgetRule() },
        onChooseAccount = { chooseAccount(it) },
        onGotoCalculator = { gotoCalculator() }
    )
}