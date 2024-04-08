package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetViewAdapter
import ms.mattschlenkrich.billsprojectionv2.adapter.TransactionPendingAdapter
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewBinding
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.budgetItem.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(
    R.layout.fragment_budget_view
) {

    private var _binding: FragmentBudgetViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val cf = CommonFunctions()

    //    private lateinit var assetList: List<String>
    private lateinit var curAsset: AccountWithType
    private val budgetList = ArrayList<BudgetDetailed>()
    private var pendingAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel =
            mainActivity.mainViewModel
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        accountViewModel =
            mainActivity.accountViewModel
        transactionViewModel =
            mainActivity.transactionViewModel
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.title = "View The Budget"
        createActions()
        fillAssetsLive()
        selectAsset()
        selectPayDay()
        resumeHistory()
    }

    private fun createActions() {
        binding.apply {
            fabAddAction.setOnClickListener {
                addAction()
            }
            tvBalanceOwing.setOnClickListener {
                gotoAccount()
            }
        }
    }

    private fun gotoAccount() {
        setToReturn()
        mainViewModel.setAccountWithType(curAsset)
        mView.findNavController().navigate(
            BudgetViewFragmentDirections.actionBudgetViewFragmentToAccountUpdateFragment()
        )
    }

    private fun resumeHistory() {
        val waitTime = 250L
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(waitTime)
                if (spAssetNames.adapter != null) {
                    for (i in 0 until spAssetNames.adapter.count) {
                        if (spAssetNames.getItemAtPosition(i).toString() ==
                            mainViewModel.getReturnToAsset()
                        ) {
                            spAssetNames.setSelection(i)
                            break
                        }
                    }
                }
                delay(waitTime)
                if (spPayDay.adapter != null) {
                    for (i in 0 until spPayDay.adapter.count) {
                        if (spPayDay.getItemAtPosition(i).toString() ==
                            mainViewModel.getReturnToPayDay()
                        ) {
                            spPayDay.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun selectPayDay() {
        binding.apply {
            spPayDay.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                    ) {
                        fillBudgetList(
                            spAssetNames.selectedItem.toString(),
                            spPayDay.selectedItem.toString()
                        )
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        //Not necessary
                    }
                }
        }
    }

    fun fillBudgetList(asset: String, payDay: String) {
        val budgetViewAdapter = BudgetViewAdapter(
            this,
            budgetItemViewModel,
            mainViewModel,
            asset,
            payDay,
            mView.context
        )
        binding.rvBudgetSummary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetViewAdapter
        }
        activity?.let {
            budgetItemViewModel.getBudgetItems(
                asset, payDay
            ).observe(
                viewLifecycleOwner
            ) { budgetItems ->
                budgetList.clear()
                budgetViewAdapter.differ.submitList(budgetItems)
                updateUi(budgetItems)
                budgetItems.listIterator().forEach {
                    budgetList.add(it)
                }
                fillAssetDetails()
                fillBudgetTotals()
            }
        }
    }

    fun fillBudgetTotals() {
        binding.apply {
            if (spPayDay.adapter.count > 0) {
                var debits = 0.0
                var credits = 0.0
                var fixedExpenses = 0.0
                var otherExpenses = 0.0
                @Suppress("UNUSED_VARIABLE") var available = 0.0
                for (details in budgetList) {
                    if (details.toAccount!!.accountName ==
                        curAsset.account.accountName
                    ) {
                        credits += details.budgetItem!!.biProjectedAmount
                    } else {
                        debits += details.budgetItem!!.biProjectedAmount
                    }
                    if (details.fromAccount!!.accountName == curAsset.account.accountName) {
                        if (details.budgetItem.biIsFixed
                        ) {
                            fixedExpenses += details.budgetItem.biProjectedAmount
                        } else {
                            otherExpenses += details.budgetItem.biProjectedAmount
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
                    val display = "Credits: ${cf.displayDollars(credits)}"
                    tvCredits.text = display
                    tvCredits.setTextColor(Color.BLACK)
                } else {
                    tvCredits.text = getString(R.string.no_credits)
                    tvCredits.setTextColor(Color.DKGRAY)
                }
                if (debits > 0.0) {
                    val display = "Debits: ${cf.displayDollars(debits)}"
                    tvDebits.text = display
                    tvDebits.setTextColor(Color.RED)
                } else {
                    tvDebits.text = getString(R.string.no_debits)
                    tvDebits.setTextColor(Color.DKGRAY)
                }
                if (fixedExpenses > 0.0) {
                    val display = "Fixed Expenses: ${cf.displayDollars(fixedExpenses)}"
                    tvFixedExpenses.text = display
                    tvFixedExpenses.setTextColor(Color.RED)
                } else {
                    tvFixedExpenses.text = getString(R.string.no_fixed_expenses)
                    tvFixedExpenses.setTextColor(Color.DKGRAY)
                }
                if (otherExpenses > 0.0) {
                    val display = "Discretionary: ${cf.displayDollars(otherExpenses)}"
                    tvDiscretionaryExpenses.text = display
                    tvDiscretionaryExpenses.setTextColor(Color.BLUE)
                } else {
                    tvDiscretionaryExpenses.text = getString(R.string.no_discretionary_expenses)
                    tvDiscretionaryExpenses.setTextColor(Color.DKGRAY)
                }
                if (surplus >= 0.0) {
                    val display = "Surplus of ${cf.displayDollars(surplus)}"
                    tvSurplusOrDeficit.text = display
                    tvSurplusOrDeficit.setTextColor(Color.BLACK)
                } else {
                    val display = "DEFICIT of ${cf.displayDollars(-surplus)}"
                    tvSurplusOrDeficit.text = display
                    tvSurplusOrDeficit.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun updateUi(budgetItems: List<BudgetDetailed>?) {
        binding.apply {
            if (budgetItems.isNullOrEmpty()) {
                crdNoTransactions.visibility = View.VISIBLE
                rvBudgetSummary.visibility = View.GONE
                lblBudgeted.visibility = View.GONE
            } else {
                binding.crdNoTransactions.visibility = View.GONE
                binding.rvBudgetSummary.visibility = View.VISIBLE
                lblBudgeted.visibility = View.VISIBLE
            }
        }
    }

    private fun selectAsset() {
        binding.apply {
            spAssetNames.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        accountViewModel.getAccountDetailed(
                            spAssetNames.selectedItem.toString()
                        ).observe(
                            viewLifecycleOwner
                        ) { account ->
                            curAsset = account
                        }
                        clearCurrentDisplay()
                        fillPayDaysLive(spAssetNames.selectedItem.toString())
                        setupPendingList()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    fun setupPendingList() {
        val transactionPendingAdapter =
            TransactionPendingAdapter(
                binding.spAssetNames.selectedItem.toString(),
                mainViewModel,
                mainActivity,
                this,
                mView.context,
            )
        binding.rvPending.apply {
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter = transactionPendingAdapter
        }
        activity?.let {
            transactionViewModel.getPendingTransactionsDetailed(
                binding.spAssetNames.selectedItem.toString()
            ).observe(
                viewLifecycleOwner
            ) { transactions ->
                transactionPendingAdapter.differ.submitList(transactions)
                updatePendingUI(transactions)
                updatePendingTotal(transactions)
                fillAssetDetails()
                fillBudgetTotals()
            }
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
            val display = "------------- Pending: ${cf.displayDollars(pendingAmount)} -------------"
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

    private fun clearCurrentDisplay() {
        binding.apply {
            lblBalanceOwing.text = ""
            tvBalanceOwing.text = ""
            llNoBudget.visibility = View.VISIBLE
            rvBudgetSummary.visibility = View.GONE
            tvDebits.text = ""
            tvCredits.text = ""
            tvFixedExpenses.text = ""
            tvDiscretionaryExpenses.text = ""
            tvSurplusOrDeficit.text = ""
        }
    }

    private fun fillAssetDetails() {
        binding.apply {
            if (curAsset.accountType!!.keepTotals) {
                lblAvailable.visibility = View.GONE
                tvAvailable.visibility = View.GONE
                lblBalanceOwing.text =
                    getString(R.string.balance_in_account)
                if (curAsset.account.accountBalance >= 0.0) {
                    tvBalanceOwing.setTextColor(Color.BLACK)
                } else {
                    tvBalanceOwing.setTextColor(Color.RED)
                }
                tvBalanceOwing.text =
                    cf.displayDollars(curAsset.account.accountBalance)
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
                tvAvailable.text = cf.displayDollars(availableReal)
                if (curAsset.account.accountOwing >= 0.0) {
                    lblBalanceOwing.text =
                        getString(R.string.balance_owing)
                    tvBalanceOwing.setTextColor(Color.RED)
                    tvBalanceOwing.text =
                        cf.displayDollars(curAsset.account.accountOwing)
                } else {
                    lblBalanceOwing.text =
                        getString(R.string.credit_of)
                    tvBalanceOwing.setTextColor(Color.BLACK)
                    tvBalanceOwing.text =
                        cf.displayDollars(-curAsset.account.accountOwing)
                }
            }
        }
    }

    private fun fillPayDaysLive(asset: String) {
        val payDayAdapter =
            ArrayAdapter<Any>(
                requireContext(),
                R.layout.spinner_item_bold
            )
        budgetItemViewModel.getPayDays(asset).observe(
            viewLifecycleOwner
        ) { payDayList ->
            payDayAdapter.clear()
            payDayList?.forEach {
                payDayAdapter.add(it)
            }
            hidePayDays(payDayList)
        }
        binding.spPayDay.adapter = payDayAdapter
    }

    private fun hidePayDays(list: List<Any>) {
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

    private fun fillAssetsLive() {
        val assetAdapter =
            ArrayAdapter<Any>(
                requireContext(),
                R.layout.spinner_item_bold
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

    private fun addAction() {
        AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.choose_an_action))
            .setItems(
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
            }
            .show()
    }

    private fun addNewTransaction() {
        setToReturn()
        mainViewModel.setTransactionDetailed(null)
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToTransactionAddFragment()
        findNavController().navigate(direction)
    }

    private fun addNewBudgetItem() {
        setToReturn()
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetItemAddFragment()
        findNavController().navigate(direction)
    }

    private fun setToReturn() {
        binding.apply {
            mainViewModel.setCallingFragments(TAG)
//            mainViewModel.setReturnToAsset(spAssetNames.selectedItem.toString())
//            mainViewModel.setReturnToPayDay(spPayDay.selectedItem.toString())
        }
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