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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_ACCOUNT_TYPE_UPDATE

class AccountTypeUpdateFragment :
    Fragment(R.layout.fragment_account_type_update) {


    private var _binding: FragmentAccountTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    private lateinit var currentAccountType: AccountType
    private lateinit var accountTypeList: List<String>
    private val df = DateFunctions()
//    private val cf = NumberFunctions()

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
        mainViewModel = mainActivity.mainViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        currentAccountType = mainViewModel.getAccountType()!!
        mainActivity.title = "Update Account Type"
        getAccountTypeList()
        fillValues()
        createMenu()
        binding.fabAccountTypeUpdate.setOnClickListener {
            updateAccountType()
        }
    }

    private fun getAccountTypeList() {
        CoroutineScope(Dispatchers.IO).launch {
            val typeList =
                async {
                    accountsViewModel.getAccountTypeNames()
                }
            accountTypeList = typeList.await()
        }
    }

    private fun createMenu() {
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
    }

    private fun updateAccountType() {
        if (binding.etAccTypeUpdate.text.toString().trim() ==
            currentAccountType.accountType
        ) {
            accountsViewModel.updateAccountType(
                AccountType(
                    currentAccountType.typeId,
                    binding.etAccTypeUpdate.text.toString().trim(),
                    binding.chkAccountTypeUKeepTotals.isChecked,
                    binding.chkAccTypeAddIsAsset.isChecked,
                    binding.chkAccountTypeUKeepOwing.isChecked,
                    false,
                    binding.chkAccountTypeUDisplayAsset.isChecked,
                    binding.chkAccTypeUAllowPending.isChecked,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            val direction = AccountTypeUpdateFragmentDirections
                .actionAccountTypeUpdateFragmentToAccountTypesFragment()
            mView?.findNavController()?.navigate(direction)
        } else if (binding.etAccTypeUpdate.text.toString().isNotBlank() &&
            checkAccountType()
        ) {
            AlertDialog.Builder(activity).apply {
                setTitle("Rename Account Type?")
                setMessage(
                    "Are you sure you want to rename this Account Type?\n " +
                            "      NOTE:\n" +
                            "This will NOT replace an existing Account Type"
                )
                setPositiveButton("Update Account Type") { _, _ ->
                    accountsViewModel.updateAccountType(
                        AccountType(
                            currentAccountType.typeId,
                            binding.etAccTypeUpdate.text.toString().trim(),
                            binding.chkAccountTypeUKeepTotals.isChecked,
                            binding.chkAccTypeAddIsAsset.isChecked,
                            binding.chkAccountTypeUKeepOwing.isChecked,
                            false,
                            binding.chkAccountTypeUDisplayAsset.isChecked,
                            binding.chkAccTypeUAllowPending.isChecked,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    val direction = AccountTypeUpdateFragmentDirections
                        .actionAccountTypeUpdateFragmentToAccountTypesFragment()
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
                    .actionAccountTypeUpdateFragmentToAccountTypesFragment()
                mView?.findNavController()?.navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun checkAccountType(): Boolean {
        if (binding.etAccTypeUpdate.text.isNullOrBlank()) return false
        for (accType in accountTypeList) {
            if (accType == binding.etAccTypeUpdate.text.toString()) {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}