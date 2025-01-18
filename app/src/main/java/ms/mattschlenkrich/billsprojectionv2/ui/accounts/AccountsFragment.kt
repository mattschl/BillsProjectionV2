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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountsBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter.AccountAdapter

private const val TAG = FRAG_ACCOUNTS

class AccountsFragment :
    Fragment(R.layout.fragment_accounts),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var accountAdapter: AccountAdapter
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
        accountAdapter = AccountAdapter(
            mainActivity,
            this@AccountsFragment,
            mainActivity.mainViewModel,
            mView
        )
        binding.rvAccounts.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = accountAdapter
        }
        activity?.let {
            mainActivity.accountViewModel.getAccountsWithType().observe(
                viewLifecycleOwner
            ) { accountWithType ->
                accountAdapter.differ.submitList(accountWithType)
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
        binding.fabAddNewAccount.setOnClickListener {
            gotoAccountAddFragment()
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
        mainActivity.accountViewModel.searchAccountsWithType(searchQuery).observe(
            this
        ) { list -> accountAdapter.differ.submitList(list) }
    }

    private fun gotoAccountAddFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainActivity.mainViewModel.setAccountWithType(null)
        val direction = AccountsFragmentDirections
            .actionAccountsFragmentToAccountAddFragment()
        mView.findNavController().navigate(direction)
    }

    fun gotoAccountUpdateFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToAccountUpdateFragment()
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
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionPerformFragment()
        )
    }

    fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionSplitFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionUpdateFragment()
        )
    }

    fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionAddFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToBudgetRuleUpdateFragment()
        )
    }

    fun gotoBudgetRuleAddFragment() {
        mView.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToBudgetRuleAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}