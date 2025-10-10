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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetRuleBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter.BudgetRuleChooseAdapter

private const val TAG = FRAG_BUDGET_RULE_CHOOSE

class BudgetRuleChooseFragment : Fragment(R.layout.fragment_budget_rule),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentBudgetRuleBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var budgetRuleChooseAdapter: BudgetRuleChooseAdapter
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
        mainActivity.title = getString(R.string.choose_a_budget_rule)
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
        setClickActions()
    }

    private fun removeFragmentReference() {
        mainActivity.mainViewModel.removeCallingFragment(TAG)
    }

    private fun setupRecyclerView() {
        budgetRuleChooseAdapter = BudgetRuleChooseAdapter(
            mainActivity,
            mView,
            TAG,
            this@BudgetRuleChooseFragment,
        )
        binding.rvBudgetRules.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
            adapter = budgetRuleChooseAdapter
        }
        activity.let {
            mainActivity.budgetRuleViewModel.getActiveBudgetRulesDetailed().observe(
                viewLifecycleOwner
            ) { budgetRuleList ->
                budgetRuleChooseAdapter.differ.submitList(
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

    private fun setClickActions() {
        binding.fabAddNew.setOnClickListener { gotoBudgetRuleAdd() }
        setMenu()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()
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
        ) { list -> budgetRuleChooseAdapter.differ.submitList(list) }
    }

    private fun gotoBudgetRuleAdd() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(null)
        gotoBudgetRuleAddFragment()
    }

    private fun gotoBudgetRuleAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleChooseFragmentDirections.actionBudgetRuleChooseFragmentToBudgetRuleAddFragment()
        )
    }

    fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleChooseFragmentDirections.actionBudgetRuleChooseFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleChooseFragmentDirections.actionBudgetRuleChooseFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleChooseFragmentDirections.actionBudgetRuleChooseFragmentToTransactionAddFragment()
        )
    }

    fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            BudgetRuleChooseFragmentDirections.actionBudgetRuleChooseFragmentToTransactionSplitFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}