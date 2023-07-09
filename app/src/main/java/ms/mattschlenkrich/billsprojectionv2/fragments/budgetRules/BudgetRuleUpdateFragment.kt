package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.app.AlertDialog
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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = FRAG_BUDGET_RULE_UPDATE

@Suppress("DEPRECATION")
class BudgetRuleUpdateFragment :
    Fragment(R.layout.fragment_budget_rule_update) {

    private var _binding: FragmentBudgetRuleUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var mView: View
    private val args: BudgetRuleUpdateFragmentArgs by navArgs()

    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var budgetNameList: List<String>? = null

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
        _binding = FragmentBudgetRuleUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            mainActivity.budgetRuleViewModel
        CoroutineScope(Dispatchers.IO).launch {
            budgetNameList = budgetRuleViewModel.getBudgetRuleNameList()
        }
        mainActivity.title = "Update Budget Rule"
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
            fabUpdateDone.setOnClickListener {
                updateBudgetRule()
            }
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

    private fun getBudgetRuleDetailed(): BudgetRuleDetailed {
        binding.apply {
            val budgetRule = BudgetRule(
                0,
                etBudgetName.text.toString().trim(),
                if (mToAccount == null) 0L else mToAccount!!.accountId,
                if (mFromAccount == null) 0L else mFromAccount!!.accountId,
                etAmount.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble(),
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
                ""
            )
            val toAccount =
                if (args.budgetRuleDetailed != null) {
                    if (args.budgetRuleDetailed!!.toAccount != null) {
                        args.budgetRuleDetailed!!.toAccount
                    } else {
                        null
                    }
                } else {
                    null
                }
            val fromAccount =
                if (args.budgetRuleDetailed != null) {
                    if (args.budgetRuleDetailed!!.fromAccount != null) {
                        args.budgetRuleDetailed!!.fromAccount
                    } else {
                        null
                    }
                } else {
                    null
                }
            return BudgetRuleDetailed(budgetRule, toAccount, fromAccount)
        }
    }

    private fun chooseFromAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            BudgetRuleUpdateFragmentDirections
                .actionBudgetRuleUpdateFragmentToAccountsFragment(
                    args.transaction,
                    getBudgetRuleDetailed(),
                    REQUEST_FROM_ACCOUNT,
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = BudgetRuleUpdateFragmentDirections
            .actionBudgetRuleUpdateFragmentToAccountsFragment(
                args.transaction,
                getBudgetRuleDetailed(),
                REQUEST_TO_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    private fun fillValues() {
        fillSpinners()
        binding.apply {
            if (args.budgetRuleDetailed != null) {
                if (args.budgetRuleDetailed!!.budgetRule != null) {
                    etBudgetName.setText(
                        args.budgetRuleDetailed!!.budgetRule!!.budgetRuleName
                    )
                    etAmount.setText(
                        dollarFormat.format(
                            args.budgetRuleDetailed!!.budgetRule!!.budgetAmount
                        )
                    )
                    if (args.budgetRuleDetailed!!.toAccount != null) {
                        tvToAccount.text =
                            args.budgetRuleDetailed!!.toAccount!!.accountName
                        mToAccount = args.budgetRuleDetailed!!.toAccount
                    }
                    if (args.budgetRuleDetailed!!.fromAccount != null) {
                        tvFromAccount.text =
                            args.budgetRuleDetailed!!.fromAccount!!.accountName
                        mFromAccount = args.budgetRuleDetailed!!.fromAccount
                    }
                    chkFixedAmount.isChecked =
                        args.budgetRuleDetailed!!.budgetRule!!.budFixedAmount
                    chkMakePayDay.isChecked =
                        args.budgetRuleDetailed!!.budgetRule!!.budIsPayDay
                    chkAutoPayment.isChecked =
                        args.budgetRuleDetailed!!.budgetRule!!.budIsAutoPay
                    etStartDate.setText(
                        args.budgetRuleDetailed!!.budgetRule!!.budStartDate
                    )
                    etEndDate.setText(
                        args.budgetRuleDetailed!!.budgetRule!!.budEndDate
                    )
                    spFrequencyType.setSelection(
                        args.budgetRuleDetailed!!.budgetRule!!.budFrequencyTypeId
                    )
                    etFrequencyCount.setText(
                        args.budgetRuleDetailed!!.budgetRule!!.budFrequencyCount.toString()
                    )
                    spDayOfWeek.setSelection(
                        args.budgetRuleDetailed!!.budgetRule!!.budDayOfWeekId
                    )
                }
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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.delete_menu, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete -> {
                deleteBudgetRule()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteBudgetRule() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Budget Rule")
            setMessage("Are you sure you want to delete this budget rule?")
            setPositiveButton("Delete") { _, _ ->
                budgetRuleViewModel.deleteBudgetRule(
                    args.budgetRuleDetailed!!.budgetRule!!.ruleId,
                    timeFormatter.format(
                        Calendar.getInstance().time
                    )
                )
                val direction = BudgetRuleUpdateFragmentDirections
                    .actionBudgetRuleUpdateFragmentToBudgetRuleFragment(
                        args.transaction,
                        args.callingFragments,
                    )
                mView.findNavController().navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun updateBudgetRule() {
        val mes = checkBudgetRule()
        binding.apply {
            if (mes == "Ok") {
                val toAccountId =
                    if (mToAccount != null) {
                        mToAccount!!.accountId
                    } else {
                        0L
                    }
                val fromAccountId =
                    if (mFromAccount != null) {
                        mFromAccount!!.accountId
                    } else {
                        0L
                    }
                val fragmentChain = "${args.callingFragments}, $TAG"
                budgetRuleViewModel.updateBudgetRule(
                    BudgetRule(
                        args.budgetRuleDetailed!!.budgetRule!!.ruleId,
                        etBudgetName.text.toString().trim(),
                        toAccountId,
                        fromAccountId,
                        etAmount.text.toString()
                            .replace(",", "")
                            .replace("$", "")
                            .toDouble(),
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
                        timeFormatter.format(Calendar.getInstance().time)
                    )
                )
                val direction =
                    BudgetRuleUpdateFragmentDirections
                        .actionBudgetRuleUpdateFragmentToBudgetRuleFragment(
                            args.transaction,
                            fragmentChain
                        )
                mView.findNavController().navigate(direction)
            } else {
                Toast.makeText(
                    mView.context,
                    mes,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkBudgetRule(): String {
        binding.apply {
            val nameIsBlank = etBudgetName.text.isNullOrBlank()
            var nameFound = false
            if (budgetNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until budgetNameList!!.size) {
                    if (budgetNameList!![i] ==
                        etBudgetName.text.toString() &&
                        budgetNameList!![i] !=
                        args.budgetRuleDetailed!!.budgetRule!!.budgetRuleName
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
            } else if (mToAccount == null
            ) {
                "     Error!!\n" +
                        "There needs to be an account money will go to."
            } else if (mFromAccount == null
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