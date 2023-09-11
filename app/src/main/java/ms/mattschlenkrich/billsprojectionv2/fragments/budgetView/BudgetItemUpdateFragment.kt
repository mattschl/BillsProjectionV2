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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel

private const val TAG = FRAG_BUDGET_ITEM_UPDATE

class BudgetItemUpdateFragment : Fragment(
    R.layout.fragment_budget_item_update
) {

    private var _binding: FragmentBudgetItemUpdateBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private val args: BudgetItemUpdateFragmentArgs by navArgs()
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
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        mainActivity.title = "Update this Budget Item"
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
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToAccountsFragment(
                args.asset,
                args.payDay,
                getCurrentBudgetItem(),
                null,
                null,
                requestedAccount,
                fragmentChain
            )
        mView?.findNavController()?.navigate(direction)
    }

    private fun chooseBudgetRule() {
        val fragmentChain = TAG
        val direction = BudgetItemUpdateFragmentDirections
            .actionBudgetItemUpdateFragmentToBudgetRuleFragment(
                args.asset,
                args.payDay,
                getCurrentBudgetItem(),
                null,
                fragmentChain
            )
        mView?.findNavController()?.navigate(direction)
    }

    private fun getCurrentBudgetItem(): BudgetDetailed {
        binding.apply {
            val budgetItem = BudgetItem(
                if (args.budgetItem!!.budgetRule != null)
                    args.budgetItem!!.budgetRule!!.ruleId else 0,
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                if (args.budgetItem!!.toAccount != null)
                    args.budgetItem!!.toAccount!!.accountId else 0,
                if (args.budgetItem!!.fromAccount != null)
                    args.budgetItem!!.fromAccount!!.accountId else 0,
                etProjectedAmount.text.toString().toDouble(),
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
            return BudgetDetailed(
                budgetItem,
                args.budgetItem!!.budgetRule,
                args.budgetItem!!.toAccount,
                args.budgetItem!!.fromAccount
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
                        args.budgetItem!!.budgetItem!!.biRuleId,
                        args.budgetItem!!.budgetItem!!.biProjectedDate,
                        df.getCurrentTimeAsString()
                    )

                }
                gotoCallingFragment()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun gotoCallingFragment() {
        if (args.callingFragments.toString().contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            val fragmentChain = args.callingFragments!!
                .replace(", $TAG", "")
            val direction = BudgetItemUpdateFragmentDirections
                .actionBudgetItemUpdateFragmentToBudgetViewFragment(
                    args.asset,
                    args.payDay,
                    fragmentChain
                )
            mView!!.findNavController().navigate(direction)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}