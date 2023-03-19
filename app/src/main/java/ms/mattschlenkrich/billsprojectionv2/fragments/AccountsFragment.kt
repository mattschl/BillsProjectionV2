package ms.mattschlenkrich.billsprojectionv2.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.AccountAdapter
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountsBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

class AccountsFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel

        setUpRecyclerView()
        binding.fabAddNewAccount.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_accountsFragment_to_accountAddFragment
            )
        }

    }


    private fun setUpRecyclerView() {
        accountAdapter = AccountAdapter()

        binding.rvAccounts.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = accountAdapter
        }
        activity?.let {
            accountsViewModel.getAllAccounts().observe(
                viewLifecycleOwner
            ) { account ->
                accountAdapter.differ.submitList(account)
                updateUI(account)

            }
        }
    }

    private fun updateUI(account: List<Account>) {
        if (account != null) {
            if (account.isNotEmpty()) {
                binding.crdAccountView.visibility = View.GONE
                binding.rvAccounts.visibility = View.VISIBLE
            } else {
                binding.crdAccountView.visibility = View.VISIBLE
                binding.rvAccounts.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateOptionsMenu(
        menu: Menu, inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)

        val mMenuSearch = menu.findItem(R.id.menu_search) as SearchView
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
        val searchQuery = "%$query"
        accountsViewModel.searchAccounts(searchQuery).observe(
            this
        ) { list -> accountAdapter.differ.submitList(list) }
    }
}