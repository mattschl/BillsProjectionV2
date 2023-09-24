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
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionPerformBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
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
    private val cf = CommonFunctions()
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
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel =
            mainActivity.transactionViewModel
        accountViewModel =
            mainActivity.accountViewModel
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        mainActivity.title = "Perform a Transaction"
        fillValues()
        createMenu()
        createActions()
    }

    private fun createActions() {
        binding.apply {
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            tvFromAccount.setOnClickListener {
                chooseFromAccount()
            }
            etTransDate.setOnLongClickListener {
                chooseDate()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b && etAmount.text.toString().isEmpty()) {
                    etAmount.setText(
                        cf.displayDollars(0.0)
                    )
                } else if (!b) {
                    etAmount.setText(
                        cf.displayDollars(
                            cf.getDoubleFromDollars(
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
                        cf.displayDollars(0.0)
                    )
                } else if (!b) {
                    etBudgetedAmount.setText(
                        cf.displayDollars(
                            cf.getDoubleFromDollars(
                                etBudgetedAmount.text.toString()
                            )
                        )
                    )
                    calculateRemainder()
                }
                val mBudgetItem =
                    mainViewModel.getBudgetItem()
                mBudgetItem!!.budgetItem!!.biProjectedAmount =
                    cf.getDoubleFromDollars(
                        etBudgetedAmount.text.toString()
                    )
                mainViewModel.setBudgetItem(mBudgetItem)
            }
        }
    }

    private fun calculateRemainder() {
        binding.apply {
            val amt =
                if (etAmount.text.toString().isNotBlank()) {
                    cf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                } else {
                    0.0
                }
            val budgeted = cf.getDoubleFromDollars(
                etBudgetedAmount.text.toString()
            )
            tvRemainder.text =
                cf.displayDollars(budgeted - amt)

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
                    etTransDate.setText(display)
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
            getCurTransaction(),
            mBudgetRule,
            mToAccount,
            mFromAccount
        )
    }

    private fun fillValues() {
        if (mainViewModel.getTransactionDetailed() != null &&
            mainViewModel.getBudgetItem() != null
        ) {
            fillFromTransaction()
        } else if (mainViewModel.getBudgetItem() != null) {
            fillFromBudgetItem()
        }
    }

    private fun fillFromBudgetItem() {
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
            etTransDate.setText(
                df.getCurrentDateAsString()
            )
            etAmount.hint =
                "Budgeted: ${cf.displayDollars(mBudgetItem.biProjectedAmount)}"
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
            etBudgetedAmount.setText(
                cf.displayDollars(
                    mBudgetItem.biProjectedAmount
                )
            )
            calculateRemainder()
        }
    }

    private fun fillFromTransaction() {
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
                etTransDate.setText(
                    mTransaction.transDate
                )
                etAmount.setText(
                    cf.displayDollars(
                        mTransaction.transAmount
                    )
                )
                etBudgetedAmount.setText(
                    cf.displayDollars(
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
                etTransDate.setText(
                    mainViewModel.getTransactionDetailed()!!
                        .transaction!!.transDate
                )
            }
            calculateRemainder()
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
        val mes = checkTransaction()
        if (mes == "Ok") {
            val mTransaction = getCurTransaction()
            transactionViewModel.insertTransaction(
                mTransaction
            )
            CoroutineScope(Dispatchers.IO).launch {
                val go = async {
                    updateAccounts(mTransaction)
                }
                if (go.await()) {
                    updateBudgetItem()
                }
            }
        }
        gotoCallingFragment()
    }

    private fun updateBudgetItem() {
        val remainder =
            cf.getDoubleFromDollars(
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

    private fun updateAccounts(mTransaction: Transactions): Boolean {
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
            mView.findNavController().navigate(
                TransactionPerformFragmentDirections
                    .actionTransactionPerformFragmentToBudgetViewFragment()
            )
        }
    }


    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                cf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule?.ruleId ?: 0L,
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

    private fun checkTransaction(): String {
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