package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
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

    private lateinit var mBudgetRule: BudgetRule
    private lateinit var mToAccount: Account
    private lateinit var mFromAccount: Account
    private lateinit var mBudgetRuleDetailed: BudgetRuleDetailed
    private var mDayOfWeekId = 0L
    private var mFrequencyTypeId = 0L


    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)
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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            (activity as MainActivity).budgetRuleViewModel

        fillValues()
        binding.apply {
            tvToAccount.setOnLongClickListener {
                chooseToAccount()
                false
            }
            tvFromAccount.setOnLongClickListener {
                chooseFromAccount()
                false
            }
            etStartDate.setOnLongClickListener {
                chooseStartDate()
                false
            }
            etEndDate.setOnLongClickListener {
                chooseEndDate()
                false
            }
        }
    }

    private fun tempSave() {

    }

    private fun chooseFromAccount() {
        binding.apply {
            val mBudgetRule = BudgetRule(
                0,
                etBudgetName.text.toString().trim(),
                if (mToAccount == null) 0L else mToAccount.accountId,
                if (mFromAccount == null) 0 else mFromAccount.accountId,
                etAmount.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble(),
                chkFixedAmount.isChecked,
                chkMakePayDay.isChecked,
                chkAutoPayment.isChecked,
                etStartDate.text.toString(),
                etEndDate.text.toString(),
                mDayOfWeekId,
                mFrequencyTypeId,
                etFrequencyCount.text.toString().toInt(),
                etLeadDays.text.toString().toInt(),
                false,
                ""
            )
            val direction = BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToAccountsFragment(
                    mBudgetRule,
                    REQUEST_FROM_ACCOUNT,
                    TAG
                )
            mView.findNavController().navigate(direction)
        }
    }

    private fun chooseToAccount() {
        binding.apply {
            val mBudgetRule = BudgetRule(
                0,
                etBudgetName.text.toString().trim(),
                if (mToAccount == null) 0L else mToAccount.accountId,
                if (mFromAccount == null) 0 else mFromAccount.accountId,
                etAmount.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble(),
                chkFixedAmount.isChecked,
                chkMakePayDay.isChecked,
                chkAutoPayment.isChecked,
                etStartDate.text.toString(),
                etEndDate.text.toString(),
                mDayOfWeekId,
                mFrequencyTypeId,
                etFrequencyCount.text.toString().toInt(),
                etLeadDays.text.toString().toInt(),
                false,
                ""
            )
            val direction = BudgetRuleAddFragmentDirections
                .actionBudgetRuleAddFragmentToAccountsFragment(
                    mBudgetRule,
                    REQUEST_TO_ACCOUNT,
                    TAG
                )
            mView.findNavController().navigate(direction)
        }
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

    private fun fillValues() {
        fillSpinners()
        binding.apply {
            if (args.budgetRuleDetailed != null) {
                etBudgetName.setText(
                    args.budgetRuleDetailed!!.budgetRule.budgetRuleName
                )
                etAmount.setText(
                    dollarFormat.format(
                        args.budgetRuleDetailed!!.budgetRule.amount
                    )
                )
                tvToAccount.text =
                    args.budgetRuleDetailed!!.toAccount.accountName
                tvFromAccount.text =
                    args.budgetRuleDetailed!!.fromAccount.accountName
                chkFixedAmount.isChecked =
                    args.budgetRuleDetailed!!.budgetRule.fixedAmount
                chkMakePayDay.isChecked =
                    args.budgetRuleDetailed!!.budgetRule.isPayDay
                chkAutoPayment.isChecked =
                    args.budgetRuleDetailed!!.budgetRule.isAutoPay
                etStartDate.setText(
                    args.budgetRuleDetailed!!.budgetRule.startDate
                )
                etEndDate.setText(
                    args.budgetRuleDetailed!!.budgetRule.endDate
                )
                spFrequencyType.setSelection(
                    args.budgetRuleDetailed!!.budgetRule.frequencyTypeId.toInt()
                )
                spDayOfWeek.setSelection(
                    args.budgetRuleDetailed!!.budgetRule.dayOfWeekId.toInt()
                )
            } else {
                etStartDate.setText(
                    dateFormatter.format(
                        Calendar.getInstance().time
                    )
                )
                etEndDate.setText(
                    dateFormatter.format(
                        Calendar.getInstance().time
                    )
                )
            }
        }
    }

    private fun fillSpinners() {
        val adapterFrequencyType =
            ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                resources.getStringArray(R.array.frequency_type)
            )
        adapterFrequencyType.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spFrequencyType.adapter = adapterFrequencyType

        val adapterDayOfWeek =
            ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                resources.getStringArray(R.array.day_of_week)
            )
        adapterDayOfWeek.setDropDownViewResource(
            R.layout.spinner_item_normal
        )
        binding.spDayOfWeek.adapter = adapterDayOfWeek
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveBudgetRule()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBudgetRule() {
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
        val frequencyTypeId =
            binding.spFrequencyType.selectedItemId
        val frequencyCount =
            binding.etFrequencyCount.text.toString().toInt()
        val dayOfWeekId =
            binding.spDayOfWeek.selectedItemId
        val leadDays =
            binding.etLeadDays.text.toString().toInt()
        val updateTime =
            timeFormatter.format(Calendar.getInstance().time)

        budgetRuleViewModel.insertBudgetRule(
            budgetName, amount, toAccount, fromAccount,
            fixedAmount, isPayDay, isAutoPayment,
            startDate, endDate, frequencyTypeId,
            frequencyCount, dayOfWeekId, leadDays, updateTime
        )
        val direction = BudgetRuleAddFragmentDirections
            .actionBudgetRuleAddFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}