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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_ACCOUNT_ADD

class AccountAddFragment :
    Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private var accountNameList = ArrayList<String>()
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountsViewModel =
            mainActivity.accountViewModel
        mainActivity.title = "Add a new Account"
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAccountNameListForValidation()
        populateValues()
        createMenuActions()
        createClickActions()
    }

    private fun getAccountNameListForValidation() {
        accountsViewModel.getAccountNameList().observe(
            viewLifecycleOwner
        ) { accounts ->
            accountNameList.clear()
            accounts.listIterator().forEach {
                accountNameList.add(it)
            }
        }
    }

    private fun createClickActions() {
        binding.apply {
            tvAccAddType.setOnClickListener {
                gotoAccountTypesFragment()
            }
            etAccAddBalance.setOnLongClickListener {
                gotoCalculator(BALANCE)
                false
            }
            etAccAddOwing.setOnLongClickListener {
                gotoCalculator(OWING)
                false
            }
            etAccAddBudgeted.setOnLongClickListener {
                gotoCalculator(BUDGETED)
                false
            }
        }
    }

    private fun gotoCalculator(type: String) {
        when (type) {
            BALANCE -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddBalance.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }

            OWING -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddOwing.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }

            BUDGETED -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddBudgeted.text.toString().ifBlank {
                            "0.0"
                        }
                    )
                )
            }
        }
        mainViewModel.setReturnTo("$TAG, $type")
        mainViewModel.setAccountWithType(
            AccountWithType(
                getCurrentAccount(),
                mainViewModel.getAccountWithType()?.accountType
            )
        )
        mView.findNavController().navigate(
            AccountAddFragmentDirections
                .actionAccountAddFragmentToCalcFragment()
        )
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
                        isAccountReadyToSave(mView)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun populateValues() {
        binding.apply {
            if (mainViewModel.getAccountWithType() != null) {
                populateAccountFromCache()
            }
            if (mainViewModel.getAccountWithType()?.accountType != null) {
                populateAccountYpeFromCache()
            }
        }
    }

    private fun populateAccountYpeFromCache() {
        binding.apply {
            tvAccAddType.text =
                mainViewModel.getAccountWithType()!!.accountType!!.accountType
            var display =
                if (
                    mainViewModel.getAccountWithType()!!.accountType!!.keepTotals
                ) "Transactions will be calculated\n" else ""
            display += if (
                mainViewModel.getAccountWithType()!!.accountType!!.isAsset
            ) "This is an asset \n" else ""
            display += if (
                mainViewModel.getAccountWithType()!!.accountType!!.displayAsAsset
            ) "This will be used for the budget \n" else ""
            display += if (
                mainViewModel.getAccountWithType()!!.accountType!!.tallyOwing)
                "Balance owing will be calculated " else ""
            display += if (
                mainViewModel.getAccountWithType()!!.accountType!!.allowPending)
                "Transactions may be delayed " else ""
            if (display.isEmpty()) {
                display =
                    "This account does not keep a balance/owing amount"
            }
            tvTypeDetails.text = display
        }
    }

    private fun populateAccountFromCache() {
        binding.apply {
            etAccAddName.setText(
                mainViewModel.getAccountWithType()!!.account.accountName
            )
            etAccAddHandle.setText(
                mainViewModel.getAccountWithType()!!.account.accountNumber
            )
            etAccAddBalance.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0 &&
                        mainViewModel.getReturnTo()!!.contains(BALANCE)
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        mainViewModel.getAccountWithType()!!.account.accountBalance
                    }
                )
            )
            etAccAddOwing.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0 &&
                        mainViewModel.getReturnTo()!!.contains(OWING)

                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        mainViewModel.getAccountWithType()!!.account.accountOwing
                    }
                )
            )
            etAccAddBudgeted.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0 &&
                        mainViewModel.getReturnTo()!!.contains(BUDGETED)
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        mainViewModel.getAccountWithType()!!.account.accBudgetedAmount
                    }
                )
            )
            mainViewModel.setTransferNum(0.0)
            etAccAddLimit.setText(
                nf.displayDollars(
                    mainViewModel.getAccountWithType()!!.account.accountCreditLimit
                )
            )
        }
    }

    private fun getCurrentAccount(): Account {
        binding.apply {
            return Account(
                nf.generateId(),
                etAccAddName.text.toString().trim(),
                etAccAddHandle.text.toString().trim(),
                mainViewModel.getAccountWithType()?.accountType?.typeId ?: 0L,
                nf.getDoubleFromDollars(etAccAddBudgeted.text.toString()),
                nf.getDoubleFromDollars(etAccAddBalance.text.toString()),
                nf.getDoubleFromDollars(etAccAddOwing.text.toString()),
                nf.getDoubleFromDollars(etAccAddLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoAccountTypesFragment() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        mainViewModel.setAccountWithType(
            AccountWithType(
                getCurrentAccount(),
                null
            )
        )
        val direction = AccountAddFragmentDirections
            .actionAccountAddFragmentToAccountTypesFragment()
        mView.findNavController().navigate(direction)

    }

    private fun isAccountReadyToSave(view: View) {
        val mes = validateAccount()
        if (mes == "Ok") {
            saveAccount(view)
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveAccount(view: View) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNT_ADD", "")
        )
        val curAccount = getCurrentAccount()
        accountsViewModel.addAccount(curAccount)
        mainViewModel.setAccountWithType(
            AccountWithType(
                curAccount,
                mainViewModel.getAccountWithType()?.accountType!!
            )
        )
        val direction = AccountAddFragmentDirections
            .actionAccountAddFragmentToAccountsFragment()
        view.findNavController().navigate(direction)
    }

    private fun validateAccount(): String {
        binding.apply {
            val nameIsBlank =
                etAccAddName.text.isNullOrEmpty()
            var nameFound = false
            if (accountNameList.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList.size) {
                    if (accountNameList[i] ==
                        etAccAddName.text.toString().trim()
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMess = if (nameIsBlank) {
                "     Error!!\n" +
                        "Please enter a name"
            } else if (nameFound) {
                "     Error!!\n" +
                        "This account rule already exists."
            } else if (mainViewModel.getAccountWithType()?.accountType == null
            ) {
                "     Error!!\n" +
                        "This account must have an account Type."
            } else {
                "Ok"
            }
            return errorMess
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}