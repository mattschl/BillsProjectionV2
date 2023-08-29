package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

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
import ms.mattschlenkrich.billsprojectionv2.adapter.AccountAdapter
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountsBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNTS

class AccountsFragment :
    Fragment(R.layout.fragment_accounts),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var mView: View

    private val args: AccountsFragmentArgs by navArgs()


//    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
//    private val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
//    private val timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountsBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        return mView
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated entered")
        accountsViewModel =
            mainActivity.accountViewModel
        mainActivity.title = "Choose an Account"
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setUpRecyclerView()
        binding.fabAddNewAccount.setOnClickListener {
            addNewAccount()
        }

    }

    private fun addNewAccount() {
        var budgetRuleDetailed: BudgetRuleDetailed? = null
        var requestedAccount: String? = null
        if (args.budgetRuleDetailed != null) {
            budgetRuleDetailed = args.budgetRuleDetailed
            requestedAccount = args.requestedAccount
        }
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = AccountsFragmentDirections
            .actionAccountsFragmentToAccountAddFragment(
                args.budgetItem,
                args.transaction,
                budgetRuleDetailed, null, null,
                requestedAccount, fragmentChain
            )
        mView.findNavController().navigate(direction)
    }


    private fun setUpRecyclerView() {
        val fragmentChain = "${args.callingFragments}, $TAG"

        accountAdapter = AccountAdapter(
            args.budgetItem,
            args.transaction,
            args.budgetRuleDetailed,
            args.requestedAccount,
            fragmentChain
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
            accountsViewModel.getAccountsWithType().observe(
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

//    override fun onCreateOptionsMenu(
//        menu: Menu, inflater: MenuInflater
//    ) {
////        super.onCreateOptionsMenu(menu, inflater)
//
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
            searchAccount(newText)
        }
        return true
    }

    private fun searchAccount(query: String?) {
        val searchQuery = "%$query%"
        accountsViewModel.searchAccountsWithType(searchQuery).observe(
            this
        ) { list -> accountAdapter.differ.submitList(list) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}