package ms.mattschlenkrich.billsprojectionv2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding


class AccountTypeAddFragment : Fragment() {

    private lateinit var _binding: FragmentAccountTypeAddBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //might need
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountTypeAddBinding.inflate(inflater, container, false)
        return binding.root
    }

}