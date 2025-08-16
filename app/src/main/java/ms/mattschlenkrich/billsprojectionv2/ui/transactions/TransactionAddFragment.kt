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
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionAddBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_TRANS_ADD

class TransactionAddFragment : Fragment(R.layout.fragment_transaction_add) {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountViewModel: AccountViewModel

    private var mBudgetRule: BudgetRule? = null
    private var mToAccount: Account? = null
    private var mFromAccount: Account? = null
    private var mToAccountWithType: AccountWithType? = null
    private var mFromAccountWithType: AccountWithType? = null

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        accountViewModel = mainActivity.accountViewModel
        mainActivity.title = getString(R.string.add_a_new_transaction)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed() != null) {
                if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                    populateValuesFromTransactionDetailed()
                }
                if (mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                    populateValuesFromBudgetRule()
                }
                if (mainViewModel.getTransactionDetailed()!!.toAccount != null) {
                    populateValuesFromToAccount()
                } else {
                    chkToAccPending.visibility = View.GONE
                }
                if (mainViewModel.getTransactionDetailed()!!.fromAccount != null) {
                    populateValuesFromAccount()
                } else {
                    chkFromAccPending.visibility = View.GONE
                }
                if (mainViewModel.getTransactionDetailed()!!.transaction != null) {
                    chkToAccPending.isChecked =
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transToAccountPending
                    chkFromAccPending.isChecked =
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transFromAccountPending
                }

            } else {
                etTransDate.text = df.getCurrentDateAsString()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                updateAmountDisplay()
            }
        }
    }

    private fun populateValuesFromAccount() {
        binding.apply {
            mFromAccount = mainViewModel.getTransactionDetailed()!!.fromAccount
            tvFromAccount.text = mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()?.fromAccount!!.accountName
                    )
                }
                mFromAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mFromAccountWithType!!.accountType!!.allowPending) {
                    chkFromAccPending.isChecked = true
                    chkFromAccPending.visibility = View.VISIBLE
                } else {
                    chkFromAccPending.visibility = View.GONE
                    chkFromAccPending.isChecked = false
                }
            }
        }
    }

    private fun populateValuesFromToAccount() {
        binding.apply {
            mToAccount = mainViewModel.getTransactionDetailed()!!.toAccount
            tvToAccount.text = mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()!!.toAccount!!.accountName
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                    chkToAccPending.isChecked = true
                } else {
                    chkToAccPending.visibility = View.GONE
                    chkToAccPending.isChecked = false
                }
            }
        }
    }

    private fun populateValuesFromBudgetRule() {
        binding.apply {
            mBudgetRule = mainViewModel.getTransactionDetailed()!!.budgetRule!!
            tvBudgetRule.text = mBudgetRule!!.budgetRuleName
            if (mainViewModel.getTransactionDetailed()!!.toAccount == null && mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId != 0L) {
                populateToAccountFromBudgetRule()
            }
            if (mainViewModel.getTransactionDetailed()!!.fromAccount == null && mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId != 0L) {
                populateFromAccountFromBudgetRule()
            }
            if (mainViewModel.getTransactionDetailed()!!.transaction == null) {
                binding.etAmount.setText(nf.displayDollars(mBudgetRule!!.budgetAmount))
            }
        }
    }

    private fun populateFromAccountFromBudgetRule() {
        binding.apply {
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccount(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budFromAccountId
                    )
                }
                mFromAccount = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                tvFromAccount.text = mFromAccount!!.accountName
                CoroutineScope(Dispatchers.IO).launch {
                    val acc = async {
                        accountViewModel.getAccountWithType(
                            mFromAccount!!.accountName
                        )
                    }
                    mFromAccountWithType = acc.await()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    if (mFromAccountWithType!!.accountType!!.allowPending) {
                        chkFromAccPending.visibility = View.VISIBLE
                        chkFromAccPending.isChecked =
                            mFromAccountWithType!!.accountType!!.tallyOwing
                    } else {
                        chkFromAccPending.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun populateToAccountFromBudgetRule() {
        binding.apply {
            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccount(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                    )
                }
                mToAccount = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                tvToAccount.text = mToAccount!!.accountName
            }

            CoroutineScope(Dispatchers.IO).launch {
                val acc = async {
                    accountViewModel.getAccountWithType(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budToAccountId
                    )
                }
                mToAccountWithType = acc.await()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mToAccountWithType!!.accountType!!.allowPending) {
                    chkToAccPending.visibility = View.VISIBLE
                    if (mToAccountWithType!!.accountType!!.tallyOwing) {
                        chkToAccPending.isChecked = true
                    }
                } else {
                    chkToAccPending.visibility = View.GONE
                }
            }
        }
    }

    private fun populateValuesFromTransactionDetailed() {
        binding.apply {
            if (mainViewModel.getTransactionDetailed()!!.budgetRule != null && mainViewModel.getTransactionDetailed()!!.transaction!!.transName.isBlank()) {
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
            etTransDate.text = mainViewModel.getTransactionDetailed()!!.transaction!!.transDate
            etAmount.hint =
                getString(R.string.budgeted) + if (mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount == 0.0 && mainViewModel.getTransactionDetailed()!!.budgetRule != null) {
                    nf.displayDollars(
                        mainViewModel.getTransactionDetailed()!!.budgetRule!!.budgetAmount
                    )
                } else {
                    nf.displayDollars(
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                    )
                }
            etAmount.setText(
                nf.displayDollars(
                    if (mainViewModel.getTransferNum()!! != 0.0) {
                        mainViewModel.getTransferNum()!!
                    } else {
                        mainViewModel.getTransactionDetailed()!!.transaction!!.transAmount
                    }
                )
            )
            mainViewModel.setTransferNum(0.0)
        }
    }

    private fun updateAmountDisplay() {
        binding.apply {
            btnSplit.isEnabled = etAmount.text.toString().isNotEmpty() && nf.getDoubleFromDollars(
                etAmount.text.toString()
            ) > 0.0 && mFromAccount != null
            etAmount.setText(
                nf.displayDollars(
                    nf.getDoubleFromDollars(
                        etAmount.text.toString()
                    )
                )
            )
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvBudgetRule.setOnClickListener { chooseBudgetRule() }
            tvToAccount.setOnClickListener { chooseToAccount() }
            tvFromAccount.setOnClickListener { chooseFromAccount() }
            etTransDate.setOnClickListener { chooseDate() }
            etAmount.setOnLongClickListener {
                gotoCalculator()
                false
            }
            etAmount.setOnFocusChangeListener { _, b ->
                if (!b) updateAmountDisplay()
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
                splitTransactions()
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
                        saveTransactionIfValid()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun chooseBudgetRule() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(
            getTransactionDetailed()
        )
        gotoBudgetRuleChooseFragment()
    }

    private fun gotoBudgetRuleChooseFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToBudgetRuleChooseFragment()
        )
    }

    private fun chooseFromAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun chooseToAccount() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoAccountChooseFragment()
    }

    private fun splitTransactions() {
        mainViewModel.setSplitTransactionDetailed(null)
        if (mFromAccount != null && nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0) {
            mainViewModel.addCallingFragment(TAG)
            mainViewModel.setTransactionDetailed(getTransactionDetailed())
            gotoTransactionSplitFragment()
        }
    }

    private fun chooseDate() {
        binding.apply {
            val curDateAll = etTransDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    etTransDate.text = display
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_transaction_date))
            datePickerDialog.show()
        }
    }

    private fun getCurrentTransactionForSave(): Transactions {
        binding.apply {
            return Transactions(
                nf.generateId(),
                etTransDate.text.toString(),
                etDescription.text.toString(),
                etNote.text.toString(),
                mainViewModel.getTransactionDetailed()?.budgetRule?.ruleId ?: 0L,
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

    private fun getTransactionDetailed(): TransactionDetailed {
        return TransactionDetailed(
            getCurrentTransactionForSave(), mBudgetRule, mToAccount, mFromAccount
        )
    }

    private fun saveTransactionIfValid() {
        val message = validateTransaction()
        if (message == ANSWER_OK) {
            confirmSaveTransaction()
        } else {
            showMessage(getString(R.string.error) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            mView.context, message, Toast.LENGTH_LONG
        ).show()
    }

    private fun validateTransaction(): String {
        binding.apply {
            val amount = if (etAmount.text.isNotEmpty()) {
                nf.getDoubleFromDollars(etAmount.text.toString())
            } else {
                0.0
            }
            if (etDescription.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_name_or_description)
            }
            if (mToAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_go_to)
            }
            if (mFromAccount == null) {
                return getString(R.string.there_needs_to_be_an_account_money_will_come_from)
            }
            if (etAmount.text.isNullOrEmpty() || amount == 0.0) {
                return getString(R.string.please_enter_an_amount_for_this_transaction)
            }
            return ANSWER_OK
        }
    }

    private fun confirmSaveTransaction() {
        binding.apply {
            var display =
                getString(R.string.this_will_perform) + etDescription.text + getString(R.string._for_) + nf.getDollarsFromDouble(
                    nf.getDoubleFromDollars(etAmount.text.toString())
                ) + getString(R.string.__from) + "${mFromAccount!!.accountName} "
            display += if (chkFromAccPending.isChecked) getString(R.string._pending) else ""
            display += getString(R.string._to) + " ${mToAccount!!.accountName}"
            display += if (chkToAccPending.isChecked) getString(R.string._pending) else ""
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.confirm_performing_transaction)).setMessage(
                    display
                ).setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    saveTransaction()
                }.setNegativeButton(getString(R.string.go_back), null).show()
        }
    }

    private fun saveTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        mainActivity.accountUpdateViewModel.performTransaction(mTransaction)
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        updateAmountDisplay()
        mainViewModel.removeCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(null)
        mainViewModel.setBudgetRuleDetailed(null)
        val mCallingFragment = mainViewModel.getCallingFragments()!!
        if (mCallingFragment.contains(FRAG_TRANSACTION_VIEW)) {
            gotoTransactionViewFragment()
        } else if (mCallingFragment.contains(FRAG_BUDGET_VIEW)) {
            gotoBudgetViewFragment()
        }
    }

    private fun gotoCalculator() {
        mainViewModel.setTransferNum(
            nf.getDoubleFromDollars(
                binding.etAmount.text.toString().ifBlank {
                    getString(R.string.zero_double)
                })
        )
        mainViewModel.setReturnTo(TAG)
        mainViewModel.setTransactionDetailed(getTransactionDetailed())
        gotoCalculatorFragment()
    }

    private fun gotoCalculatorFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToCalcFragment()
        )
    }

    private fun gotoAccountChooseFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToAccountChooseFragment()
        )
    }

    private fun gotoBudgetViewFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToBudgetViewFragment()
        )
    }

    private fun gotoTransactionViewFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToTransactionViewFragment()
        )
    }

    private fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            TransactionAddFragmentDirections.actionTransactionAddFragmentToTransactionSplitFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}