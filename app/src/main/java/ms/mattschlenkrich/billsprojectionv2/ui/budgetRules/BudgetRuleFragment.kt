package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.os.Bundle
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter.BudgetRuleAdapter

private const val TAG = FRAG_BUDGET_RULES

class BudgetRuleFragment :
    Fragment(R.layout.fragment_budget_rule),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentBudgetRuleBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetRuleAdapter: BudgetRuleAdapter
    private lateinit var mView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Choose a Budget Rule"
//        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        return mView
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        removeFragmentReference()
        setupRecyclerView()
        createClickActions()
    }

    private fun setupRecyclerView() {
        budgetRuleAdapter = BudgetRuleAdapter(
            mainActivity.budgetRuleViewModel,
            mainActivity.mainViewModel,
            mView,
        )
        binding.rvBudgetRules.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            adapter = budgetRuleAdapter
        }
        activity.let {
            mainActivity.budgetRuleViewModel.getActiveBudgetRulesDetailed().observe(
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

    private fun createClickActions() {
        binding.fabAddNew.setOnClickListener {
            gotoBudgetRuleAddFragment()
        }
        setMenu()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        mainActivity.budgetRuleViewModel.searchBudgetRules(searchQuery).observe(
            this
        ) { list -> budgetRuleAdapter.differ.submitList(list) }
    }

    private fun gotoBudgetRuleAddFragment() {
        mainActivity.mainViewModel.setBudgetRuleDetailed(null)
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        val direction = BudgetRuleFragmentDirections
            .actionBudgetRuleFragmentToBudgetRuleAddFragment()
        mView.findNavController().navigate(direction)
    }

    private fun removeFragmentReference() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}