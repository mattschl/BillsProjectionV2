package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_BUDGET_ITEM_ADD

class BudgetItemAddFragment : Fragment(
    R.layout.fragment_budget_item_add
) {

    private var _binding: FragmentBudgetItemAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetRuleDetailed: BudgetRuleDetailed

    private val cf = CommonFunctions()
    private val df = DateFunctions()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetItemAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        budgetRuleViewModel =
            mainActivity.budgetRuleViewModel
        mainActivity.title = "Add a new Budget Item"
        fillPayDaysLive()
        createMenu()
        createActions()
        budgetRuleDetailed =
            BudgetRuleDetailed(
                null,
                null,
                null
            )
        fillValues()
    }

    private fun createActions() {
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
                gotoCalc()
                false
            }
        }
    }

    private fun gotoCalc() {
        mainViewModel.setTransferNum(
            cf.getDoubleFromDollars(
                binding.etProjectedAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setBudgetItem(getCurBudgetDetailed())
        mView.findNavController().navigate(
            BudgetItemAddFragmentDirections
                .actionBudgetItemAddFragmentToCalcFragment()
        )
    }

    private fun fillPayDaysLive() {
        val payDayAdapter =
            ArrayAdapter<Any>(
                requireContext(),
                R.layout.spinner_item_bold
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

    private fun createMenu() {
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
                        saveBudgetItem()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveBudgetItem() {
        val mes = checkBudgetItem()
        if (mes == "Ok") {
            budgetItemViewModel.insertBudgetItem(
                getCurBudgetItem()
            )
            gotoCallingFragment()
        } else {
            Toast.makeText(
                requireContext(),
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        if (mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            val direction = BudgetItemAddFragmentDirections
                .actionBudgetItemAddFragmentToBudgetViewFragment()
            mView.findNavController().navigate(direction)
        }
    }

    private fun checkBudgetItem(): String {
        binding.apply {
            val errorMes =
                if (etBudgetItemName.text.isNullOrBlank()) {
                    "     Error!!\n" +
                            "Please enter a name or description"
                } else if (budgetRuleDetailed.toAccount == null) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (budgetRuleDetailed.fromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etProjectedAmount.text.isNullOrEmpty()
                ) {
                    "     Error!!\n" +
                            "Please enter a budget amount (including zero)"
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etProjectedDate.text.toString()
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
                    etProjectedDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_the_projected_date))
            datePickerDialog.show()
        }
    }

    private fun chooseAccount(requestedAccount: String) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainViewModel.setRequestedAccount(requestedAccount)
        mainViewModel.setBudgetItem(getCurBudgetDetailed())
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetItemAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainViewModel.setBudgetItem(getCurBudgetDetailed())
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetItemAddFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun getCurBudgetDetailed(): BudgetDetailed {
        return BudgetDetailed(
            getCurBudgetItem(),
            mainViewModel.getBudgetItem()?.budgetRule,
            mainViewModel.getBudgetItem()?.toAccount,
            mainViewModel.getBudgetItem()?.fromAccount
        )
    }

    private fun getCurBudgetItem(): BudgetItem {
        binding.apply {
            return BudgetItem(
                budgetRuleDetailed.budgetRule?.ruleId
                    ?: cf.generateId(),
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                budgetRuleDetailed.toAccount?.accountId ?: 0L,
                budgetRuleDetailed.fromAccount?.accountId ?: 0L,
                if (etProjectedAmount.text.isNotEmpty()) {
                    cf.getDoubleFromDollars(
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

    private fun fillValues() {
        if (mainViewModel.getBudgetItem() != null) {
            fillFromTemp()
        } else {
            fillFromBlank()
        }
    }

    private fun fillFromBlank() {
        binding.apply {
            etProjectedDate.setText(df.getCurrentDateAsString())
        }
    }

    private fun fillFromTemp() {
        binding.apply {
            etProjectedDate.setText(
                mainViewModel.getBudgetItem()?.budgetItem?.biProjectedDate
            )
            if (mainViewModel.getBudgetItem()!!.budgetRule != null) {
                tvBudgetRule.text =
                    mainViewModel.getBudgetItem()!!.budgetRule!!.budgetRuleName
                CoroutineScope(Dispatchers.IO).launch {
                    val mBudgetRuleDetailed =
                        async {
                            budgetRuleViewModel.getBudgetRuleDetailed(
                                mainViewModel.getBudgetItem()!!.budgetRule!!.ruleId
                            )
                        }
                    budgetRuleDetailed.budgetRule =
                        mainViewModel.getBudgetItem()!!.budgetRule
                    budgetRuleDetailed =
                        mBudgetRuleDetailed.await()
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                if (mainViewModel.getBudgetItem()!!.budgetItem!!.biBudgetName.isEmpty()) {
                    etBudgetItemName.setText(
                        budgetRuleDetailed.budgetRule?.budgetRuleName
                    )
                } else {
                    etBudgetItemName.setText(
                        mainViewModel.getBudgetItem()?.budgetItem?.biBudgetName
                    )
                }
                if (mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedAmount == 0.0) {
                    etProjectedAmount.setText(
                        cf.displayDollars(
                            if (mainViewModel.getTransferNum()!! != 0.0) {
                                mainViewModel.getTransferNum()!!
                            } else {
                                budgetRuleDetailed.budgetRule?.budgetAmount ?: 0.0
                            }
                        )
                    )
                } else {
                    etProjectedAmount.setText(
                        cf.displayDollars(
                            if (mainViewModel.getTransferNum()!! != 0.0) {
                                mainViewModel.getTransferNum()!!
                            } else {
                                mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedAmount
                            }
                        )
                    )
                }
                mainViewModel.setTransferNum(0.0)
                if (mainViewModel.getBudgetItem()!!.toAccount != null) {
                    tvToAccount.text =
                        mainViewModel.getBudgetItem()!!.toAccount!!.accountName
                    budgetRuleDetailed.toAccount =
                        mainViewModel.getBudgetItem()!!.toAccount
                } else {
                    tvToAccount.text =
                        budgetRuleDetailed.toAccount?.accountName
                    budgetRuleDetailed.toAccount =
                        budgetRuleDetailed.toAccount
                }
                if (mainViewModel.getBudgetItem()!!.fromAccount != null) {
                    tvFromAccount.text =
                        mainViewModel.getBudgetItem()!!.fromAccount!!.accountName
                    budgetRuleDetailed.fromAccount =
                        mainViewModel.getBudgetItem()!!.fromAccount
                } else {
                    tvFromAccount.text =
                        budgetRuleDetailed.fromAccount?.accountName
                    budgetRuleDetailed.fromAccount =
                        budgetRuleDetailed.fromAccount
                }
            }
            chkFixedAmount.isChecked =
                mainViewModel.getBudgetItem()!!.budgetItem!!.biIsFixed
            chkIsAutoPayment.isChecked =
                mainViewModel.getBudgetItem()!!.budgetItem!!.biIsAutomatic
            chkIsPayDay.isChecked =
                mainViewModel.getBudgetItem()!!.budgetItem!!.biIsPayDayItem
            chkIsLocked.isChecked =
                mainViewModel.getBudgetItem()!!.budgetItem!!.biLocked
            for (i in 0 until spPayDays.adapter.count) {
                if (spPayDays.getItemAtPosition(i) ==
                    mainViewModel.getBudgetItem()!!.budgetItem!!.biPayDay
                ) {
                    spPayDays.setSelection(i)
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}