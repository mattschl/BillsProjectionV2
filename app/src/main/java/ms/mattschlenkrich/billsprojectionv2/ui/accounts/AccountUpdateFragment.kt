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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGETED
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.OWING
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter.AccountUpdateHistoryAdapter

private const val TAG = FRAG_ACCOUNT_UPDATE

class AccountUpdateFragment :
    Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var transactionViewModel: TransactionViewModel
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
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        transactionViewModel = mainActivity.transactionViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_account)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        CoroutineScope(Dispatchers.Main).launch {
            val accountWithType = mainViewModel.getAccountWithType()!!
            getAccountListNamesForValidation()
            populateHistory(accountWithType)
            populateAccountInfo(accountWithType)
            updateBalances()
        }
    }

    private fun populateAccountInfo(accountWithType: AccountWithType) {
        mainViewModel.setAccountWithType(accountWithType)
        binding.apply {
            edAccountUpdateName.setText(accountWithType.account.accountName)
            edAccountUpdateHandle.setText(accountWithType.account.accountNumber)
            if (accountWithType.accountType != null) {
                drpAccountUpdateType.text = accountWithType.accountType.accountType
            }
            edAccountUpdateBalance.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0 &&
                        mainViewModel.getReturnTo()!!.contains(BALANCE)
                    ) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        accountWithType.account.accountBalance
                    }
                )
            )
            edAccountUpdateOwing.setText(
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
            edAccountUpdateBudgeted.setText(
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
            etAccUpdateLimit.setText(
                nf.displayDollars(accountWithType.account.accountCreditLimit)
            )
            txtAccountUpdateAccountId.text =
                String.format(accountWithType.account.accountId.toString())
        }
    }

    fun updateBalances() {
        binding.apply {
            accountViewModel.getAccountWithTypeLive(mainViewModel.getAccountWithType()!!.account.accountId)
                .observe(viewLifecycleOwner) { accountWithType ->
                    if (accountWithType.accountType != null) {
                        if (accountWithType.accountType.keepTotals) {
                            edAccountUpdateBalance.setText(
                                nf.displayDollars(accountWithType.account.accountBalance)
                            )
                        } else if (accountWithType.accountType.tallyOwing) {
                            edAccountUpdateOwing.setText(
                                nf.displayDollars(accountWithType.account.accountOwing)
                            )
                        }
                    }
                }
        }
    }

    private fun populateHistory(accountWithType: AccountWithType) {
        val historyAdapter = AccountUpdateHistoryAdapter(mainActivity, mView, TAG, this)
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = historyAdapter
        }
        transactionViewModel.getActiveTransactionByAccount(accountWithType.account.accountId)
            .observe(viewLifecycleOwner) { transactionList ->
                historyAdapter.differ.submitList(transactionList)
                toggleRecycler(transactionList)
            }
    }

    private fun toggleRecycler(transactionList: List<TransactionDetailed>) {
        binding.apply {
            if (transactionList.isEmpty()) {
                rvHistory.visibility = View.GONE
            } else {
                rvHistory.visibility = View.VISIBLE
            }
        }
    }

    private fun getAccountListNamesForValidation() {
        accountViewModel.getAccountNameList().observe(
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
            drpAccountUpdateType.setOnClickListener { gotoAccountTypes() }
            fabAccountUpdateDone.setOnClickListener { updateAccountIfValid() }
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
                        confirmDeleteAccount()
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
                mainViewModel.getAccountWithType()!!.account.accountId,
                edAccountUpdateName.text.toString().trim(),
                edAccountUpdateHandle.text.toString().trim(),
                mainViewModel.getAccountWithType()!!.accountType?.typeId ?: 0L,
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
            if (edAccountUpdateName.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name)
            }
            for (i in 0 until accountNameList.size) {
                if (accountNameList[i] ==
                    edAccountUpdateName.text.toString() &&
                    accountNameList[i] !=
                    mainViewModel.getAccountWithType()!!.account.accountName
                ) {
                    return getString(R.string.this_budget_rule_already_exists)
                }
            }
            if (drpAccountUpdateType.text.isNullOrBlank()) {
                return getString(R.string.please_choose_an_account_type)
            }
            return ANSWER_OK
        }
    }

    private fun updateAccountIfValid() {
        val answer = validateAccount()
        if (answer == ANSWER_OK) {
            confirmUpdateAccount()
        } else {
            showMessage(getString(R.string.error) + answer)
        }

    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun confirmUpdateAccount() {
        val accountWithType = mainViewModel.getAccountWithType()!!
        val name = binding.edAccountUpdateName.text.trim().toString()
        if (name == accountWithType.account.accountName.trim()) {
            accountViewModel.updateAccount(getUpdatedAccount())
            gotoCallingFragment()
        } else if (name != accountWithType.account.accountName.trim()) {
            confirmRenameAccount()
        }
    }

    private fun confirmRenameAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.rename_account))
            setMessage(
                getString(R.string.are_you_sure_you_want_to_rename_this_account) +
                        getString(R.string.note) +
                        getString(R.string.this_will_not_replace_an_existing_account_type)
            )
            setPositiveButton(getString(R.string.update_account)) { _, _ ->
                accountViewModel.updateAccount(getUpdatedAccount())
                gotoCallingFragment()

            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.delete_account))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_account))
            setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccount()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteAccount() {
        accountViewModel.deleteAccount(
            mainViewModel.getAccountWithType()!!.account.accountId,
            df.getCurrentTimeAsString()
        )
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setAccountWithType(null)
        gotoAccountsFragment()
    }

    private fun gotoCalculator(type: String) {
        when (type) {
            BALANCE -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateBalance.text.toString().ifBlank {
                            getString(R.string.zero_double)
                        }
                    )
                )
            }

            OWING -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateOwing.text.toString().ifBlank {
                            getString(R.string.zero_double)
                        }
                    )
                )
            }

            BUDGETED -> {
                mainViewModel.setTransferNum(
                    nf.getDoubleFromDollars(
                        binding.edAccountUpdateBudgeted.text.toString().ifBlank {
                            getString(R.string.zero_double)
                        }
                    )
                )
            }
        }
        mainViewModel.setReturnTo("$TAG, $type")
        mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(),
                mainViewModel.getAccountWithType()!!.accountType
            )
        )
        gotoCalculatorFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(TAG)
        val callingFragments = mainViewModel.getCallingFragments()!!
        if (callingFragments.contains(FRAG_ACCOUNTS)) {
            gotoAccountsFragment()
        } else if (callingFragments.contains(FRAG_BUDGET_VIEW)) {
            gotoBudgetViewFragment()
        }
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToAccountsFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoAccountTypes() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setAccountWithType(
            AccountWithType(
                getUpdatedAccount(),
                mainViewModel.getAccountWithType()!!.accountType
            )
        )
        gotoAccountTypesFragment()
    }

    private fun gotoAccountTypesFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToAccountTypesFragment()
        )
    }

    fun gotoTransactionUpdate() {
        mainViewModel.addCallingFragment(TAG)
        gotoTransactionUpdateFragment()
    }

    private fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToTransactionUpdateFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        mainViewModel.addCallingFragment(TAG)
        mView.findNavController().navigate(
            AccountUpdateFragmentDirections.actionAccountUpdateFragmentToBudgetRuleUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}