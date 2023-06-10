package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionViewBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

class TransactionViewFragment :
    Fragment(R.layout.fragment_transaction_view) {

    private var _binding: FragmentTransactionViewBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel

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
                .actionTransactionViewFragmentToTransactionAddFragment()
            mView!!.findNavController().navigate(direction)
        }
    }

    private fun setupRecyclerView() {
        //TODO make the recycler
    }

}