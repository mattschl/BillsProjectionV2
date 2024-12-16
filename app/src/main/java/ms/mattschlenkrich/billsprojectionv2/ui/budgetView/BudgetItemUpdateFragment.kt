package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
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
    private lateinit var mBudgetRuleDetailed: BudgetRuleDetailed

    private val nf = NumberFunctions()
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
        mainActivity.title = "Update this Budget Item"
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
        mBudgetRuleDetailed =
            BudgetRuleDetailed(
                null,
                null,
                null
            )
    }

    private fun populateValues() {
        populatePayDaySpinner()
        if (mainActivity.mainViewModel.getBudgetItem() != null) {
            binding.apply {
                val curBudgetItem = mainActivity.mainViewModel.getBudgetItem()!!
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
                    nf.displayDollars(
                        if (mainActivity.mainViewModel.getTransferNum()!! != 0.0) {
                            mainActivity.mainViewModel.getTransferNum()!!
                        } else {
                            curBudgetItem.budgetItem.biProjectedAmount
                        }
                    )
                )
                mainActivity.mainViewModel.setTransferNum(0.0)
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

    private fun populatePayDaySpinner() {
        val payDayAdapter =
            ArrayAdapter<Any>(
                requireContext(),
                R.layout.spinner_item_bold
            )
        payDayAdapter.setDropDownViewResource(
            R.layout.spinner_item_bold
        )
        mainActivity.budgetItemViewModel.getPayDays().observe(
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
                isBudgetItemReadyToUpdate()
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
                // Add menu items here
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        chooseToDeleteBudgetItem()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainActivity.mainViewModel.setBudgetItem(getCurrentBudgetItemDetailed())
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseAccount(requestedAccount: String) {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainActivity.mainViewModel.setRequestedAccount(requestedAccount)
        mainActivity.mainViewModel.setBudgetItem(getCurrentBudgetItemDetailed())
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
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

    private fun isBudgetItemReadyToUpdate() {
        val mess = validateBudgetItem()
        if (mess == ANSWER_OK) {
            updateBudgetItem()
            gotoCallingFragment()
        } else {
            Toast.makeText(
                requireContext(),
                mess,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateBudgetItem() {
        mainActivity.budgetItemViewModel.updateBudgetItem(
            getCurrentBudgetItemForUpdating()
        )
    }

    private fun getCurrentBudgetItemDetailed(): BudgetDetailed {
        binding.apply {
            val budgetItem =
                getCurrentBudgetItemForUpdating()
            return BudgetDetailed(
                budgetItem,
                mainActivity.mainViewModel.getBudgetItem()!!.budgetRule,
                mainActivity.mainViewModel.getBudgetItem()!!.toAccount,
                mainActivity.mainViewModel.getBudgetItem()!!.fromAccount
            )
        }
    }

    private fun getCurrentBudgetItemForUpdating(): BudgetItem {
        binding.apply {
            return BudgetItem(
                if (mainActivity.mainViewModel.getBudgetItem()!!.budgetRule != null)
                    mainActivity.mainViewModel.getBudgetItem()!!.budgetRule!!.ruleId else 0L,
                mainActivity.mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedDate,
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                mainActivity.mainViewModel.getBudgetItem()!!.toAccount?.accountId ?: 0L,
                mainActivity.mainViewModel.getBudgetItem()!!.fromAccount?.accountId ?: 0L,
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

    private fun chooseToDeleteBudgetItem() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Budget Item")
            setMessage("Are you sure you want to delete this budget item?")
            setPositiveButton("Delete") { _, _ ->
                deleteBudgetItem()
                gotoCallingFragment()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun validateBudgetItem(): String {
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

    private fun deleteBudgetItem() {
        binding.apply {
            mainActivity.budgetItemViewModel.deleteBudgetItem(
                mainActivity.mainViewModel.getBudgetItem()!!.budgetItem!!.biRuleId,
                mainActivity.mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedDate,
                df.getCurrentTimeAsString()
            )

        }
    }

    private fun gotoCalculator() {
        mainActivity.mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etProjectedAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainActivity.mainViewModel.setReturnTo(TAG)
        mainActivity.mainViewModel.setBudgetItem(getCurrentBudgetItemDetailed())
        mView.findNavController().navigate(
            BudgetItemUpdateFragmentDirections
                .actionBudgetItemUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            mainActivity.mainViewModel.setCallingFragments(
                mainActivity.mainViewModel.getCallingFragments()!!
                    .replace(", $TAG", "")
            )
            gotoBudgetViewFragment()
        }
    }

    private fun gotoBudgetViewFragment() {
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToBudgetViewFragment()
        mView.findNavController().navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}