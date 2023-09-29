package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAverageBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

class TransactionAverageFragment : Fragment(
    R.layout.fragment_transaction_average
) {

    private var _binding: FragmentTransactionAverageBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAverageBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        mView = binding.root
        return binding.root

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}