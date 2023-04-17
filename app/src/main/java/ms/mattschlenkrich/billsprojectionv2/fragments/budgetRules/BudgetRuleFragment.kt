package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetRuleAdapter
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel

private const val TAG = FRAG_BUDGET_RULES

class BudgetRuleFragment :
    Fragment(R.layout.fragment_budget_rule) {

    private var _binding: FragmentBudgetRuleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BudgetRuleViewModel
    private lateinit var budgetRuleAdapter: BudgetRuleAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            (activity as MainActivity).budgetRuleViewModel
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        TODO("Not yet implemented")
    }
}