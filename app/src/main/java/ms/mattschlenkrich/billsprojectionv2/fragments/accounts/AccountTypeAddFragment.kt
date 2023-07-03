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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_TYPE_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountTypeAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

private const val TAG = FRAG_ACCOUNT_TYPE_ADD

class AccountTypeAddFragment :
    Fragment(R.layout.fragment_account_type_add) {

    private var _binding: FragmentAccountTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private val args: AccountTypeAddFragmentArgs by navArgs()

    private val timeFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        Log.d(
            TAG, "saveAccountType entered callingFragments are\n" +
                    "${args.callingFragments}"
        )
        var id =
            Random().nextInt(Int.MAX_VALUE).toLong()
        id = if (Random().nextBoolean()) -id
        else id
        val accountTypeName = binding.etAccTypeAdd.text.toString().trim()
        val keepTotals = binding.chkAccTypeAddKeepTotals.isChecked
        val keepOwing = binding.chkAccTypeAddKeepOwing.isChecked
        val isAsset = binding.chkAccTypeAddIsAsset.isChecked
        val displayAsAsset = binding.chkAccTypeAddDisplayAsset.isChecked
        val currTime = timeFormatter.format(Calendar.getInstance().time)

        if (accountTypeName.isNotEmpty()) {
            val accountType = AccountType(
                id, accountTypeName, keepTotals,
                isAsset, keepOwing, false, displayAsAsset,
                false, currTime
            )
            accountsViewModel.addAccountType(accountType)
            val direction = AccountTypeAddFragmentDirections
                .actionAccountTypeAddFragmentToAccountTypesFragment(
                    args.transaction,
                    args.budgetRuleDetailed,
                    args.account,
                    args.requestedAccount,
                    args.callingFragments
                )
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