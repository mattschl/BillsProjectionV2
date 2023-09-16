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
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_TRANS_ADD

class TransactionAddFragment :
    Fragment(R.layout.fragment_transaction_add) {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel
    private var success = false
    private val args: TransactionAddFragmentArgs by navArgs()

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

    private fun chooseBudgetRule() {
        val fragmentChain =
            args.callingFragments + "', " + TAG

        val direction =
            TransactionAddFragmentDirections
                .actionTransactionAddFragmentToBudgetRuleFragment(
                    args.asset,
                    args.payDay,
                    null,
                    getTransactionDetailed(),
                    fragmentChain
                )
        mView.findNavController().navigate(direction)
    }

    private fun getCurTransaction(): Transactions {
        binding.apply {
            return Transactions(
                cf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                args.transaction?.budgetRule?.ruleId ?: 0L,
                args.transaction?.toAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                args.transaction?.fromAccount?.accountId ?: 0L,
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
        binding.apply {
            return TransactionDetailed(
                getCurTransaction(),
                args.transaction?.budgetRule,
                args.transaction?.toAccount,
                args.transaction?.fromAccount
            )
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
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment(
                args.asset,
                args.payDay,
                null,
                getTransactionDetailed(),
                null,
                REQUEST_FROM_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }

    private fun chooseToAccount() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = TransactionAddFragmentDirections
            .actionTransactionAddFragmentToAccountsFragment(
                args.asset,
                args.payDay,
                null,
                getTransactionDetailed(),
                null,
                REQUEST_TO_ACCOUNT,
                fragmentChain
            )
        mView.findNavController().navigate(direction)
    }


    private fun fillValues() {
        binding.apply {
            if (args.transaction != null) {
                if (args.transaction!!.transaction != null) {
                    etDescription.setText(
                        args.transaction!!.transaction!!.transName.ifBlank {
                            if (args.transaction!!.budgetRule != null) {
                                args.transaction!!.budgetRule!!.budgetRuleName
                            } else {
                                ""
                            }
                        }
                    )
                    etNote.setText(
                        args.transaction!!.transaction!!.transNote
                    )
                    etTransDate.setText(
                        args.transaction!!.transaction!!.transDate
                    )
                    etAmount.hint = "Budgeted " +
                            if (args.transaction!!.transaction!!.transAmount == 0.0 &&
                                args.transaction!!.budgetRule != null
                            ) {
                                cf.displayDollars(
                                    args.transaction!!.budgetRule!!.budgetAmount
                                )
                            } else {
                                cf.displayDollars(
                                    args.transaction!!.transaction!!.transAmount
                                )
                            }
                    if (args.transaction!!.transaction!!.transAmount != 0.0) {
                        etAmount.setText(
                            cf.displayDollars(
                                args.transaction!!.transaction!!.transAmount
                            )
                        )
                    }

                }
                if (args.transaction!!.budgetRule != null) {
                    args.transaction!!.budgetRule = args.transaction!!.budgetRule!!
                    tvBudgetRule.text =
                        args.transaction!!.budgetRule!!.budgetRuleName
                    if (args.transaction!!.toAccount == null) {
                        if (args.transaction!!.budgetRule!!.budToAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val toAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            args.transaction!!.budgetRule!!.budToAccountId
                                        )
                                    }
                                mToAccount = toAccount.await()
                                val toAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            args.transaction!!.budgetRule!!.budToAccountId
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
                    if (args.transaction!!.fromAccount == null) {
                        if (args.transaction!!.budgetRule!!.budFromAccountId != 0L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val fromAccount =
                                    async {
                                        accountViewModel.getAccount(
                                            args.transaction!!.budgetRule!!.budFromAccountId
                                        )
                                    }
                                mFromAccount = fromAccount.await()
                                val fromAccountWithType =
                                    async {
                                        accountViewModel.getAccountWithType(
                                            args.transaction!!.budgetRule!!.budFromAccountId
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
                if (args.transaction!!.toAccount != null) {
                    mToAccount = args.transaction!!.toAccount
                    tvToAccount.text =
                        args.transaction!!.toAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val toAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    args.transaction!!.toAccount!!.accountName
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
                    args.transaction!!.transaction!!.transToAccountPending
                if (args.transaction!!.fromAccount != null) {
                    mFromAccount = args.transaction!!.fromAccount
                    tvFromAccount.text =
                        args.transaction?.fromAccount!!.accountName
                    CoroutineScope(Dispatchers.IO).launch {
                        val fromAccountWithType =
                            async {
                                accountViewModel.getAccountWithType(
                                    args.transaction?.fromAccount!!.accountName
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
                    args.transaction!!.transaction!!.transFromAccountPending

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
//            val direction =
//                TransactionAddFragmentDirections
//                    .actionTransactionAddFragmentToTransactionViewFragment(
//                        args.asset,
//                        args.payDay,
//                        null,
//                        null
//                    )
//            mView.findNavController().navigate(direction)

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
            if (toAccountWithType.accountType.keepTotals) {
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
            if (fromAccountWithType.accountType.keepTotals) {
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
        val fragmentChain = args.callingFragments!!
            .replace(", $TAG", "")
        if (args.callingFragments!!.contains(FRAG_TRANSACTIONS)) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToTransactionViewFragment(
                        args.asset,
                        args.payDay,
                        null,
                        fragmentChain
                    )
            mView.findNavController().navigate(direction)
        } else if (args.callingFragments!!.contains(FRAG_BUDGET_VIEW)) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToBudgetViewFragment(
                        asset = args.asset,
                        payDay = args.payDay,
                        callingFragments = fragmentChain
                    )
            mView.findNavController().navigate(direction)
        } /*else if (args.callingFragments!!.contains(FRAG_BUDGET_RULES)) {
            val direction =
                TransactionAddFragmentDirections
                    .actionTransactionAddFragmentToBudgetRuleFragment(
                        null,
                        null,
                        fragmentChain
                    )
            mView.findNavController().navigate(direction)
        }*/
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