package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetListMonthlyAdapter
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetListBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_BUDGET_LIST

class BudgetListFragment : Fragment(R.layout.fragment_budget_list) {
    private var _binding: FragmentBudgetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var accountViewModel: AccountViewModel
    private var monthlyVisible = false
    private val budgetsMonthly = ArrayList<BudgetRuleComplete>()
    private val budgetsOccasionally = ArrayList<BudgetRuleComplete>()
    private val budgetsYearly = ArrayList<BudgetRuleComplete>()
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
        }
    }

    private fun toggleMonthly() {
        binding.apply {
            if (monthlyVisible) {
                monthlyVisible = false
                rvMonthly.visibility = View.GONE
                crdSummaryMonthly.visibility = View.GONE
                imgMonthlyArrow.setImageResource(R.drawable.ic_arrow_up_24)
            } else {
                monthlyVisible = true
                rvMonthly.visibility = View.VISIBLE
                crdSummaryMonthly.visibility = View.VISIBLE
                imgMonthlyArrow.setImageResource(R.drawable.ic_arrow_down_24)
                fillMonthly()
            }
        }
    }

    private fun fillMonthly() {
        activity?.let {
            //create and load a budgetNotablyAdapter
            val budgetListAdapter = BudgetListMonthlyAdapter(
                mainActivity, accountViewModel, mainViewModel, mView,
            )
            binding.rvMonthly.apply {
                layoutManager =
                    LinearLayoutManager(requireContext())
                adapter = budgetListAdapter
            }
            budgetRuleViewModel.getBudgetRulesCompleteMonthly(df.getCurrentDateAsString())
                .observe(viewLifecycleOwner) { rules ->
                    budgetsMonthly.clear()
                    rules.listIterator().forEach {
                        budgetsMonthly.add(it)
                        Log.d(
                            TAG, "rule is ${it.budgetRule!!.budgetRuleName} " +
                                    "toAccount asset is ${it.toAccount!!.accountType!!.isAsset} " +
                                    "fromAccount asset is ${it.fromAccount!!.accountType!!.isAsset}"
                        )
                    }
                    budgetListAdapter.differ.submitList(budgetsMonthly)
                    fillMonthlyTotals()
                }
        }
    }

    private fun fillMonthlyTotals() {
        binding.apply {
            var totalCredits = 0.0
            var totalDebits = 0.0
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
                }
                if (budget.fromAccount!!.accountType!!.displayAsAsset) {
                    totalDebits += amt
                }
                var info = "Credits: " + cf.displayDollars(totalCredits)
                tvCreditsMonthly.text = info
                info = "Debits: " + cf.displayDollars(totalDebits)
                tvDebitsMonthly.text = info
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