package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetRuleAdapter
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel

private const val TAG = FRAG_BUDGET_RULES

class BudgetRuleFragment :
    Fragment(R.layout.fragment_budget_rule),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentBudgetRuleBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val args: BudgetRuleFragmentArgs by navArgs()
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetRuleAdapter: BudgetRuleAdapter
    private var mView: View? = null


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        budgetRuleViewModel =
            mainActivity.budgetRuleViewModel
        mainActivity.title = "Choose a Budget Rule"
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()
        binding.fabAddNew.setOnClickListener {
            val direction = BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetRuleAddFragment(
                    args.budgetItem,
                    args.transaction,
                    null,
                    args.callingFragments
                )
            mView!!.findNavController().navigate(direction)
        }
    }

    private fun setupRecyclerView() {
        budgetRuleAdapter = BudgetRuleAdapter(
            args.budgetItem,
            args.transaction,
            mView!!.context,
            args.callingFragments
        )

        binding.rvBudgetRules.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            adapter = budgetRuleAdapter
        }
        activity.let {
            budgetRuleViewModel.getActiveBudgetRulesDetailed().observe(
                viewLifecycleOwner
            ) { budgetRuleList ->
                budgetRuleAdapter.differ.submitList(
                    budgetRuleList
                )
                updateUI(budgetRuleList)
            }
        }
    }

    private fun updateUI(budgetRuleDetailed: List<BudgetRuleDetailed>?) {
        if (budgetRuleDetailed!!.isNotEmpty()) {
            binding.crdBudgetRule.visibility = View.GONE
            binding.rvBudgetRules.visibility = View.VISIBLE
        } else {
            binding.crdBudgetRule.visibility = View.VISIBLE
            binding.rvBudgetRules.visibility = View.GONE
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menuInflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch = menu.findItem(R.id.menu_search)
            .actionView as SearchView
        mMenuSearch.isSubmitButtonEnabled = false
        mMenuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }


//    @Deprecated("Deprecated in Java")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
////        super.onCreateOptionsMenu(menu, inflater)
////        menu.clear()
//        inflater.inflate(R.menu.search_menu, menu)
//        val mMenuSearch = menu.findItem(R.id.menu_search)
//            .actionView as SearchView
//        mMenuSearch.isSubmitButtonEnabled = false
//        mMenuSearch.setOnQueryTextListener(this)
//    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchBudgetRules(newText)
        }
        return true
    }

    private fun searchBudgetRules(query: String?) {
        val searchQuery = "%$query%"
        budgetRuleViewModel.searchBudgetRules(searchQuery).observe(
            this
        ) { list -> budgetRuleAdapter.differ.submitList(list) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}