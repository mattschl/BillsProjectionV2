package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionSplitBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_TRANSACTION_SPLIT

class TransactionSplitFragment : Fragment(R.layout.fragment_transaction_split) {

    private var _binding: FragmentTransactionSplitBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mView: View
    private val nf = NumberFunctions()
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
        mainActivity.title = "Splitting Transaction"
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvBudgetRule.setOnClickListener {
                chooseBudgetRule()
            }
            tvToAccount.setOnClickListener {
                chooseToAccount()
            }
            etAmount.setOnLongClickListener {
                gotoCalculator()
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
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainActivity.mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToAccountsFragment()
        )
    }

    private fun chooseBudgetRule() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + "', " + TAG
        )
        mainActivity.mainViewModel.setSplitTransactionDetailed(
            getSplitTransDetailed()
        )
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToBudgetRuleFragment()
        )
    }

    private fun gotoCalculator() {
        mainActivity.mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    "0.0"
                }
            )
        )
        mainActivity.mainViewModel.setReturnTo(TAG)
        mainActivity.mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToCalcFragment()
        )
    }

    private fun getCurrentTransactionForSave(): Transactions {
        binding.apply {
            return Transactions(
                nf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mainActivity.mainViewModel.getTransactionDetailed()?.budgetRule?.ruleId ?: 0L,
                mToAccount?.accountId ?: 0L,
                chkToAccPending.isChecked,
                mFromAccount.accountId,
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

    private fun getSplitTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            mBudgetRule,
            mToAccount,
            mFromAccount,
        )
    }

    private fun populateValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getTransactionDetailed() != null) {
                populateValuesFromOriginalTransaction()
            }
            if (mainActivity.mainViewModel.getSplitTransactionDetailed() != null) {
                populateValuesFromSplitTransaction()
            }
        }
    }

    private fun populateValuesFromSplitTransaction() {
        binding.apply {
            etDescription.setText(
                mainActivity.mainViewModel.getSplitTransactionDetailed()?.transaction?.transName
            )
            etNote.setText(
                mainActivity.mainViewModel.getSplitTransactionDetailed()?.transaction?.transNote
            )
            chkFromAccPending.isChecked =
                mainActivity.mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transFromAccountPending
            if (mainActivity.mainViewModel.getTransferNum() != null &&
                mainActivity.mainViewModel.getTransferNum() != 0.0
            ) {
                etAmount.setText(
                    nf.displayDollars(
                        mainActivity.mainViewModel.getTransferNum()!!
                    )
                )
            } else if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transAmount != 0.0) {
                etAmount.setText(
                    nf.displayDollars(
                        mainActivity.mainViewModel.getSplitTransactionDetailed()!!.transaction!!.transAmount
                    )
                )
            } else {
                etAmount.setText(
                    nf.displayDollars(0.0)
                )
            }
            updateAmountsDisplay()
            if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.toAccount != null) {
                populateToAccountFromTransaction()
            }
            if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.budgetRule != null) {
                populateValuesFromBudgetRule()
            }
        }
    }

    private fun populateValuesFromBudgetRule() {
        binding.apply {
            mBudgetRule =
                mainActivity.mainViewModel.getSplitTransactionDetailed()!!.budgetRule!!
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.transaction!!
                    .transName.isBlank()
            ) {
                etDescription.setText(mBudgetRule!!.budgetRuleName)
            }
            if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.toAccount == null) {
                populateToAccountFromBudgetRule()
            }
        }
    }

    private fun populateToAccountFromTransaction() {
        binding.apply {
            mToAccount =
                mainActivity.mainViewModel.getSplitTransactionDetailed()!!.toAccount
            tvToAccount.text = mToAccount!!.accountName
            mainActivity.accountViewModel.getAccountDetailed(mToAccount!!.accountId).observe(
                viewLifecycleOwner
            ) {
                mToAccountWithType = it
                if (it.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                    chkToAccPending.isChecked =
                        mainActivity.mainViewModel.getSplitTransactionDetailed()!!
                            .transaction!!.transToAccountPending
                } else {
                    chkToAccPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateToAccountFromBudgetRule() {
        binding.apply {
            mainActivity.budgetRuleViewModel.getBudgetRuleFullLive(mBudgetRule!!.ruleId).observe(
                viewLifecycleOwner
            ) { it ->
                mToAccount = it.toAccount
                tvToAccount.text = it.toAccount!!.accountName
                mainActivity.accountViewModel.getAccountDetailed(mToAccount!!.accountId).observe(
                    viewLifecycleOwner
                ) {
                    mToAccountWithType = it
                    if (mToAccountWithType!!.accountType!!.allowPending) {
                        chkToAccPending.visibility = View.VISIBLE
                        chkToAccPending.isChecked =
                            mainActivity.mainViewModel.getSplitTransactionDetailed()!!
                                .transaction!!.transToAccountPending
                    } else {
                        chkToAccPending.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun populateValuesFromOriginalTransaction() {
        binding.apply {
            tvOriginalAmount.text = nf.displayDollars(
                mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
            )
            etTransDate.setText(
                mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
            )
            etTransDate.isEnabled = false
            mFromAccount =
                mainActivity.mainViewModel.getTransactionDetailed()!!.fromAccount!!
            tvFromAccount.text = mFromAccount.accountName
            mainActivity.accountViewModel.getAccountDetailed(mFromAccount.accountId).observe(
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
                mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending
        }
    }

    private fun updateAmountsDisplay() {
        binding.apply {
            etAmount.setText(
                nf.displayDollars(
                    nf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                )
            )
            val amount = nf.getDoubleFromDollars(
                etAmount.text.toString()
            )
            val original = nf.getDoubleFromDollars(
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
                    nf.displayDollars(0.0)
                )
            } else {
                tvRemainder.text = nf.displayDollars(original - amount)
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
        val mes = validateTransaction()
        if (mes == "Ok") {
            val mTransaction = getCurrentTransactionForSave()
            mainActivity.accountUpdateViewModel.performTransaction(
                mTransaction
            )
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoCallingFragment() {
        val oldTransaction =
            mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!
        oldTransaction.transAmount =
            nf.getDoubleFromDollars(binding.tvRemainder.text.toString())
        if (mainActivity.mainViewModel.getUpdatingTransaction()) {
            mainActivity.transactionViewModel.updateTransaction(oldTransaction)
        }
        mainActivity.mainViewModel.setUpdatingTransaction(false)
        mainActivity.mainViewModel.setTransactionDetailed(
            TransactionDetailed(
                oldTransaction,
                mainActivity.mainViewModel.getTransactionDetailed()!!.budgetRule,
                mainActivity.mainViewModel.getTransactionDetailed()!!.toAccount,
                mainActivity.mainViewModel.getTransactionDetailed()!!.fromAccount
            )
        )
        mainActivity.mainViewModel.setSplitTransactionDetailed(null)
        if (mainActivity.mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_ADD)) {
            mainActivity.mainViewModel.setUpdatingTransaction(false)
            goBackToTransactionsAddFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANS_PERFORM)
        ) {
            mainActivity.mainViewModel.setUpdatingTransaction(false)
            goBackToTransactionPerformFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_UPDATE)) {
            mainActivity.mainViewModel.setUpdatingTransaction(true)
            gotoTransactionViewFragment()
        }
    }

    private fun gotoTransactionViewFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToTransactionViewFragment()
        )
    }

    private fun goBackToTransactionPerformFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToTransactionPerformFragment()
        )
    }

    private fun goBackToTransactionsAddFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToTransactionAddFragment()
        )
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
                if (nf.getDoubleFromDollars(etAmount.text.toString()) >=
                    nf.getDoubleFromDollars(tvOriginalAmount.text.toString())
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
                } else if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.budgetRule == null) {
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