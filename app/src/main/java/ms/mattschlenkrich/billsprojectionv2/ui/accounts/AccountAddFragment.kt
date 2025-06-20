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
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
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
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var mView: View
    private var accountNameList = ArrayList<String>()
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.title = getString(R.string.add_a_new_account)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        getAccountNameListForValidation()
        binding.apply {
            if (mainViewModel.getAccountWithType() != null) {
                populateAccountFromCache()
            }
            if (mainViewModel.getAccountWithType()?.accountType != null) {
                populateAccountTypeFromCache()
            }
        }
    }

    private fun populateAccountFromCache() {
        val accountWithType = mainViewModel.getAccountWithType()!!
        binding.apply {
            etAccAddName.setText(accountWithType.account.accountName)
            etAccAddHandle.setText(accountWithType.account.accountNumber)
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
                        accountWithType.account.accountOwing
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
                        accountWithType.account.accBudgetedAmount
                    }
                )
            )
            mainViewModel.setTransferNum(0.0)
            etAccAddLimit.setText(
                nf.displayDollars(
                    accountWithType.account.accountCreditLimit
                )
            )
        }
    }

    private fun populateAccountTypeFromCache() {
        val accountWithType = mainViewModel.getAccountWithType()!!
        binding.apply {
            tvAccAddType.text = accountWithType.accountType!!.accountType
            var display =
                if (
                    accountWithType.accountType.keepTotals
                ) getString(R.string.this_account_does_not_keep_a_balance_owing_amount) else ""
            display += if (
                accountWithType.accountType.isAsset
            ) getString(R.string.this_is_an_asset) else ""
            display += if (
                accountWithType.accountType.displayAsAsset
            ) getString(R.string.this_will_be_used_for_the_budget) else ""
            display += if (
                accountWithType.accountType.tallyOwing)
                getString(R.string.balance_owing_will_be_calculated) else ""
            display += if (
                accountWithType.accountType.allowPending)
                getString(R.string.transactions_may_be_postponed) else ""
            if (display.isEmpty()) {
                display =
                    getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
            }
            tvTypeDetails.text = display
        }
    }

    private fun getAccountNameListForValidation() {
        accountViewModel.getAccountNameList().observe(viewLifecycleOwner) { accounts ->
            accountNameList.clear()
            accounts.listIterator().forEach {
                accountNameList.add(it)
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvAccAddType.setOnClickListener { gotoAccountTypes() }
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
                        isAccountReadyToSave()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

    private fun isAccountReadyToSave() {
        val message = validateAccount()
        if (message == ANSWER_OK) {
            saveAccount()
        } else {
            showMessage(message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun saveAccount() {
        mainViewModel.removeCallingFragment(TAG)
        val curAccount = getCurrentAccount()
        accountViewModel.addAccount(curAccount)
        mainViewModel.setAccountWithType(
            AccountWithType(
                curAccount,
                mainViewModel.getAccountWithType()?.accountType!!
            )
        )
        gotoAccountsFragment()
    }

    private fun validateAccount(): String {
        binding.apply {
            if (etAccAddName.text.isNullOrEmpty()) {
                return getString(R.string.error) + getString(R.string.please_enter_a_name)
            }
            for (i in 0 until accountNameList.size) {
                if (accountNameList[i] == etAccAddName.text.toString().trim()
                ) {
                    return getString(R.string.error) + getString(R.string.this_account_rule_already_exists)
                }
            }
            if (mainViewModel.getAccountWithType()?.accountType == null
            ) {
                return getString(R.string.error) +
                        getString(R.string.this_account_must_have_an_account_type)
            }
            return ANSWER_OK
        }
    }

    private fun gotoAccountTypes() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(
            AccountWithType(getCurrentAccount(), null)
        )
        gotoAccountTypesFragment()

    }

    private fun gotoCalculator(type: String) {
        when (type) {
            BALANCE -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddBalance.text.toString().ifBlank {
                            getString(R.string.zero_double)
                        }
                    )
                )
            }

            OWING -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddOwing.text.toString().ifBlank {
                            getString(R.string.zero_double)
                        }
                    )
                )
            }

            BUDGETED -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.etAccAddBudgeted.text.toString().ifBlank {
                            getString(R.string.zero_double)
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
        gotoCalculatorFragment()
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            AccountAddFragmentDirections
                .actionAccountAddFragmentToAccountsFragment()
        )
    }

    private fun gotoAccountTypesFragment() {
        mView.findNavController().navigate(
            AccountAddFragmentDirections
                .actionAccountAddFragmentToAccountTypesFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            AccountAddFragmentDirections
                .actionAccountAddFragmentToCalcFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}