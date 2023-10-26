package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetListBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
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
    private var monthlyVisible = false
    private val budgetsMonthly = ArrayList<BudgetRuleDetailed>()
    private val budgetsOccasionally = ArrayList<BudgetRuleDetailed>()
    private val budgetsYearly = ArrayList<BudgetRuleDetailed>()

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
            } else {
                monthlyVisible = true
                rvMonthly.visibility = View.VISIBLE
                fillMonthly()
            }
        }
    }

    private fun fillMonthly() {
        activity?.let {
            //create and load a budgetNotablyAdapter
            budgetRuleViewModel.getActiveBudgetRulesDetailed()
                .observe(viewLifecycleOwner) { rules ->
                    budgetsMonthly.clear()
                    rules.listIterator().forEach {
                        if (it.budgetRule!!.budFrequencyTypeId == FREQ_MONTHLY
                            && it.budgetRule!!.budFrequencyCount == 1
                        ) {
                            budgetsMonthly.add(it)
                        }
                        if (it.budgetRule!!.budFrequencyTypeId == FREQ_WEEKLY
                            && it.budgetRule!!.budFrequencyCount >= 4
                        ) {
                            budgetsMonthly.add(it)
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