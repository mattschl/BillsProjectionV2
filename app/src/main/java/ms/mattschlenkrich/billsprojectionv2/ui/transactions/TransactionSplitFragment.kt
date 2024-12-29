package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
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
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.splitting_transaction)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
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

    private fun populateValuesFromOriginalTransaction() {
        val transaction = mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!
        binding.apply {
            tvOriginalAmount.text = nf.displayDollars(
                transaction.transAmount
            )
            etTransDate.setText(
                transaction.transDate
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
                transaction.transFromAccountPending
        }
    }

    private fun populateValuesFromBudgetRule() {
        val transactionDetailed = mainActivity.mainViewModel.getSplitTransactionDetailed()!!
        binding.apply {
            mBudgetRule =
                transactionDetailed.budgetRule!!
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            if (transactionDetailed.transaction!!
                    .transName.isBlank()
            ) {
                etDescription.setText(mBudgetRule!!.budgetRuleName)
            }
            if (transactionDetailed.toAccount == null) {
                populateToAccountFromBudgetRule()
            }
        }
    }

    private fun populateValuesFromSplitTransaction() {
        val transaction = mainActivity.mainViewModel.getSplitTransactionDetailed()!!.transaction!!
        binding.apply {
            etDescription.setText(
                transaction.transName
            )
            etNote.setText(
                transaction.transNote
            )
            chkFromAccPending.isChecked =
                transaction.transFromAccountPending
            if (mainActivity.mainViewModel.getTransferNum() != null &&
                mainActivity.mainViewModel.getTransferNum() != 0.0
            ) {
                etAmount.setText(
                    nf.displayDollars(
                        mainActivity.mainViewModel.getTransferNum()!!
                    )
                )
            } else if (transaction.transAmount != 0.0) {
                etAmount.setText(
                    nf.displayDollars(
                        transaction.transAmount
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

    private fun chooseBudgetRule() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + "', " + TAG
        )
        mainActivity.mainViewModel.setSplitTransactionDetailed(
            getSplitTransDetailed()
        )
        gotoBudgetRuleFragment()
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
                        saveTransactionIfValid()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseToAccount() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainActivity.mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        gotoAccountsFragment()
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
                    getString(R.string.error) +
                            getString(R.string.new_amount_cannot_be_more_than_the_original_amount),
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

    private fun saveTransactionIfValid() {
        updateAmountsDisplay()
        val mes = validateTransaction()
        if (mes == ANSWER_OK) {
            confirmPerformTransaction()
        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun confirmPerformTransaction() {
        binding.apply {
            var display =
                getString(R.string.this_will_perform) +
                        etDescription.text +
                        getString(R.string._for_) +
                        nf.getDollarsFromDouble(
                            nf.getDoubleFromDollars(etAmount.text.toString())
                        ) +
                        getString(R.string.__from) +
                        mFromAccount.accountName
            display += if (chkFromAccPending.isChecked) getString(R.string._pending) else ""
            display += getString(R.string._to) +
                    mToAccount!!.accountName
            display += if (chkToAccPending.isChecked) getString(R.string._pending) else ""
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.confirm_performing_transaction))
                .setMessage(
                    display
                )
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    saveTransaction()
                }
                .setNegativeButton(getString(R.string.go_back), null)
                .show()
        }
    }

    private fun saveTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        mainActivity.accountUpdateViewModel.performTransaction(
            mTransaction
        )
        gotoCallingFragment()
    }

    private fun validateTransaction(): String {
        binding.apply {
            if (etAmount.text.isNullOrBlank()) {
                return getString(R.string.error) +
                        getString(R.string.please_enter_an_amount_for_this_transaction)
            }
            if (nf.getDoubleFromDollars(etAmount.text.toString()) >=
                nf.getDoubleFromDollars(tvOriginalAmount.text.toString())
            ) {
                return getString(R.string.error) +
                        getString(R.string.the_amount_of_a_split_transaction_must_be_less_than_the_original)
            }
            if (etDescription.text.isNullOrBlank()) {
                return getString(R.string.error) +
                        getString(R.string.please_enter_a_name_or_description)
            }
            if (mToAccount == null) {
                return getString(R.string.error) +
                        getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (mainActivity.mainViewModel.getSplitTransactionDetailed()!!.budgetRule == null) {
                return if (!saveWithoutBudget()) {
                    getString(R.string.choose_a_budget_rule)
                } else {
                    getString(R.string.choose_a_budget_rule)
                }
            }
            return ANSWER_OK
        }
    }

    private fun saveWithoutBudget(): Boolean {
        AlertDialog.Builder(activity).apply {
            setMessage(
                getString(R.string.there_is_no_budget_rule) +
                        getString(R.string.budget_rules_are_used_to_update_the_budget)
            )
            setNegativeButton(getString(R.string.retry), null)
        }.create().show()
        return false
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

    private fun gotoCallingFragment() {
        val transactionDetailed = mainActivity.mainViewModel.getTransactionDetailed()!!
        val oldTransaction =
            transactionDetailed.transaction!!
        oldTransaction.transAmount =
            nf.getDoubleFromDollars(binding.tvRemainder.text.toString())
        if (mainActivity.mainViewModel.getUpdatingTransaction()) {
            mainActivity.transactionViewModel.updateTransaction(oldTransaction)
        }
        mainActivity.mainViewModel.setUpdatingTransaction(false)
        mainActivity.mainViewModel.setTransactionDetailed(
            TransactionDetailed(
                oldTransaction,
                transactionDetailed.budgetRule,
                transactionDetailed.toAccount,
                transactionDetailed.fromAccount
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

    private fun gotoCalculator() {
        mainActivity.mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    getString(R.string.zero_double)
                }
            )
        )
        mainActivity.mainViewModel.setReturnTo(TAG)
        mainActivity.mainViewModel.setSplitTransactionDetailed(getSplitTransDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToAccountsFragment()
        )
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToCalcFragment()
        )
    }

    private fun gotoBudgetRuleFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToBudgetRuleFragment()
        )
    }

    private fun gotoTransactionViewFragment() {
        mView.findNavController().navigate(
            TransactionSplitFragmentDirections
                .actionTransactionSplitFragmentToTransactionViewFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}