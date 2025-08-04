package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter.BudgetViewAdapter
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter.TransactionPendingAdapter

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(
    R.layout.fragment_budget_view
) {

    private var _binding: FragmentBudgetViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val nf = NumberFunctions()
//    private val df = DateFunctions()

    private lateinit var curAsset: AccountWithType
    private val budgetList = ArrayList<BudgetItemDetailed>()
    private var pendingAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_the_budget)
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        transactionViewModel = mainActivity.transactionViewModel
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateAssets()
        setOnClickActions()
        resumeHistory()
    }

    private fun populateAssets() {
        val assetAdapter = ArrayAdapter<Any>(
            requireContext(), R.layout.spinner_item_bold
        )
        budgetItemViewModel.getAssetsForBudget().observe(
            viewLifecycleOwner
        ) { assetList ->
            assetAdapter.clear()
            assetList?.forEach {
                assetAdapter.add(it)
            }
        }
        binding.spAssetNames.adapter = assetAdapter
    }

    private fun populatePayDays(asset: String) {
        val payDayAdapter = ArrayAdapter<Any>(
            requireContext(), R.layout.spinner_item_bold
        )
        budgetItemViewModel.getPayDays(asset).observe(
            viewLifecycleOwner
        ) { payDayList ->
            payDayAdapter.clear()
            var count = 0
            payDayList?.forEach {
                if (count == 0) {
                    payDayAdapter.add("$it${getString(R.string.__current)}")
                } else {
                    payDayAdapter.add(it)
                }
                count++
            }
            hideUnHidePayDays(payDayList)
        }
        binding.spPayDay.adapter = payDayAdapter
    }

    private fun populateAssetDetails() {
        binding.apply {
            if (curAsset.accountType!!.keepTotals) {
                lblAvailable.visibility = View.GONE
                tvAvailable.visibility = View.GONE
                lblBalanceOwing.text = getString(R.string.balance_in_account)
                if (curAsset.account.accountBalance >= 0.0) {
                    tvBalanceOwing.setTextColor(Color.BLACK)
                } else {
                    tvBalanceOwing.setTextColor(Color.RED)
                }
                tvBalanceOwing.text = nf.displayDollars(curAsset.account.accountBalance)
            } else if (curAsset.accountType!!.tallyOwing) {
                val creditLimit = curAsset.account.accountCreditLimit
                lblAvailable.visibility = View.VISIBLE
                tvAvailable.visibility = View.VISIBLE
                val available = creditLimit + pendingAmount - curAsset.account.accountOwing
                val availableReal = if (available > creditLimit) {
                    creditLimit
                } else {
                    available
                }
                tvAvailable.text = nf.displayDollars(availableReal)
                if (curAsset.account.accountOwing >= 0.0) {
                    lblBalanceOwing.text = getString(R.string.balance_owing)
                    tvBalanceOwing.setTextColor(Color.RED)
                    tvBalanceOwing.text = nf.displayDollars(curAsset.account.accountOwing)
                } else {
                    lblBalanceOwing.text = getString(R.string.credit_of)
                    tvBalanceOwing.setTextColor(Color.BLACK)
                    tvBalanceOwing.text = nf.displayDollars(-curAsset.account.accountOwing)
                }
            }
        }
    }

    fun populatePendingList() {
        val transactionPendingAdapter = TransactionPendingAdapter(
            mainActivity,
            binding.spAssetNames.selectedItem.toString(),
            mView,
            TAG,
            this@BudgetViewFragment,
        )
        binding.rvPending.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = transactionPendingAdapter
        }
        transactionViewModel.getPendingTransactionsDetailed(
            binding.spAssetNames.selectedItem.toString()
        ).observe(viewLifecycleOwner) { transactions ->
            transactionPendingAdapter.differ.submitList(transactions)
            updatePendingUI(transactions)
            updatePendingTotal(transactions)
            populateAssetDetails()
            populateBudgetTotals()
        }
    }

    private fun updatePendingTotal(transactions: List<TransactionDetailed>) {
        pendingAmount = 0.0
        for (item in transactions) {
            if (item.transaction!!.transToAccountPending) {
                pendingAmount += item.transaction.transAmount
            } else {
                pendingAmount -= item.transaction.transAmount
            }
        }
        binding.apply {
            val display =
                getString(R.string.pending) + getString(R.string.lines) + nf.displayDollars(
                    pendingAmount
                ) + getString(R.string.lines)
            if (pendingAmount < 0.0) {
                lblPending.setTextColor(Color.RED)
            } else {
                lblPending.setTextColor(Color.BLACK)
            }
            lblPending.text = display
        }
    }

    private fun updatePendingUI(transactions: List<TransactionDetailed>?) {
        binding.apply {
            if (transactions.isNullOrEmpty()) {
                rvPending.visibility = View.GONE
                lblPending.visibility = View.GONE
                lblPending.setTextColor(Color.BLACK)
            } else {
                rvPending.visibility = View.VISIBLE
                lblPending.visibility = View.VISIBLE
                if (transactions.size > 3) {
                    rvPending.layoutParams.height = 300
                } else {
                    rvPending.layoutParams.height = LayoutParams.WRAP_CONTENT
                }
                lblPending.setTextColor(Color.RED)
            }
        }
    }

    private fun setOnClickActions() {
        binding.apply {
            onSelectAsset()
            onSelectPayDay()
            fabAddAction.setOnClickListener {
                onAddButtonPress()
            }
            tvBalanceOwing.setOnClickListener {
                gotoAccount()
            }
        }
    }

    private fun onSelectAsset() {
        binding.apply {
            spAssetNames.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    accountViewModel.getAccountDetailed(
                        spAssetNames.selectedItem.toString()
                    ).observe(
                        viewLifecycleOwner
                    ) { account ->
                        curAsset = account
                    }
                    clearCurrentDisplay()
                    populatePayDays(spAssetNames.selectedItem.toString())
                    populatePendingList()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun clearCurrentDisplay() {
        binding.apply {
            lblBalanceOwing.text = getString(R.string.blank)
            tvBalanceOwing.text = getString(R.string.blank)
            llNoBudget.visibility = View.VISIBLE
            rvBudgetSummary.visibility = View.GONE
            tvDebits.text = getString(R.string.blank)
            tvCredits.text = getString(R.string.blank)
            tvFixedExpenses.text = getString(R.string.blank)
            tvDiscretionaryExpenses.text = getString(R.string.blank)
            tvSurplusOrDeficit.text = getString(R.string.blank)
            updateBudgetListUi(ArrayList<Any>().toList())
        }
    }

    private fun onSelectPayDay() {
        binding.apply {
            spPayDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    populateBudgetList()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //Not necessary
                }
            }
        }
    }

    fun populateBudgetList() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            binding.apply {
                val asset = spAssetNames.selectedItem.toString()
                val payDay = if (spPayDay.selectedItem != null) {
                    spPayDay.selectedItem.toString().replace(getString(R.string.__current), "")
                } else {
                    ""
                }
                val budgetViewAdapter = BudgetViewAdapter(
                    mainActivity,
                    asset,
                    payDay,
                    mView,
                    TAG,
                    this@BudgetViewFragment,
                )
                binding.rvBudgetSummary.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = budgetViewAdapter
                }
                activity?.let {
                    budgetItemViewModel.getBudgetItems(
                        asset, payDay
                    ).observe(viewLifecycleOwner) { budgetItems ->
                        budgetList.clear()
                        budgetViewAdapter.differ.submitList(budgetItems)
                        budgetItems.listIterator().forEach {
                            budgetList.add(it)
                        }
                        populateAssetDetails()
                        populateBudgetTotals()
                        updateBudgetListUi(budgetItems)
                    }
                }
            }
        }
    }

    private fun onAddButtonPress() {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.choose_an_action)).setItems(
            arrayOf(
                getString(R.string.schedule_a_new_budget_item),
                getString(R.string.add_an_unscheduled_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    addNewBudgetItem()
                }

                1 -> {
                    addNewTransaction()
                }
            }
        }.show()
    }

    private fun resumeHistory() {
        val waitTime = WAIT_250
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(waitTime)
                if (spAssetNames.adapter != null) {
                    for (i in 0 until spAssetNames.adapter.count) {
                        if (spAssetNames.getItemAtPosition(i)
                                .toString() == mainViewModel.getReturnToAsset()
                        ) {
                            spAssetNames.setSelection(i)
                            break
                        }
                    }
                }
                delay(waitTime)
                if (spPayDay.adapter != null) {
                    for (i in 0 until spPayDay.adapter.count) {
                        if (spPayDay.getItemAtPosition(i)
                                .toString() == mainViewModel.getReturnToPayDay()
                        ) {
                            spPayDay.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun updateBudgetListUi(budgetItems: List<Any>?) {
        binding.apply {
            if (budgetItems.isNullOrEmpty()) {
                crdNoTransactions.visibility = View.VISIBLE
                rvBudgetSummary.visibility = View.GONE
                lblBudgeted.visibility = View.GONE
            } else {
                crdNoTransactions.visibility = View.GONE
                rvBudgetSummary.visibility = View.VISIBLE
                lblBudgeted.visibility = View.VISIBLE
            }
        }
    }

    private fun populateBudgetTotals() {
        binding.apply {
            if (spPayDay.adapter.count > 0) {
                var debits = 0.0
                var credits = 0.0
                var fixedExpenses = 0.0
                var otherExpenses = 0.0
                for (details in budgetList) {
                    if (details.toAccount!!.accountName == curAsset.account.accountName) {
                        credits += details.budgetItem!!.biProjectedAmount
                    } else {
                        debits += details.budgetItem!!.biProjectedAmount
                    }
                    if (details.fromAccount!!.accountName == curAsset.account.accountName) {
                        if (details.budgetItem!!.biIsFixed) {
                            fixedExpenses += details.budgetItem!!.biProjectedAmount
                        } else {
                            otherExpenses += details.budgetItem!!.biProjectedAmount
                        }
                    }
                }
                var surplus = credits - debits
                ibDivider.setBackgroundColor(Color.BLACK)
                if (spPayDay.selectedItemId == 0L) {
                    if (curAsset.accountType!!.keepTotals) {
                        surplus += curAsset.account.accountBalance
                    } else {
                        surplus -= curAsset.account.accountOwing
                    }
                }
                if (credits > 0.0) {
                    val display = getString(R.string.credits_) + nf.displayDollars(credits)
                    tvCredits.text = display
                    tvCredits.setTextColor(Color.BLACK)
                } else {
                    tvCredits.text = getString(R.string.no_credits)
                    tvCredits.setTextColor(Color.DKGRAY)
                }
                if (debits > 0.0) {
                    val display = getString(R.string.debits_) + nf.displayDollars(debits)
                    tvDebits.text = display
                    tvDebits.setTextColor(Color.RED)
                } else {
                    tvDebits.text = getString(R.string.no_debits)
                    tvDebits.setTextColor(Color.DKGRAY)
                }
                if (fixedExpenses > 0.0) {
                    val display =
                        getString(R.string.fixed_expenses) + nf.displayDollars(fixedExpenses)
                    tvFixedExpenses.text = display
                    tvFixedExpenses.setTextColor(Color.RED)
                } else {
                    tvFixedExpenses.text = getString(R.string.no_fixed_expenses)
                    tvFixedExpenses.setTextColor(Color.DKGRAY)
                }
                if (otherExpenses > 0.0) {
                    val display =
                        getString(R.string.discretionary_) + nf.displayDollars(otherExpenses)
                    tvDiscretionaryExpenses.text = display
                    tvDiscretionaryExpenses.setTextColor(Color.BLUE)
                } else {
                    tvDiscretionaryExpenses.text = getString(R.string.no_discretionary_expenses)
                    tvDiscretionaryExpenses.setTextColor(Color.DKGRAY)
                }
                if (surplus >= 0.0) {
                    val display = getString(R.string.surplus_of) + nf.displayDollars(surplus)
                    tvSurplusOrDeficit.text = display
                    tvSurplusOrDeficit.setTextColor(Color.BLACK)
                } else {
                    val display = getString(R.string.deficit_of) + nf.displayDollars(-surplus)
                    tvSurplusOrDeficit.text = display
                    tvSurplusOrDeficit.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun hideUnHidePayDays(list: List<Any>) {
        binding.apply {
            if (list.isEmpty()) {
                lblPayDay.visibility = View.GONE
                spPayDay.visibility = View.GONE
            } else {
                lblPayDay.visibility = View.VISIBLE
                spPayDay.visibility = View.VISIBLE
            }
        }
    }

    private fun addNewTransaction() {
        setReturnVariables()
        mainViewModel.setTransactionDetailed(null)
        gotoTransactionAddFragment()
    }

    private fun addNewBudgetItem() {
        setReturnVariables()
        gotoBudgetItemAddFragment()
    }

    private fun setReturnVariables() {
        mainViewModel.setCallingFragments(TAG)
    }

    private fun gotoAccount() {
        setReturnVariables()
        mainViewModel.setAccountWithType(curAsset)
        gotoAccountUpdateFragment()
    }

    private fun gotoAccountUpdateFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToAccountUpdateFragment()
        )
    }

    fun gotoBudgetItemUpdateFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemUpdateFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun gotoTransactionAddFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionAddFragment()
        )
    }

    fun gotoTransactionPerformFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionPerformFragment()
        )
    }

    private fun gotoBudgetItemAddFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemAddFragment()
        )
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionUpdateFragment()
        )
    }

    override fun onStop() {
        binding.apply {
            mainViewModel.setReturnToAsset(spAssetNames.selectedItem.toString())
            if (spPayDay.adapter.count > 0) {
                mainViewModel.setReturnToPayDay(spPayDay.selectedItem.toString())
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}