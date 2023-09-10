package ms.mattschlenkrich.billsprojectionv2.fragments.budgetView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.adapter.BudgetViewAdapter
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentBudgetViewBinding
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(
    R.layout.fragment_transaction_view
) {

    private var _binding: FragmentBudgetViewBinding? = null
    private val binding get() = _binding!!
    private var mView: View? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var accountViewModel: AccountViewModel
    private val cf = CommonFunctions()

    //    private lateinit var assetList: List<String>
    private lateinit var curAsset: AccountWithType
    private val budgetList = ArrayList<BudgetDetailed>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "building BudgetViewFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBudgetViewBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        budgetItemViewModel =
            mainActivity.budgetItemViewModel
        accountViewModel =
            mainActivity.accountViewModel
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.title = "View The Budget"
        binding.apply {
            fabAddAction.setOnClickListener {
                addAction()
            }
        }
        fillAssetsLive()
        selectAsset()
        selectPayDay()
    }

    private fun selectPayDay() {
        binding.apply {
            spPayDay.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        fillBudgetTotals(
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

    private fun fillBudgetTotals(asset: String, payDay: String) {
        val budgetViewAdapter = BudgetViewAdapter(
            asset
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
                budgetViewAdapter.differ.submitList(budgetItems)
                updateUi(budgetItems)
            }
        }
//        budgetList.clear()
//        budgetItemViewModel.getBudgetItems(
//            asset, payDay
//        ).observe(
//            viewLifecycleOwner
//        ) { budgetItemList ->
//            budgetItemList.forEach {
//                budgetList.add(it)
//            }
//        }
//        Log.d(TAG, "budget list size is ${budgetList.size}")
//        var debits = 0.0
//        var credits = 0.0
//        var fixedExpenses = 0.0
//        var otherExpenses = 0.0
//        for (details in budgetList) {
//            if (details.toAccount!!.accountName ==
//                curAsset.account.accountName
//            ) {
//                debits += details.budgetItem!!.biProjectedAmount
//            } else {
//                credits = details.budgetItem!!.biProjectedAmount
//            }
//            if (details.budgetItem.biIsFixed) {
//                fixedExpenses += details.budgetItem.biProjectedAmount
//            } else {
//                otherExpenses += details.budgetItem.biProjectedAmount
//            }
//        }
//        var surplus = credits - debits
//        binding.apply {
//            if (spPayDay.selectedItemId == 0L) {
//                if (curAsset.accountType.keepTotals) {
//                    surplus += curAsset.account.accountBalance
//                } else {
//                    surplus -= curAsset.account.accountOwing
//                }
//            }
//            if (credits > 0.0) {
//                val display = "Credits: ${cf.displayDollars(credits)}"
//                tvCredits.text = display
//                tvCredits.setTextColor(Color.BLACK)
//            } else {
//                tvCredits.text = getString(R.string.no_credits)
//                tvCredits.setTextColor(Color.DKGRAY)
//            }
//            if (debits > 0.0) {
//                val display = "Debits: ${cf.displayDollars(debits)}"
//                tvDebits.text = display
//                tvDebits.setTextColor(Color.RED)
//            } else {
//                tvDebits.text = getString(R.string.no_debits)
//                tvDebits.setTextColor(Color.DKGRAY)
//            }
//            if (fixedExpenses > 0.0) {
//                val display = "Fixed Expenses: ${cf.displayDollars(fixedExpenses)}"
//                tvFixedExpenses.text = display
//                tvFixedExpenses.setTextColor(Color.RED)
//            } else {
//                tvFixedExpenses.text = getString(R.string.no_fixed_expenses)
//                tvFixedExpenses.setTextColor(Color.DKGRAY)
//            }
//            if (otherExpenses > 0.0) {
//                val display = "Discretionary: ${cf.displayDollars(otherExpenses)}"
//                tvDiscretionaryExpenses.text = display
//                tvDiscretionaryExpenses.setTextColor(Color.BLUE)
//            } else {
//                tvDiscretionaryExpenses.text = getString(R.string.no_discretionary_expenses)
//                tvDiscretionaryExpenses.setTextColor(Color.DKGRAY)
//            }
//            if (surplus >= 0.0) {
//                val display = "Surplus of ${cf.displayDollars(surplus)}"
//                tvSurplusOrDeficit.text = display
//                tvSurplusOrDeficit.setTextColor(Color.BLACK)
//            } else {
//                val display = "DEFICIT of ${cf.displayDollars(-surplus)}"
//                tvSurplusOrDeficit.text = display
//                tvSurplusOrDeficit.setTextColor(Color.RED)
//            }
//        }
    }

    private fun updateUi(budgetItems: List<BudgetDetailed>?) {
        if (budgetItems == null) {
            binding.crdNoTransactions.visibility = View.VISIBLE
            binding.rvBudgetSummary.visibility = View.GONE
        } else {
            binding.crdNoTransactions.visibility = View.GONE
            binding.rvBudgetSummary.visibility = View.VISIBLE

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
                        CoroutineScope(Dispatchers.IO).launch {
                            val asset = async {
                                accountViewModel.getAccountWithType(
                                    spAssetNames.selectedItem.toString()
                                )
                            }
                            curAsset = asset.await()
                        }
                        fillPayDaysLive(spAssetNames.selectedItem.toString())
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        //not needed
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
            payDayList?.forEach {
                payDayAdapter.add(it)
            }
        }
        binding.spPayDay.adapter = payDayAdapter
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
            assetList?.forEach {
                assetAdapter.add(it)
            }
        }
        binding.spAssetNames.adapter = assetAdapter
    }
//
//    private fun selectAsset() {
//        CoroutineScope(Dispatchers.IO).launch {
//            delay(1000)
//            val payDays =
//                async {
//                    budgetItemViewModel.getPayDays(
//                        binding.spAssetNames.selectedItem.toString()
//                    )
//                }
//            populatePayDays(payDays.await())
//        }
//    }
//
//    private fun populatePayDays(payDays: List<String>): Boolean {
//        val adapterPayDays = ArrayAdapter(
//            requireContext(),
//            R.layout.spinner_item_bold,
//            payDays
//        )
//        adapterPayDays.setDropDownViewResource(
//            R.layout.spinner_item_bold
//        )
//        binding.spPayDay.adapter = adapterPayDays
//        return true
//    }
//
//    private fun fillAssets(assetList: List<String>, view: View):Boolean {
//        val adapterAssets = ArrayAdapter(
//            requireContext(),
//            R.layout.spinner_item_bold,
//            assetList
//        )
//        adapterAssets.setDropDownViewResource(
//            R.layout.spinner_item_bold
//        )
//       view.findOnBackInvokedDispatcher().spAssetNames.adapter = adapterAssets
//        return true
//    }
//
////    private fun selectPayDay() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val budgetItemList =
//                async {
//                    budgetItemViewModel.getBudgetItems(
//                        binding.spAssetNames.selectedItem.toString(),
//                        binding.spPayDay.selectedItem.toString()
//                    )
//                }
//            if (budgetItemList.await().isNotEmpty()) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    fillBudgetItems(
//                        budgetItemList.await(),
////                        binding.spPayDay.selectedItem.toString()
//                        )
//                }
//            }
//        }
//    }
//
//    private fun fillBudgetItems(
//        budgetItemList: List<BudgetDetailed>,
////        curAsset: AccountWithType
//    ) {


//    }
//
//    private fun selectAsset() {
//        binding.spAssetNames.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val payDays =
//                           async {
//                                budgetItemViewModel.getPayDays(
//                                    binding.spAssetNames.selectedItem.toString()
//                                )
//                            }
//                        val asset =
//                            async {
//                                accountViewModel.getAccountWithType(
//                                    binding.spAssetNames.selectedItem.toString()
//                                )
//                            }
//                        CoroutineScope(Dispatchers.Main).launch {
//                            curAsset = asset.await()
//                            fillAssetDetails(
//                                asset.await(),
//                                payDays.await())
//                        }
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    // should be deprecated but need it for object
//                }
//            }
//    }
//
//    private fun fillAssetDetails(
//        asset: AccountWithType,
//        payDayList: List<String>) {
//        binding.apply {
//            if (payDayList.isNotEmpty()) {
//                val adapterPayDay = ArrayAdapter(
//                    requireContext(),
//                    R.layout.spinner_item_bold,
//                    payDayList
//                )
//                adapterPayDay.setDropDownViewResource(
//                    R.layout.spinner_item_bold
//                )
//                spPayDay.adapter = adapterPayDay
//            }
//            if (asset.accountType.keepTotals) {
//                lblBalanceOwing.text =
//                    getString(R.string.balance_in_account)
//                if (asset.account.accountBalance >= 0.0) {
//                    tvBalanceOwing.setTextColor(Color.BLACK)
//                } else {
//                    tvBalanceOwing.setTextColor(Color.RED)
//                }
//                tvBalanceOwing.text =
//                    cf.displayDollars(asset.account.accountBalance)
//            } else if (asset.accountType.tallyOwing) {
//                if (asset.account.accountOwing >= 0.0) {
//                    lblBalanceOwing.text =
//                        getString(R.string.balance_owing)
//                    tvBalanceOwing.setTextColor(Color.RED)
//                    tvBalanceOwing.text =
//                        cf.displayDollars(asset.account.accountOwing)
//                } else {
//                    lblBalanceOwing.text =
//                        getString(R.string.credit_of)
//                    tvBalanceOwing.setTextColor(Color.BLACK)
//                    tvBalanceOwing.text =
//                        cf.displayDollars(-asset.account.accountOwing)
//                }
//            }
//        }
//    }
//
//    private suspend fun fillAssets() {
//        CoroutineScope(Dispatchers.IO).launch {
//           val assets =
//              async { budgetItemViewModel.getAccountsForBudget() }
//            if (assets.await().isNotEmpty()) {
//               CoroutineScope(Dispatchers.Main).launch {
//                    assetList = assets.await()
//                    val adapterAssets = ArrayAdapter(
//                        requireContext(),
//                        R.layout.spinner_item_bold,
//                        assetList
//                    )
//                    adapterAssets.setDropDownViewResource(
//                        R.layout.spinner_item_bold
//                    )
//                    binding.spAssetNames.adapter = adapterAssets
//                }
//            }
//        }
//        binding.spAssetNames.setSelection(0)
//    }

    private fun addAction() {
        AlertDialog.Builder(mView!!.context)
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
        val fragmentChain = TAG
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToTransactionAddFragment(
                    null,
                    fragmentChain
                )
        findNavController().navigate(direction)
    }

    private fun addNewBudgetItem() {
        val fragmentChain = TAG
        val direction =
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetViewAddFragment2(
                    null,
                    fragmentChain
                )
        findNavController().navigate(direction)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}