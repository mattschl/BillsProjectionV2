package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetListAnnualAdapter
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetListMonthlyAdapter
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetListOccasionalAdapter
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetListBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

//private const val TAG = FRAG_BUDGET_LIST

class BudgetListFragment : Fragment(R.layout.fragment_budget_list) {
    private var _binding: FragmentBudgetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var accountViewModel: AccountViewModel
    private var monthlyVisible = false
    private var occasionalVisible = false
    private var annualVisible = false
    private val budgetsMonthly = ArrayList<BudgetRuleComplete>()
    private val budgetsOccasional = ArrayList<BudgetRuleComplete>()
    private val budgetsAnnual = ArrayList<BudgetRuleComplete>()
    val cf = CommonFunctions()
    val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetListBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        accountViewModel = mainActivity.accountViewModel
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.title = "View the complete budget"
        createActions()
    }

    private fun createActions() {
        binding.apply {
            imgMonthlyArrow.setOnClickListener {
                toggleMonthly()
            }
            imgOccasionalArrow.setOnClickListener {
                toggleOccasional()
            }
            imgAnnualArrow.setOnClickListener {
                toggleAnnual()
            }
        }
    }

    private fun toggleAnnual() {
        binding.apply {
            if (annualVisible) {
                annualVisible = false
                rvAnnual.visibility = View.GONE
                crdSummaryAnnual.visibility = View.GONE
                imgAnnualArrow.setImageResource(R.drawable.ic_arrow_down_24)
            } else {
                annualVisible = true
                rvAnnual.visibility = View.VISIBLE
                crdSummaryAnnual.visibility = View.VISIBLE
                imgAnnualArrow.setImageResource(R.drawable.ic_arrow_up_24)
                fillAnnual()
            }
        }
    }

    private fun fillAnnual() {
        activity.let {
            val budgetListAnnualAdapter = BudgetListAnnualAdapter(
                mainViewModel, budgetRuleViewModel, mView
            )
            binding.rvAnnual.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = budgetListAnnualAdapter
            }
            budgetRuleViewModel.getBudgetRulesCompletedAnnually(
                df.getCurrentDateAsString()
            ).observe(viewLifecycleOwner) { rules ->
                budgetsAnnual.clear()
                rules.listIterator().forEach {
                    budgetsAnnual.add(it)
                }
                budgetListAnnualAdapter.differ.submitList(budgetsAnnual)
                fillAnnualTotals()
            }
        }
    }

    private fun fillAnnualTotals() {
        binding.apply {
            var totalCredits = 0.0
            var totalDebits = 0.0
            for (budget in budgetsAnnual) {
                val amt = budget.budgetRule!!.budgetAmount / 12 /
                        budget.budgetRule!!.budFrequencyCount
                if (budget.toAccount!!.accountType!!.displayAsAsset) {
                    totalCredits += amt
                }
                if (budget.fromAccount!!.accountType!!.displayAsAsset) {
                    totalDebits += amt
                }
                var info = "Credits: " + cf.displayDollars(totalCredits)
                tvDebitsAnnual.text = info
                info = "Debits: " + cf.displayDollars(totalDebits)
                tvCreditsAnnual.text = info
                if (totalCredits >= totalDebits) {
                    info = "Surplus of " + cf.displayDollars(totalCredits - totalDebits)
                    tvTotalOccasional.setTextColor(Color.BLACK)
                } else {
                    info = "DEFICIT of " + cf.displayDollars(totalDebits - totalCredits)
                    tvTotalOccasional.setTextColor(Color.RED)
                }
                tvTotalAnnual.text = info
            }
        }
    }

    private fun toggleOccasional() {
        binding.apply {
            if (occasionalVisible) {
                occasionalVisible = false
                rvOccasional.visibility = View.GONE
                crdSummaryOccasional.visibility = View.GONE
                imgOccasionalArrow.setImageResource(R.drawable.ic_arrow_down_24)
            } else {
                occasionalVisible = true
                rvOccasional.visibility = View.VISIBLE
                crdSummaryOccasional.visibility = View.VISIBLE
                imgOccasionalArrow.setImageResource(R.drawable.ic_arrow_up_24)
                fillOccasional()
            }
        }
    }

    private fun fillOccasional() {
        activity.let {
            val budgetListOccasionalAdapter = BudgetListOccasionalAdapter(
                mainViewModel, budgetRuleViewModel, mView
            )
            binding.rvOccasional.apply {
                layoutManager =
                    LinearLayoutManager(requireContext())
                adapter = budgetListOccasionalAdapter
            }
            budgetRuleViewModel.getBudgetRulesCompletedOccasional(
                df.getCurrentDateAsString()
            ).observe(viewLifecycleOwner) { rules ->
                budgetsOccasional.clear()
                rules.listIterator().forEach {
                    budgetsOccasional.add(it)
                }
                budgetListOccasionalAdapter.differ.submitList(budgetsOccasional)
                fillOccasionalTotals()
            }
        }
    }

    private fun fillOccasionalTotals() {
        binding.apply {
            var totalCredits = 0.0
            var totalDebits = 0.0
            for (budget in budgetsOccasional) {
                val amt = when (budget.budgetRule!!.budFrequencyTypeId) {
                    FREQ_WEEKLY -> {
                        budget.budgetRule!!.budgetAmount * 4 /
                                budget.budgetRule!!.budFrequencyCount
                    }

                    FREQ_MONTHLY -> {
                        budget.budgetRule!!.budgetAmount /
                                budget.budgetRule!!.budFrequencyCount
                    }

                    else -> 0.0
                }
                if (budget.toAccount!!.accountType!!.displayAsAsset) {
                    totalCredits += amt
                }
                if (budget.fromAccount!!.accountType!!.displayAsAsset) {
                    totalDebits += amt
                }
                var info = "Credits: " + cf.displayDollars(totalCredits)
                tvCreditsOccasional.text = info
                info = "Debits: " + cf.displayDollars(totalDebits)
                tvDebitsOccasional.text = info
                if (totalCredits >= totalDebits) {
                    info = "Surplus of " + cf.displayDollars(totalCredits - totalDebits)
                    tvTotalOccasional.setTextColor(Color.BLACK)
                } else {
                    info = "DEFICIT of " + cf.displayDollars(totalDebits - totalCredits)
                    tvTotalOccasional.setTextColor(Color.RED)
                }
                tvTotalOccasional.text = info
            }
        }
    }

    private fun toggleMonthly() {
        binding.apply {
            if (monthlyVisible) {
                monthlyVisible = false
                rvMonthly.visibility = View.GONE
                crdSummaryMonthly.visibility = View.GONE
                imgMonthlyArrow.setImageResource(R.drawable.ic_arrow_down_24)
            } else {
                monthlyVisible = true
                rvMonthly.visibility = View.VISIBLE
                crdSummaryMonthly.visibility = View.VISIBLE
                imgMonthlyArrow.setImageResource(R.drawable.ic_arrow_up_24)
                fillMonthly()
            }
        }
    }

    private fun fillMonthly() {
        activity?.let {
            val budgetListMonthlyAdapter = BudgetListMonthlyAdapter(
                mainViewModel, budgetRuleViewModel, mView,
            )
            binding.rvMonthly.apply {
                layoutManager =
                    LinearLayoutManager(requireContext())
                adapter = budgetListMonthlyAdapter
            }
            budgetRuleViewModel.getBudgetRulesCompleteMonthly(df.getCurrentDateAsString())
                .observe(viewLifecycleOwner) { rules ->
                    budgetsMonthly.clear()
                    rules.listIterator().forEach {
                        budgetsMonthly.add(it)
                    }
                    budgetListMonthlyAdapter.differ.submitList(budgetsMonthly)
                    fillMonthlyTotals()
                }
        }
    }

    private fun fillMonthlyTotals() {
        binding.apply {
            var totalCredits = 0.0
            var totalDebits = 0.0
            var totalFixed = 0.0
            for (budget in budgetsMonthly) {
                val amt = when (budget.budgetRule!!.budFrequencyTypeId) {
                    FREQ_WEEKLY -> {
                        budget.budgetRule!!.budgetAmount * 4 /
                                budget.budgetRule!!.budFrequencyCount
                    }

                    FREQ_MONTHLY -> {
                        budget.budgetRule!!.budgetAmount
                    }

                    else -> {
                        0.0
                    }
                }
                if (budget.toAccount!!.accountType!!.displayAsAsset) {
                    totalCredits += amt
                    if (budget.budgetRule!!.budFixedAmount) {
                        totalFixed += amt
                    }
                }
                if (budget.fromAccount!!.accountType!!.displayAsAsset) {
                    totalDebits += amt
                    if (budget.budgetRule!!.budFixedAmount) {
                        totalFixed -= amt
                    }
                }

                var info = "Credits: " + cf.displayDollars(totalCredits)
                tvCreditsMonthly.text = info
                info = "Debits: " + cf.displayDollars(totalDebits)
                tvDebitsMonthly.text = info
                info = "Fixed: " + cf.displayDollars(totalFixed)
                tvFixedMonthly.text = info
                if (totalCredits >= totalDebits) {
                    info = "Surplus of " + cf.displayDollars(totalCredits - totalDebits)
                    tvTotalMonthly.setTextColor(Color.BLACK)
                } else {
                    info = "DEFICIT of " + cf.displayDollars(totalDebits - totalCredits)
                    tvTotalMonthly.setTextColor(Color.RED)
                }
                tvTotalMonthly.text = info
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}