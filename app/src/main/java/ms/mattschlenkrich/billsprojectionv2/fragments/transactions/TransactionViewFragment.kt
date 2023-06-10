package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.os.Bundle
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
import ms.mattschlenkrich.billsprojectionv2.FRAG_TRANSACTIONS
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.TransactionAdapter
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionViewBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANSACTIONS

class TransactionViewFragment :
    Fragment(R.layout.fragment_transaction_view),
    SearchView.OnQueryTextListener {

    private var _binding: FragmentTransactionViewBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private val args: TransactionViewFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // keep for later
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel =
            mainActivity.transactionViewModel
        mainActivity.title = "View Transaction History"
        setupRecyclerView()
        binding.fabAddTransaction.setOnClickListener {
            val direction = TransactionViewFragmentDirections
                .actionTransactionViewFragmentToTransactionAddFragment(
                    null,
                    TAG
                )
            mView!!.findNavController().navigate(direction)
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            TAG
        )

        binding.rvTransactions.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            adapter = transactionAdapter
        }
        activity.let {
            transactionViewModel.getActiveTransactionsDetailed()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
            searchTransactions(newText)
        }
        return true
    }

    private fun searchTransactions(query: String) {
        val searchQuery = "%$query%"
        //update with a new query
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}