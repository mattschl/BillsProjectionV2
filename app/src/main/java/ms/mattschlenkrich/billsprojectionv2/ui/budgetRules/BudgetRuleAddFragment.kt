package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
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

                    BudgetRuleScreen(
                        name = name,
                        onNameChange = { name = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        isFixed = isFixed,
                        onIsFixedChange = { isFixed = it },
                        isPayDay = isPayDay,
                        onIsPayDayChange = { isPayDay = it },
                        isAuto = isAuto,
                        onIsAutoChange = { isAuto = it },
                        startDate = startDate,
                        onStartDateChange = { startDate = it },
                        endDate = endDate,
                        onEndDateChange = { endDate = it },
                        frequencyType = frequencyType,
                        onFrequencyTypeChange = { frequencyType = it },
                        frequencyCount = frequencyCount,
                        onFrequencyCountChange = { frequencyCount = it },
                        dayOfWeek = dayOfWeek,
                        onDayOfWeekChange = { dayOfWeek = it },
                        leadDays = leadDays,
                        onLeadDaysChange = { leadDays = it },
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