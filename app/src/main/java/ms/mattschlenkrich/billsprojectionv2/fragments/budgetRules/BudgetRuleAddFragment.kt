package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

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
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

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

    private val cf = CommonFunctions()
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
        mainViewModel =
            mainActivity.mainViewModel
        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            mainActivity.budgetRuleViewModel
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList =
                budgetRuleViewModel.getBudgetRuleNameList()
        }
        mainActivity.title = "Add a new Budget Rule"
        fillMenu()
        fillValues()

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
                gotoCalc()
                false
            }
        }
    }

    private fun gotoCalc() {
        mainViewModel.setTransferNum(
            cf.getDoubleFromDollars(
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

    private fun fillMenu() {
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
                        saveBudgetRule()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun getCurBudgetRule(): BudgetRule {
        binding.apply {
            return BudgetRule(
                cf.generateId(),
                etBudgetName.text.toString().trim(),
                mainViewModel.getBudgetRuleDetailed()?.toAccount?.accountId ?: 0L,
                mainViewModel.getBudgetRuleDetailed()?.fromAccount?.accountId ?: 0L,
                if (etAmount.text.isNotEmpty()) {
                    cf.getDoubleFromDollars(
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
            getCurBudgetRule(),
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

    private fun fillValues() {
        fillSpinners()
        binding.apply {
            if (mainViewModel.getBudgetRuleDetailed() != null) {
                if (mainViewModel.getBudgetRuleDetailed()!!.budgetRule != null) {
                    etBudgetName.setText(
                        mainViewModel.getBudgetRuleDetailed()?.budgetRule?.budgetRuleName
                    )
                    etAmount.setText(
                        cf.displayDollars(
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
            } else {
                etStartDate.setText(df.getCurrentDateAsString())
                etEndDate.setText(df.getCurrentDateAsString())
            }
        }
    }

    private fun fillSpinners() {
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

    private fun saveBudgetRule() {
        val mes = checkBudgetRule()
        if (mes == "Ok") {
            binding.apply {
                mainViewModel.setCallingFragments(
                    "${mainViewModel.getCallingFragments()}, $TAG"
                )
                budgetRuleViewModel.insertBudgetRule(
                    getCurBudgetRule()
                )
                gotoCallingFragment()
            }
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoCallingFragment() {
        val direction = BudgetRuleAddFragmentDirections
            .actionBudgetRuleAddFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun checkBudgetRule(): String {
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
