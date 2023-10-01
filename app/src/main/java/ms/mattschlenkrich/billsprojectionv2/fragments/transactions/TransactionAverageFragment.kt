package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAverageBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAverageFragment : Fragment(
    R.layout.fragment_transaction_average
) {

    private var _binding: FragmentTransactionAverageBinding? = null
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
        _binding = FragmentTransactionAverageBinding.inflate(
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
    }

    private fun setStartValues() {
        if (mainViewModel.getBudgetRuleDetailed() != null) {
            fillFromBudgetRule()
        } else if (mainViewModel.getAccountWithType() != null) {
            fillFromAccount()
        }
    }

    private fun fillFromAccount() {
        TODO("Not yet implemented")
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
        val startDate = df.getFirstOfMonth(
            df.getCurrentDateAsString()
        )
        val endDate = df.getCurrentDateAsString()
        if (mainViewModel.getBudgetRuleDetailed() != null) {
            fillFromBudgetRuleAndDates(startDate, endDate)
        } else if (mainViewModel.getAccountWithType() != null) {
            fillFromAccountAndDates(startDate, endDate)
        }
    }

    private fun fillFromAccountAndDates(startDate: String, endDate: String) {

    }

    private fun fillFromBudgetRuleAndDates(startDate: String, endDate: String) {
        var total = 0.0
        val transList = ArrayList<TransactionDetailed>()
        val budgetRule =
            mainViewModel.getBudgetRuleDetailed()!!.budgetRule!!
        binding.apply {
            tvBudgetRule.text =
                budgetRule.budgetRuleName
            tvAccount.text =
                getString(R.string.no_account_selected)

            transactionViewModel.getMaxTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { max ->
                tvHighest.text =
                    cf.displayDollars(max)
            }
            transactionViewModel.getMinTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(
                viewLifecycleOwner
            ) { min ->
                tvLowest.text =
                    cf.displayDollars(min)
            }
            transactionViewModel.getSumTransactionByBudgetRule(
                budgetRule.ruleId,
                startDate, endDate
            ).observe(viewLifecycleOwner) { sum ->
                tvTotalCredits.text =
                    cf.displayDollars(sum)
                total = sum
                lblTotalCredits.text =
                    getString(R.string.total)
                lblTotalDebits.visibility = View.GONE
                tvTotalDebits.visibility = View.GONE
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
                    delay(250)
                    val months =
                        df.getMonthsBetween(startDate, endDate)
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
                tvHighest.text =
                    cf.displayDollars(max)
            }
            transactionViewModel.getMinTransactionByBudgetRule(
                budgetRule.ruleId
            ).observe(
                viewLifecycleOwner
            ) { min ->
                tvLowest.text =
                    cf.displayDollars(min)
            }
            transactionViewModel.getSumTransactionByBudgetRule(
                budgetRule.ruleId
            ).observe(viewLifecycleOwner) { sum ->
                tvTotalCredits.text =
                    cf.displayDollars(sum)
                total = sum
                lblTotalCredits.text =
                    getString(R.string.total)
                lblTotalDebits.visibility = View.GONE
                tvTotalDebits.visibility = View.GONE
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
                    delay(250)
                    endDate = transList.first()
                        .transaction!!.transDate
                    startDate = transList.last()
                        .transaction!!.transDate
                    val months =
                        df.getMonthsBetween(startDate, endDate)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}