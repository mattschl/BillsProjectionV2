package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewAddBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetViewViewModel

class BudgetViewAddFragment : Fragment(
    R.layout.fragment_budget_view_add
) {

    private var _binding: FragmentBudgetViewAddBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetViewViewModel: BudgetViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetViewAddBinding.inflate(
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
        mainActivity.title = "Add a new Budget Item"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}