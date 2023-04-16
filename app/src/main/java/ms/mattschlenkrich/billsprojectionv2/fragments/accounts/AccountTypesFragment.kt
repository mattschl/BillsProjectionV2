package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.AccountTypeAdapter
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypesBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNT_TYPES

class AccountTypesFragment
    : Fragment(R.layout.fragment_account_types),
    SearchView.OnQueryTextListener {

    private var _binding: FragmentAccountTypesBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountTypeAdapter: AccountTypeAdapter
    private val args: AccountTypesFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountTypesBinding.inflate(
            inflater, container, false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated Entered")
        accountViewModel = (activity as MainActivity).accountViewModel

        setupRecyclerView()
        binding.fabAddAccountType.setOnClickListener {
            val direction = AccountTypesFragmentDirections
                .actionAccountTypesFragmentToAccountTypeAddFragment(
                    args.account,
                    args.callingFragment
                )
            it.findNavController().navigate(direction)
        }
    }

    private fun setupRecyclerView() {
        accountTypeAdapter = AccountTypeAdapter(args.account, args.callingFragment)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch =
            menu.findItem(R.id.menu_search).actionView as SearchView
        mMenuSearch.isSubmitButtonEnabled = false
        mMenuSearch.setOnQueryTextListener(this)
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