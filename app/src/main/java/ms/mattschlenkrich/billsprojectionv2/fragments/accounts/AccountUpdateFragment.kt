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
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_ACCOUNT_UPDATE

class AccountUpdateFragment :
    Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private lateinit var mView: View
    private lateinit var accountsViewModel: AccountViewModel

    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private var accountNameList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        mView = binding.root
        createMenu()
        return mView as View
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
                        deleteAccount()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        accountsViewModel.getAccountNameList().observe(
            viewLifecycleOwner
        ) { accounts ->
            accountNameList.clear()
            accounts.listIterator().forEach {
                accountNameList.add(it)
            }
        }

        mainActivity.title = "Update Account"
        fillValues()
        binding.drpAccountUpdateType.setOnClickListener {
            gotoAccountTypes()
        }
        binding.fabAccountUpdateDone.setOnClickListener {
            updateAccount()
        }
    }

    private fun getUpdatedAccount(): Account {
        binding.apply {
            return Account(
                mainViewModel.getAccountWithType()!!.account.accountId,
                edAccountUpdateName.text.toString().trim(),
                edAccountUpdateHandle.text.toString().trim(),
                mainViewModel.getAccountWithType()!!.accountType?.typeId ?: 0L,
                cf.getDoubleFromDollars(edAccountUpdateBudgeted.text.toString()),
                cf.getDoubleFromDollars(edAccountUpdateBalance.text.toString()),
                cf.getDoubleFromDollars(edAccountUpdateOwing.text.toString()),
                cf.getDoubleFromDollars(etAccUpdateLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoAccountTypes() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(),
                mainViewModel.getAccountWithType()!!.accountType
            )
        )
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountTypesFragment()
        mView.findNavController().navigate(direction)

    }

    private fun checkAccount(): String {
        binding.apply {
            val nameIsBlank =
                edAccountUpdateName.text.isNullOrBlank()
            var nameFound = false
            if (accountNameList.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList.size) {
                    if (accountNameList[i] ==
                        edAccountUpdateName.text.toString() &&
                        accountNameList[i] !=
                        mainViewModel.getAccountWithType()!!.account.accountName
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMes = if (nameIsBlank) {
                "     Error!!\n" +
                        "Please enter a name"
            } else if (nameFound) {
                "     Error!!\n" +
                        "This budget rule already exists."
            } else if (drpAccountUpdateType.text.isNullOrBlank()) {
                "     Error!!\n" +
                        "Please choose an account Type"
            } else {
                "Ok"
            }
            return errorMes
        }
    }

    private fun updateAccount() {
        val mess = checkAccount()

        if (mess == "Ok") {
            val name = binding.edAccountUpdateName.text.trim().toString()
            if (name == mainViewModel.getAccountWithType()!!.account.accountName.trim()) {
                accountsViewModel.updateAccount(getUpdatedAccount())
                gotoCallingFragment()
            } else if (name != mainViewModel.getAccountWithType()!!.account.accountName.trim()) {
                AlertDialog.Builder(activity).apply {
                    setTitle("Rename Account?")
                    setMessage(
                        "Are you sure you want to rename this Account?\n " +
                                "      NOTE:\n" +
                                "This will NOT replace an existing Account"
                    )
                    setPositiveButton("Update Account") { _, _ ->
                        accountsViewModel.updateAccount(getUpdatedAccount())
                        gotoCallingFragment()

                    }
                    setNegativeButton("Cancel", null)
                }.create().show()
            }
        } else {
            Toast.makeText(
                mView!!.context,
                mess,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun gotoCallingFragment() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_ACCOUNTS)) {
            val direction = AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToAccountsFragment()
            mView.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_VIEW)) {
            mView.findNavController().navigate(
                AccountUpdateFragmentDirections.actionAccountUpdateFragmentToBudgetViewFragment()
            )
        }
    }

    private fun fillValues() {
        binding.apply {
            edAccountUpdateName.setText(
                mainViewModel.getAccountWithType()!!.account.accountName
            )
            edAccountUpdateHandle.setText(
                mainViewModel.getAccountWithType()!!.account.accountNumber
            )
            if (mainViewModel.getAccountWithType()!!.accountType != null) {
                drpAccountUpdateType.text =
                    mainViewModel.getAccountWithType()!!.accountType!!.accountType
            }
            edAccountUpdateBalance.setText(
                cf.displayDollars(
                    mainViewModel.getAccountWithType()!!.account.accountBalance
                )
            )
            edAccountUpdateOwing.setText(
                cf.displayDollars(
                    mainViewModel.getAccountWithType()!!.account.accountOwing
                )
            )
            edAccountUpdateBudgeted.setText(
                cf.displayDollars(
                    mainViewModel.getAccountWithType()!!.account.accBudgetedAmount
                )
            )
            etAccUpdateLimit.setText(
                cf.displayDollars(
                    mainViewModel.getAccountWithType()!!.account.accountCreditLimit
                )
            )
            txtAccountUpdateAccountId.text =
                mainViewModel.getAccountWithType()!!.account.accountId.toString()
        }
    }

    private fun deleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account?")
            setMessage("Are you sure you want to delete this account? ")
            setPositiveButton("Delete") { _, _ ->
                doDelete()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun doDelete() {
        accountsViewModel.deleteAccount(
            mainViewModel.getAccountWithType()!!.account.accountId,
            df.getCurrentTimeAsString()
        )
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNT_UPDATE", "")
        )
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountsFragment()
        mView?.findNavController()?.navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}