package ms.mattschlenkrich.billsprojectionv2.ui.accounts

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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountsBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter.AccountChooseAdapter

private const val TAG = FRAG_ACCOUNT_CHOOSE

class AccountChooseFragment :
    Fragment(R.layout.fragment_accounts),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountChooseAdapter: AccountChooseAdapter
    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountsBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.title = getString(R.string.choose_an_account)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setUpRecyclerView()
        setClickActions()
    }

    private fun setUpRecyclerView() {
        accountChooseAdapter = AccountChooseAdapter(
            mainActivity,
            TAG,
            this,
        )
        binding.rvAccounts.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = accountChooseAdapter
        }
        activity?.let {
            accountViewModel.getAccountsWithType().observe(
                viewLifecycleOwner
            ) { accountWithType ->
                accountChooseAdapter.differ.submitList(accountWithType)
                updateUI(accountWithType)
            }
        }
    }

    private fun updateUI(account: List<AccountWithType>) {
        if (account.isNotEmpty()) {
            binding.crdAccountView.visibility = View.GONE
            binding.rvAccounts.visibility = View.VISIBLE
        } else {
            binding.crdAccountView.visibility = View.VISIBLE
            binding.rvAccounts.visibility = View.GONE
        }
    }

    private fun setClickActions() {
        binding.fabAddNewAccount.setOnClickListener { gotoAccountAddFragment() }
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
            searchAccount(newText)
        }
        return true
    }

    private fun searchAccount(query: String?) {
        val searchQuery = "%$query%"
        accountViewModel.searchAccountsWithType(searchQuery)
            .observe(this) { list -> accountChooseAdapter.differ.submitList(list) }
    }

    private fun gotoAccountAddFragment() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToAccountAddFragment()
        )
    }

    fun gotoTransactionAverageFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionAnalysisFragment()
        )
    }

    fun gotoTransactionPerformFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToTransactionPerformFragment()
        )
    }

    fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToTransactionSplitFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToTransactionAddFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToBudgetRuleUpdateFragment()
        )
    }

    fun gotoBudgetRuleAddFragment() {
        mView.findNavController().navigate(
            AccountChooseFragmentDirections
                .actionAccountChooseFragmentToBudgetRuleAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}