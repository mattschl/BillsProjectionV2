package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.os.Bundle
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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment :
    Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
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
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_new_account_type)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAccountTypeListForValidation()
        setMenuActions()
    }

    private fun getAccountTypeListForValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            val typeList =
                async {
                    mainActivity.accountViewModel.getAccountTypeNames()
                }
            accountTypeList = typeList.await()
        }
    }

    private fun setMenuActions() {
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
                        saveAccountIfValid()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveAccountIfValid() {
        if (validateAccountType()) {
            saveAccountType()

        } else {
            showMessage(getString(R.string.enter_a_unique_name_for_this_account_type))
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            mView.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateAccountType(): Boolean {
        if (binding.etAccTypeAdd.text.isNullOrBlank()) return false
        for (accType in accountTypeList) {
            if (accType == binding.etAccTypeAdd.text.toString()) {
                return false
            }
        }
        return true
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

    private fun saveAccountType() {
        val accountType = getCurrentAccountType()
        mainActivity.accountViewModel.addAccountType(accountType)
        mainActivity.mainViewModel.setAccountWithType(
            AccountWithType(
                mainActivity.mainViewModel.getAccountWithType()!!.account,
                accountType
            )
        )
        gotoAccountTypes()
    }

    private fun gotoAccountTypes() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNT_TYPES", "")
        )
        gotoAccountTypesFragment()
    }

    private fun gotoAccountTypesFragment() {
        mView.findNavController().navigate(
            AccountTypeAddFragmentDirections
                .actionAccountTypeAddFragmentToAccountTypesFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}