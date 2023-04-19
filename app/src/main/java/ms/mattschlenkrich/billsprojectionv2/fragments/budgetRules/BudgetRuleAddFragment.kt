package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleAddBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = FRAG_BUDGET_RULE_ADD

class BudgetRuleAddFragment :
    Fragment(R.layout.fragment_budget_rule_add) {

    private var _binding: FragmentBudgetRuleAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var mView: View
    private val args: BudgetRuleAddFragmentArgs by navArgs()

    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val timeFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_DATE, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleAddBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            (activity as MainActivity).budgetRuleViewModel

        fillValues()
    }

    private fun fillValues() {
        if (args.budgetRuleDetailed != null) {
            binding.etBudgetName.setText(
                args.budgetRuleDetailed!!.budgetRule.budgetRuleName
            )
            binding.etAmount.setText(
                dollarFormat.format(
                    args.budgetRuleDetailed!!.budgetRule.amount
                )
            )
            binding.tvToAccount.text =
                args.budgetRuleDetailed!!.toAccount.accountName
            binding.tvFromAccount.text =
                args.budgetRuleDetailed!!.fromAccount.accountName
            binding.chkFixedAmount.isChecked =
                args.budgetRuleDetailed!!.budgetRule.fixedAmount
            binding.chkMakePayDay.isChecked =
                args.budgetRuleDetailed!!.budgetRule.isPayDay
            binding.chkAutoPayment.isChecked =
                args.budgetRuleDetailed!!.budgetRule.isAutoPay
            binding.etStartDate.setText(
                args.budgetRuleDetailed!!.budgetRule.startDate
            )
            binding.etEndDate.setText(
                args.budgetRuleDetailed!!.budgetRule.endDate
            )
            binding.tvFrequencyType.text =
                args.budgetRuleDetailed!!.frequencyTypes.frequencyType
            binding.etFrequencyCount.setText(
                args.budgetRuleDetailed!!.budgetRule.frequencyCount
            )
            binding.tvDayOfWeek.text =
                args.budgetRuleDetailed!!.daysOfWeek.dayOfWeek
            binding.etLeadDays.setText(
                args.budgetRuleDetailed!!.budgetRule.leadDays
            )
        } else {
            binding.etStartDate.setText(
                dateFormatter.format(
                    Calendar.getInstance().time
                )
            )
            binding.etEndDate.setText(
                dateFormatter.format(
                    Calendar.getInstance().time
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveBudgetRule(mView)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBudgetRule(view: View) {
        val budgetName =
            binding.etBudgetName.text.toString().trim()
        val amount =
            binding.etAmount.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val toAccount =
            binding.tvToAccount.text.toString()
        val fromAccount =
            binding.tvFromAccount.text.toString()
        val fixedAmount =
            if (binding.chkFixedAmount.isChecked) 1 else 0
        val isPayDay =
            if (binding.chkMakePayDay.isChecked) 1 else 0
        val isAutoPayment =
            if (binding.chkAutoPayment.isChecked) 1 else 0
        val startDate =
            binding.etStartDate.text.toString()
        val endDate =
            binding.etEndDate.text.toString()
        val frequencyType =
            binding.tvFrequencyType.text.toString()
        val frequencyCount =
            binding.etFrequencyCount.text.toString().toInt()
        val dayOfWeek =
            binding.tvDayOfWeek.text.toString()
        val leadDays =
            binding.etLeadDays.text.toString().toInt()
        val updateTime =
            timeFormatter.format(Calendar.getInstance().time)

        budgetRuleViewModel.insertBudgetRule(
            budgetName, amount, toAccount, fromAccount,
            fixedAmount, isPayDay, isAutoPayment,
            startDate, endDate, frequencyType,
            frequencyCount, dayOfWeek, leadDays, updateTime
        )
        //TODO: create the direction back to budgetRuleFragment
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}