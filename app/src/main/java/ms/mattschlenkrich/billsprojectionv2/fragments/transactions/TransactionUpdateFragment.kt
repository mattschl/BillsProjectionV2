package ms.mattschlenkrich.billsprojectionv2.fragments.transactions

import android.app.AlertDialog
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
import androidx.navigation.fragment.navArgs
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
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_UPDATE

class TransactionUpdateFragment :
    Fragment(R.layout.fragment_transaction_update) {

    private var _binding: FragmentTransactionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var mView: View
    private var success = false
    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private val args: TransactionUpdateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
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
        fillValues()
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
        }
    }

    private fun updateTransaction() {
        val mes = checkTransaction()
        binding.apply {
            if (mes == "Ok") {
//                val fragmentChain = "${args.callingFragments}, $TAG"
                transactionViewModel.updateTransaction(
                    getCurTransaction()
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val go = async {
                        updateAccounts(getCurTransaction())
                    }
                    if (go.await()) {
                        success = true
                    }
                }
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment(
                            getCurTransDetailed(),
                            TAG
                        )
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
                } else if (args.transaction!!.toAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will go to."
                } else if (args.transaction?.fromAccount == null
                ) {
                    "     Error!!\n" +
                            "There needs to be an account money will come from."
                } else if (etAmount.text.isNullOrEmpty() ||
                    amount == 0.0
                ) {
                    "     Error!!\n" +
                            "Please enter an amount fr this transaction"
                } else if (args.transaction!!.budgetRule == null) {
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
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment(
                    null,
                    getCurTransDetailed(),
                    null,
                    REQUEST_FROM_ACCOUNT,
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment(
                    null,
                    getCurTransDetailed(),
                    null,
                    REQUEST_TO_ACCOUNT,
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun chooseBudgetRule() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetRuleFragment(
                    null,
                    getCurTransDetailed(),
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun getCurTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurTransaction(),
            args.transaction!!.budgetRule,
            args.transaction!!.toAccount,
            args.transaction?.fromAccount
        )

    }

    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                args.transaction!!.transaction!!.transId,
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                args.transaction!!.budgetRule!!.ruleId,
                args.transaction!!.toAccount!!.accountId,
                chkToAccountPending.isChecked,
                args.transaction?.fromAccount!!.accountId,
                chkFromAccountPending.isChecked,
                cf.getDoubleFromDollars(etAmount.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun fillValues() {
        binding.apply {
            if (args.transaction != null) {
                if (args.transaction!!.transaction != null) {
                    etTransDate.setText(
                        args.transaction!!.transaction!!.transDate
                    )
                    etAmount.setText(
                        cf.displayDollars(
                            args.transaction!!.transaction!!.transAmount
                        )
                    )
                    etDescription.setText(
                        args.transaction!!.transaction!!.transName
                    )
                    etNote.setText(
                        args.transaction!!.transaction!!.transNote
                    )
                }
                if (args.transaction!!.budgetRule != null) {
                    args.transaction!!.budgetRule =
                        args.transaction!!.budgetRule
                    tvBudgetRule.text =
                        args.transaction!!.budgetRule!!.budgetRuleName
                }
                if (args.transaction!!.toAccount != null) {
                    args.transaction!!.toAccount =
                        args.transaction!!.toAccount
                    tvToAccount.text =
                        args.transaction!!.toAccount!!.accountName
                }
                chkToAccountPending.isChecked =
                    args.transaction!!.transaction!!.transToAccountPending
                if (args.transaction!!.fromAccount != null) {
                    args.transaction?.fromAccount =
                        args.transaction!!.fromAccount
                    tvFromAccount.text =
                        args.transaction?.fromAccount!!.accountName
                }
                chkFromAccountPending.isChecked =
                    args.transaction!!.transaction!!.transFromAccountPending
            }
        }
    }

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete this Transaction")
            setMessage("Are you sure you want to delete this Transaction?")
            setPositiveButton("Delete") { _, _ ->
                transactionViewModel.deleteTransaction(
                    args.transaction!!.transaction!!.transId,
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
                val fragmentChain = args.callingFragments!!.replace(
                    FRAG_TRANS_UPDATE, ""
                )
                val direction =
                    TransactionUpdateFragmentDirections
                        .actionTransactionUpdateFragmentToTransactionViewFragment(
                            null, fragmentChain
                        )
                mView.findNavController().navigate(direction)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun updateAccounts(
        mTransaction: Transactions,
    ): Boolean {
        val oldTransaction = args.transaction!!.transaction!!
        val oldToAccountWithType =
            accountViewModel.getAccountWithType(
                oldTransaction.transToAccountId
            )
        if (!oldTransaction.transToAccountPending) {
            if (oldToAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldToAccountWithType.account.accountBalance -
                            oldTransaction.transAmount,
                    oldTransaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldToAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldToAccountWithType.account.accountBalance +
                            oldTransaction.transAmount,
                    oldTransaction.transToAccountId,
                    df.getCurrentTimeAsString()

                )
            }
        }
        val oldFromAccountWithType =
            accountViewModel.getAccountWithType(
                oldTransaction.transFromAccountId
            )
        if (!oldTransaction.transFromAccountPending) {
            if (oldFromAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldFromAccountWithType.account.accountBalance +
                            oldTransaction.transAmount,
                    oldTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldFromAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldFromAccountWithType.account.accountOwing -
                            oldTransaction.transAmount,
                    oldTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        val toAccountWithType =
            accountViewModel.getAccountWithType(
                mTransaction.transToAccountId
            )
        if (!mTransaction.transToAccountPending) {
            if (toAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    toAccountWithType.account.accountBalance +
                            mTransaction.transAmount,
                    mTransaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (toAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    toAccountWithType.account.accountOwing -
                            mTransaction.transAmount,
                    mTransaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        val fromAccountWithType =
            accountViewModel.getAccountWithType(
                mTransaction.transFromAccountId
            )
        if (!mTransaction.transFromAccountPending) {
            if (fromAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    fromAccountWithType.account.accountBalance -
                            mTransaction.transAmount,
                    mTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
                Log.d(TAG, "updating fromAccountBalance")
            }
            if (fromAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    fromAccountWithType.account.accountOwing +
                            mTransaction.transAmount,
                    mTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        return true
    }

    private fun updateAccounts(): Boolean {
        val oldTransaction = args.transaction!!.transaction!!
        val oldToAccountWithType =
            accountViewModel.getAccountWithType(
                oldTransaction.transToAccountId
            )
        if (!oldTransaction.transToAccountPending) {
            if (oldToAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldToAccountWithType.account.accountBalance -
                            oldTransaction.transAmount,
                    oldTransaction.transToAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldToAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldToAccountWithType.account.accountBalance +
                            oldTransaction.transAmount,
                    oldTransaction.transToAccountId,
                    df.getCurrentTimeAsString()

                )
            }
        }
        val oldFromAccountWithType =
            accountViewModel.getAccountWithType(
                oldTransaction.transFromAccountId
            )
        if (!oldTransaction.transFromAccountPending) {
            if (oldFromAccountWithType.accountType.keepTotals) {
                transactionViewModel.updateAccountBalance(
                    oldFromAccountWithType.account.accountBalance +
                            oldTransaction.transAmount,
                    oldTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
            if (oldFromAccountWithType.accountType.tallyOwing) {
                transactionViewModel.updateAccountOwing(
                    oldFromAccountWithType.account.accountOwing -
                            oldTransaction.transAmount,
                    oldTransaction.transFromAccountId,
                    df.getCurrentTimeAsString()
                )
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}