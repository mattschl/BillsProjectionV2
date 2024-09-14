package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.DatePickerDialog
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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionPerformBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_PERFORM

class TransactionPerformFragment : Fragment(
    R.layout.fragment_transaction_perform
) {

    private var _binding: FragmentTransactionPerformBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var accountViewModel: AccountViewModel

    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mBudgetRule: BudgetRule? = null
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionPerformBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        transactionViewModel =
            mainActivity.transactionViewModel
        accountViewModel =
            mainActivity.accountViewModel
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        mainActivity.title = "Perform a Transaction"
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        createMenuActions()
        createClickActions()
    }

    private fun createClickActions() {
        binding.apply {
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
            }
            etTransDate.setOnClickListener {
                chooseDate()
            }
            etAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b && etAmount.text.toString().isEmpty()) {
                    etAmount.setText(
                        nf.displayDollars(0.0)
                    )
                } else if (!b) {
                    etAmount.setText(
                        nf.displayDollars(
                            nf.getDoubleFromDollars(
                                etAmount.text.toString()
                            )
                        )
                    )
                    calculateRemainder()
                }
            }
            etBudgetedAmount.setOnFocusChangeListener { _, b ->
                if (!b && etBudgetedAmount.text.toString().isEmpty()) {
                    etBudgetedAmount.setText(
                        nf.displayDollars(0.0)
                    )
                } else if (!b) {
                    etBudgetedAmount.setText(
                        nf.displayDollars(
                            nf.getDoubleFromDollars(
                                etBudgetedAmount.text.toString()
                            )
                        )
                    )
                    calculateRemainder()
                }
                if (nf.getDoubleFromDollars(etBudgetedAmount.text.toString()) !=
                    mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedAmount
                ) {
                    val mBudgetItem =
                        mainViewModel.getBudgetItem()
                    mBudgetItem!!.budgetItem!!.biProjectedAmount =
                        nf.getDoubleFromDollars(
                            etBudgetedAmount.text.toString()
                        )
                    mainViewModel.setBudgetItem(mBudgetItem)
                }
            }
            btnSplit.setOnClickListener {
                splitTransaction()
            }
        }
    }

    private fun splitTransaction() {
        mainViewModel.setSplitTransactionDetailed(null)
        mainViewModel.setTransferNum(0.0)
        if (mFromAccount != null &&
            nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0
        ) {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments() + ", " + TAG
            )
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            mView.findNavController().navigate(
                TransactionPerformFragmentDirections
                    .actionTransactionPerformFragmentToTransactionSplitFragment()
            )
        }
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        mView.findNavController().navigate(
            TransactionPerformFragmentDirections
                .actionTransactionPerformFragmentToCalcFragment()
        )
    }

    private fun calculateRemainder() {
        binding.apply {
            val amt =
                if (etAmount.text.toString().isNotBlank()) {
                    nf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                } else {
                    0.0
                }
            val budgeted = nf.getDoubleFromDollars(
                etBudgetedAmount.text.toString()
            )
            tvRemainder.text =
                nf.displayDollars(budgeted - amt)
            btnSplit.isEnabled = amt > 0 && mFromAccount != null
        }
    }

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etTransDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etTransDate.text = display
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the first date")
            datePickerDialog.show()
        }
    }

    private fun chooseFromAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        mView.findNavController().navigate(
            TransactionPerformFragmentDirections
                .actionTransactionPerformFragmentToAccountsFragment()
        )
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        mView.findNavController().navigate(
            TransactionPerformFragmentDirections
                .actionTransactionPerformFragmentToAccountsFragment()
        )
    }

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            mBudgetRule,
            mToAccount,
            mFromAccount
        )
    }

    private fun populateValues() {
        if (mainViewModel.getTransactionDetailed() != null
        ) {
            populateValuesFromTransaction()
        } else if (mainViewModel.getBudgetItem() != null) {
            populateValuesFromBudgetItem()
        }

    }

    private fun populateValuesFromBudgetItem() {
        mToAccount = mainViewModel.getBudgetItem()!!.toAccount
        mFromAccount = mainViewModel.getBudgetItem()!!.fromAccount
        mBudgetRule = mainViewModel.getBudgetItem()!!.budgetRule
        binding.apply {
            val mBudgetItem =
                mainViewModel.getBudgetItem()!!.budgetItem!!
            tvBudgetRule.text =
                mBudgetRule!!.budgetRuleName
            etDescription.setText(
                mBudgetItem.biBudgetName
            )
            etTransDate.text = df.getCurrentDateAsString()
            etAmount.hint =
                "Budgeted: ${nf.displayDollars(mBudgetItem.biProjectedAmount)}"
            tvToAccount.text =
                mToAccount!!.accountName
            accountViewModel.getAccountDetailed(
                mToAccount!!.accountId
            ).observe(
                viewLifecycleOwner
            ) { accWType ->
                if (accWType.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                    if (accWType.accountType.tallyOwing) {
                        chkToAccPending.isChecked = true
                    }
                } else {
                    chkToAccPending.visibility = View.GONE
                }
            }
            tvFromAccount.text =
                mFromAccount!!.accountName
            accountViewModel.getAccountDetailed(
                mFromAccount!!.accountId
            ).observe(
                viewLifecycleOwner
            ) { accWType ->
                if (accWType.accountType!!.allowPending) {
                    chkFromAccPending.visibility = View.VISIBLE
                    chkFromAccPending.isChecked = accWType.accountType.tallyOwing
                } else {
                    chkFromAccPending.visibility = View.GONE
                }
            }
            etBudgetedAmount.setText(
                nf.displayDollars(
                    mBudgetItem.biProjectedAmount
                )
            )
            calculateRemainder()
        }
    }

    private fun populateValuesFromTransaction() {
        if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
            val mTransaction =
                mainViewModel.getTransactionDetailed()!!.transaction!!
            mBudgetRule = mainViewModel.getTransactionDetailed()!!.budgetRule
            mToAccount = mainViewModel.getTransactionDetailed()!!.toAccount
            mFromAccount = mainViewModel.getTransactionDetailed()!!.fromAccount
            binding.apply {
                etDescription.setText(
                    mTransaction.transName
                )
                etNote.setText(
                    mTransaction.transNote
                )
                etTransDate.text = mTransaction.transDate
                etAmount.setText(
                    nf.displayDollars(
                        if (mainViewModel.getTransferNum()!! != 0.0) {
                            mainViewModel.getTransferNum()!!
                        } else {
                            mTransaction.transAmount
                        }
                    )
                )
                mainViewModel.setTransferNum(0.0)
                etBudgetedAmount.setText(
                    nf.displayDollars(
                        mainViewModel.getBudgetItem()!!.budgetItem!!.biProjectedAmount
                    )
                )
                mBudgetRule =
                    mainViewModel.getTransactionDetailed()!!.budgetRule
                tvBudgetRule.text =
                    mBudgetRule!!.budgetRuleName
                mToAccount =
                    mainViewModel.getTransactionDetailed()!!.toAccount!!
                tvToAccount.text =
                    mToAccount!!.accountName
                accountViewModel.getAccountDetailed(
                    mToAccount!!.accountId
                ).observe(
                    viewLifecycleOwner
                ) { accWType ->
                    if (accWType.accountType!!.allowPending) {
                        chkToAccPending.visibility = View.VISIBLE
                    } else {
                        chkToAccPending.visibility = View.GONE
                    }
                }
                chkToAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
                mFromAccount =
                    mainViewModel.getTransactionDetailed()!!.fromAccount!!
                tvFromAccount.text =
                    mFromAccount!!.accountName
                accountViewModel.getAccountDetailed(
                    mFromAccount!!.accountId
                ).observe(
                    viewLifecycleOwner
                ) { accWType ->
                    if (accWType.accountType!!.allowPending) {
                        chkFromAccPending.visibility = View.VISIBLE
                    } else {
                        chkFromAccPending.visibility = View.GONE
                    }
                }
                chkFromAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!
                        .transaction!!.transFromAccountPending
                etTransDate.text = mainViewModel.getTransactionDetailed()!!
                    .transaction!!.transDate
            }
            calculateRemainder()
        }
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
                        menuItem.isEnabled = false
                        performTransaction()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun performTransaction() {
        calculateRemainder()
        val mes = validateTransaction()
        if (mes == "Ok") {
            val mTransaction = getCurrentTransactionForSave()
            transactionViewModel.insertTransaction(
                mTransaction
            )
            CoroutineScope(Dispatchers.IO).launch {
                val go = async {
                    updateAccountBalances(mTransaction)
                }
                if (go.await()) {
                    updateBudgetItem()
                }
            }
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mView.context,
                mes, Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun updateBudgetItem() {
        val remainder =
            nf.getDoubleFromDollars(
                binding.tvRemainder.text.toString()
            )
        var completed = false
        if (remainder < 2.0) {
            completed = true
        }
        val mBudget = mainViewModel.getBudgetItem()!!.budgetItem!!
        budgetItemViewModel.updateBudgetItem(
            BudgetItem(
                mBudget.biRuleId,
                mBudget.biProjectedDate,
                mBudget.biActualDate,
                mBudget.biPayDay,
                mBudget.biBudgetName,
                mBudget.biIsPayDayItem,
                mBudget.biToAccountId,
                mBudget.biFromAccountId,
                remainder,
                mBudget.biIsPending,
                mBudget.biIsFixed,
                mBudget.biIsAutomatic,
                mBudget.biManuallyEntered,
                completed,
                mBudget.biIsCancelled,
                mBudget.biIsDeleted,
                df.getCurrentTimeAsString(),
                mBudget.biLocked
            )
        )
    }

    private fun updateAccountBalances(mTransaction: Transactions): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val toAccountWithType =
                accountViewModel.getAccountWithType(
                    mToAccount!!.accountId
                )
            if (!mTransaction.transToAccountPending) {
                if (toAccountWithType.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        toAccountWithType.account.accountBalance +
                                mTransaction.transAmount,
                        mToAccount!!.accountId,
                        df.getCurrentTimeAsString()
                    )
                    Log.d(TAG, "updating toAccountBalance")
                }
                if (toAccountWithType.accountType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        toAccountWithType.account.accountOwing -
                                mTransaction.transAmount,
                        mToAccount!!.accountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }
            val fromAccountWithType =
                accountViewModel.getAccountWithType(
                    mFromAccount!!.accountId
                )
            if (!mTransaction.transFromAccountPending) {
                if (fromAccountWithType.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        fromAccountWithType.account.accountBalance -
                                mTransaction.transAmount,
                        mFromAccount!!.accountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (fromAccountWithType.accountType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        fromAccountWithType.account.accountOwing +
                                mTransaction.transAmount,
                        mFromAccount!!.accountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }
        }
        return true
    }

    private fun gotoCallingFragment() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.setBudgetRuleDetailed(null)
        if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_VIEW)
        ) {
            gotoBudgetViewFragment()
        }
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            TransactionPerformFragmentDirections
                .actionTransactionPerformFragmentToBudgetViewFragment()
        )
    }


    private fun getCurrentTransactionForSave(): Transactions {
        binding.apply {
            return Transactions(
                nf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule?.ruleId ?: 0L,
                mToAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                mFromAccount?.accountId ?: 0L,
                chkFromAccPending.isChecked,
                if (etAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                },
                transIsDeleted = false,
                transUpdateTime = df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateTransaction(): String {
        binding.apply {
            val amount =
                if (etAmount.text.isNotEmpty()) {
                    nf.getDoubleFromDollars(etAmount.text.toString())
                } else {
                    0.0
                }
            val errorMes =
                if (etDescription.text.isNullOrBlank()
                ) {
                    "     Error!!\n" +
                            "Please enter a description"
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount for this transaction"
                } else {
                    "Ok"
                }
            return errorMes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}