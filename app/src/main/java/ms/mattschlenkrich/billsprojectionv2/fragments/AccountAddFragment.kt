package ms.mattschlenkrich.billsprojectionv2.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel


class AccountAddFragment : Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel

    private lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountAddBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        mView = view
    }

    private fun saveAccount(view: View) {
        val accountName = binding.editAccAddName.text.toString().trim()
        val accountHandle = binding.editAccAddHandle.text.toString().trim()
        val accountType = binding.dropAccAddType.text.toString().toLong()
        val accountCategory = binding.dropAccAddCategory.text.toString().toLong()
        val accountBalance = binding.editAccAddBalance.text.toString().toDouble()
        val accountOwing = binding.editAccAddOwing.text.toString().toDouble()
        val accountBudgeted = binding.editAccAddBudgeted.text.toString().toDouble()
        val isAsset = binding.checkAccAddIsAsset.isChecked
        val displayAsset = binding.CheckAccAddDisplayAsset.isChecked
        val keepTotals = binding.checkAccAddKeepTotals.isChecked
        val keepOwing = binding.checkAccAddBalanceOwing.isChecked
        val keepMileage = binding.checkAccAddKeepMileage.isChecked

        if (accountName.isNotEmpty()) {
            val account = Account(
                0, accountName, accountHandle,
                accountCategory, accountType, accountBudgeted, accountBalance,
                accountOwing, keepTotals, isAsset, keepOwing, displayAsset,
                keepMileage, false,
                "no date"
            )

            accountsViewModel.addAccount(account)

            Toast.makeText(
                mView.context,
                "Account was saved successfully",
                Toast.LENGTH_LONG
            ).show()
            view.findNavController().navigate(R.id.action_accountAddFragment_to_accountsFragment)
        } else {
            Toast.makeText(
                mView.context,
                "Please enter a unique account name",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveAccount(mView)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}