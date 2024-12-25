package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAnalysisBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.adapter.TransactionAnalysisAdapter

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
    private var transactionAdapter: TransactionAnalysisAdapter? = null
    private val cf = NumberFunctions()
    private val nf = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAnalysisBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        mainActivity.title = "Transaction analysis"
        transactionViewModel = mainActivity.transactionViewModel
        mView = binding.root
        Log.d(TAG, "creating $TAG")
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        if (mainViewModel.getBudgetRuleDetailed() != null) {
            populateAnalysisFromBudgetRule()
        } else if (mainViewModel.getAccountWithType() != null) {
            populateValuesFromAccount()
        }
    }

    private fun populateValuesFromAccount() {
        var totalCredits = 0.0
        var totalDebits = 0.0
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
                        tvTotalCredits.text = cf.displayDollars(sum)
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
            transactionViewModel.getMaxTransactionByAccount(account.accountId)
                .observe(viewLifecycleOwner) { max ->
                    if (max != null) tvHighest.text = cf.displayDollars(max)
                }
            transactionViewModel.getMinTransactionByAccount(account.accountId)
                .observe(viewLifecycleOwner) { min ->
                    if (min != null) tvLowest.text = cf.displayDollars(min)
                }
            transactionAdapter = null
            transactionAdapter =
                TransactionAnalysisAdapter(
                    mainActivity,
                    mView
                )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(
                    requireContext()
                )
                adapter = transactionAdapter
            }
            activity?.let {
                transactionViewModel.getActiveTransactionByAccount(account.accountId)
                    .observe(viewLifecycleOwner) { transactionList ->
                        transactionAdapter!!.differ.submitList(transactionList)
                        populateAnalysisFromAccount(transactionList, totalCredits, totalDebits)
                        updateUiHelpText(transactionList)
                    }
            }
        }
    }

    private fun setClickActions() {
        setRadioOptions()
        setGotoOptions()
        setSearchAction()
        setSearchListener()
    }

    private fun setSearchListener() {
        binding.apply {
            btnSearch.setOnClickListener {
                val searchQuery = "%${etSearch.text}%"
                fillFromSearch(searchQuery)
            }
        }
    }

    private fun fillFromSearch(
        searchQuery: String
    ) {
        binding.apply {
            tvBudgetRule.text =
                getString(R.string.no_budget_rule_selected)
            tvAccount.text =
                getString(R.string.no_account_selected)

            transactionViewModel.getSumTransactionBySearch(searchQuery).observe(
                viewLifecycleOwner
            ) { sum ->
                if (sum != null) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    lblTotalCredits.text =
                        getString(R.string.total)
                    lblTotalDebits.visibility = View.GONE
                    tvTotalDebits.visibility = View.GONE
                }
            }
            transactionViewModel.getMaxTransactionBySearch(searchQuery).observe(
                viewLifecycleOwner
            ) { max ->
                if (max != null && !max.isNaN()) {
                    tvHighest.text = cf.displayDollars(max)
                }
            }
            transactionViewModel.getMinTransactionBySearch(searchQuery).observe(
                viewLifecycleOwner
            ) { min ->
                if (min != null) tvLowest.text = cf.displayDollars(min)
            }
            transactionAdapter = null
            transactionAdapter =
                TransactionAnalysisAdapter(
                    mainActivity,
                    mView
                )
            if (etSearch.text.isNotEmpty()) {
                rvTransactions.apply {
                    layoutManager = LinearLayoutManager(
                        requireContext()
                    )
                    adapter = transactionAdapter
                }
                activity?.let {
                    transactionViewModel.getActiveTransactionBySearch(
                        searchQuery
                    ).observe(viewLifecycleOwner) { transList ->
                        transactionAdapter!!.differ.submitList(transList)
                        populateAnalysisFromBudgetRuleOrSearch(transList)
                        updateUiHelpText(transList)
                    }
                }
            }
        }
    }


    private fun setSearchAction() {
        binding.apply {
            chkSearch.setOnClickListener {
                if (chkSearch.isChecked) {
                    etSearch.visibility = View.VISIBLE
                    btnSearch.visibility = View.VISIBLE
                } else {
                    etSearch.visibility = View.GONE
                    btnSearch.visibility = View.GONE
                    etSearch.text = null
                    transactionAdapter = null
                }
            }
        }
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
                .actionTransactionAnalysisFragmentToAccountsFragment()
        )
    }

    private fun gotoBudgetRule() {
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setAccountWithType(null)
        mainViewModel.setBudgetRuleDetailed(null)
        mView.findNavController().navigate(
            TransactionAnalysisFragmentDirections
                .actionTransactionAnalysisFragmentToBudgetRuleFragment()
        )
    }

    private fun updateUiHelpText(transactionList: List<TransactionDetailed>) {
        binding.apply {
            if (transactionList.isEmpty()) {
                rvTransactions.visibility = View.GONE
                crdTransactionAnalysisHelp.visibility = View.VISIBLE
            } else {
                rvTransactions.visibility = View.VISIBLE
                crdTransactionAnalysisHelp.visibility = View.GONE
            }
        }
    }

    private fun populateAnalysisFromAccount(
        transList: List<TransactionDetailed>,
        totalCredits: Double,
        totalDebits: Double
    ) {
        if (transList.isNotEmpty()) {
            binding.apply {
                CoroutineScope(Dispatchers.Main).launch {
                    Log.d(TAG, "count of transactions is ${transList.count()}")
                    val endDate = transList.first().transaction!!.transDate
                    val startDate = transList.last().transaction!!.transDate
                    val months = nf.getMonthsBetween(startDate, endDate)
                    tvRecent.text = cf.displayDollars(
                        transList.first().transaction!!.transAmount
                    )
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

    private fun setRadioOptions() {
        binding.apply {
            rdShowAll.setOnClickListener {
                rdShowAll.isChecked = true
                rdLastMonth.isChecked = false
                rdDateRange.isChecked = false
                setDateRangeVisibility(false)
                populateValues()
            }
            rdLastMonth.setOnClickListener {
                rdShowAll.isChecked = false
                rdLastMonth.isChecked = true
                rdDateRange.isChecked = false
                setDateRangeVisibility(false)
                populateAnalysisLastMonth()
            }
            rdDateRange.setOnClickListener {
                rdShowAll.isChecked = false
                rdLastMonth.isChecked = false
                rdDateRange.isChecked = true
                setDateRangeVisibility(true)
                tvStartDate.setText(
                    nf.getFirstOfMonth(
                        nf.getCurrentDateAsString()
                    )
                )
                tvEndDate.setText(
                    nf.getCurrentDateAsString()
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
                        populateAnalysisFromBudgetRuleAndDates(
                            tvStartDate.text.toString(),
                            tvEndDate.text.toString()
                        )
                    } else if (mainViewModel.getAccountWithType() != null) {
                        populateAnalysisFromAccountAndDates(
                            tvStartDate.text.toString(),
                            tvEndDate.text.toString()
                        )
                    } else if (chkSearch.isChecked) {
                        populateAnalysisFromSearchAndDates(
                            "%${etSearch.text}%",
                            tvStartDate.text.toString(),
                            tvEndDate.text.toString()
                        )
                    }
                }
            }
            rdShowAll.isChecked = true
        }
    }

    private fun populateAnalysisFromSearchAndDates(
        searchQuery: String,
        startDate: String,
        endDate: String
    ) {
        binding.apply {
            tvBudgetRule.text =
                getString(R.string.no_budget_rule_selected)
            tvAccount.text =
                getString(R.string.no_account_selected)
            transactionViewModel.getSumTransactionBySearch(
                searchQuery, startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { sum ->
                if (sum != null) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    lblTotalCredits.text =
                        getString(R.string.total)
                    lblTotalDebits.visibility = View.GONE
                    tvTotalDebits.visibility = View.GONE
                }
            }
            transactionViewModel.getMaxTransactionBySearch(
                searchQuery, startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { max ->
                if (max != null && !max.isNaN()) {
                    tvHighest.text = cf.displayDollars(max)
                }
            }
            transactionViewModel.getMinTransactionBySearch(
                searchQuery, startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { min ->
                if (min != null) tvLowest.text = cf.displayDollars(min)
            }
            transactionAdapter = null
            transactionAdapter =
                TransactionAnalysisAdapter(
                    mainActivity,
                    mView
                )
            if (etSearch.text.isNotEmpty()) {
                rvTransactions.apply {
                    layoutManager = LinearLayoutManager(
                        requireContext()
                    )
                    adapter = transactionAdapter
                }
                activity?.let {
                    transactionViewModel.getActiveTransactionBySearch(
                        searchQuery, startDate, endDate
                    ).observe(viewLifecycleOwner) { transList ->
                        transactionAdapter!!.differ.submitList(transList)
                        populateAnalysisFromBudgetRuleOrSearch(transList)
                        updateUiHelpText(transList)
                    }
                }
            }
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

    private fun populateAnalysisLastMonth() {
        val startDate = nf.getFirstOfPreviousMonth(
            nf.getCurrentDateAsString()
        )
        val endDate = nf.getLastOfPreviousMonth(nf.getCurrentDateAsString())
        binding.apply {
            if (mainViewModel.getBudgetRuleDetailed() != null) {
                populateAnalysisFromBudgetRuleAndDates(startDate, endDate)
            } else if (mainViewModel.getAccountWithType() != null) {
                populateAnalysisFromAccountAndDates(startDate, endDate)
            } else if (chkSearch.isChecked) {
                populateAnalysisFromSearchAndDates(
                    "%${etSearch.text}%",
                    startDate,
                    endDate
                )
            }
        }
    }

    private fun populateAnalysisFromAccountAndDates(startDate: String, endDate: String) {
        var totalCredits = 0.0
        var totalDebits = 0.0
        val account =
            mainViewModel.getAccountWithType()!!.account
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
            transactionViewModel.getMaxTransactionByAccount(
                account.accountId, startDate, endDate
            ).observe(viewLifecycleOwner) { max ->
                if (max != null) tvHighest.text = cf.displayDollars(max)
            }
            transactionViewModel.getMinTransactionByAccount(
                account.accountId, startDate, endDate
            )
                .observe(viewLifecycleOwner) { min ->
                    if (min != null) tvLowest.text = cf.displayDollars(min)
                }
            transactionAdapter = null
            transactionAdapter =
                TransactionAnalysisAdapter(
                    mainActivity,
                    mView
                )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = transactionAdapter
            }
            activity.let {
                transactionViewModel.getActiveTransactionByAccount(
                    account.accountId, startDate, endDate
                ).observe(viewLifecycleOwner) { transactionList ->
                    transactionAdapter!!.differ.submitList(transactionList)
                    populateAnalysisFromAccount(transactionList, totalCredits, totalDebits)
                    updateUiHelpText(transactionList)
                }
            }
        }
    }

    private fun populateAnalysisFromBudgetRuleAndDates(startDate: String, endDate: String) {
        val budgetRule =
            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                tvBudgetRule.text = budgetRule.budgetRuleName
                tvAccount.text = getString(R.string.no_account_selected)
                transactionViewModel.getSumTransactionByBudgetRule(
                    budgetRule.ruleId,
                    startDate, endDate
                ).observe(viewLifecycleOwner) { sum ->
                    if (sum != null) {
                        tvTotalCredits.text = cf.displayDollars(sum)
                        lblTotalCredits.text = getString(R.string.total)
                        lblTotalDebits.visibility = View.GONE
                        tvTotalDebits.visibility = View.GONE
                    }
                }
                transactionViewModel.getMaxTransactionByBudgetRule(
                    budgetRule.ruleId,
                    startDate, endDate
                ).observe(
                    viewLifecycleOwner
                ) { max ->
                    if (max != null) tvHighest.text = cf.displayDollars(max)
                }
                transactionViewModel.getMinTransactionByBudgetRule(
                    budgetRule.ruleId,
                    startDate, endDate
                ).observe(
                    viewLifecycleOwner
                ) { min ->
                    if (min != null) tvLowest.text = cf.displayDollars(min)
                }
                transactionAdapter = null
                transactionAdapter =
                    TransactionAnalysisAdapter(
                        mainActivity,
                        mView
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
                        transactionAdapter!!.differ.submitList(
                            transactionList
                        )
                        populateAnalysisFromBudgetRuleOrSearch(transactionList)
                        updateUiHelpText(transactionList)
                    }
                }
            }
        }

    }

    private fun populateAnalysisFromBudgetRuleOrSearch(
        transList: List<TransactionDetailed>,
    ) {
        if (transList.isNotEmpty()) {
            binding.apply {
                CoroutineScope(Dispatchers.Main).launch {
                    Log.d(TAG, "Total of transactions is ${transList.count()}")
                    var totals = 0.0
                    for (trans in transList) {
                        totals += trans.transaction!!.transAmount
                    }
                    delay(WAIT_250)
                    val endDate = transList.first().transaction!!.transDate
                    val startDate = transList.last().transaction!!.transDate
                    val months =
                        nf.getMonthsBetween(startDate, endDate)
                    Log.d(TAG, "Number of months is $months")
                    lblAverage.text = getString(R.string.average)
                    tvAverage.text =
                        cf.displayDollars(totals / months)

                    tvRecent.text =
                        cf.displayDollars(
                            transList.first().transaction!!.transAmount
                        )
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

    private fun populateAnalysisFromBudgetRule() {
        val budgetRule =
            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!
        binding.apply {
            tvBudgetRule.text =
                budgetRule.budgetRuleName
            tvAccount.text =
                getString(R.string.no_account_selected)
            transactionViewModel.getSumTransactionByBudgetRule(
                budgetRule.ruleId
            ).observe(viewLifecycleOwner) { sum ->
                if (sum != null) {
                    tvTotalCredits.text = cf.displayDollars(sum)
                    lblTotalCredits.text =
                        getString(R.string.total)
                    lblTotalDebits.visibility = View.GONE
                    tvTotalDebits.visibility = View.GONE
                }
            }
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
                    if (min != null) tvLowest.text = cf.displayDollars(min)
                }
            transactionAdapter = null
            transactionAdapter =
                TransactionAnalysisAdapter(
                    mainActivity,
                    mView
                )
            rvTransactions.apply {
                layoutManager = LinearLayoutManager(
                    requireContext()
                )
                adapter = transactionAdapter
            }
            transactionViewModel.getActiveTransactionsDetailed(
                budgetRule.ruleId
            ).observe(
                viewLifecycleOwner
            ) { transactionList ->
                transactionAdapter!!.differ.submitList(transactionList)
                populateAnalysisFromBudgetRuleOrSearch(transactionList)
                updateUiHelpText(transactionList)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}