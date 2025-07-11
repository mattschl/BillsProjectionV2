package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetListBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter.BudgetListAnnualAdapter
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter.BudgetListMonthlyAdapter
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter.BudgetListOccasionalAdapter

private const val TAG = FRAG_BUDGET_LIST

class BudgetListFragment : Fragment(R.layout.fragment_budget_list) {
    private var _binding: FragmentBudgetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private var monthlyVisible = false
    private var occasionalVisible = false
    private var annualVisible = false
    private val budgetsMonthly = ArrayList<BudgetRuleComplete>()
    private val budgetsOccasional = ArrayList<BudgetRuleComplete>()
    private val budgetsAnnual = ArrayList<BudgetRuleComplete>()
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetListBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.title = getString(R.string.view_the_complete_budget)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createArrowActions()
    }

    private fun createArrowActions() {
        binding.apply {
            imgMonthlyArrow.setOnClickListener { toggleShowMonthlyItems() }
            imgOccasionalArrow.setOnClickListener { toggleShowOccasionalItems() }
            imgAnnualArrow.setOnClickListener { toggleShowAnnualItems() }
        }
    }

    private fun toggleShowAnnualItems() {
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
                populateAnnualBudget()
            }
        }
    }

    private fun populateAnnualBudget() {
        val budgetListAnnualAdapter = BudgetListAnnualAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetListFragment,
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
            populateAnnualTotals()
        }
    }

    private fun populateAnnualTotals() {
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
                var info =
                    getString(R.string.credits_) +
                            nf.displayDollars(totalCredits)
                tvDebitsAnnual.text = info
                info = getString(R.string.debits_) + nf.displayDollars(totalDebits)
                tvCreditsAnnual.text = info
                if (totalCredits >= totalDebits) {
                    info =
                        getString(R.string.surplus_of) +
                                nf.displayDollars(totalCredits - totalDebits)
                    tvTotalOccasional.setTextColor(Color.BLACK)
                } else {
                    info =
                        getString(R.string.deficit_of) +
                                nf.displayDollars(totalDebits - totalCredits)
                    tvTotalOccasional.setTextColor(Color.RED)
                }
                tvTotalAnnual.text = info
            }
        }
    }

    private fun toggleShowOccasionalItems() {
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
                populateOccasionalItems()
            }
        }
    }

    private fun populateOccasionalItems() {
        val budgetListOccasionalAdapter = BudgetListOccasionalAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetListFragment,
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
            populateOccasionalTotals()
        }
    }

    private fun populateOccasionalTotals() {
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
                var info = getString(R.string.credits_) + nf.displayDollars(totalCredits)
                tvCreditsOccasional.text = info
                info =
                    getString(R.string.debits_) + nf.displayDollars(totalDebits)
                tvDebitsOccasional.text = info
                if (totalCredits >= totalDebits) {
                    info =
                        getString(R.string.surplus_of) + nf.displayDollars(totalCredits - totalDebits)
                    tvTotalOccasional.setTextColor(Color.BLACK)
                } else {
                    info =
                        getString(R.string.deficit_of) + nf.displayDollars(totalDebits - totalCredits)
                    tvTotalOccasional.setTextColor(Color.RED)
                }
                tvTotalOccasional.text = info
            }
        }
    }

    private fun toggleShowMonthlyItems() {
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
                populateMonthlyItems()
            }
        }
    }

    private fun populateMonthlyItems() {
        val budgetListMonthlyAdapter = BudgetListMonthlyAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetListFragment,
        )
        binding.rvMonthly.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetListMonthlyAdapter
        }
        budgetRuleViewModel.getBudgetRulesCompleteMonthly(df.getCurrentDateAsString())
            .observe(viewLifecycleOwner) { rules ->
                budgetsMonthly.clear()
                rules.listIterator().forEach {
                    budgetsMonthly.add(it)
                }
                budgetListMonthlyAdapter.differ.submitList(budgetsMonthly)
                populateMonthlyTotals()
            }
    }

    private fun populateMonthlyTotals() {
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

                var info =
                    getString(R.string.credits_) +
                            nf.displayDollars(totalCredits)
                tvCreditsMonthly.text = info
                info =
                    getString(R.string.debits_) +
                            nf.displayDollars(totalDebits)
                tvDebitsMonthly.text = info
                info = getString(R.string.fixed_) + nf.displayDollars(totalFixed)
                tvFixedMonthly.text = info
                if (totalCredits >= totalDebits) {
                    info =
                        getString(R.string.surplus_of) +
                                nf.displayDollars(totalCredits - totalDebits)
                    tvTotalMonthly.setTextColor(Color.BLACK)
                } else {
                    info =
                        getString(R.string.deficit_of) +
                                nf.displayDollars(totalDebits - totalCredits)
                    tvTotalMonthly.setTextColor(Color.RED)
                }
                tvTotalMonthly.text = info
            }
        }
    }

    fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToBudgetRuleUpdateFragment()
        )
    }

    fun gotoTransactionAverageFragment() {
        mView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToTransactionAnalysisFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}