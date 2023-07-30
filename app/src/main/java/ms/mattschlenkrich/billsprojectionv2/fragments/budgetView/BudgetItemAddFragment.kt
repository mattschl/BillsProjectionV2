package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetItemAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
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
    private var budgetAmount = 0.0
    private var budgetRule: BudgetRule? = null
    private var toAccount: Account? = null
    private var fromAccount: Account? = null

    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "creating $TAG")
    }

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
        fillValues()
        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
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
            ).show()
        }
    }

    private fun chooseFromAccount() {
        val fragmentChain = TAG
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetViewAddFragmentToAccountsFragment2(
                getCurrentBudgetItem(),
                null,
                null,
                REQUEST_FROM_ACCOUNT,
                fragmentChain
            )
    }

    private fun chooseToAccount() {
        val fragmentChain = TAG
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetViewAddFragmentToAccountsFragment2(
                getCurrentBudgetItem(),
                null,
                null,
                REQUEST_TO_ACCOUNT,
                fragmentChain
            )
    }

    private fun chooseBudgetRule() {
        val fragmentChain = TAG
        val direction = BudgetItemAddFragmentDirections
            .actionBudgetViewAddFragmentToBudgetRuleFragment2(
                getCurrentBudgetItem(),
                null,
                fragmentChain
            )
        mView!!.findNavController().navigate(direction)
    }

    private fun getCurrentBudgetItem(): BudgetDetailed {
        binding.apply {
            val budgetItem = BudgetItem(
                if (budgetRule != null) budgetRule!!.ruleId else 0,
                etProjectedDate.text.toString(),
                etProjectedDate.text.toString(),
                spPayDays.selectedItem.toString(),
                etBudgetItemName.text.toString(),
                chkIsPayDay.isChecked,
                if (toAccount != null) toAccount!!.accountId else 0,
                if (fromAccount != null) fromAccount!!.accountId else 0,
                etProjectedAmount.text.toString().toDouble(),
                biIsPending = false,
                chkFixedAmount.isChecked,
                chkIsAutoPayment.isChecked,
                biManuallyEntered = true,
                biIsCompleted = false,
                biIsCancelled = false,
                biIsDeleted = false,
                df.getCurrentTimeAsString()
            )
            return BudgetDetailed(
                budgetItem, budgetRule, toAccount, fromAccount
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
                budgetRule = args.budgetItem!!.budgetRule
            }
            if (args.budgetItem!!.toAccount != null) {
                tvToAccount.text = args.budgetItem!!.toAccount!!.accountName
                toAccount = args.budgetItem!!.toAccount
            }
            if (args.budgetItem!!.fromAccount != null) {
                tvFromAccount.text = args.budgetItem!!.fromAccount!!.accountName
                fromAccount = args.budgetItem!!.fromAccount
            }
            chkFixedAmount.isChecked = args.budgetItem!!.budgetItem!!.biIsFixed
            chkIsAutoPayment.isChecked = args.budgetItem!!.budgetItem!!.biIsAutomatic
            chkIsPayDay.isChecked = args.budgetItem!!.budgetItem!!.biIsPayDayItem
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}