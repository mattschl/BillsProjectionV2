package ms.mattschlenkrich.billsprojectionv2.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.*


class AccountTypeAddFragment : Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel

    private lateinit var mView: View

    private val timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountTypeAddBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        mView = view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveAccountType(mView)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveAccountType(view: View) {
        val accountTypeName = binding.etAccTypeAdd.text.toString().trim()
        val keepTotals = binding.chkAccTypeAddKeepTotals.isChecked
        val keepOwing = binding.chkAccTypeAddKeepOwing.isChecked
        val isAsset = binding.chkAccTypeAddIsAsset.isChecked
        val displayAsAsset = binding.chkAccTypeAddDisplayAsset.isChecked
        val currTime = timeFormatter.format(Calendar.getInstance().time)

        if (accountTypeName.isNotEmpty()) {

            val accountType = AccountType(
                0, accountTypeName, keepTotals,
                isAsset, keepOwing, false, displayAsAsset,
                false, currTime
            )

            if (accountsViewModel.addAccountType(accountType).isCompleted) {
                Toast.makeText(
                    mView.context,
                    "Account Type was saved successfully",
                    Toast.LENGTH_LONG
                ).show()
                view.findNavController().navigate(
                    R.id.action_accountTypeAddFragment_to_accountTypesFragment
                )
            } else {
                Toast.makeText(
                    mView.context,
                    "This Account Type already exists!!\n" +
                            "Please use another name",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                mView.context,
                "Please enter a unique account name",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}