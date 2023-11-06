package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.TransactionAnalysisAdapter
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAnalysisBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisFragment : Fragment(
    R.layout.fragment_transaction_analysis
) {

    private var _binding: FragmentTransactionAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAnalysisAdapter
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAnalysisBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        mView = binding.root
        Log.d(TAG, "creating $TAG")
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.title = "Transaction analysis"
        transactionViewModel =
            mainActivity.transactionViewModel
        setStartValues()
        setRadioOptions()
        setGotoOptions()
    }

    private fun setGotoOptions() {
        binding.apply {
            tvBudgetRule.setOnClickListener {
                gotoBudgetRule()
            }
            tvAccount.setOnClickListener {
                gotoAccount()
            }
        }
    }

    private fun gotoAccount() {
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setAccountWithType(null)
        mainViewModel.setBudgetRuleDetailed(null)
        mView.findNavController().navigate(
            TransactionAnalysisFragmentDirections
                .actionTransactionAverageFragmentToAccountsFragment()
        )
    }

    private fun gotoBudgetRule() {
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setAccountWithType(null)
        mainViewModel.setBudgetRuleDetailed(null)
        mView.findNavController().navigate(
            TransactionAnalysisFragmentDirections
                .actionTransactionAverageFragmentToBudgetRuleFragment()
        )
    }

    private fun setStartValues() {
        if (mainViewModel.getBudgetRuleDetailed() != null) {
            fillFromBudgetRule()
        } else if (mainViewModel.getAccountWithType() != null) {
            fillFromAccount()
        }
    }

    private fun fillFromAccount() {
        var totalCredits = 0.0
        var totalDebits = 0.0
        val transList = ArrayList<TransactionDetailed>()
        val account =
            mainViewModel.getAccountWithType()!!.account
        binding.apply {
            tvBudgetRule.text =
                getString(R.string.no_budget_rule_selected)
            tvAccount.text =
                account.accountName
            transactionViewModel.getSumTransactionToAccount(account.accountId)
                .observe(viewLifecycleOwner) { sum ->
                    if (sum != null && !sum.isNaN()) {
                        tvTotalCredits.text = cf.displayDollars(totalCredits)
                        tvTotalCredits.visibility = View.VISIBLE
                        lblTotalCredits.text = getString(R.string.total_credits)
                        lblTotalCredits.visibility = View.VISIBLE
                        totalCredits = sum
                    }
                }
            transactionViewModel.getSumTransactionFromAccount(account.accountId)
                .observe(viewLifecycleOwner) { sum ->
                    if (sum != null && !sum.isNaN()) {
                        tvTotalDebits.text = cf.displayDollars(sum)
                        tvTotalDebits.visibility = View.VISIBLE
                        tvTotalDebits.setTextColor(Color.RED)
                        lblTotalDebits.text = getString(R.string.total_debits)
                        lblTotalDebits.visibility = View.VISIBLE
                        lblTotalDebits.setTextColor(Color.RED)
                        totalDebits = sum
                    }
                }
            transactionAdapter = TransactionAnalysisAdapter(
//                mainActivity,
//                mainViewModel,
//                mView.context
            )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(
                    requireContext()
                )
                adapter = transactionAdapter
            }
            activity.let {
                transactionViewModel.getActiveTransactionByAccount(account.accountId)
                    .observe(viewLifecycleOwner) { transactionList ->
                        transactionAdapter.differ.submitList(transactionList)
                        transList.clear()
                        transactionList.forEach {
                            transList.add(it)
                        }
                    }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    if (transList.size > 0) {
                        val endDate = transList.first().transaction!!.transDate
                        val startDate = transList.last().transaction!!.transDate
                        val months = df.getMonthsBetween(startDate, endDate)
                        lblAverage.text = getString(R.string.credit_average)
                        tvAverage.text = cf.displayDollars(totalCredits / months)
                        lblHighest.text = getString(R.string.debit_average)
                        lblHighest.setTextColor(Color.RED)
                        tvHighest.text = cf.displayDollars(totalDebits / months)
                        tvHighest.setTextColor(Color.RED)
                    }
                }
            }
        }
    }

    private fun setRadioOptions() {
        binding.apply {
            rdShowAll.setOnClickListener {
                rdShowAll.isChecked = true
                rdLastMonth.isChecked = false
                rdDateRange.isChecked = false
                setDateRangeVisibility(false)
                setStartValues()
            }
            rdLastMonth.setOnClickListener {
                rdShowAll.isChecked = false
                rdLastMonth.isChecked = true
                rdDateRange.isChecked = false
                setDateRangeVisibility(false)
                setValuesLastMonth()
            }
            rdDateRange.setOnClickListener {
                rdShowAll.isChecked = false
                rdLastMonth.isChecked = false
                rdDateRange.isChecked = true
                setDateRangeVisibility(true)
                tvStartDate.setText(
                    df.getFirstOfMonth(
                        df.getCurrentDateAsString()
                    )
                )
                tvEndDate.setText(
                    df.getCurrentDateAsString()
                )
                tvStartDate.setOnLongClickListener {
                    chooseStartDate()
                    false
                }
                tvEndDate.setOnLongClickListener {
                    chooseEndDate()
                    false
                }
                btnFill.setOnClickListener {
                    if (mainViewModel.getBudgetRuleDetailed() != null) {
                        fillFromBudgetRuleAndDates(
                            tvStartDate.text.toString(),
                            tvEndDate.text.toString()
                        )
                    } else if (mainViewModel.getAccountWithType() != null) {
                        fillFromAccountAndDates(
                            tvStartDate.text.toString(),
                            tvEndDate.text.toString()
                        )
                    }
                }
            }
            rdShowAll.isChecked = true
        }
    }

    private fun chooseEndDate() {
        binding.apply {
            val curDateAll = tvEndDate.text.toString()
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
                    tvEndDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the end date")
            datePickerDialog.show()
        }
    }

    private fun chooseStartDate() {
        binding.apply {
            val curDateAll = tvStartDate.text.toString()
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
                    tvStartDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the start date")
            datePickerDialog.show()
        }
    }

    private fun setValuesLastMonth() {
        val startDate = df.getFirstOfPreviousMonth(
            df.getCurrentDateAsString()
        )
        val endDate = df.getLastOfPreviousMonth(df.getCurrentDateAsString())
        if (mainViewModel.getBudgetRuleDetailed() != null) {
            fillFromBudgetRuleAndDates(startDate, endDate)
        } else if (mainViewModel.getAccountWithType() != null) {
            fillFromAccountAndDates(startDate, endDate)
        }
    }

    private fun fillFromAccountAndDates(startDate: String, endDate: String) {
        var totalCredits = 0.0
        var totalDebits = 0.0
        val account =
            mainViewModel.getAccountWithType()!!.account
        val transList = ArrayList<TransactionDetailed>()
        binding.apply {
            tvBudgetRule.text = getString(R.string.no_budget_rule_selected)
            tvAccount.text = account.accountName
            transactionViewModel.getSumTransactionToAccount(
                account.accountId, startDate, endDate
            ).observe(viewLifecycleOwner) { sum ->
                if (sum != null && !sum.isNaN()) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    tvTotalCredits.visibility = View.VISIBLE
                    lblTotalCredits.text = getString(R.string.total_credits)
                    lblTotalCredits.visibility = View.VISIBLE
                    totalCredits = sum
                }
            }
            transactionViewModel.getSumTransactionFromAccount(
                account.accountId, startDate, endDate
            ).observe(viewLifecycleOwner) { sum ->
                if (sum != null && !sum.isNaN()) {
                    tvTotalDebits.text = cf.displayDollars(-sum)
                    tvTotalDebits.visibility = View.VISIBLE
                    tvTotalDebits.setTextColor(Color.RED)
                    lblTotalDebits.text = getString(R.string.total_debits)
                    lblTotalDebits.visibility = View.VISIBLE
                    lblTotalDebits.setTextColor(Color.RED)
                    totalDebits = sum
                }
            }
            transactionAdapter = TransactionAnalysisAdapter()
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = transactionAdapter
            }
            activity.let {
                transactionViewModel.getActiveTransactionByAccount(
                    account.accountId, startDate, endDate
                ).observe(viewLifecycleOwner) { transactionList ->
                    transactionAdapter.differ.submitList(transactionList)
                    transList.clear()
                    transactionList.forEach {
                        transList.add(it)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    if (transList.size > 0) {
                        val end = transList.first().transaction!!.transDate
                        val start = transList.last().transaction!!.transDate
                        val months = df.getMonthsBetween(start, end)
                        lblAverage.text = getString(R.string.credit_average)
                        tvAverage.text = cf.displayDollars(totalCredits / months)
                        lblHighest.text = getString(R.string.debit_average)
                        lblHighest.setTextColor(Color.RED)
                        tvHighest.text = cf.displayDollars(totalDebits / months)
                        tvHighest.setTextColor(Color.RED)
                    }
                }
            }
        }
    }

    private fun fillFromBudgetRuleAndDates(startDate: String, endDate: String) {
        var total = 0.0
        val transList = ArrayList<TransactionDetailed>()
        val budgetRule =
            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!
        binding.apply {
            tvBudgetRule.text = budgetRule.budgetRuleName
            tvAccount.text = getString(R.string.no_account_selected)
            transactionViewModel.getMaxTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { max ->
                if (max != null && !max.isNaN()) {
                    tvHighest.text = cf.displayDollars(max)
                }
            }
            transactionViewModel.getMinTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { min ->
                if (min != null && !min.isNaN()) {
                    tvLowest.text =
                        cf.displayDollars(min)
                }
            }
            transactionViewModel.getSumTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(viewLifecycleOwner) { sum ->
                if (sum != null && !sum.isNaN()) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    total = sum
                    lblTotalCredits.text = getString(R.string.total)
                    lblTotalDebits.visibility = View.GONE
                    tvTotalDebits.visibility = View.GONE
                }
            }
            transactionAdapter = TransactionAnalysisAdapter(
//                mainActivity,
//                mainViewModel,
//                mView.context
            )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(
                    requireContext()
                )
                adapter = transactionAdapter
            }
            activity.let {
                transactionViewModel.getActiveTransactionsDetailed(
                    budgetRule.ruleId, startDate, endDate
                ).observe(
                    viewLifecycleOwner
                ) { transactionList ->
                    transactionAdapter.differ.submitList(
                        transactionList
                    )
                    transList.clear()
                    transactionList.forEach {
                        transList.add(it)
                    }

                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_500)
                    if (transList.size > 0) {
                        val months =
                            df.getMonthsBetween(startDate, endDate)
                        lblAverage.text = getString(R.string.average)
                        tvAverage.text =
                            cf.displayDollars(total / months)
                        tvRecent.text =
                            cf.displayDollars(
                                transList.first().transaction!!.transAmount
                            )
                    }
                }
            }
        }

    }

    private fun setDateRangeVisibility(visible: Boolean) {
        binding.apply {
            if (visible) {
                lblStartDate.visibility = View.VISIBLE
                lblEndDate.visibility = View.VISIBLE
                tvStartDate.visibility = View.VISIBLE
                tvEndDate.visibility = View.VISIBLE
                btnFill.visibility = View.VISIBLE
            } else {
                lblStartDate.visibility = View.GONE
                lblEndDate.visibility = View.GONE
                tvStartDate.visibility = View.GONE
                tvEndDate.visibility = View.GONE
                btnFill.visibility = View.GONE
            }
        }
    }

    private fun fillFromBudgetRule() {
        var total = 0.0
        var startDate: String
        var endDate: String
        val transList = ArrayList<TransactionDetailed>()
        val budgetRule =
            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!
        binding.apply {
            tvBudgetRule.text =
                budgetRule.budgetRuleName
            tvAccount.text =
                getString(R.string.no_account_selected)
            transactionViewModel.getMaxTransactionByBudgetRule(
                budgetRule.ruleId
            ).observe(
                viewLifecycleOwner
            ) { max ->
                if (max != null && !max.isNaN()) {
                    tvHighest.text = cf.displayDollars(max)
                }
            }
            transactionViewModel.getMinTransactionByBudgetRule(budgetRule.ruleId)
                .observe(viewLifecycleOwner) { min ->
                    if (min != null && !min.isNaN()) {
                        tvLowest.text = cf.displayDollars(min)
                    }
                }
            transactionViewModel.getSumTransactionByBudgetRule(
                budgetRule.ruleId
            ).observe(viewLifecycleOwner) { sum ->
                if (sum != null && !sum.isNaN()) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    total = sum
                    lblTotalCredits.text =
                        getString(R.string.total)
                    lblTotalDebits.visibility = View.GONE
                    tvTotalDebits.visibility = View.GONE
                }
            }
            transactionAdapter = TransactionAnalysisAdapter(
//                mainActivity,
//                mainViewModel,
//                mView.context
            )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(
                    requireContext()
                )
                adapter = transactionAdapter
            }
            activity.let {
                transactionViewModel.getActiveTransactionsDetailed(
                    budgetRule.ruleId
                ).observe(
                    viewLifecycleOwner
                ) { transactionList ->
                    transactionAdapter.differ.submitList(
                        transactionList
                    )
                    transList.clear()
                    transactionList.forEach {
                        transList.add(it)
                    }

                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    if (transList.size > 0) {
                        endDate = transList.first().transaction!!.transDate
                        startDate = transList.last().transaction!!.transDate
                        val months = df.getMonthsBetween(startDate, endDate)
                        lblAverage.text = getString(R.string.average_per_month)
                        tvAverage.text =
                            cf.displayDollars(total / months)
                        lblHighest.text = getString(R.string.highest)
                        tvRecent.text =
                            cf.displayDollars(
                                transList.first().transaction!!.transAmount
                            )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}