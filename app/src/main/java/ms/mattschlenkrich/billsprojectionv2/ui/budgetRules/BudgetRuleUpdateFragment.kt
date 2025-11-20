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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_1000
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
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
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter.BudgetRuleDatesAdapter

private const val TAG = FRAG_BUDGET_RULE_UPDATE

class BudgetRuleUpdateFragment : Fragment(R.layout.fragment_budget_rule_update) {

    private var _binding: FragmentBudgetRuleUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var mView: View
    private lateinit var budgetRuleDetailed: BudgetRuleDetailed
    private var budgetNameList: List<String>? = null

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_budget_rule)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        getBudgetRuleNameForValidation()
        populateSpinners()
        CoroutineScope(Dispatchers.Main).launch {
            if (mainViewModel.getCallingFragments() != null) {
                if (mainViewModel.getBudgetRuleDetailed() != null) {
                    val mCallingFragment = mainViewModel.getCallingFragments()!!
                    if (mCallingFragment.contains(FRAG_TRANSACTION_VIEW) &&
                        !mCallingFragment.contains(FRAG_ACCOUNT_CHOOSE)
                    ) {
                        withContext(Dispatchers.Default) {
                            budgetRuleDetailed = budgetRuleViewModel.getBudgetRuleDetailed(
                                mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.ruleId
                            )
                        }
                    } else {
                        budgetRuleDetailed = mainViewModel.getBudgetRuleDetailed()!!
                    }
                    delay(WAIT_250)
                    populateFromBudgetRuleDetailed()
                }
            } else {
                populateDatesOnly()
            }
            delay(WAIT_500)
            populateDateRecycler(budgetRuleDetailed.budgetRule!!.ruleId)
        }

    }

    private fun populateDatesOnly() {
        binding.apply {
            etStartDate.setText(df.getCurrentDateAsString())
            etEndDate.setText(df.getCurrentDateAsString())
        }
    }

    private fun populateFromBudgetRuleDetailed() {
        binding.apply {
            if (budgetRuleDetailed.budgetRule != null) {
                etBudgetName.setText(budgetRuleDetailed.budgetRule!!.budgetRuleName)
                etAmount.setText(
                    nf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            budgetRuleDetailed.budgetRule!!.budgetAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                if (budgetRuleDetailed.toAccount != null) {
                    tvToAccount.text = budgetRuleDetailed.toAccount!!.accountName
                }
                if (budgetRuleDetailed.fromAccount != null) {
                    tvFromAccount.text = budgetRuleDetailed.fromAccount!!.accountName
                }
                chkFixedAmount.isChecked = budgetRuleDetailed.budgetRule!!.budFixedAmount
                chkMakePayDay.isChecked = budgetRuleDetailed.budgetRule!!.budIsPayDay
                chkAutoPayment.isChecked = budgetRuleDetailed.budgetRule!!.budIsAutoPay
                etStartDate.setText(budgetRuleDetailed.budgetRule!!.budStartDate)
                etEndDate.setText(budgetRuleDetailed.budgetRule!!.budEndDate)
                spFrequencyType.setSelection(budgetRuleDetailed.budgetRule!!.budFrequencyTypeId)
                etFrequencyCount.setText(
                    String.format(budgetRuleDetailed.budgetRule!!.budFrequencyCount.toString())
                )
                spDayOfWeek.setSelection(budgetRuleDetailed.budgetRule!!.budDayOfWeekId)
                etLeadDays.setText(
                    String.format(budgetRuleDetailed.budgetRule!!.budLeadDays.toString())
                )
            }
        }
    }

    private fun populateDateRecycler(budgetRuleId: Long) {
        val budgetRuleDatesAdapter = BudgetRuleDatesAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetRuleUpdateFragment,
        )
        binding.rvProjectedDates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetRuleDatesAdapter
        }
        budgetItemViewModel.getBudgetItems(budgetRuleId).observe(
            viewLifecycleOwner
        ) { budgetItems ->
            budgetRuleDatesAdapter.differ.submitList(budgetItems)
        }
    }

    private fun populateSpinners() {
        val adapterFrequencyType = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_normal,
            resources.getStringArray(R.array.frequency_types)
        )
        adapterFrequencyType.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spFrequencyType.adapter = adapterFrequencyType

        val adapterDayOfWeek = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_normal,
            resources.getStringArray(R.array.days_of_week)
        )
        adapterDayOfWeek.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spDayOfWeek.adapter = adapterDayOfWeek
    }

    private fun getBudgetRuleNameForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvToAccount.setOnClickListener { chooseToAccount() }
            tvFromAccount.setOnClickListener { chooseFromAccount() }
            etStartDate.setOnLongClickListener {
                chooseStartDate()
                false
            }
            etEndDate.setOnLongClickListener {
                chooseEndDate()
                false
            }
            fabUpdateDone.setOnClickListener { updateBudgetRuleIfValid() }
            fabAddOptions.setOnClickListener { chooseAddOptionsOrUpdateBudgetRuleToContinue() }
            etAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
        }
    }

    private fun chooseAddOptionsOrUpdateBudgetRuleToContinue() {
        val curBudgetRule = getCurrentBudgetRuleForSaving()
        val cachedBudgetRule = budgetRuleDetailed.budgetRule!!
        if (
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
            AlertDialog.Builder(mView.context)
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
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(R.string.choose_an_action_for) + " " +
                        budgetRuleDetailed.budgetRule!!.budgetRuleName
            )
            .setItems(
                arrayOf(
                    getString(R.string.add_a_new_transaction_based_on_the_budget_rule),
                    getString(R.string.create_a_scheduled_item_with_this_budget_rule),
                    mView.context.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
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
        mainViewModel.setTransactionDetailed(createTransactionDetailed())
        mainViewModel.addCallingFragment(TAG)
        gotoTransactionAddFragment()
    }

    private fun createTransactionDetailed(): TransactionDetailed {
        val tempTransaction = Transactions(
            nf.generateId(),
            df.getCurrentDateAsString(),
            budgetRuleDetailed.budgetRule!!.budgetRuleName,
            "",
            budgetRuleDetailed.budgetRule!!.ruleId,
            budgetRuleDetailed.budgetRule!!.budToAccountId,
            false,
            budgetRuleDetailed.budgetRule!!.budFromAccountId,
            false,
            budgetRuleDetailed.budgetRule!!.budgetAmount,
            false,
            df.getCurrentTimeAsString()
        )
        return TransactionDetailed(
            tempTransaction,
            budgetRuleDetailed.budgetRule!!,
            toAccount = budgetRuleDetailed.toAccount!!,
            fromAccount = budgetRuleDetailed.fromAccount!!,
        )
    }

    private fun createNewBudgetItem() {
        CoroutineScope(Dispatchers.Main).launch {
            mainViewModel.setBudgetItemDetailed(createBudgetItemDetailed())
            mainViewModel.addCallingFragment(TAG)
            delay(WAIT_1000)
            gotoBudgetItemAddFragment()
        }
    }

    private suspend fun createBudgetItemDetailed(): BudgetItemDetailed {
        var tempBudgetItemDetailed = BudgetItemDetailed(null, null, null, null)
        var curPayday: String
        withContext(Dispatchers.Default) {
            curPayday = budgetItemViewModel.getPayDaysActive().first()
        }
        delay(WAIT_250)
        binding.apply {
            val tempBudgetItem =
                BudgetItem(
                    budgetRuleDetailed.budgetRule!!.ruleId,
                    biProjectedDate = df.getCurrentDateAsString(),
                    biActualDate = df.getCurrentDateAsString(),
                    biPayDay = curPayday,
                    biBudgetName = etBudgetName.text.toString(),
                    biIsPayDayItem = false,
                    biToAccountId = budgetRuleDetailed.toAccount!!.accountId,
                    biFromAccountId = budgetRuleDetailed.fromAccount!!.accountId,
                    biProjectedAmount = budgetRuleDetailed.budgetRule!!.budgetAmount,
                    biIsPending = true,
                    biIsFixed = chkFixedAmount.isChecked,
                    biIsAutomatic = chkAutoPayment.isChecked,
                    biManuallyEntered = true,
                    biIsCompleted = false,
                    biIsCancelled = false,
                    biIsDeleted = false,
                    biUpdateTime = df.getCurrentTimeAsString(),
                    biLocked = true,
                )
            tempBudgetItemDetailed = BudgetItemDetailed(
                tempBudgetItem,
                budgetRuleDetailed.budgetRule!!,
                budgetRuleDetailed.toAccount!!,
                budgetRuleDetailed.fromAccount!!
            )
        }
        delay(WAIT_500)
        return tempBudgetItemDetailed
    }

    private fun gotoAnalysis() {
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mainViewModel.addCallingFragment(TAG)
        gotoTransactionAnalysisFragment()
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        confirmDeleteBudgetRule()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseEndDate() {
        binding.apply {
            val curDateAll = etEndDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etEndDate.setText(display)
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_final_date))
            datePickerDialog.show()
        }
    }

    private fun chooseStartDate() {
        binding.apply {
            val curDateAll = etStartDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etStartDate.setText(display)
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_first_date))
            datePickerDialog.show()
        }
    }

    private fun getCurrentBudgetRuleForSaving(): BudgetRule {
        val toAccId = if (budgetRuleDetailed.toAccount == null) {
            0L
        } else {
            budgetRuleDetailed.toAccount!!.accountId
        }
        val fromAccId = if (budgetRuleDetailed.fromAccount == null) {
            0L
        } else {
            budgetRuleDetailed.fromAccount!!.accountId
        }
        binding.apply {
            return BudgetRule(
                budgetRuleDetailed.budgetRule!!.ruleId,
                etBudgetName.text.toString().trim(),
                toAccId,
                fromAccId,
                nf.getDoubleFromDollars(
                    etAmount.text.toString()
                ),
                chkFixedAmount.isChecked,
                chkMakePayDay.isChecked,
                chkAutoPayment.isChecked,
                etStartDate.text.toString(),
                etEndDate.text.toString(),
                spDayOfWeek.selectedItemId.toInt(),
                spFrequencyType.selectedItemId.toInt(),
                etFrequencyCount.text.toString().toInt(),
                etLeadDays.text.toString().toInt(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun getBudgetRuleDetailed(): BudgetRuleDetailed {
        binding.apply {
            val toAccount = if (mainViewModel.getBudgetRuleDetailed() != null) {
                if (budgetRuleDetailed.toAccount != null) {
                    budgetRuleDetailed.toAccount
                } else {
                    null
                }
            } else {
                null
            }
            val fromAccount = if (mainViewModel.getBudgetRuleDetailed() != null) {
                if (budgetRuleDetailed.fromAccount != null) {
                    budgetRuleDetailed.fromAccount
                } else {
                    null
                }
            } else {
                null
            }
            return BudgetRuleDetailed(getCurrentBudgetRuleForSaving(), toAccount, fromAccount)
        }
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
        AlertDialog.Builder(activity).apply {
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
        budgetRuleViewModel.deleteBudgetRule(
            budgetRuleDetailed.budgetRule!!.ruleId, df.getCurrentTimeAsString()
        )
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
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateBudgetRule() {
        budgetRuleDetailed = BudgetRuleDetailed(
            getCurrentBudgetRuleForSaving(),
            budgetRuleDetailed.toAccount!!,
            budgetRuleDetailed.fromAccount
        )
        mainActivity.budgetRuleViewModel.updateBudgetRule(budgetRuleDetailed.budgetRule!!)
    }

    private fun validateBudgetRule(): String {
        binding.apply {
            if (etBudgetName.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name)
            }
            for (i in 0 until budgetNameList!!.size) {
                if (budgetNameList!![i] == etBudgetName.text.toString() && mainViewModel.getBudgetRuleDetailed() != null && budgetNameList!![i] != budgetRuleDetailed.budgetRule!!.budgetRuleName) {
                    return getString(R.string.this_budget_rule_already_exists)
                }
            }
            if (budgetRuleDetailed.toAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (budgetRuleDetailed.fromAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etAmount.text.isNullOrEmpty()) {
                return getString(R.string.please_enter_a_budgeted_amount_including_zero)
            }
            return ANSWER_OK
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(null)
        val mCallingFragments = mainViewModel.getCallingFragments()!!
        if (mCallingFragments.contains(FRAG_BUDGET_RULES)) {
            gotoBudgetRuleFragment()
        } else if (mCallingFragments.contains(FRAG_BUDGET_VIEW)) {
            gotoBudgetViewFragment()
        } else if (mCallingFragments.contains(FRAG_BUDGET_LIST)) {
            gotoBudgetListFragment()
        } else if (mCallingFragments.contains(FRAG_TRANSACTION_VIEW)) {
            gotoTransactionViewFragment()
        } else if (mCallingFragments.contains(FRAG_TRANSACTION_ANALYSIS)) {
            gotoTransactionAnalysisFragment()
        } else if (mCallingFragments.contains(FRAG_ACCOUNT_UPDATE)) {
            gotoAccountUpdateFragment()
        }
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    getString(R.string.zero_double)
                })
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetListFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetListFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoBudgetRuleFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetRuleFragment()
        )
    }

    private fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToTransactionAddFragment()
        )
    }

    private fun gotoAccountUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToAccountUpdateFragment()
        )
    }

    private fun gotoTransactionAnalysisFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToTransactionAnalysisFragment()
        )
    }

    private fun gotoTransactionViewFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToTransactionViewFragment()
        )
    }

    private fun gotoAccountChooseFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections.actionBudgetRuleUpdateFragmentToAccountChooseFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}