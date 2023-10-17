package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionSplitBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANSACTION_SPLIT

class TransactionSplitFragment : Fragment(R.layout.fragment_transaction_split) {

    private var _binding: FragmentTransactionSplitBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var mView: View
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSplitBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "Creating $TAG")
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel =
            mainActivity.transactionViewModel
        accountViewModel =
            mainActivity.accountViewModel
        mainActivity.title = "Splitting Transaction"
        createMenu()
        fillValues()
        createActions()
    }

    private fun createActions() {
        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
            }
            etAmount.setOnLongClickListener {
                gotoCalc()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b) {
                    updateAmountDisplay()
                }
            }
        }
    }

    private fun chooseFromAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToAccountsFragment()
        )
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToAccountsFragment()
        )
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + "', " + TAG
        )
        mainViewModel.setSplitTransactionDetailed(
            getSplitTransDetailed()
        )
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToBudgetRuleFragment()
        )
    }

    private fun gotoCalc() {
        mainViewModel.setTransferNum(
            cf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToCalcFragment()
        )
    }

    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                cf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mainViewModel.getTransactionDetailed()?.budgetRule?.ruleId ?: 0L,
                mToAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                mFromAccount?.accountId ?: 0L,
                chkFromAccPending.isChecked,
                if (etAmount.text.isNotEmpty()) {
                    cf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                },
                transIsDeleted = false,
                transUpdateTime = df.getCurrentTimeAsString()
            )
        }
    }

    private fun getSplitTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurTransaction(),
            mainViewModel.getSplitTransactionDetailed()?.budgetRule,
            mainViewModel.getSplitTransactionDetailed()?.toAccount,
            mainViewModel.getSplitTransactionDetailed()?.fromAccount,
        )
    }

    private fun fillValues() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed() != null) {
                tvOriginalAmount.text = cf.displayDollars(
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                )
                etTransDate.setText(
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
                )
                etTransDate.isEnabled = false
            } else {
                etTransDate.isEnabled = true
            }
            if (mainViewModel.getSplitTransactionDetailed() != null) {
                etDescription.setText(
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transName
                )
                etNote.setText(
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transNote
                )
                if (mainViewModel.getSplitTransactionDetailed()!!.budgetRule != null) {
                    tvBudgetRule.text =
                        mainViewModel.getSplitTransactionDetailed()!!.budgetRule!!.budgetRuleName
                }
                etAmount.setText(
                    cf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            mainViewModel.getSplitTransactionDetailed()?.transaction?.transAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                updateAmountDisplay()
                if (mainViewModel.getSplitTransactionDetailed()?.budgetRule != null) {
                    mBudgetRule = mainViewModel.getSplitTransactionDetailed()!!.budgetRule!!
                    tvBudgetRule.text = mBudgetRule!!.budgetRuleName
                    if (mainViewModel.getSplitTransactionDetailed()!!.toAccount != null) {
                        if (mBudgetRule!!.budToAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val toAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            mBudgetRule!!.budToAccountId
                                        )
                                    }
                                mToAccount = toAccount.await()
                                val toAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            mBudgetRule!!.budgetRuleName
                                        )
                                    }
                                mToAccountWithType = toAccountWithType.await()
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WAIT_250)
                                tvToAccount.text = mToAccount?.accountName
                                if (mToAccountWithType?.accountType?.allowPending == true) {
                                    chkToAccPending.visibility = View.VISIBLE
                                } else {
                                    chkToAccPending.visibility = View.GONE
                                }
                            }
                        }
                    }
                    if (mainViewModel.getSplitTransactionDetailed()!!.fromAccount != null) {
                        if (mBudgetRule!!.budFromAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val fromAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            mBudgetRule!!.budToAccountId
                                        )
                                    }
                                mFromAccount = fromAccount.await()
                                val fromAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            mBudgetRule!!.budFromAccountId
                                        )
                                    }
                                mFromAccountWithType = fromAccountWithType.await()
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(WAIT_250)
                            tvFromAccount.text = mFromAccount?.accountName
                            if (mFromAccountWithType?.accountType?.allowPending == true) {
                                chkFromAccPending.visibility = View.VISIBLE
                            } else {
                                chkFromAccPending.visibility = View.GONE
                            }
                        }
                    }
                }
                if (mainViewModel.getSplitTransactionDetailed()?.toAccount != null) {
                    mToAccount = mainViewModel.getSplitTransactionDetailed()!!.toAccount
                    tvToAccount.text = mToAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val toAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    mToAccount!!.accountName
                                )
                            }
                        mToAccountWithType = toAccountWithType.await()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(WAIT_250)
                        if (mToAccountWithType?.accountType?.allowPending == true) {
                            chkToAccPending.visibility = View.VISIBLE
                        } else {
                            chkToAccPending.visibility = View.GONE
                        }
                    }
                }
                chkToAccPending.isChecked =
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transToAccountPending
                if (mainViewModel.getSplitTransactionDetailed()?.fromAccount != null) {
                    mFromAccount =
                        mainViewModel.getSplitTransactionDetailed()!!.fromAccount
                    tvFromAccount.text = mFromAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val fromAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    mFromAccount!!.accountId
                                )
                            }
                        mFromAccountWithType = fromAccountWithType.await()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(WAIT_250)
                        if (mFromAccountWithType?.accountType?.allowPending == true) {
                            chkFromAccPending.visibility = View.VISIBLE
                        } else {
                            chkFromAccPending.visibility = View.GONE
                        }
                    }
                } else {
                    chkFromAccPending.visibility = View.GONE
                    chkToAccPending.visibility = View.GONE
                }
                chkFromAccPending.isChecked =
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transFromAccountPending
            }
        }
    }

    private fun updateAmountDisplay() {
        binding.apply {
            etAmount.setText(
                cf.displayDollars(
                    cf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                )
            )
        }
    }

    private fun createMenu() {
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
                        menuItem.isEnabled = false
                        saveTransaction()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveTransaction() {
        val mes = checkTransactions()
        if (mes == "Ok") {
            val mTransaction = getCurTransaction()
            transactionViewModel.insertTransaction(
                mTransaction
            )
            updateAccounts(mTransaction)
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateAccounts(mTransaction: Transactions): Boolean {
        if (!mTransaction.transToAccountPending) {
            if (mToAccountWithType!!.accountType!!.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    mToAccountWithType!!.account.accountBalance +
                            mTransaction.transAmount,
                    mToAccount!!.accountId,
                    df.getCurrentTimeAsString()
                )
                Log.d(TAG, "updating toAccountBalance")
            }
            if (mToAccountWithType!!.accountType!!.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    mToAccountWithType!!.account.accountOwing -
                            mTransaction.transAmount,
                    mToAccount!!.accountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        if (!mTransaction.transFromAccountPending) {
            if (mFromAccountWithType!!.accountType!!.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    mFromAccountWithType!!.account.accountBalance -
                            mTransaction.transAmount,
                    mFromAccount!!.accountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (mFromAccountWithType!!.accountType!!.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    mFromAccountWithType!!.account.accountOwing +
                            mTransaction.transAmount,
                    mFromAccount!!.accountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        gotoCallingFragment()
        return true
    }

    private fun gotoCallingFragment() {
        TODO("Not yet implemented")
    }

    private fun checkTransactions(): String {
        binding.apply {
            val amount =
                if (etAmount.text.isNotEmpty()) {
                    cf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                }
            val errorMes =
                if (etDescription.text.isNullOrBlank()
                ) {
                    "     Error!!\n" +
                            "Please enter a description"
                } else if (mToAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (mFromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount for this transaction"
                } else if (mainViewModel.getSplitTransactionDetailed()!!.budgetRule == null) {
                    if (saveWithoutBudget()) {
                        "Ok"
                    } else {
                        "Choose a Budget Rule"
                    }
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

    private fun saveWithoutBudget(): Boolean {
        var bool = false
        AlertDialog.Builder(activity).apply {
            setMessage(
                "There is no Budget Rule!" +
                        "Budget Rules are used to update the budget."
            )
            setPositiveButton("Save anyway") { _, _ ->

                bool = true
            }
            setNegativeButton("Retry", null)
        }.create().show()
        return bool
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}