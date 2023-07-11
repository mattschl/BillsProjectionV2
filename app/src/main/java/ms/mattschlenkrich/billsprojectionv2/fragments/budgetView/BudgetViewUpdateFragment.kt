package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetViewViewModel

private const val TAG = FRAG_BUDGET_RULE_UPDATE

class BudgetViewUpdateFragment : Fragment(
    R.layout.fragment_budget_rule_update
) {

    private var _binding: FragmentBudgetViewUpdateBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetViewViewModel: BudgetViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "building BudgetViewAddFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetViewUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetViewViewModel =
            mainActivity.budgetViewViewModel
        mainActivity.title = "Update this Budget Item"

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}