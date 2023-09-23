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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTIONS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_ADD

class TransactionAddFragment :
    Fragment(R.layout.fragment_transaction_add) {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private var success = false

    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    //    private var mBudgetRule: BudgetRule? = null
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(
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
        mainActivity.title = "Add a new Transaction"
        fillValues()
        createMenu()
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

    private fun chooseBudgetRule() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + "', " + TAG
        )
        mainViewModel.setTransactionDetailed(
            getTransactionDetailed()
        )
        mView.findNavController().navigate(
            TransactionAddFragmentDirections
                .actionTransactionAddFragmentToBudgetRuleFragment()
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
                mainViewModel.getTransactionDetailed()?.toAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                mainViewModel.getTransactionDetailed()?.fromAccount?.accountId ?: 0L,
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

    private fun getTransactionDetailed(): TransactionDetailed {
            return TransactionDetailed(
                getCurTransaction(),
                mainViewModel.getTransactionDetailed()?.budgetRule,
                mainViewModel.getTransactionDetailed()?.toAccount,
                mainViewModel.getTransactionDetailed()?.fromAccount
            )
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
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        mainViewModel.setCallingFragments(
            "${mainViewModel.getCallingFragments()}, $TAG"
        )
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment()
        mView.findNavController().navigate(direction)
    }


    private fun fillValues() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed() != null) {
                if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                    if (mainViewModel.getTransactionDetailed()!!.budgetRule != null &&
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transName.isBlank()
                    ) {
                        etDescription.setText(
                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetRuleName
                        )
                    } else {
                        etDescription.setText(
                            mainViewModel.getTransactionDetailed()!!.transaction?.transName ?: ""
                        )
                    }
                    etNote.setText(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transNote
                    )
                    etTransDate.setText(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
                    )
                    etAmount.hint = "Budgeted " +
                            if (mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount == 0.0 &&
                                mainViewModel.getTransactionDetailed()!!.budgetRule != null
                            ) {
                                cf.displayDollars(
                                    mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetAmount
                                )
                            } else {
                                cf.displayDollars(
                                    mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                                )
                            }
                    if (mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount != 0.0) {
                        etAmount.setText(
                            cf.displayDollars(
                                mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                            )
                        )
                    }

                }
                if (mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                    tvBudgetRule.text =
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetRuleName
                    if (mainViewModel.getTransactionDetailed()!!.toAccount == null) {
                        if (mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val toAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                                        )
                                    }
                                mToAccount = toAccount.await()
                                val toAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                                        )
                                    }
                                mToAccountWithType = toAccountWithType.await()
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000)
                                tvToAccount.text = mToAccount?.accountName
                                if (mToAccountWithType?.accountType?.allowPending == true) {
                                    chkToAccPending.visibility = View.VISIBLE
                                } else {
                                    chkToAccPending.visibility = View.GONE
                                }
                            }
                        }
                    }
                    if (mainViewModel.getTransactionDetailed()!!.fromAccount == null) {
                        if (mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val fromAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId
                                        )
                                    }
                                mFromAccount = fromAccount.await()
                                val fromAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId
                                        )
                                    }
                                mFromAccountWithType = fromAccountWithType.await()
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000)
                                tvFromAccount.text = mFromAccount?.accountName
                                if (mFromAccountWithType?.accountType?.allowPending == true) {
                                    chkFromAccPending.visibility = View.VISIBLE
                                } else {
                                    chkFromAccPending.visibility = View.GONE
                                }
                            }

                        }
                    }
                }
                if (mainViewModel.getTransactionDetailed()!!.toAccount != null) {
                    mToAccount =
                        mainViewModel.getTransactionDetailed()!!.toAccount
                    tvToAccount.text =
                        mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val toAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
                                )
                            }
                        mToAccountWithType = toAccountWithType.await()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(250)
                        if (mToAccountWithType?.accountType?.allowPending == true) {
                            chkToAccPending.visibility = View.VISIBLE
                        } else {
                            chkToAccPending.visibility = View.GONE
                        }
                    }
                }
                chkToAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
                if (mainViewModel.getTransactionDetailed()!!.fromAccount != null) {
                    mFromAccount =
                        mainViewModel.getTransactionDetailed()!!.fromAccount
                    tvFromAccount.text =
                        mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val fromAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
                                )
                            }
                        mFromAccountWithType = fromAccountWithType.await()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(250)
                        if (mFromAccountWithType?.accountType?.allowPending == true) {
                            chkFromAccPending.visibility = View.VISIBLE
                        } else {
                            chkFromAccPending.visibility = View.GONE
                        }
                    }
                } else {
                    chkFromAccPending.visibility = View.GONE
                }
                chkFromAccPending.isChecked =
                    mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending

            } else {
                etTransDate.setText(df.getCurrentDateAsString())
            }
        }
    }

    private fun saveTransaction() {
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
                    success = true
                }
            }
            gotoCallingFragment()

        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
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
            gotoCallingFragment()
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
                .contains(FRAG_TRANSACTIONS)
        ) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToTransactionViewFragment()
            mView.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_VIEW)) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToBudgetViewFragment()
            mView.findNavController().navigate(direction)
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
                } else if (mainViewModel.getTransactionDetailed()!!.budgetRule == null) {
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