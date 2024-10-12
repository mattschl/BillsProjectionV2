package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_BUDGET_RULE_ADD

class BudgetRuleAddFragment :
    Fragment(R.layout.fragment_budget_rule_add) {

    private var _binding: FragmentBudgetRuleAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
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
        mainActivity.title = "Add a new Budget Rule"
        mainViewModel =
            mainActivity.mainViewModel
        budgetRuleViewModel =
            mainActivity.budgetRuleViewModel
        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBudgetRuleNameListForValidation()
        createMenuActions()
        populateValues()
        createClickActions()
    }

    private fun getBudgetRuleNameListForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList =
                budgetRuleViewModel.getBudgetRuleNameList()
        }
    }

    private fun createClickActions() {
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
            etAmount.setOnLongClickListener {
                gotoCalculatorFragment()
                false
            }
        }
    }

    private fun gotoCalculatorFragment() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        mView.findNavController().navigate(
            BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToCalcFragment()
        )
    }

    private fun createMenuActions() {
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
                        isBudgetRuleReadyToSave()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun getCurrentBudgetRuleForSave(): BudgetRule {
        binding.apply {
            return BudgetRule(
                nf.generateId(),
                etBudgetName.text.toString().trim(),
                mainViewModel.getBudgetRuleDetailed()?.toAccount?.accountId ?: 0L,
                mainViewModel.getBudgetRuleDetailed()?.fromAccount?.accountId ?: 0L,
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
            mainViewModel.getBudgetRuleDetailed()?.toAccount,
            mainViewModel.getBudgetRuleDetailed()?.fromAccount
        )
    }

    private fun chooseFromAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        val direction = BudgetRuleAddFragmentDirections
            .actionBudgetRuleAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setBudgetRuleDetailed(getBudgetRuleDetailed())
        val direction = BudgetRuleAddFragmentDirections
            .actionBudgetRuleAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
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
            datePickerDialog.setTitle("Choose the first date")
            datePickerDialog.show()
        }
    }

    private fun populateValues() {
        populateSpinners()
        binding.apply {
            if (mainViewModel.getBudgetRuleDetailed() != null) {
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
            if (mainViewModel.getBudgetRuleDetailed()!!.budgetRule != null) {
                etBudgetName.setText(
                    mainViewModel.getBudgetRuleDetailed()?.budgetRule?.budgetRuleName
                )
                etAmount.setText(
                    nf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budgetAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                if (mainViewModel.getBudgetRuleDetailed()!!.toAccount != null) {
                    tvToAccount.text =
                        mainViewModel.getBudgetRuleDetailed()!!.toAccount!!.accountName
                }
                if (mainViewModel.getBudgetRuleDetailed()!!.fromAccount != null) {
                    tvFromAccount.text =
                        mainViewModel.getBudgetRuleDetailed()!!.fromAccount!!.accountName
                }
                chkFixedAmount.isChecked =
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budFixedAmount
                chkMakePayDay.isChecked =
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budIsPayDay
                chkAutoPayment.isChecked =
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budIsAutoPay
                etStartDate.setText(
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budStartDate
                )
                etEndDate.setText(
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budEndDate
                )
                spFrequencyType.setSelection(
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budFrequencyTypeId
                )
                spDayOfWeek.setSelection(
                    mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!.budDayOfWeekId
                )
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

    private fun isBudgetRuleReadyToSave() {
        val mes = validateBudgetRule()
        if (mes == "Ok") {
            binding.apply {
                mainViewModel.setCallingFragments(
                    "${mainViewModel.getCallingFragments()}, $TAG"
                )
                saveBudgetRule()
                gotoBudgetRuleFragment()
            }
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveBudgetRule() {
        budgetRuleViewModel.insertBudgetRule(
            getCurrentBudgetRuleForSave()
        )
    }

    private fun gotoBudgetRuleFragment() {
        val direction = BudgetRuleAddFragmentDirections
            .actionBudgetRuleAddFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun validateBudgetRule(): String {
        binding.apply {
            val nameIsBlank = etBudgetName.text.isNullOrBlank()
            var nameFound = false
            if (budgetNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until budgetNameList!!.size) {
                    if (budgetNameList!![i] ==
                        etBudgetName.text.toString().trim()
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
            } else if (mainViewModel.getBudgetRuleDetailed()!!.toAccount == null
            ) {
                "     Error!!\n" +
                        "There needs to be an account money will go to."
            } else if (mainViewModel.getBudgetRuleDetailed()!!.fromAccount == null
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
