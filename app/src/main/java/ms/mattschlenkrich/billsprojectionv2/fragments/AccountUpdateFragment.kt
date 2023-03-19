package ms.mattschlenkrich.billsprojectionv2.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

class AccountUpdateFragment : Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountUpdateFragmentArgs by navArgs()

    private lateinit var currentAccount: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        currentAccount = args.account!!

        binding.edAccountUpdateName.setText(currentAccount.accountName)
        binding.edAccountUpdateHandle.setText(currentAccount.accountNumber)
        binding.drpAccountUpdateType.setText(currentAccount.accountTypeId.toString())
        binding.drpAccountUpdateCategory.setText(currentAccount.accountCategoryId.toString())
        binding.edAccountUpdateBalance.setText(currentAccount.accountBalance.toString())
        binding.edAccountUpdateOwing.setText(currentAccount.accountOwing.toString())
        binding.edAccountUpdateBudgeted.setText(currentAccount.budgetAmount.toString())
        binding.chkAccountUpdateIsAsset.isChecked = currentAccount.isAsset
        binding.chkAccountUpdateDisplayAsset.isChecked = currentAccount.displayAsAsset
        binding.chkAccountUpdateBalanceOwing.isChecked = currentAccount.tallyOwing
        binding.chkAccountUpdateKeepTotals.isChecked = currentAccount.keepTotals
        binding.chkAccountUpdateKeepMileage.isChecked = currentAccount.keepMileage

        binding.fabAccountUpdateDone.setOnClickListener {
            val accountName = binding.edAccountUpdateName.text.toString().trim()
            val accountHandle = binding.edAccountUpdateHandle.text.toString().trim()
            val accountTypeId = binding.drpAccountUpdateType.text.toString().toLong()
            val accountCategoryId = binding.drpAccountUpdateCategory.text.toString().toLong()
            val balance = binding.edAccountUpdateBalance.text.toString().toDouble()
            val owing = binding.edAccountUpdateOwing.text.toString().toDouble()
            val budgeted = binding.edAccountUpdateBudgeted.text.toString().toDouble()
            val isAsset = binding.chkAccountUpdateIsAsset.isChecked
            val displayAsset = binding.chkAccountUpdateDisplayAsset.isChecked
            val keepBalance = binding.chkAccountUpdateBalanceOwing.isChecked
            val keepTotals = binding.chkAccountUpdateKeepTotals.isChecked
            val keepMileage = binding.chkAccountUpdateKeepMileage.isChecked

            if (accountName.isNotEmpty()) {
                val account = Account(
                    currentAccount.accountId, accountName,
                    accountHandle, accountCategoryId, accountTypeId, budgeted,
                    balance, owing, keepTotals, isAsset, keepBalance, keepMileage, displayAsset,
                    false, "no time"
                )
                accountsViewModel.updateAccount(account)
                view.findNavController()
                    .navigate(R.id.action_accountUpdateFragment_to_accountsFragment)
            } else {
                Toast.makeText(
                    context,
                    "Enter a unique name for this account",
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    private fun deleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account")
            setMessage("Are you sure you want to delete this account?")
            setPositiveButton("Delete") { _, _ ->
                accountsViewModel.deleteAccount(
                    currentAccount.accountId,
                    "no date"
                )
                view?.findNavController()?.navigate(
                    R.id.action_accountUpdateFragment_to_accountsFragment
                )

            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.delete_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete -> {
                deleteAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}