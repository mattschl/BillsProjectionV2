package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_BUDGET_ITEM_ADD

class BudgetItemAddFragment : Fragment(
    R.layout.fragment_budget_item_add
) {

    private var _binding: FragmentBudgetItemAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetRuleDetailed: BudgetRuleDetailed
    private var mTopAccount: Account? = null
    private var mTFromAccount: Account? = null

    private val nf = NumberFunctions()
    private val df = DateFunctions()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetItemAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.title = getString(R.string.add_a_new_budget_item)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populatePayDays()
        initializeBudgetItemDetailed()
        if (mainViewModel.getBudgetItemDetailed() != null) {
            populateFromCache()
        } else {
            populateDateToCurrent()
        }
    }

    private fun initializeBudgetItemDetailed() {
        budgetRuleDetailed = BudgetRuleDetailed(
            null,
            null,
            null,
        )
    }

    private fun populatePayDays() {
        val payDayAdapter = ArrayAdapter<Any>(
            requireContext(), R.layout.spinner_item_bold
        )
        payDayAdapter.setDropDownViewResource(
            R.layout.spinner_item_bold
        )
        budgetItemViewModel.getPayDays().observe(
            viewLifecycleOwner
        ) { payDayList ->
            payDayList?.forEach {
                payDayAdapter.add(it)
            }
        }
        binding.spPayDays.adapter = payDayAdapter
    }

    private fun populateFromCache() {
        if (mainViewModel.getBudgetItemDetailed() != null) {
            val mBudgetItemDetailed = mainViewModel.getBudgetItemDetailed()!!
            binding.apply {
                etProjectedDate.setText(
                    mBudgetItemDetailed.budgetItem?.biProjectedDate
                )
                mainViewModel.setTransferNum(0.0)
                if (mBudgetItemDetailed.budgetRule != null) {
                    val mBudgetRule = mBudgetItemDetailed.budgetRule!!
                    tvBudgetRule.text = mBudgetRule.budgetRuleName
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Default) {
                            budgetRuleDetailed = budgetRuleViewModel.getBudgetRuleDetailed(
                                mBudgetRule.ruleId
                            )
                        }
                        delay(WAIT_250)
                        if (mBudgetItemDetailed.toAccount != null) {
                            tvToAccount.text = mBudgetItemDetailed.toAccount!!.accountName
                            budgetRuleDetailed.toAccount = mBudgetItemDetailed.toAccount
                        } else {
                            tvToAccount.text = budgetRuleDetailed.toAccount?.accountName
                        }
                        if (mBudgetItemDetailed.fromAccount != null) {
                            tvFromAccount.text = mBudgetItemDetailed.fromAccount!!.accountName
                            budgetRuleDetailed.fromAccount = mBudgetItemDetailed.fromAccount
                        } else {
                            tvFromAccount.text = budgetRuleDetailed.fromAccount?.accountName
                        }
                        delay(WAIT_250)
                        if (mBudgetItemDetailed.budgetItem!!.biBudgetName.isEmpty()) {
                            etBudgetItemName.setText(
                                mBudgetRule.budgetRuleName
                            )
                        } else {
                            etBudgetItemName.setText(
                                mBudgetItemDetailed.budgetItem!!.biBudgetName
                            )
                        }

                        if (mBudgetItemDetailed.budgetItem!!.biProjectedAmount == 0.0) {
                            etProjectedAmount.setText(
                                nf.displayDollars(
                                    if (mainViewModel.getTransferNum()!! != 0.0) {
                                        mainViewModel.getTransferNum()!!
                                    } else {
                                        budgetRuleDetailed.budgetRule?.budgetAmount ?: 0.0
                                    }
                                )
                            )
                        } else {
                            etProjectedAmount.setText(
                                nf.displayDollars(
                                    if (mainViewModel.getTransferNum()!! != 0.0) {
                                        mainViewModel.getTransferNum()!!
                                    } else {
                                        mBudgetItemDetailed.budgetItem!!.biProjectedAmount
                                    }
                                )
                            )
                        }
                    }
                }
                if (mBudgetItemDetailed.toAccount != null && mBudgetItemDetailed.budgetRule == null) {
                    tvToAccount.text = mBudgetItemDetailed.toAccount!!.accountName
                    budgetRuleDetailed.toAccount = mBudgetItemDetailed.toAccount
                }
                if (mBudgetItemDetailed.fromAccount != null && mBudgetItemDetailed.budgetRule == null) {
                    tvFromAccount.text = mBudgetItemDetailed.fromAccount!!.accountName
                    budgetRuleDetailed.fromAccount = mBudgetItemDetailed.fromAccount
                }
                chkFixedAmount.isChecked = mBudgetItemDetailed.budgetItem!!.biIsFixed
                chkIsAutoPayment.isChecked = mBudgetItemDetailed.budgetItem!!.biIsAutomatic
                chkIsPayDay.isChecked = mBudgetItemDetailed.budgetItem!!.biIsPayDayItem
                chkIsLocked.isChecked = mBudgetItemDetailed.budgetItem!!.biLocked
                for (i in 0 until spPayDays.adapter.count) {
                    if (spPayDays.getItemAtPosition(i) == mBudgetItemDetailed.budgetItem!!.biPayDay) {
                        spPayDays.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    private fun populateDateToCurrent() {
        binding.apply {
            etProjectedDate.setText(df.getCurrentDateAsString())
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseAccount(REQUEST_TO_ACCOUNT)
            }
            tvFromAccount.setOnClickListener {
                chooseAccount(REQUEST_FROM_ACCOUNT)
            }
            etProjectedDate.setOnLongClickListener {
                chooseDate()
                false
            }
            etProjectedAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
        }
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveBudgetItemIfValid()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        gotoBudgetRulesChooseFragment()
    }

    private fun chooseAccount(requestedAccount: String) {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setRequestedAccount(requestedAccount)
        mainActivity.mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        gotoAccountChooseFragment()
    }

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etProjectedDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                mView.context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etProjectedDate.setText(display)
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_projected_date))
            datePickerDialog.show()
        }
    }

    private fun saveBudgetItemIfValid() {
        val message = validateBudgetItem()
        if (message == ANSWER_OK) {
            saveBudgetItem()
            gotoCallingFragment()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun validateBudgetItem(): String {
        binding.apply {
            if (etBudgetItemName.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name_or_description)
            }
            if (budgetRuleDetailed.toAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (budgetRuleDetailed.fromAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etProjectedAmount.text.isNullOrEmpty()) {
                return getString(R.string.please_enter_a_budgeted_amount_including_zero)
            }
            return ANSWER_OK
        }
    }

    private fun saveBudgetItem() {
        budgetItemViewModel.insertBudgetItem(
            getCurrentBudgetItemForSaving()
        )
    }

    private fun getCurrentBudgetItemDetailed(): BudgetItemDetailed {
        return BudgetItemDetailed(
            getCurrentBudgetItemForSaving(),
            mainViewModel.getBudgetItemDetailed()?.budgetRule,
            mainViewModel.getBudgetItemDetailed()?.toAccount,
            mainViewModel.getBudgetItemDetailed()?.fromAccount
        )
    }

    private fun getCurrentBudgetItemForSaving(): BudgetItem {
        binding.apply {
            return BudgetItem(
                budgetRuleDetailed.budgetRule?.ruleId ?: nf.generateId(),
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                budgetRuleDetailed.toAccount?.accountId ?: 0L,
                budgetRuleDetailed.fromAccount?.accountId ?: 0L,
                if (etProjectedAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(
                        etProjectedAmount.text.toString()
                    )
                } else {
                    0.0
                },
                biIsPending = false,
                chkFixedAmount.isChecked,
                chkIsAutoPayment.isChecked,
                biManuallyEntered = true,
                biLocked = true,
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.setBudgetItemDetailed(null)
        mainViewModel.removeCallingFragment(TAG)
        if (mainViewModel.getCallingFragments() != null) {
            val mCallingFragment = mainViewModel.getCallingFragments()!!
            if (mCallingFragment.contains(FRAG_BUDGET_VIEW)) {
                gotoBudgetViewFragment()
            }
            if (mCallingFragment.contains(FRAG_BUDGET_RULES)) {
                gotoBudgetRulesChooseFragment()
            }
        }
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etProjectedAmount.text.toString().ifBlank {
                    getString(R.string.zero_double)
                })
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoAccountChooseFragment() {
        mView.findNavController().navigate(
            BudgetItemAddFragmentDirections.actionBudgetItemAddFragmentToAccountChooseFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            BudgetItemAddFragmentDirections.actionBudgetItemAddFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetRulesChooseFragment() {
        mView.findNavController().navigate(
            BudgetItemAddFragmentDirections.actionBudgetItemAddFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            BudgetItemAddFragmentDirections.actionBudgetItemAddFragmentToBudgetViewFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}