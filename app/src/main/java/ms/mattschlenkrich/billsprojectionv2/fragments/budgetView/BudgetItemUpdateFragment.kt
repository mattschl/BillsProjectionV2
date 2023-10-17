package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.app.AlertDialog
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

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

    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Entering $TAG")
        _binding = FragmentBudgetItemUpdateBinding.inflate(
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
        mainActivity.title = "Update this Budget Item"
        createMenu()
        mBudgetRuleDetailed =
            BudgetRuleDetailed(
                null,
                null,
                null
            )
        fillPayDaysLive()
        fillValues()
        createActions()
    }

    private fun createActions() {
        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseAccount(REQUEST_TO_ACCOUNT)
            }
            tvBiFromAccount.setOnClickListener {
                chooseAccount(REQUEST_FROM_ACCOUNT)
            }
            etProjectedDate.setOnLongClickListener {
                chooseDate()
                false
            }
            fabUpdateDone.setOnClickListener {
                updateBudgetItem()
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
        mainViewModel.setBudgetItem(getCurBudgetItemDetailed())
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections
                .actionBudgetItemUpdateFragmentToCalcFragment()
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

    private fun fillValues() {
        if (mainViewModel.getBudgetItem() != null) {
            binding.apply {
                val curBudgetItem = mainViewModel.getBudgetItem()!!
                etProjectedDate.setText(
                    curBudgetItem.budgetItem!!.biActualDate
                )
                etBudgetItemName.setText(
                    curBudgetItem.budgetItem.biBudgetName
                )
                tvBudgetRule.text =
                    curBudgetItem.budgetRule?.budgetRuleName
                mBudgetRuleDetailed.budgetRule =
                    curBudgetItem.budgetRule
                etProjectedAmount.setText(
                    cf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            curBudgetItem.budgetItem.biProjectedAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                tvToAccount.text =
                    curBudgetItem.toAccount?.accountName
                mBudgetRuleDetailed.toAccount =
                    curBudgetItem.toAccount
                tvBiFromAccount.text =
                    curBudgetItem.fromAccount?.accountName
                mBudgetRuleDetailed.fromAccount =
                    curBudgetItem.fromAccount
                chkFixedAmount.isChecked =
                    curBudgetItem.budgetItem.biIsFixed
                chkIsAutoPayment.isChecked =
                    curBudgetItem.budgetItem.biIsAutomatic
                chkIsPayDay.isChecked =
                    curBudgetItem.budgetItem.biIsPayDayItem
                chkIsLocked.isChecked =
                    curBudgetItem.budgetItem.biLocked
                CoroutineScope(Dispatchers.Main).launch {
                    delay(250)
                    for (i in 0 until spPayDays.adapter.count) {
                        if (spPayDays.getItemAtPosition(i) ==
                            curBudgetItem.budgetItem.biPayDay
                        ) {
                            spPayDays.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun createMenu() {
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
                        deleteBudgetItem()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateBudgetItem() {
        val mess = checkBudgetItem()
        if (mess == ANSWER_OK) {
            budgetItemViewModel.updateBudgetItem(
                getCurBudgetItem()
            )
            gotoCallingFragment()
        } else {
            Toast.makeText(
                requireContext(),
                mess,
                Toast.LENGTH_LONG
            ).show()
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
        mainViewModel.setBudgetItem(getCurBudgetItemDetailed())
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainViewModel.setBudgetItem(getCurBudgetItemDetailed())
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun getCurBudgetItemDetailed(): BudgetDetailed {
        binding.apply {
            val budgetItem =
                getCurBudgetItem()
            return BudgetDetailed(
                budgetItem,
                mainViewModel.getBudgetItem()!!.budgetRule,
                mainViewModel.getBudgetItem()!!.toAccount,
                mainViewModel.getBudgetItem()!!.fromAccount
            )
        }
    }

    private fun getCurBudgetItem(): BudgetItem {
        binding.apply {
            return BudgetItem(
                if (mainViewModel.getBudgetItem()!!.budgetRule != null)
                    mainViewModel.getBudgetItem()!!.budgetRule!!.ruleId else 0L,
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                mainViewModel.getBudgetItem()!!.toAccount?.accountId ?: 0L,
                mainViewModel.getBudgetItem()!!.fromAccount?.accountId ?: 0L,
                cf.getDoubleFromDollars(etProjectedAmount.text.toString()),
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
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Budget Item")
            setMessage("Are you sure you want to delete this budget item?")
            setPositiveButton("Delete") { _, _ ->
                binding.apply {
                    budgetItemViewModel.deleteBudgetItem(
                        mainViewModel.getBudgetItem()!!.budgetItem!!.biRuleId,
                        mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedDate,
                        df.getCurrentTimeAsString()
                    )

                }
                gotoCallingFragment()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments()!!
                    .replace(", $TAG", "")
            )
            val direction = BudgetItemUpdateFragmentDirections
                .actionBudgetItemUpdateFragmentToBudgetViewFragment()
            mView.findNavController().navigate(direction)
        }
    }

    private fun checkBudgetItem(): String {
        binding.apply {
            val errorMes =
                if (etBudgetItemName.text.isNullOrBlank()) {
                    "     Error!!\n" +
                            "Please enter a name or description"
                } else if (mBudgetRuleDetailed.toAccount == null) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (mBudgetRuleDetailed.fromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etProjectedAmount.text.isNullOrEmpty()
                ) {
                    "     Error!!\n" +
                            "Please enter a budget amount (including zero)"
                } else {
                    ANSWER_OK
                }
            return errorMes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}