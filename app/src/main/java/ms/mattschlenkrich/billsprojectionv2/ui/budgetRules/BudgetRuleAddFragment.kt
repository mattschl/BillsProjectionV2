package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_BUDGET_RULE_ADD

class BudgetRuleAddFragment :
    Fragment(R.layout.fragment_budget_rule_add) {

    private var _binding: FragmentBudgetRuleAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mView: View
    private var budgetNameList: List<String>? = null

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_budget_rule)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        getBudgetRuleNameListForValidation()
        populateSpinners()
        binding.apply {
            if (mainActivity.mainViewModel.getBudgetRuleDetailed() != null) {
                populateFromCache()
            } else {
                populateDatesOnly()
            }
        }
    }

    private fun populateDatesOnly() {
        binding.apply {
            etStartDate.setText(df.getCurrentDateAsString())
            etEndDate.setText(df.getCurrentDateAsString())
        }
    }

    private fun populateFromCache() {
        binding.apply {
            if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule != null) {
                val budgetRuleDetailed = mainActivity.mainViewModel.getBudgetRuleDetailed()!!
                etBudgetName.setText(
                    budgetRuleDetailed.budgetRule!!.budgetRuleName
                )
                etAmount.setText(
                    nf.displayDollars(
                        if (mainActivity.mainViewModel.getTransferNum()!! != 0.0) {
                            mainActivity.mainViewModel.getTransferNum()!!
                        } else {
                            budgetRuleDetailed.budgetRule!!.budgetAmount
                        }
                    )
                )
                mainActivity.mainViewModel.setTransferNum(0.0)
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
                spDayOfWeek.setSelection(budgetRuleDetailed.budgetRule!!.budDayOfWeekId)
            }
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

    private fun getBudgetRuleNameListForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList = mainActivity.budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    private fun setClickActions() {
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
            etAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
        }
        setMenuActions()
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveBudgetRuleIfValid()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun chooseFromAccount() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainActivity.mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        gotoAccountsFragment()
    }

    private fun chooseToAccount() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainActivity.mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        gotoAccountsFragment()
    }

    private fun chooseEndDate() {
        binding.apply {
            val curDateAll = etEndDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                mView.context,
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etEndDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_final_date))
            datePickerDialog.show()
        }
    }

    private fun chooseStartDate() {
        binding.apply {
            val curDateAll = etStartDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etStartDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_first_date))
            datePickerDialog.show()
        }
    }

    private fun saveBudgetRuleIfValid() {
        val message = validateBudgetRule()
        if (message == ANSWER_OK) {
            binding.apply {
                mainActivity.mainViewModel.setCallingFragments(
                    "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
                )
                saveBudgetRule()
                gotoBudgetRuleFragment()
            }
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun getCurrentBudgetRuleForSave(): BudgetRule {
        binding.apply {
            return BudgetRule(
                nf.generateId(),
                etBudgetName.text.toString().trim(),
                mainActivity.mainViewModel.getBudgetRuleDetailed()?.toAccount?.accountId ?: 0L,
                mainActivity.mainViewModel.getBudgetRuleDetailed()?.fromAccount?.accountId ?: 0L,
                if (etAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(
                        etAmount.text.toString().trim()
                    )
                } else {
                    0.0
                },
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
        return BudgetRuleDetailed(
            getCurrentBudgetRuleForSave(),
            mainActivity.mainViewModel.getBudgetRuleDetailed()?.toAccount,
            mainActivity.mainViewModel.getBudgetRuleDetailed()?.fromAccount
        )
    }

    private fun validateBudgetRule(): String {
        binding.apply {
            if (etBudgetName.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name)
            }
            for (i in 0 until budgetNameList!!.size) {
                if (budgetNameList!![i] ==
                    etBudgetName.text.toString().trim()
                ) {
                    return getString(R.string.this_budget_rule_already_exists)
                }
            }
            if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount == null
            ) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount == null
            ) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etAmount.text.isNullOrEmpty()) {
                return getString(R.string.please_enter_a_budgeted_amount_including_zero)
            }
            return ANSWER_OK
        }
    }

    private fun saveBudgetRule() {
        mainActivity.budgetRuleViewModel.insertBudgetRule(getCurrentBudgetRuleForSave())
    }

    private fun gotoCalculator() {
        mainActivity.mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    getString(R.string.zero_double)
                }
            )
        )
        mainActivity.mainViewModel.setReturnTo(TAG)
        mainActivity.mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToAccountsFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetRuleFragment() {
        mView.findNavController().navigate(
            BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToBudgetRuleFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
