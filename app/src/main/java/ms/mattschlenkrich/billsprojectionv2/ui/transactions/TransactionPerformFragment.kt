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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentTransactionPerformBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAG_TRANS_PERFORM

class TransactionPerformFragment : Fragment(
    R.layout.fragment_transaction_perform
) {

    private var _binding: FragmentTransactionPerformBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private var mTransactionDetailed: TransactionDetailed? = null
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
        mainActivity.title = "Perform a Transaction"
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun setClickActions() {
        setMenuActions()
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
                    mainActivity.mainViewModel.getBudgetItem()!!
                        .budgetItem!!.biProjectedAmount
                ) {
                    val mBudgetItem =
                        mainActivity.mainViewModel.getBudgetItem()
                    mBudgetItem!!.budgetItem!!.biProjectedAmount =
                        nf.getDoubleFromDollars(
                            etBudgetedAmount.text.toString()
                        )
                    mainActivity.mainViewModel.setBudgetItem(mBudgetItem)
                }
            }
            btnSplit.setOnClickListener {
                splitTransaction()
            }
        }
    }

    private fun splitTransaction() {
        mainActivity.mainViewModel.setSplitTransactionDetailed(null)
        mainActivity.mainViewModel.setTransferNum(0.0)
        if (mFromAccount != null &&
            nf.getDoubleFromDollars(binding.etAmount.text.toString()) > 2.0
        ) {
            mainActivity.mainViewModel.setCallingFragments(
                mainActivity.mainViewModel.getCallingFragments() + ", " + TAG
            )
            mainActivity.mainViewModel
                .setTransactionDetailed(getTransactionDetailed())
            mView.findNavController().navigate(
                TransactionPerformFragmentDirections
                    .actionTransactionPerformFragmentToTransactionSplitFragment()
            )
        }
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
        mainActivity.mainViewModel
            .setTransactionDetailed(getTransactionDetailed())
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
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_FROM_ACCOUNT)
        mainActivity.mainViewModel
            .setTransactionDetailed(getTransactionDetailed())
        mView.findNavController().navigate(
            TransactionPerformFragmentDirections
                .actionTransactionPerformFragmentToAccountsFragment()
        )
    }

    private fun chooseToAccount() {
        mainActivity.mainViewModel.setCallingFragments(
            "${mainActivity.mainViewModel.getCallingFragments()}, $TAG"
        )
        mainActivity.mainViewModel.setRequestedAccount(REQUEST_TO_ACCOUNT)
        mainActivity.mainViewModel
            .setTransactionDetailed(getTransactionDetailed())
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
        if (mainActivity.mainViewModel.getTransactionDetailed() != null
        ) {
            populateValuesFromTransaction()
        } else if (mainActivity.mainViewModel.getBudgetItem() != null) {
            populateValuesFromBudgetItem()
        }

    }

    private fun populateValuesFromBudgetItem() {
        mToAccount =
            mainActivity.mainViewModel.getBudgetItem()!!.toAccount
        mFromAccount =
            mainActivity.mainViewModel.getBudgetItem()!!.fromAccount
        mBudgetRule =
            mainActivity.mainViewModel.getBudgetItem()!!.budgetRule
        binding.apply {
            val mBudgetItem =
                mainActivity.mainViewModel.getBudgetItem()!!.budgetItem!!
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
            mainActivity.accountViewModel.getAccountDetailed(
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
            mainActivity.accountViewModel.getAccountDetailed(
                mFromAccount!!.accountId
            ).observe(
                viewLifecycleOwner
            ) { accWType ->
                if (accWType.accountType!!.allowPending) {
                    chkFromAccPending.visibility = View.VISIBLE
                    chkFromAccPending.isChecked =
                        accWType.accountType.tallyOwing
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
        if (mainActivity.mainViewModel.getTransactionDetailed()!!.transaction != null) {
            mTransactionDetailed =
                mainActivity.mainViewModel.getTransactionDetailed()!!
            val mTransaction =
                mainActivity.mainViewModel.getTransactionDetailed()!!
                    .transaction!!
            mBudgetRule =
                mainActivity.mainViewModel.getTransactionDetailed()!!
                    .budgetRule
            mToAccount =
                mainActivity.mainViewModel.getTransactionDetailed()!!
                    .toAccount
            mFromAccount =
                mainActivity.mainViewModel.getTransactionDetailed()!!
                    .fromAccount
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
                        if (mainActivity.mainViewModel.getTransferNum()!!
                            != 0.0
                        ) {
                            mainActivity.mainViewModel.getTransferNum()!!
                        } else {
                            mTransaction.transAmount
                        }
                    )
                )
                mainActivity.mainViewModel.setTransferNum(0.0)
                etBudgetedAmount.setText(
                    nf.displayDollars(
                        mainActivity.mainViewModel.getBudgetItem()!!
                            .budgetItem!!.biProjectedAmount
                    )
                )
                mBudgetRule =
                    mTransactionDetailed!!.budgetRule
                tvBudgetRule.text =
                    mBudgetRule!!.budgetRuleName
                mToAccount =
                    mTransactionDetailed!!.toAccount!!
                tvToAccount.text =
                    mToAccount!!.accountName
                mainActivity.accountViewModel.getAccountDetailed(
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
                    mTransactionDetailed!!.transaction!!
                        .transToAccountPending
                mFromAccount =
                    mTransactionDetailed!!.fromAccount!!
                tvFromAccount.text =
                    mFromAccount!!.accountName
                mainActivity.accountViewModel.getAccountDetailed(
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
                    mTransactionDetailed!!.transaction!!
                        .transFromAccountPending
                etTransDate.text =
                    mTransactionDetailed!!.transaction!!.transDate
            }
            calculateRemainder()
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
                        performTransactionIfValid()
                        menuItem.isEnabled = true
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun performTransactionIfValid() {
        calculateRemainder()
        val mes = validateTransaction()
        if (mes == "Ok") {
            confirmPerformTransaction()
        } else {
            Toast.makeText(
                mView.context,
                mes, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun confirmPerformTransaction() {
        binding.apply {
            AlertDialog.Builder(mView.context)
                .setTitle("Confirm performing transaction")
                .setMessage(
                    "This will perform transaction ${etDescription.text} " +
                            "for ${nf.getDollarsFromDouble(nf.getDoubleFromDollars(etAmount.text.toString()))} " +
                            "\n\nFROM:   ${mFromAccount!!.accountName} " +
                            "\nTO:   ${mToAccount!!.accountName}."
                )
                .setPositiveButton("Confirm") { _, _ ->
                    performTransaction()
                }
                .setNegativeButton("Go back", null)
                .show()
        }
    }

    private fun performTransaction() {
        val mTransaction = getCurrentTransactionForSave()
        mainActivity.accountUpdateViewModel.performTransaction(
            mTransaction
        )
        updateBudgetItem()
        gotoCallingFragment()
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
        val mBudget =
            mainActivity.mainViewModel.getBudgetItem()!!.budgetItem!!
        mainActivity.budgetItemViewModel.updateBudgetItem(
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

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments()!!
                .replace(", $TAG", "")
        )
        mainActivity.mainViewModel.setTransactionDetailed(null)
        mainActivity.mainViewModel.setBudgetRuleDetailed(null)
        if (mainActivity.mainViewModel.getCallingFragments()!!
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}