package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment :
    Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private lateinit var accountTypeList: List<String>
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountTypeAddBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountsViewModel = mainActivity.accountViewModel
        mainActivity.title = "Add a new Account Type"
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAccountTypeListForValidation()
        createMenuActions()
    }

    private fun getAccountTypeListForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            val typeList =
                async {
                    accountsViewModel.getAccountTypeNames()
                }
            accountTypeList = typeList.await()
        }
    }

    private fun createMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        isAccountTypeReadyToSave()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun isAccountTypeReadyToSave() {
        if (validateAccountType()) {
            saveAccountType()

        } else {
            Toast.makeText(
                mView.context,
                "Enter a unique Name for this Account Type",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun saveAccountType() {
        val accountType = getCurrentAccountType()
        accountsViewModel.addAccountType(accountType)
        mainViewModel.setAccountWithType(
            AccountWithType(
                mainViewModel.getAccountWithType()!!.account,
                accountType
            )
        )
        gotoAccountTypesFragment()
    }

    private fun gotoAccountTypesFragment() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNT_TYPES", "")
        )
        val direction = AccountTypeAddFragmentDirections
            .actionAccountTypeAddFragmentToAccountTypesFragment()
        mView.findNavController().navigate(direction)
    }

    private fun getCurrentAccountType() = AccountType(
        nf.generateId(),
        binding.etAccTypeAdd.text.toString().trim(),
        binding.chkAccTypeAddKeepTotals.isChecked,
        binding.chkAccTypeAddIsAsset.isChecked,
        binding.chkAccTypeAddKeepOwing.isChecked,
        false,
        binding.chkAccTypeAddDisplayAsset.isChecked,
        binding.chkAccTypeAddAllowPending.isChecked,
        false,
        df.getCurrentTimeAsString()
    )

    private fun validateAccountType(): Boolean {
        if (binding.etAccTypeAdd.text.isNullOrBlank()) return false
        for (accType in accountTypeList) {
            if (accType == binding.etAccTypeAdd.text.toString()) {
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