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
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter.BudgetRuleAdapter

private const val TAG = FRAG_BUDGET_RULES

class BudgetRuleFragment : Fragment(R.layout.fragment_budget_rule), SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentBudgetRuleBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetRuleAdapter: BudgetRuleAdapter
    private lateinit var mView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetRuleBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_a_budget_rule)
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

    private fun removeFragmentReference() {
        mainActivity.mainViewModel.removeCallingFragment(TAG)
    }

    private fun setupRecyclerView() {
        budgetRuleAdapter = BudgetRuleAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetRuleFragment,
        )
        binding.rvBudgetRules.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
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
            binding.crdList.visibility = View.GONE
            binding.rvBudgetRules.visibility = View.VISIBLE
        } else {
            binding.crdList.visibility = View.VISIBLE
            binding.rvBudgetRules.visibility = View.GONE
        }
    }

    private fun createClickActions() {
        setMenu()
        binding.fabAddNew.setOnClickListener {
            gotoBudgetRuleAdd()
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch = menu.findItem(R.id.menu_search).actionView as SearchView
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
        budgetRuleViewModel.searchBudgetRules(searchQuery).observe(
            this
        ) { list -> budgetRuleAdapter.differ.submitList(list) }
    }

    private fun gotoBudgetRuleAdd() {
        mainViewModel.setBudgetRuleDetailed(null)
        mainViewModel.addCallingFragment(TAG)
        gotoBudgetRuleAddFragment()
    }

    private fun gotoBudgetRuleAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetRuleAddFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetRuleUpdateFragment()
        )
    }

    fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionAddFragment()
        )
    }

    fun gotoTransactionAverageFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionAnalysisFragment()
        )
    }

    fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionSplitFragment()
        )
    }

    fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}