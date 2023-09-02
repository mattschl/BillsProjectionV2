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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNT_UPDATE

class AccountUpdateFragment :
    Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountUpdateFragmentArgs by navArgs()
    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private var accountNameList: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")
        mainActivity = (activity as MainActivity)
        mView = binding.root
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
        return mView as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        CoroutineScope(Dispatchers.IO).launch {
            accountNameList = accountsViewModel.getAccountNameList()
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
                args.account!!.accountId,
                edAccountUpdateName.text.toString().trim(),
                edAccountUpdateHandle.text.toString().trim(),
                args.account!!.accountTypeId,
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
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountTypesFragment(
                args.budgetItem,
                args.transaction,
                args.budgetRuleDetailed,
                getUpdatedAccount(),
                args.requestedAccount,
                fragmentChain
            )
        mView?.findNavController()?.navigate(direction)

    }

    private fun checkAccount(): String {
        binding.apply {
            val nameIsBlank =
                edAccountUpdateName.text.isNullOrBlank()
            var nameFound = false
            if (accountNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList!!.size) {
                    if (accountNameList!![i] ==
                        edAccountUpdateName.text.toString() &&
                        accountNameList!![i] !=
                        args.account!!.accountName
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
            if (name == args.account!!.accountName.trim()) {
                accountsViewModel.updateAccount(getUpdatedAccount())
                gotoAccountFragment()
            } else if (name != args.account!!.accountName.trim()) {
                AlertDialog.Builder(activity).apply {
                    setTitle("Rename Account?")
                    setMessage(
                        "Are you sure you want to rename this Account?\n " +
                                "      NOTE:\n" +
                                "This will NOT replace an existing Account"
                    )
                    setPositiveButton("Update Account") { _, _ ->
                        accountsViewModel.updateAccount(getUpdatedAccount())
                        gotoAccountFragment()
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

    private fun gotoAccountFragment() {
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountsFragment(
                args.budgetItem,
                args.transaction,
                args.budgetRuleDetailed,
                args.requestedAccount,
                args.callingFragments
            )
        mView?.findNavController()?.navigate(direction)
    }

    private fun fillValues() {
        binding.apply {
            edAccountUpdateName.setText(
                args.account!!.accountName
            )
            edAccountUpdateHandle.setText(
                args.account!!.accountNumber
            )
            if (args.accountType != null) {
                drpAccountUpdateType.text = args.accountType!!.accountType
            }
            edAccountUpdateBalance.setText(
                cf.displayDollars(args.account!!.accountBalance)
            )
            edAccountUpdateOwing.setText(
                cf.displayDollars(args.account!!.accountOwing)
            )
            edAccountUpdateBudgeted.setText(
                cf.displayDollars(args.account!!.accBudgetedAmount)
            )
            etAccUpdateLimit.setText(
                cf.displayDollars(args.account!!.accountCreditLimit)
            )
            txtAccountUpdateAccountId.text =
                args.account!!.accountId.toString()
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
            args.account!!.accountId,
            df.getCurrentTimeAsString()
        )
        val fragmentChain =
            args.callingFragments!!
                .replace(", $FRAG_ACCOUNT_UPDATE", "")
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountsFragment(
                args.budgetItem,
                args.transaction,
                args.budgetRuleDetailed,
                args.requestedAccount,
                fragmentChain,
            )
        mView?.findNavController()?.navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}