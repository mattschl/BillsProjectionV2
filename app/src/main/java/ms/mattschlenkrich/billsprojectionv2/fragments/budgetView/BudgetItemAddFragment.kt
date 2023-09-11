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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
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
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel

private const val TAG = FRAG_BUDGET_ITEM_ADD

class BudgetItemAddFragment : Fragment(
    R.layout.fragment_budget_item_add
) {

    private var _binding: FragmentBudgetItemAddBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private val args: BudgetItemAddFragmentArgs by navArgs()

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
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        mainActivity.title = "Add a new Budget Item"
        fillPayDaysLive()
        fillMenu()
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
        }
        fillValues()
    }

    private fun fillPayDaysLive() {
        val payDayAdapter =
            ArrayAdapter<Any>(
                requireContext(),
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
        if (mes == "OK") {
            binding.apply {
                budgetItemViewModel.insertBudgetItem(
                    getCurBudgetItem()
                )
                gotoCallingFragment()
            }
        }
    }

    private fun gotoCallingFragment() {
        if (args.callingFragments.toString().contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            val fragmentChain = args.callingFragments!!
                .replace(",${TAG}", "")
            val direction = BudgetItemAddFragmentDirections
                .actionBudgetItemAddFragmentToBudgetViewFragment(
                    args.asset,
                    args.payDay,
                    fragmentChain
                )
            mView!!.findNavController().navigate(direction)
        }
    }

    private fun checkBudgetItem(): String {
        binding.apply {
            val errorMes =
                if (etBudgetItemName.text.isNullOrBlank()) {
                    "     Error!!\n" +
                            "Please enter a name or description"
                } else if (args.budgetItem!!.toAccount == null) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (args.budgetItem!!.fromAccount == null
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
                mView!!.context,
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
        val fragmentChain = TAG
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetItemAddFragmentToAccountsFragment(
                args.asset,
                args.payDay,
                getCurBudgetDetailed(),
                null,
                null,
                requestedAccount,
                fragmentChain
            )
        mView!!.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        val fragmentChain = TAG
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetItemAddFragmentToBudgetRuleFragment(
                args.asset,
                args.payDay,
                getCurBudgetDetailed(),
                null,
                fragmentChain
            )
        mView!!.findNavController().navigate(direction)
    }

    private fun getCurBudgetDetailed(): BudgetDetailed {
        return BudgetDetailed(
            getCurBudgetItem(),
            args.budgetItem?.budgetRule,
            args.budgetItem?.toAccount,
            args.budgetItem?.fromAccount
        )
    }

    private fun getCurBudgetItem(): BudgetItem {
        binding.apply {
            return BudgetItem(
                args.budgetItem?.budgetRule?.ruleId ?: 0L,
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                args.budgetItem?.toAccount?.accountId ?: 0L,
                args.budgetItem?.fromAccount?.accountId ?: 0L,
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
                biLocked = chkIsLocked.isChecked,
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                biUpdateTime = df.getCurrentTimeAsString()
            )
        }
    }

    private fun fillValues() {
        if (args.budgetItem != null) {
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
            etProjectedDate.setText(args.budgetItem!!.budgetItem!!.biProjectedDate)
            etBudgetItemName.setText(args.budgetItem?.budgetItem?.biBudgetName)
            etProjectedAmount.setText(
                cf.displayDollars(args.budgetItem!!.budgetItem!!.biProjectedAmount)
            )
            if (args.budgetItem!!.budgetRule != null) {
                tvBudgetRule.text = args.budgetItem!!.budgetRule!!.budgetRuleName
            }
            if (args.budgetItem!!.toAccount != null) {
                tvToAccount.text = args.budgetItem!!.toAccount!!.accountName
            }
            if (args.budgetItem!!.fromAccount != null) {
                tvFromAccount.text = args.budgetItem!!.fromAccount!!.accountName
            }
            chkFixedAmount.isChecked = args.budgetItem!!.budgetItem!!.biIsFixed
            chkIsAutoPayment.isChecked = args.budgetItem!!.budgetItem!!.biIsAutomatic
            chkIsPayDay.isChecked = args.budgetItem!!.budgetItem!!.biIsPayDayItem
            chkIsLocked.isChecked = args.budgetItem!!.budgetItem!!.biLocked
            for (i in 0 until spPayDays.adapter.count) {
                if (spPayDays.getItemAtPosition(i) ==
                    args.budgetItem!!.budgetItem!!.biPayDay
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