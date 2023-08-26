package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(
    R.layout.fragment_transaction_view
) {

    private var _binding: FragmentBudgetViewBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetItemViewModel: BudgetItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "building BudgetViewFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        mainActivity.title = "View The Budget"
        binding.fabAddAction.setOnClickListener {
            addAction()
        }
    }

    private fun addAction() {
        AlertDialog.Builder(mView!!.context)
            .setTitle(getString(R.string.choose_an_action))
            .setItems(
                arrayOf(
                    getString(R.string.schedule_a_new_budget_item),
                    getString(R.string.add_an_unscheduled_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        addNewBudgetItem()
                    }

                    1 -> {
                        addNewTransaction()
                    }
                }
            }
            .show()
    }

    private fun addNewTransaction() {
        val fragmentChain = TAG
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToTransactionAddFragment(
                    null,
                    fragmentChain
                )
        findNavController().navigate(direction)
    }

    private fun addNewBudgetItem() {
        val fragmentChain = TAG
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetViewAddFragment2(
                    null,
                    fragmentChain
                )
        findNavController().navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}