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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter.BudgetRuleDatesAdapter

private const val TAG = FRAG_BUDGET_RULE_UPDATE

class BudgetRuleUpdateFragment :
    Fragment(R.layout.fragment_budget_rule_update) {

    private var _binding: FragmentBudgetRuleUpdateBinding? = null
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
        _binding = FragmentBudgetRuleUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update Budget Rule"
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        createClickActions()
    }

    private fun getBudgetRuleNameForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList = mainActivity.budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    private fun createClickActions() {
        setMenuActions()
        binding.apply {
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
            }
            etStartDate.setOnLongClickListener {
                chooseStartDate()
                false
            }
            etEndDate.setOnLongClickListener {
                chooseEndDate()
                false
            }
            fabUpdateDone.setOnClickListener {
                isBudgetRuleReadyToUpdate()
            }
            etAmount.setOnLongClickListener {
                gotoCalculatorFragment()
                false
            }
        }
    }

    private fun gotoCalculatorFragment() {
        mainActivity.mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainActivity.mainViewModel.setReturnTo(TAG)
        mainActivity.mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToCalcFragment()
        )
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        chooseToDeleteBudgetRule()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseEndDate() {
        binding.apply {
            val curDateAll = etEndDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etEndDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the final date")
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
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etStartDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the first date")
            datePickerDialog.show()
        }
    }

    private fun getCurrentBudgetRuleForSaving(): BudgetRule {
        val toAccId =
            if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount == null) {
                0L
            } else {
                mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount!!.accountId
            }
        val fromAccId =
            if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount == null) {
                0L
            } else {
                mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount!!.accountId
            }
        binding.apply {
            return BudgetRule(
                mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.ruleId,
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
            val toAccount =
                if (mainActivity.mainViewModel.getBudgetRuleDetailed() != null) {
                    if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount != null) {
                        mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount
                    } else {
                        null
                    }
                } else {
                    null
                }
            val fromAccount =
                if (mainActivity.mainViewModel.getBudgetRuleDetailed() != null) {
                    if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount != null) {
                        mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount
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
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setBudgetRuleDetailed(
            getBudgetRuleDetailed()
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        val direction =
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setBudgetRuleDetailed(
            getBudgetRuleDetailed()
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        val direction = BudgetRuleUpdateFragmentDirections
            .actionBudgetRuleUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun populateValues() {
        getBudgetRuleNameForValidation()
        populateSpinners()
        if (mainActivity.mainViewModel.getBudgetRuleDetailed() != null) {
            populateFromCache()
        } else {
            populateDatesOnly()
        }
        populateDateRecycler(mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.ruleId)
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
                etBudgetName.setText(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budgetRuleName
                )
                etAmount.setText(
                    nf.displayDollars(
                        if (mainActivity.mainViewModel.getTransferNum()!! != 0.0) {
                            mainActivity.mainViewModel.getTransferNum()!!
                        } else {
                            mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budgetAmount
                        }
                    )
                )
                mainActivity.mainViewModel.setTransferNum(0.0)
                if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount != null) {
                    tvToAccount.text =
                        mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount!!.accountName
                }
                if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount != null) {
                    tvFromAccount.text =
                        mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount!!.accountName
                }
                chkFixedAmount.isChecked =
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budFixedAmount
                chkMakePayDay.isChecked =
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budIsPayDay
                chkAutoPayment.isChecked =
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budIsAutoPay
                etStartDate.setText(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budStartDate
                )
                etEndDate.setText(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budEndDate
                )
                spFrequencyType.setSelection(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budFrequencyTypeId
                )
                etFrequencyCount.setText(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budFrequencyCount.toString()
                )
                spDayOfWeek.setSelection(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budDayOfWeekId
                )
                etLeadDays.setText(
                    mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budLeadDays.toString()
                )
            }
        }
    }

    private fun populateDateRecycler(budgetRuleId: Long) {
        val budgetRuleDatesAdapter = BudgetRuleDatesAdapter(mainActivity.mainViewModel, mView)
        binding.rvProjectedDates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetRuleDatesAdapter
        }
        activity?.let {
            mainActivity.budgetItemViewModel.getBudgetItems(budgetRuleId).observe(
                viewLifecycleOwner
            ) { budgetItems ->
                budgetRuleDatesAdapter.differ.submitList(budgetItems)
            }
        }
    }

    private fun populateSpinners() {
        val adapterFrequencyType =
            ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                resources.getStringArray(R.array.frequency_types)
            )
        adapterFrequencyType.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spFrequencyType.adapter = adapterFrequencyType

        val adapterDayOfWeek =
            ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                resources.getStringArray(R.array.days_of_week)
            )
        adapterDayOfWeek.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spDayOfWeek.adapter = adapterDayOfWeek
    }

    private fun chooseToDeleteBudgetRule() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Budget Rule")
            setMessage("Are you sure you want to delete this budget rule?")
            setPositiveButton("Delete") { _, _ ->
                deleteBudgeRule()
                gotoCallingFragment()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun deleteBudgeRule() {
        mainActivity.budgetRuleViewModel.deleteBudgetRule(
            mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.ruleId,
            df.getCurrentTimeAsString()
        )
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", TAG", "")
        )
        mainActivity.mainViewModel.setBudgetRuleDetailed(null)
        if (mainActivity.mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_RULES)
        ) {
            gotoBudgetRuleFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            gotoBudgetViewFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_LIST
            )
        ) {
            gotoBudgetListFragment()
        }
    }

    private fun gotoBudgetListFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToBudgetListFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoBudgetRuleFragment() {
        mView.findNavController().navigate(
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToBudgetRuleFragment()
        )
    }

    private fun isBudgetRuleReadyToUpdate() {
        val mes = validateBudgetRule()
        if (mes == "Ok") {
            updateBudgetRule()
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateBudgetRule() {
        mainActivity.budgetRuleViewModel.updateBudgetRule(
            getCurrentBudgetRuleForSaving()
        )
    }

    private fun validateBudgetRule(): String {
        binding.apply {
            val nameIsBlank = etBudgetName.text.isNullOrBlank()
            var nameFound = false
            if (budgetNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until budgetNameList!!.size) {
                    if (budgetNameList!![i] ==
                        etBudgetName.text.toString() &&
                        mainActivity.mainViewModel.getBudgetRuleDetailed() != null &&
                        budgetNameList!![i] !=
                        mainActivity.mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budgetRuleName
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMes = if (nameIsBlank) {
                "     Error!!\n" +
                        "Please enter a name"
            } else if (nameFound) {
                "     Error!!\n" +
                        "This budget rule already exists."
            } else if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.toAccount == null
            ) {
                "     Error!!\n" +
                        "There needs to be an account money will go to."
            } else if (mainActivity.mainViewModel.getBudgetRuleDetailed()!!.fromAccount == null
            ) {
                "     Error!!\n" +
                        "There needs to be an account money will come from."
            } else if (etAmount.text.isNullOrEmpty()) {
                "     Error!!\n" +
                        "Please enter a budget amount (including zero)"
            } else {
                "Ok"
            }
            return errorMes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}