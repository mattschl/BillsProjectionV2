package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_BUDGET_ITEM_UPDATE

class BudgetItemUpdateFragment : Fragment(
    R.layout.fragment_budget_item_update
) {

    private var _binding: FragmentBudgetItemUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var mBudgetRuleDetailed: BudgetRuleDetailed
    private lateinit var curBudgetItem: BudgetItemDetailed

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetItemUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        mainActivity.title = getString(R.string.update_this_budget_item)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBudgetRuleDetailedToBlank()
        populateValues()
        setClickActions()
    }

    private fun setBudgetRuleDetailedToBlank() {
        mBudgetRuleDetailed = BudgetRuleDetailed(
            null, null, null
        )
    }

    private fun populateValues() {
        populatePayDaySpinner()
        if (mainViewModel.getBudgetItemDetailed() != null) {
            curBudgetItem = mainViewModel.getBudgetItemDetailed()!!
            binding.apply {
                etProjectedDate.setText(curBudgetItem.budgetItem?.biActualDate)
                etBudgetItemName.setText(curBudgetItem.budgetItem?.biBudgetName)
                tvBudgetRule.text = curBudgetItem.budgetRule?.budgetRuleName
                mBudgetRuleDetailed.budgetRule = curBudgetItem.budgetRule
                etProjectedAmount.setText(
                    nf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            curBudgetItem.budgetItem!!.biProjectedAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                tvToAccount.text = curBudgetItem.toAccount?.accountName
                mBudgetRuleDetailed.toAccount = curBudgetItem.toAccount
                tvBiFromAccount.text = curBudgetItem.fromAccount?.accountName
                mBudgetRuleDetailed.fromAccount = curBudgetItem.fromAccount
                chkFixedAmount.isChecked = curBudgetItem.budgetItem!!.biIsFixed
                chkIsAutoPayment.isChecked = curBudgetItem.budgetItem!!.biIsAutomatic
                chkIsPayDay.isChecked = curBudgetItem.budgetItem!!.biIsPayDayItem
                chkIsLocked.isChecked = curBudgetItem.budgetItem!!.biLocked
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    for (i in 0 until spPayDays.adapter.count) {
                        if (spPayDays.getItemAtPosition(i) == curBudgetItem.budgetItem!!.biPayDay) {
                            spPayDays.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun populatePayDaySpinner() {
        val payDayAdapter = ArrayAdapter<Any>(
            requireContext(), R.layout.spinner_item_bold
        )
        payDayAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
        budgetItemViewModel.getPayDays().observe(
            viewLifecycleOwner
        ) { payDayList ->
            payDayList?.forEach {
                payDayAdapter.add(it)
            }
        }
        binding.spPayDays.adapter = payDayAdapter
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvBudgetRule.setOnClickListener { chooseBudgetRule() }
            tvToAccount.setOnClickListener { chooseAccount(REQUEST_TO_ACCOUNT) }
            tvBiFromAccount.setOnClickListener { chooseAccount(REQUEST_FROM_ACCOUNT) }
            etProjectedDate.setOnLongClickListener {
                chooseDate()
                false
            }
            etProjectedAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
            fabUpdateDone.setOnClickListener { updateBudgetItemIfValid() }
        }
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        confirmDeleteBudgetItem()
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
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        gotoAccountsFragment()
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

    private fun updateBudgetItemIfValid() {
        val message = validateBudgetItem()
        if (message == ANSWER_OK) {
            updateBudgetItem()
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
            if (mBudgetRuleDetailed.toAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (mBudgetRuleDetailed.fromAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etProjectedAmount.text.isNullOrEmpty()) {
                return getString(R.string.please_enter_a_budgeted_amount_including_zero)
            }
            return ANSWER_OK
        }
    }

    private fun updateBudgetItem() {
        budgetItemViewModel.updateBudgetItem(getCurrentBudgetItemForUpdating())
    }

    private fun getCurrentBudgetItemDetailed(): BudgetItemDetailed {
        val budgetItem = getCurrentBudgetItemForUpdating()
        return BudgetItemDetailed(
            budgetItem, curBudgetItem.budgetRule, curBudgetItem.toAccount, curBudgetItem.fromAccount
        )
    }

    private fun getCurrentBudgetItemForUpdating(): BudgetItem {
        binding.apply {
            return BudgetItem(
                curBudgetItem.budgetRule?.ruleId ?: 0L,
                curBudgetItem.budgetItem!!.biProjectedDate,
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                curBudgetItem.toAccount?.accountId ?: 0L,
                curBudgetItem.fromAccount?.accountId ?: 0L,
                nf.getDoubleFromDollars(etProjectedAmount.text.toString()),
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

    private fun deleteBudgetItem() {
        binding.apply {
            budgetItemViewModel.deleteBudgetItem(
                curBudgetItem.budgetItem!!.biRuleId,
                curBudgetItem.budgetItem!!.biProjectedDate,
                df.getCurrentTimeAsString()
            )

        }
    }

    private fun confirmDeleteBudgetItem() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.delete_budget_item))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_budget_item))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteBudgetItem()
                gotoCallingFragment()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etProjectedAmount.text.toString().ifBlank {
                    "0.0"
                })
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setBudgetItemDetailed(getCurrentBudgetItemDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setBudgetItemDetailed(null)

        if (mainViewModel.getCallingFragments() != null) {
            val mCallingFragment = mainViewModel.getCallingFragments()!!
            if (mCallingFragment.contains(FRAG_BUDGET_VIEW)) {
                mainActivity.mainViewModel.setCallingFragments(
                    mainActivity.mainViewModel.getCallingFragments()!!.replace(", $TAG", "")
                )
                gotoBudgetViewFragment()
            } else if (mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)) {
                mainActivity.mainViewModel.setCallingFragments(
                    mainActivity.mainViewModel.getCallingFragments()!!.replace(", $TAG", "")
                )
                gotoBudgetRuleUpdateFragment()
            }
        }
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToAccountsFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun gotoBudgetRulesChooseFragment() {
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections.actionBudgetItemUpdateFragmentToBudgetRuleChooseFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}