package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_TYPE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNT_TYPE_UPDATE

class AccountTypeUpdateFragment :
    Fragment(R.layout.fragment_account_type_update) {


    private var _binding: FragmentAccountTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountTypeUpdateFragmentArgs by navArgs()
    private lateinit var currentAccountType: AccountType
    private val df = DateFunctions()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountTypeUpdateBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        currentAccountType = args.accountType!!
        mainActivity.title = "Update Account Type"
        fillValues()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.delete_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteAccountType()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.fabAccountTypeUpdate.setOnClickListener {
            updateAccountType()
        }
    }

    private fun updateAccountType() {
        Log.d(TAG, "entering accountType update")
        val accountTypeName = binding.etAccTypeUpdate.text
            .toString().trim()
        val keepTotals = binding.chkAccountTypeUKeepTotals.isChecked
        val keepOwing = binding.chkAccountTypeUKeepOwing.isChecked
        val isAsset = binding.chkAccTypeAddIsAsset.isChecked
        val displayAsAsset = binding.chkAccountTypeUDisplayAsset.isChecked
        val allowPending = binding.chkAccTypeUAllowPending.isChecked
        val accountType = AccountType(
            currentAccountType.typeId, accountTypeName,
            keepTotals, isAsset, keepOwing, false, displayAsAsset,
            allowPending, false, df.getCurrentTimeAsString()
        )

        if (accountTypeName == currentAccountType.accountType) {
            accountsViewModel.updateAccountType(accountType)
            val direction = AccountTypeUpdateFragmentDirections
                .actionAccountTypeUpdateFragmentToAccountTypesFragment(
                    args.budgetItem,
                    args.transaction,
                    args.budgetRuleDetailed,
                    args.account,
                    args.requestedAccount,
                    args.callingFragments
                )
            mView?.findNavController()?.navigate(direction)
        } else if (accountTypeName.isNotBlank()) {
            AlertDialog.Builder(activity).apply {
                setTitle("Rename Account Type?")
                setMessage(
                    "Are you sure you want to rename this Account Type?\n " +
                            "      NOTE:\n" +
                            "This will NOT replace an existing Account Type"
                )
                setPositiveButton("Update Account Type") { _, _ ->
                    accountsViewModel.updateAccountType(accountType)
                    val direction = AccountTypeUpdateFragmentDirections
                        .actionAccountTypeUpdateFragmentToAccountTypesFragment(
                            args.budgetItem,
                            args.transaction,
                            args.budgetRuleDetailed,
                            args.account,
                            args.requestedAccount,
                            args.callingFragments
                        )
                    mView?.findNavController()?.navigate(direction)
                }
                setNegativeButton("Cancel", null)
            }.create().show()
        } else {
            Toast.makeText(
                context,
                "Enter a name for this Account Type",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fillValues() {
        binding.etAccTypeUpdate.setText(currentAccountType.accountType)
        binding.chkAccountTypeUKeepTotals.isChecked =
            currentAccountType.keepTotals
        binding.chkAccountTypeUKeepOwing.isChecked =
            currentAccountType.tallyOwing
        binding.chkAccTypeAddIsAsset.isChecked =
            currentAccountType.isAsset
        binding.chkAccountTypeUDisplayAsset.isChecked =
            currentAccountType.displayAsAsset
        binding.chkAccTypeUAllowPending.isChecked =
            currentAccountType.allowPending
    }

//    @Deprecated("Deprecated in Java")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
////        menu.clear()
//        inflater.inflate(R.menu.delete_menu, menu)
//    }

    private fun deleteAccountType() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account Type?")
            setMessage("Are you sure you want to delete this Account Type?")
            setPositiveButton("Delete") { _, _ ->
                accountsViewModel.deleteAccountType(
                    currentAccountType.typeId,
                    df.getCurrentTimeAsString()
                )
                val direction = AccountTypeUpdateFragmentDirections
                    .actionAccountTypeUpdateFragmentToAccountTypesFragment(
                        args.budgetItem,
                        args.transaction,
                        args.budgetRuleDetailed,
                        args.account,
                        args.requestedAccount,
                        args.callingFragments
                    )
                mView?.findNavController()?.navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

//    @Deprecated("Deprecated in Java")
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_delete -> {
//                deleteAccountType()
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}