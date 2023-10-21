package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_UPDATE

class TransactionUpdateFragment :
    Fragment(R.layout.fragment_transaction_update) {

    private var _binding: FragmentTransactionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var mView: View
    private var success = false
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionUpdateBinding.inflate(
            inflater, container, false
        )
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
        mainActivity.title = "Update this Transaction"
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
            etTransDate.setOnLongClickListener {
                chooseDate()
                false
            }
            fabUpdateDone.setOnClickListener {
                updateTransaction()
            }
            etAmount.setOnLongClickListener {
                gotoCalc()
                false
            }
        }
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
                        deleteTransaction()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        mainViewModel.setTransactionDetailed(getCurTransDetailed())
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToCalcFragment()
        )
    }

    private fun updateTransaction() {
        val mes = checkTransaction()
        binding.apply {
            if (mes == "Ok") {
                mainViewModel.setCallingFragments(
                    mainViewModel.getCallingFragments()!!
                        .replace(", $TAG", "")
                )
                val newTransaction = getCurTransaction()
                transactionViewModel.updateTransaction(
                    newTransaction
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val go = async {
                        updateAccounts(newTransaction)
                    }
                    if (go.await()) {
                        success = true
                    }
                }
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment()
                mView.findNavController().navigate(direction)
            } else {
                Toast.makeText(
                    mView.context,
                    mes,
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun checkTransaction(): String {
        binding.apply {
            val amount =
                cf.getDoubleFromDollars(etAmount.text.toString())
            val errorMes =
                if (etDescription.text.isNullOrBlank()) {
                    "     ERROR!!\n" +
                            "Please enter a description."
                } else if (mainViewModel.getTransactionDetailed()?.toAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (mainViewModel.getTransactionDetailed()?.fromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount fr this transaction"
                } else if (mainViewModel.getTransactionDetailed()?.budgetRule == null) {
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

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etTransDate.text.toString()
                .split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${month.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etTransDate.setText(display)
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle("Choose the final date")
            datePickerDialog.show()
        }
    }

    private fun chooseFromAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setTransactionDetailed(getCurTransDetailed())
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setTransactionDetailed(getCurTransDetailed())
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetRuleFragment()
        mView.findNavController().navigate(direction)
    }

    private fun getCurTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurTransaction(),
            mainViewModel.getTransactionDetailed()?.budgetRule,
            mainViewModel.getTransactionDetailed()?.toAccount,
            mainViewModel.getTransactionDetailed()?.fromAccount
        )

    }

    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transId,
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mainViewModel.getTransactionDetailed()!!.budgetRule!!.ruleId,
                mainViewModel.getTransactionDetailed()!!.toAccount!!.accountId,
                chkToAccountPending.isChecked,
                mainViewModel.getTransactionDetailed()!!.fromAccount!!.accountId,
                chkFromAccountPending.isChecked,
                cf.getDoubleFromDollars(etAmount.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun fillValues() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed() != null) {
                if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                    etTransDate.setText(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
                    )
                    etAmount.setText(
                        cf.displayDollars(
                            if (mainViewModel.getTransferNum()!! != 0.0) {
                                mainViewModel.getTransferNum()!!
                            } else {
                                mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                            }
                        )
                    )
                    mainViewModel.setTransferNum(0.0)
                    etDescription.setText(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transName
                    )
                    etNote.setText(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transNote
                    )
                }
                if (mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                    tvBudgetRule.text =
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetRuleName
                }
                if (mainViewModel.getTransactionDetailed()!!.toAccount != null) {
                    tvToAccount.text =
                        mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
                }
                chkToAccountPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
                if (mainViewModel.getTransactionDetailed()!!.fromAccount != null) {
                    tvFromAccount.text =
                        mainViewModel.getTransactionDetailed()!!.fromAccount!!.accountName
                }
                chkFromAccountPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending
            }
        }
    }

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete this Transaction")
            setMessage("Are you sure you want to delete this Transaction?")
            setPositiveButton("Delete") { _, _ ->
                transactionViewModel.deleteTransaction(
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transId,
                    df.getCurrentTimeAsString()
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val go = async {
                        updateAccounts()
                    }
                    if (go.await()) {
                        success = true
                    }
                }
                mainViewModel.setCallingFragments(
                    mainViewModel.getCallingFragments()!!.replace(
                        FRAG_TRANS_UPDATE, ""
                    )
                )
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment()
                mView.findNavController().navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun updateAccounts(
        newTransaction: Transactions
    ): Boolean {
        val oldTransaction =
            transactionViewModel.getTransactionFull(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transId,
                mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountId,
                mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountId
            )
        if (!oldTransaction.transaction.transToAccountPending) {
            if (oldTransaction.toAccountAndType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldTransaction.toAccountAndType.accountBalance -
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldTransaction.toAccountAndType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldTransaction.toAccountAndType.accountOwing +
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (!oldTransaction.transaction.transFromAccountPending) {
                if (oldTransaction.fromAccountAndType.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        oldTransaction.fromAccountAndType.accountBalance +
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (oldTransaction.fromAccountAndType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        oldTransaction.fromAccountAndType.accountOwing -
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )

                }
            }
        }
        if (!newTransaction.transToAccountPending) {
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        newTransaction.transToAccountId
                    )
                }
                if (acc.await().accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        acc.await().account.accountBalance +
                                newTransaction.transAmount,
                        newTransaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (acc.await().accountType!!.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        acc.await().account.accountOwing -
                                newTransaction.transAmount,
                        newTransaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }

        }
        if (!newTransaction.transFromAccountPending) {

            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        newTransaction.transFromAccountId
                    )
                }
                if (acc.await().accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        acc.await().account.accountBalance -
                                newTransaction.transAmount,
                        newTransaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (acc.await().accountType!!.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        acc.await().account.accountOwing +
                                newTransaction.transAmount,
                        newTransaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
            }
        }
        return true
    }

    private fun updateAccounts(): Boolean {
        val oldTransaction =
            transactionViewModel.getTransactionFull(
                mainViewModel.getTransactionDetailed()!!.transaction!!.transId,
                mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountId,
                mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountId
            )
        if (!oldTransaction.transaction.transToAccountPending) {
            if (oldTransaction.toAccountAndType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldTransaction.toAccountAndType.accountBalance -
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldTransaction.toAccountAndType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldTransaction.toAccountAndType.accountOwing +
                            oldTransaction.transaction.transAmount,
                    oldTransaction.transaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (!oldTransaction.transaction.transFromAccountPending) {
                if (oldTransaction.fromAccountAndType.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        oldTransaction.fromAccountAndType.accountBalance +
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (oldTransaction.fromAccountAndType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        oldTransaction.fromAccountAndType.accountOwing -
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transFromAccountId,
                        df.getCurrentTimeAsString()
                    )

                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}