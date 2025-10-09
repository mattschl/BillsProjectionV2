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
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment :
    Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
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
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
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
                    accountViewModel.getAccountTypeNames()
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
        val answer = validateAccountType()
        if (answer == ANSWER_OK) {
            saveAccountType()

        } else {
            showMessage("${getString(R.string.error)}: $answer")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateAccountType(): String {
        if (binding.etAccTypeAdd.text.isNullOrBlank()) {
            return getString(R.string.enter_a_name_for_this_account_type)
        }
        for (accType in accountTypeList) {
            if (accType == binding.etAccTypeAdd.text.toString()) {
                return getString(R.string.enter_a_unique_name_for_this_account_type)
            }
        }
        return ANSWER_OK
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
        accountViewModel.addAccountType(accountType)
        mainViewModel.setAccountWithType(
            AccountWithType(
                mainViewModel.getAccountWithType()!!.account,
                accountType
            )
        )
        gotoAccountTypes()
    }

    private fun gotoAccountTypes() {
        mainViewModel.removeCallingFragment(TAG)
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