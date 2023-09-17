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
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.util.Random

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment :
    Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private val df = DateFunctions()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = mainActivity.accountViewModel
        mainActivity.title = "Add a new Account Type"
        mView = view
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
                        saveAccountType()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    private fun saveAccountType() {
        var id =
            Random().nextInt(Int.MAX_VALUE).toLong()
        id = if (Random().nextBoolean()) -id
        else id
        val accountTypeName = binding.etAccTypeAdd.text.toString().trim()
        val keepTotals = binding.chkAccTypeAddKeepTotals.isChecked
        val keepOwing = binding.chkAccTypeAddKeepOwing.isChecked
        val isAsset = binding.chkAccTypeAddIsAsset.isChecked
        val displayAsAsset = binding.chkAccTypeAddDisplayAsset.isChecked
        val allowPending = binding.chkAccTypeAddAllowPending.isChecked

        if (accountTypeName.isNotEmpty()) {
            val accountType = AccountType(
                id, accountTypeName, keepTotals,
                isAsset, keepOwing, false, displayAsAsset,
                allowPending, false, df.getCurrentTimeAsString()
            )
            accountsViewModel.addAccountType(accountType)
            val direction = AccountTypeAddFragmentDirections
                .actionAccountTypeAddFragmentToAccountTypesFragment()
            mView.findNavController().navigate(direction)

        } else {
            Toast.makeText(
                mView.context,
                "Enter a unique Name for this Account Type",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}