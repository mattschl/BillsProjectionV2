package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.AccountAdapter
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountsBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNTS

@Suppress("DEPRECATION")
class AccountsFragment :
    Fragment(R.layout.fragment_accounts),
    SearchView.OnQueryTextListener {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var mView: View

    private val args: AccountsFragmentArgs by navArgs()


//    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
//    private val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
//    private val timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountsBinding.inflate(
            inflater, container, false
        )
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
            (activity as MainActivity).accountViewModel

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
        val direction = AccountsFragmentDirections
            .actionAccountsFragmentToAccountAddFragment(
                budgetRuleDetailed, null, null,
                requestedAccount, arrayOf(TAG)
            )
        mView.findNavController().navigate(direction)
    }


    private fun setUpRecyclerView() {

        accountAdapter = AccountAdapter(
            args.budgetRuleDetailed,
            args.requestedAccount,
            args.callingFragments
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
            accountsViewModel.getAccountWithType().observe(
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


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu, inflater: MenuInflater
    ) {
//        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch = menu.findItem(R.id.menu_search)
            .actionView as SearchView
        mMenuSearch.isSubmitButtonEnabled = false
        mMenuSearch.setOnQueryTextListener(this)
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
        accountsViewModel.searchAccountsWithType(searchQuery).observe(
            this
        ) { list -> accountAdapter.differ.submitList(list) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}