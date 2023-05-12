package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel

private const val TAG = FRAG_BUDGET_RULE_UPDATE

class BudgetRuleUpdateFragment :
    Fragment(R.layout.fragment_budget_rule_update) {

    private var _binding: FragmentBudgetRuleUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private var mView: View? = null
    private val args: BudgetRuleUpdateFragmentArgs by navArgs()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            (activity as MainActivity).budgetRuleViewModel
    }
}