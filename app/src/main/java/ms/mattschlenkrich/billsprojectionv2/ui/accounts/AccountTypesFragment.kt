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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypesBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter.AccountTypeAdapter

private const val TAG = FRAG_ACCOUNT_TYPES

class AccountTypesFragment
    : Fragment(R.layout.fragment_account_types),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentAccountTypesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
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
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_an_account_type)
        return mView
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d(TAG, "onViewCreated Entered")
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setupRecyclerView()
        setClickActions()
    }

    private fun setupRecyclerView() {
        accountTypeAdapter = AccountTypeAdapter(
            mainActivity,
            mView,
            TAG,
            this@AccountTypesFragment,
        )

        binding.rvAccountTypes.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = accountTypeAdapter
        }
        accountViewModel.getActiveAccountTypes().observe(
            viewLifecycleOwner
        ) { accountType ->
            accountTypeAdapter.differ.submitList(accountType)
            updateUI(accountType)

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

    private fun setClickActions() {
        binding.fabAddAccountType.setOnClickListener { gotoAccountTypeAdd() }
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

    private fun gotoAccountTypeAdd() {
        mainViewModel.addCallingFragment(TAG)
        gotoAccountYpeAddFragment()
    }

    private fun gotoAccountYpeAddFragment() {
        mView.findNavController().navigate(
            AccountTypesFragmentDirections
                .actionAccountTypesFragmentToAccountTypeAddFragment()
        )
    }

    fun gotoAccountAddFragment() {
        mView.findNavController().navigate(
            AccountTypesFragmentDirections
                .actionAccountTypesFragmentToAccountAddFragment()
        )
    }

    fun gotoAccountUpdateFragment() {
        mView.findNavController().navigate(
            AccountTypesFragmentDirections
                .actionAccountTypesFragmentToAccountUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}