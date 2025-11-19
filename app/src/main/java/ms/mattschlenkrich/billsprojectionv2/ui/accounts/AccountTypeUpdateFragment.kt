package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import android.app.AlertDialog
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
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

//private const val TAG = FRAG_ACCOUNT_TYPE_UPDATE

class AccountTypeUpdateFragment :
    Fragment(R.layout.fragment_account_type_update) {


    private var _binding: FragmentAccountTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var mView: View

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
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        currentAccountType = mainActivity.mainViewModel.getAccountType()!!
        mainActivity.topMenuBar.title = getString(R.string.update_account_type)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        getAccountTypeListForValidation()
        binding.apply {
            etAccTypeUpdate.setText(currentAccountType.accountType)
            chkAccountTypeUKeepTotals.isChecked = currentAccountType.keepTotals
            chkAccountTypeUKeepOwing.isChecked = currentAccountType.tallyOwing
            chkAccTypeAddIsAsset.isChecked = currentAccountType.isAsset
            chkAccountTypeUDisplayAsset.isChecked = currentAccountType.displayAsAsset
            chkAccTypeUAllowPending.isChecked = currentAccountType.allowPending
        }
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

    private fun setClickActions() {
        setMenuActions()
        binding.fabAccountTypeUpdate.setOnClickListener { isAccountTypeReadyToUpdate() }
    }

    private fun setMenuActions() {
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
                        confirmDeleteAccountType()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun isAccountTypeReadyToUpdate() {
        if (binding.etAccTypeUpdate.text.toString().trim() ==
            currentAccountType.accountType
        ) {
            val answer = validateAccountType()
            if (answer == ANSWER_OK) {
                updateAccountType()
            } else {
                showMessage("${getString(R.string.error)}: $answer")
            }
        } else {
            val answer = validateAccountType()
            if (answer == ANSWER_OK) {
                confirmRenameAccountType()
            } else {
                showMessage("${getString(R.string.error)}: $answer")
            }
        }
    }

    private fun confirmRenameAccountType() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.rename_account_type))
            setMessage(
                getString(R.string.are_you_sure_you_want_to_rename_this_account_type) +
                        getString(R.string.note) +
                        getString(R.string.this_will_not_replace_an_existing_account_type)
            )
            setPositiveButton(
                getString(R.string.update_account_type)
            ) { _, _ ->
                updateAccountType()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateAccountType(): String {
        if (binding.etAccTypeUpdate.text.isNullOrBlank()) {
            return getString(R.string.enter_a_name_for_this_account_type)
        }
        for (accType in accountTypeList) {
            if (accType == binding.etAccTypeUpdate.text.toString()) {
                return getString(R.string.enter_a_unique_name_for_this_account_type)
            }
        }
        return ANSWER_OK
    }

    private fun updateAccountType() {
        binding.apply {
            accountViewModel.updateAccountType(
                AccountType(
                    currentAccountType.typeId,
                    etAccTypeUpdate.text.toString().trim(),
                    chkAccountTypeUKeepTotals.isChecked,
                    chkAccTypeAddIsAsset.isChecked,
                    chkAccountTypeUKeepOwing.isChecked,
                    false,
                    chkAccountTypeUDisplayAsset.isChecked,
                    chkAccTypeUAllowPending.isChecked,
                    false,
                    df.getCurrentTimeAsString()
                )

            )
            gotoAccountTypesFragment()
        }
    }

    private fun confirmDeleteAccountType() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.delete_account_type))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_account_type))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccountType()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteAccountType() {
        accountViewModel.deleteAccountType(
            currentAccountType.typeId,
            df.getCurrentTimeAsString()
        )
        gotoAccountTypesFragment()
    }

    private fun gotoAccountTypesFragment() {
        mView.findNavController().navigate(
            AccountTypeUpdateFragmentDirections
                .actionAccountTypeUpdateFragmentToAccountTypesFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}