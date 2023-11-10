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
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionSplitBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
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
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var mView: View
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private lateinit var mFromAccount: Account
    private var mToAccountWithType: AccountWithType? = null
    private lateinit var mFromAccountWithType: AccountWithType

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
        transactionViewModel = mainActivity.transactionViewModel
        accountViewModel = mainActivity.accountViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
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
            etAmount.setOnLongClickListener {
                gotoCalc()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b) updateAmountsDisplay()
            }
            etDescription.setOnFocusChangeListener { _, _ ->
                updateAmountsDisplay()
            }
            etNote.setOnFocusChangeListener { _, _ ->
                updateAmountsDisplay()
            }
        }
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
                mFromAccount.accountId,
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
            mBudgetRule,
            mToAccount,
            mFromAccount,
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
                mFromAccount = mainViewModel.getTransactionDetailed()!!.fromAccount!!
                tvFromAccount.text = mFromAccount.accountName
                accountViewModel.getAccountDetailed(mFromAccount.accountId).observe(
                    viewLifecycleOwner
                ) {
                    mFromAccountWithType = it
                    if (mFromAccountWithType.accountType!!.allowPending) {
                        chkFromAccPending.visibility = View.VISIBLE
                    } else {
                        chkFromAccPending.visibility = View.GONE
                    }
                }
                chkFromAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending
            }
            if (mainViewModel.getSplitTransactionDetailed() != null) {
                etDescription.setText(
                    mainViewModel.getSplitTransactionDetailed()?.transaction?.transName
                )
                etNote.setText(
                    mainViewModel.getSplitTransactionDetailed()?.transaction?.transNote
                )
                chkFromAccPending.isChecked =
                    mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transFromAccountPending
                if (mainViewModel.getTransferNum() != null &&
                    mainViewModel.getTransferNum() != 0.0
                ) {
                    etAmount.setText(
                        cf.displayDollars(
                            mainViewModel.getTransferNum()!!
                        )
                    )
                } else if (mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transAmount != 0.0) {
                    etAmount.setText(
                        cf.displayDollars(
                            mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transAmount
                        )
                    )
                } else {
                    etAmount.setText(
                        cf.displayDollars(0.0)
                    )
                }
                updateAmountsDisplay()
                if (mainViewModel.getSplitTransactionDetailed()!!.toAccount != null) {
                    mToAccount = mainViewModel.getSplitTransactionDetailed()!!.toAccount
                    tvToAccount.text = mToAccount!!.accountName

                    accountViewModel.getAccountDetailed(mToAccount!!.accountId).observe(
                        viewLifecycleOwner
                    ) {
                        mToAccountWithType = it
                        if (it.accountType!!.allowPending) {
                            chkToAccPending.visibility = View.VISIBLE
                            chkToAccPending.isChecked =
                                mainViewModel.getSplitTransactionDetailed()!!
                                    .transaction!!.transToAccountPending
                        } else {
                            chkToAccPending.visibility = View.GONE
                        }
                    }
                }
                if (mainViewModel.getSplitTransactionDetailed()!!.budgetRule != null) {
                    mBudgetRule = mainViewModel.getSplitTransactionDetailed()!!.budgetRule!!
                    tvBudgetRule.text = mBudgetRule!!.budgetRuleName
                    if (mainViewModel.getSplitTransactionDetailed()!!.transaction!!
                            .transName.isBlank()
                    ) {
                        etDescription.setText(mBudgetRule!!.budgetRuleName)
                    }
                    if (mainViewModel.getSplitTransactionDetailed()!!.toAccount == null) {
                        budgetRuleViewModel.getBudgetRuleFullLive(mBudgetRule!!.ruleId).observe(
                            viewLifecycleOwner
                        ) { it ->
                            mToAccount = it.toAccount
                            tvToAccount.text = it.toAccount!!.accountName
                            accountViewModel.getAccountDetailed(mToAccount!!.accountId).observe(
                                viewLifecycleOwner
                            ) {
                                mToAccountWithType = it
                                if (mToAccountWithType!!.accountType!!.allowPending) {
                                    chkToAccPending.visibility = View.VISIBLE
                                    chkToAccPending.isChecked =
                                        mainViewModel.getSplitTransactionDetailed()!!
                                            .transaction!!.transToAccountPending
                                } else {
                                    chkToAccPending.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateAmountsDisplay() {
        binding.apply {
            etAmount.setText(
                cf.displayDollars(
                    cf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                )
            )
            val amount = cf.getDoubleFromDollars(
                etAmount.text.toString()
            )
            val original = cf.getDoubleFromDollars(
                tvOriginalAmount.text.toString()
            )
            if (original <= amount) {
                Toast.makeText(
                    mView.context,
                    "     ERROR!!!\n" +
                            "New amount cannot be more than the original amount",
                    Toast.LENGTH_LONG
                ).show()
                etAmount.setText(
                    cf.displayDollars(0.0)
                )
            } else {
                tvRemainder.text = cf.displayDollars(original - amount)
            }

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
        updateAmountsDisplay()
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
        if (!mTransaction.transFromAccountPending &&
            !mainViewModel.getUpdatingTransaction()
        ) {
            if (mFromAccountWithType.accountType!!.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    mFromAccountWithType.account.accountBalance -
                            mTransaction.transAmount,
                    mFromAccount.accountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (mFromAccountWithType.accountType!!.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    mFromAccountWithType.account.accountOwing +
                            mTransaction.transAmount,
                    mFromAccount.accountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        gotoCallingFragment()
        return true
    }

    private fun gotoCallingFragment() {
        val oldTransaction = mainViewModel.getTransactionDetailed()!!.transaction!!
        oldTransaction.transAmount =
            cf.getDoubleFromDollars(binding.tvRemainder.text.toString())
        if (mainViewModel.getUpdatingTransaction()) {
            transactionViewModel.updateTransaction(oldTransaction)
        }
        mainViewModel.setUpdatingTransaction(false)
        mainViewModel.setTransactionDetailed(
            TransactionDetailed(
                oldTransaction,
                mainViewModel.getTransactionDetailed()!!.budgetRule,
                mainViewModel.getTransactionDetailed()!!.toAccount,
                mainViewModel.getTransactionDetailed()!!.fromAccount
            )
        )
        mainViewModel.setSplitTransactionDetailed(null)
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_ADD)) {
            mainViewModel.setUpdatingTransaction(false)
            mView.findNavController().navigate(
                TransactionSplitFragmentDirections
                    .actionTransactionSplitFragmentToTransactionAddFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_PERFORM)) {
            mainViewModel.setUpdatingTransaction(false)
            mView.findNavController().navigate(
                TransactionSplitFragmentDirections
                    .actionTransactionSplitFragmentToTransactionPerformFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_UPDATE)) {
            mainViewModel.setUpdatingTransaction(true)
            mView.findNavController().navigate(
                TransactionSplitFragmentDirections
                    .actionTransactionSplitFragmentToTransactionViewFragment()
            )
        }
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
                if (cf.getDoubleFromDollars(etAmount.text.toString()) >=
                    cf.getDoubleFromDollars(tvOriginalAmount.text.toString())
                ) {
                    "     Error!!\n" +
                            "The amount of a split transaction must be less than the original!"
                } else if (etDescription.text.isNullOrBlank()
                ) {
                    "     Error!!\n" +
                            "Please enter a description"
                } else if (mToAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount for this transaction"
                } else if (mainViewModel.getSplitTransactionDetailed()!!.budgetRule == null) {
                    if (!saveWithoutBudget()) {
                        "Choose a Budget Rule"
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
        AlertDialog.Builder(activity).apply {
            setMessage(
                "There is no Budget Rule!" +
                        "Budget Rules are used to update the budget."
            )
            setNegativeButton("Retry", null)
        }.create().show()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}