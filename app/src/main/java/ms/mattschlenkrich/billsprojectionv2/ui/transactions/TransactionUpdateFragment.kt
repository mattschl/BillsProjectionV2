package ms.mattschlenkrich.billsprojectionv2.ui.transactions

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ANSWER_OK
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_TRANS_UPDATE

class TransactionUpdateFragment :
    Fragment(R.layout.fragment_transaction_update) {

    private var _binding: FragmentTransactionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mView: View
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    //    private var mOldTransactionFull: TransactionFull? = null
    private var mTransaction: Transactions? = null
    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionUpdateBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_this_transaction)
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
            if (mainActivity.mainViewModel.getOldTransaction() != null &&
                mainActivity.mainViewModel.getTransactionDetailed() == null
            ) {
                populateValuesFromOldTransaction()
            } else if (mainActivity.mainViewModel.getTransactionDetailed() != null) {
                populateValuesFromCache()
            }
        }
        updateAmountDisplay()
        if (mainActivity.mainViewModel.getUpdatingTransaction()) {
            updateTransactionIfValid()
        }
    }

    private fun populateValuesFromCache() {
        val transactionDetailed = mainActivity.mainViewModel.getTransactionDetailed()!!
        binding.apply {
            if (transactionDetailed.transaction != null) {
                populateValuesFromTransactionInCache()
            }
            if (transactionDetailed.budgetRule != null) {
                populateBudgetRuleFromCache()
            }
            if (transactionDetailed.toAccount != null) {
                populateToAccountFromCache()
            }
            chkToAccountPending.isChecked =
                transactionDetailed.transaction!!.transToAccountPending
            if (transactionDetailed.fromAccount != null) {
                populateFromAccountFromCache()

            }
            chkFromAccountPending.isChecked =
                transactionDetailed.transaction.transFromAccountPending
        }
    }

    private fun populateFromAccountFromCache() {
        binding.apply {
            mFromAccount =
                mainActivity.mainViewModel.getTransactionDetailed()!!.fromAccount!!
            tvFromAccount.text = mFromAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    mainActivity.accountViewModel.getAccountWithType(
                        mFromAccount!!.accountId
                    )
                }
                mFromAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mFromAccountWithType!!.accountType!!.allowPending) {
                    chkFromAccountPending.visibility = View.VISIBLE
                } else {
                    chkFromAccountPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateToAccountFromCache() {
        binding.apply {
            mToAccount =
                mainActivity.mainViewModel.getTransactionDetailed()!!.toAccount!!
            tvToAccount.text =
                mToAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    mainActivity.accountViewModel.getAccountWithType(
                        mToAccount!!.accountId
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccountPending.visibility = View.VISIBLE
                } else {
                    chkToAccountPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateBudgetRuleFromCache() {
        binding.apply {
            mBudgetRule =
                mainActivity.mainViewModel.getTransactionDetailed()!!.budgetRule!!
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
        }
    }

    private fun populateValuesFromTransactionInCache() {
        binding.apply {
            mTransaction =
                mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!
            etTransDate.text =
                mTransaction!!.transDate
            etAmount.setText(
                nf.displayDollars(
                    if (mainActivity.mainViewModel.getTransferNum()!! != 0.0) {
                        mainActivity.mainViewModel.getTransferNum()!!
                    } else {
                        mTransaction!!.transAmount
                    }
                )
            )
            mainActivity.mainViewModel.setTransferNum(0.0)
            etDescription.setText(
                mTransaction!!.transName
            )
            etNote.setText(
                mTransaction!!.transNote
            )
        }
    }

    private fun populateValuesFromOldTransaction() {
        binding.apply {
            val transFull =
                mainActivity.mainViewModel.getOldTransaction()!!
            mTransaction = transFull.transaction
            etTransDate.text = mTransaction!!.transDate
            etAmount.setText(nf.displayDollars(mTransaction!!.transAmount))
            etDescription.setText(mTransaction!!.transName)
            etNote.setText(mTransaction!!.transNote)
            mBudgetRule = transFull.budgetRule
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            mToAccount = transFull.toAccountAndType.account
            tvToAccount.text = mToAccount!!.accountName
            chkToAccountPending.isChecked = mTransaction!!.transToAccountPending
            mFromAccount = transFull.fromAccountAndType.account
            tvFromAccount.text = mFromAccount!!.accountName
            chkFromAccountPending.isChecked = mTransaction!!.transFromAccountPending
            updateAmountDisplay()
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
                if (!b)
                    updateAmountDisplay()
            }
            etDescription.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            etNote.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            etTransDate.setOnFocusChangeListener { _, _ ->
                updateAmountDisplay()
            }
            btnSplit.setOnClickListener {
                gotoSplitTransaction()
            }
            fabUpdateDone.setOnClickListener {
                updateTransactionIfValid()
            }
        }
    }

    private fun setMenuActions() {
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
                        confirmDeleteTransaction()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        gotoBudgetRuleFragment()
    }

    private fun chooseToAccount() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        gotoAccountsFragment()
    }

    private fun chooseFromAccount() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        gotoAccountsFragment()
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
                    etTransDate.text = display
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_transaction_date))
            datePickerDialog.show()
        }
    }

    private fun updateAmountDisplay() {
        binding.apply {
            btnSplit.isEnabled = etAmount.text.toString().isNotEmpty() &&
                    nf.getDoubleFromDollars(etAmount.text.toString()) > 0.0 &&
                    mFromAccount != null
        }
    }

    private fun updateTransactionIfValid() {
        val message = validateTransactionForUpdate()
        binding.apply {
            if (message == ANSWER_OK) {
                confirmPerformTransaction()
            } else {
                showMessage(getString(R.string.error) + message)
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            mView.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateTransactionForUpdate(): String {
        binding.apply {
            if (etDescription.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name_or_description)
            }
            if (mToAccount == null
            ) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (mFromAccount == null
            ) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etAmount.text.isNullOrEmpty()
            ) {
                return getString(R.string.please_enter_an_amount_for_this_transaction)
            }
            if (mBudgetRule == null) {
                return if (updateWithoutBudget()) {
                    ANSWER_OK
                } else {
                    getString(R.string.choose_a_budget_rule)
                }
            }
            return ANSWER_OK
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
                        mFromAccount!!.accountName
            display += if (chkFromAccountPending.isChecked) getString(R.string.pending) else ""
            display += getString(R.string._to) +
                    mToAccount!!.accountName
            display += if (chkToAccountPending.isChecked) getString(R.string.pending) else ""
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.confirm_performing_transaction))
                .setMessage(
                    display
                )
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    updateTransaction()
                }
                .setNegativeButton(getString(R.string.go_back), null)
                .show()
        }
    }

    private fun updateWithoutBudget(): Boolean {
        var bool = false
        AlertDialog.Builder(activity).apply {
            setMessage(
                getString(R.string.there_is_no_budget_rule) +
                        getString(R.string.budget_rules_are_used_to_update_the_budget)
            )
            setPositiveButton(getString(R.string.save_anyway)) { _, _ ->
                bool = true
            }
            setNegativeButton(getString(R.string.retry), null)
        }.create().show()
        return bool
    }

    private fun updateTransaction() {
        mainActivity.accountUpdateViewModel.updateTransaction(
            mainActivity.mainViewModel.getOldTransaction()!!.transaction,
            getCurrentTransactionForSave()
        )
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        gotoCallingFragment()
    }

    private fun getCurrentTransDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(),
            mBudgetRule,
            mToAccount,
            mFromAccount
        )

    }

    private fun getCurrentTransactionForSave(): Transactions {
        binding.apply {
            return Transactions(
                mTransaction!!.transId,
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mBudgetRule!!.ruleId,
                mToAccount!!.accountId,
                chkToAccountPending.isChecked,
                mFromAccount!!.accountId,
                chkFromAccountPending.isChecked,
                nf.getDoubleFromDollars(etAmount.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun confirmDeleteTransaction() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.delete_this_transaction))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_transaction))
            setPositiveButton(getString(R.string.delete_this_transaction)) { _, _ ->
                deleteTransaction()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create().show()
    }

    private fun deleteTransaction() {
        mainActivity.accountUpdateViewModel.deleteTransaction(
            mainActivity.mainViewModel.getTransactionDetailed()!!.transaction!!
        )
        mainActivity.mainViewModel.setTransactionDetailed(null)
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!.replace(
                FRAG_TRANS_UPDATE, ""
            )
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_BUDGET_VIEW
            )
        ) {
            gotoBudgetViewFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_TRANSACTION_VIEW
            )
        ) {
            gotoTransactionViewFragment()
        } else if (mainActivity.mainViewModel.getCallingFragments()!!.contains(
                FRAG_TRANSACTION_ANALYSIS
            )
        ) {
            gotoTransactionAnalysisFragment()
        }
    }

    private fun gotoSplitTransaction() {
        mainActivity.mainViewModel.setSplitTransactionDetailed(null)
        mainActivity.mainViewModel.setTransferNum(0.0)
        mainActivity.mainViewModel.setUpdatingTransaction(true)
        if (mFromAccount != null &&
            nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0
        ) {
            mainActivity.mainViewModel.setCallingFragments(
                mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
            )
            mainActivity.mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
            gotoTransactionSplitFragment()
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
        mainActivity.mainViewModel.setTransactionDetailed(getCurrentTransDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToCalcFragment()
        )
    }

    private fun gotoAccountsFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToAccountsFragment()
        )
    }

    private fun gotoBudgetRuleFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetRuleFragment()
        )
    }

    private fun gotoTransactionViewFragment() {
        val direction =
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToTransactionViewFragment()
        mView.findNavController().navigate(direction)
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToBudgetViewFragment()
        )
    }

    private fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToTransactionSplitFragment()
        )
    }

    private fun gotoTransactionAnalysisFragment() {
        mView.findNavController().navigate(
            TransactionUpdateFragmentDirections
                .actionTransactionUpdateFragmentToTransactionAnalysisFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}