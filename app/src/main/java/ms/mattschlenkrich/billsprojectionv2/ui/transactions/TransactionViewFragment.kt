package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionViewBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.adapter.TransactionAdapter

private const val TAG = FRAG_TRANSACTION_VIEW

class TransactionViewFragment :
    Fragment(R.layout.fragment_transaction_view),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentTransactionViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = "View Transaction History"
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setupRecyclerView()
        setClickActions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            mainActivity,
            mainActivity.mainViewModel,
            mView,
        )
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(
                requireContext()
            )
            adapter = transactionAdapter
        }
        activity.let {
            mainActivity.transactionViewModel.getActiveTransactionsDetailed()
                .observe(
                    viewLifecycleOwner
                ) { transactionList ->
                    transactionAdapter.differ.submitList(
                        transactionList
                    )
                    updateUI(transactionList)
                }
        }
    }

    private fun updateUI(transactionList: List<TransactionDetailed>?) {
        binding.apply {
            if (transactionList!!.isNotEmpty()) {
                crdTransactionView.visibility = View.GONE
                rvTransactions.visibility = View.VISIBLE
            } else {
                crdTransactionView.visibility = View.VISIBLE
                rvTransactions.visibility = View.GONE
            }
        }
    }

    private fun setClickActions() {
        binding.fabAddTransaction.setOnClickListener {
            gotoAddTransactionFragment()
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
            searchTransactions(newText)
        }
        return true
    }

    private fun searchTransactions(query: String) {
        val searchQuery = "%$query%"
        mainActivity.transactionViewModel
            .searchActiveTransactionsDetailed(searchQuery)
            .observe(
                this
            ) { list ->
                transactionAdapter.differ.submitList(list)
            }
    }

    private fun gotoAddTransactionFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainActivity.mainViewModel.setTransactionDetailed(null)
        val direction = TransactionViewFragmentDirections
            .actionTransactionViewFragmentToTransactionAddFragment()
        mView.findNavController().navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}