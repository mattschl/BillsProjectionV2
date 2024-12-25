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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_ACCOUNT_UPDATE

class AccountUpdateFragment :
    Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mView: View

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private var accountNameList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.mainViewModel = mainActivity.mainViewModel
        mainActivity.accountViewModel =
            mainActivity.accountViewModel
        mainActivity.title = "Update Account"
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        val accountWithType = mainActivity.mainViewModel.getAccountWithType()!!
        getAccountListNamesForValidation()
        binding.apply {
            edAccountUpdateName.setText(
                accountWithType.account.accountName
            )
            edAccountUpdateHandle.setText(
                accountWithType.account.accountNumber
            )
            if (accountWithType.accountType != null) {
                drpAccountUpdateType.text =
                    accountWithType.accountType.accountType
            }
            edAccountUpdateBalance.setText(
                nf.displayDollars(
                    if (mainActivity.mainViewModel.getTransferNum()!! != 0.0 &&
                        mainActivity.mainViewModel.getReturnTo()!!.contains(BALANCE)
                    ) {
                        mainActivity.mainViewModel.getTransferNum()!!
                    } else {
                        accountWithType.account.accountBalance
                    }
                )
            )
            edAccountUpdateOwing.setText(
                nf.displayDollars(
                    if (mainActivity.mainViewModel.getTransferNum()!! != 0.0 &&
                        mainActivity.mainViewModel.getReturnTo()!!.contains(OWING)

                    ) {
                        mainActivity.mainViewModel.getTransferNum()!!
                    } else {
                        accountWithType.account.accountOwing
                    }
                )
            )
            edAccountUpdateBudgeted.setText(
                nf.displayDollars(
                    if (mainActivity.mainViewModel.getTransferNum()!! != 0.0 &&
                        mainActivity.mainViewModel.getReturnTo()!!.contains(BUDGETED)
                    ) {
                        mainActivity.mainViewModel.getTransferNum()!!
                    } else {
                        accountWithType.account.accBudgetedAmount
                    }
                )
            )
            mainActivity.mainViewModel.setTransferNum(0.0)
            etAccUpdateLimit.setText(
                nf.displayDollars(
                    accountWithType.account.accountCreditLimit
                )
            )
            txtAccountUpdateAccountId.text =
                accountWithType.account.accountId.toString()
        }
    }

    private fun getAccountListNamesForValidation() {
        mainActivity.accountViewModel.getAccountNameList().observe(
            viewLifecycleOwner
        ) { accounts ->
            accountNameList.clear()
            accounts.listIterator().forEach {
                accountNameList.add(it)
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            drpAccountUpdateType.setOnClickListener {
                gotoAccountTypesFragment()
            }
            fabAccountUpdateDone.setOnClickListener {
                updateAccountIfValid()
            }
            edAccountUpdateBalance.setOnLongClickListener {
                gotoCalculator(BALANCE)
                false
            }
            edAccountUpdateOwing.setOnLongClickListener {
                gotoCalculator(OWING)
                false
            }
            edAccountUpdateBudgeted.setOnLongClickListener {
                gotoCalculator(BUDGETED)
                false
            }
        }
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
                        chooseDeleteAccount()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getUpdatedAccount(): Account {
        binding.apply {
            return Account(
                mainActivity.mainViewModel.getAccountWithType()!!.account.accountId,
                edAccountUpdateName.text.toString().trim(),
                edAccountUpdateHandle.text.toString().trim(),
                mainActivity.mainViewModel.getAccountWithType()!!.accountType?.typeId ?: 0L,
                nf.getDoubleFromDollars(edAccountUpdateBudgeted.text.toString()),
                nf.getDoubleFromDollars(edAccountUpdateBalance.text.toString()),
                nf.getDoubleFromDollars(edAccountUpdateOwing.text.toString()),
                nf.getDoubleFromDollars(etAccUpdateLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateAccount(): String {
        binding.apply {
            val nameIsBlank =
                edAccountUpdateName.text.isNullOrBlank()
            var nameFound = false
            if (accountNameList.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList.size) {
                    if (accountNameList[i] ==
                        edAccountUpdateName.text.toString() &&
                        accountNameList[i] !=
                        mainActivity.mainViewModel.getAccountWithType()!!.account.accountName
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

    private fun updateAccountIfValid() {
        val mess = validateAccount()

        if (mess == "Ok") {
            chooseToUpdate()
        } else {
            Toast.makeText(
                mView.context,
                mess,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun chooseToUpdate() {
        val accountWithType = mainActivity.mainViewModel.getAccountWithType()!!
        val name = binding.edAccountUpdateName.text.trim().toString()
        if (name == accountWithType.account.accountName.trim()) {
            mainActivity.accountViewModel.updateAccount(getUpdatedAccount())
            gotoCallingFragment()
        } else if (name != accountWithType.account.accountName.trim()) {
            AlertDialog.Builder(activity).apply {
                setTitle("Rename Account?")
                setMessage(
                    "Are you sure you want to rename this Account?\n " +
                            "      NOTE:\n" +
                            "This will NOT replace an existing Account"
                )
                setPositiveButton("Update Account") { _, _ ->
                    mainActivity.accountViewModel.updateAccount(getUpdatedAccount())
                    gotoCallingFragment()

                }
                setNegativeButton("Cancel", null)
            }.create().show()
        }
    }

    private fun chooseDeleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account?")
            setMessage("Are you sure you want to delete this account? ")
            setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun deleteAccount() {
        mainActivity.accountViewModel.deleteAccount(
            mainActivity.mainViewModel.getAccountWithType()!!.account.accountId,
            df.getCurrentTimeAsString()
        )
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNT_UPDATE", "")
        )
        gotoAccountsFragment()
    }

    private fun gotoCalculator(type: String) {
        when (type) {
            BALANCE -> {
                mainActivity.mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateBalance.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }

            OWING -> {
                mainActivity.mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateOwing.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }

            BUDGETED -> {
                mainActivity.mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateBudgeted.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }
        }
        mainActivity.mainViewModel.setReturnTo("$TAG, $type")
        mainActivity.mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(),
                mainActivity.mainViewModel.getAccountWithType()!!.accountType
            )
        )
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        if (mainActivity.mainViewModel.getCallingFragments()!!.contains(FRAG_ACCOUNTS)) {
            gotoAccountsFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_VIEW)) {
            gotoBudgetViewFragment()
        }
    }

    private fun gotoAccountsFragment() {
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoAccountTypesFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainActivity.mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(),
                mainActivity.mainViewModel.getAccountWithType()!!.accountType
            )
        )
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountTypesFragment()
        mView.findNavController().navigate(direction)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}