package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

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
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.AccountTypeAdapter
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypesBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_ACCOUNT_TYPES

class AccountTypesFragment
    : Fragment(R.layout.fragment_account_types),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentAccountTypesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountTypeAdapter: AccountTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountTypesBinding.inflate(
            inflater, container, false
        )
//        Log.d(TAG, "$TAG is entered")
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.title = "Choose an Account Type"
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d(TAG, "onViewCreated Entered")
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setupRecyclerView()
        binding.fabAddAccountType.setOnClickListener {
            gotoAccountTypeAddFragment(it)
        }
    }

    private fun gotoAccountTypeAddFragment(it: View) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        val direction = AccountTypesFragmentDirections
            .actionAccountTypesFragmentToAccountTypeAddFragment()
        it.findNavController().navigate(direction)
    }

    private fun setupRecyclerView() {
        accountTypeAdapter = AccountTypeAdapter(
            mainViewModel
        )

        binding.rvAccountTypes.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = accountTypeAdapter
        }
        activity?.let {
            accountViewModel.getActiveAccountTypes().observe(
                viewLifecycleOwner
            ) { accountType ->
                accountTypeAdapter.differ.submitList(accountType)
                updateUI(accountType)

            }
        }
    }

    private fun updateUI(accountType: List<AccountType>) {
        if (accountType.isNotEmpty()) {
            binding.crdNoAccountTypes.visibility = View.GONE
            binding.rvAccountTypes.visibility = View.VISIBLE
        } else {
            binding.crdNoAccountTypes.visibility = View.VISIBLE
            binding.rvAccountTypes.visibility = View.GONE
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
            searchAccountTypes(newText)
        }
        return true
    }

    private fun searchAccountTypes(query: String?) {
        val searchQuery = "%$query%"
        accountViewModel.searchAccountTypes(searchQuery).observe(
            this
        ) { list -> accountTypeAdapter.differ.submitList(list) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}