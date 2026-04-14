package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_RULES

class BudgetRuleFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val vf = VisualsFunctions()
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.choose_a_budget_rule)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    val key by refreshKey
                    var searchQuery by remember(key) { mutableStateOf("") }
                    val budgetRulesDetailed by if (searchQuery.isEmpty()) {
                        budgetRuleViewModel.getActiveBudgetRulesDetailed()
                            .observeAsState(emptyList())
                    } else {
                        budgetRuleViewModel.searchBudgetRules("%$searchQuery%")
                            .observeAsState(emptyList())
                    }
                    BudgetRulesListScreen(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        budgetRulesDetailed = budgetRulesDetailed,
                        onAddClick = { gotoBudgetRuleAdd() },
                        onItemClick = { chooseOptionsForBudgetRule(it) }
                    )
                }
            }
        }
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.budget_rules)
        lifecycleScope.launch {
            delay(100)
            refreshKey.intValue++
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.removeCallingFragment(TAG)
    }

    private fun chooseOptionsForBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + budgetRuleDetailed.budgetRule!!.budgetRuleName
        ).setItems(
            arrayOf(
                getString(R.string.view_or_edit_this_budget_rule),
                getString(R.string.add_a_new_transaction_based_on_the_budget_rule),
                getString(R.string.create_a_scheduled_item_with_this_budget_rule),
                getString(R.string.view_a_summary_of_transactions_for_this_budget_rule),
                getString(R.string.delete_this_budget_rule),
            )
        ) { _, pos ->
            when (pos) {
                0 -> editBudgetRule(budgetRuleDetailed)
                1 -> gotoAddTransaction(budgetRuleDetailed)
                2 -> gotoCreateBudgetItem(budgetRuleDetailed)
                3 -> gotoAverages(budgetRuleDetailed)
                4 -> deleteBudgetRule(budgetRuleDetailed)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun editBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun gotoAddTransaction(budgetRuleDetailed: BudgetRuleDetailed) {
        val mTransaction = Transactions(
            nf.generateId(),
            df.getCurrentDateAsString(),
            budgetRuleDetailed.budgetRule!!.budgetRuleName,
            "",
            budgetRuleDetailed.budgetRule!!.ruleId,
            0L,
            false,
            0L,
            false,
            budgetRuleDetailed.budgetRule!!.budgetAmount,
            false,
            df.getCurrentTimeAsString()
        )
        mainViewModel.setTransactionDetailed(
            TransactionDetailed(
                mTransaction,
                budgetRuleDetailed.budgetRule,
                null,
                null,
            )
        )
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionAddFragment()
        )
    }

    private fun gotoCreateBudgetItem(budgetRuleDetailed: BudgetRuleDetailed) {
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetItemDetailed(
            BudgetItemDetailed(
                BudgetItem(
                    budgetRuleDetailed.budgetRule!!.ruleId,
                    df.getCurrentDateAsString(),
                    df.getCurrentDateAsString(),
                    "",
                    budgetRuleDetailed.budgetRule!!.budgetRuleName,
                    budgetRuleDetailed.budgetRule!!.budIsPayDay,
                    budgetRuleDetailed.toAccount!!.accountId,
                    budgetRuleDetailed.fromAccount!!.accountId,
                    budgetRuleDetailed.budgetRule!!.budgetAmount,
                    false,
                    budgetRuleDetailed.budgetRule!!.budFixedAmount,
                    budgetRuleDetailed.budgetRule!!.budIsAutoPay,
                    biManuallyEntered = true,
                    biIsCompleted = false,
                    biIsCancelled = false,
                    biIsDeleted = false,
                    biUpdateTime = df.getCurrentTimeAsString(),
                    biLocked = true
                ),
                budgetRuleDetailed.budgetRule!!,
                budgetRuleDetailed.toAccount!!,
                budgetRuleDetailed.fromAccount!!,
            )
        )
        findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToBudgetItemAddFragment()
        )
    }

    private fun gotoAverages(budgetRuleDetailed: BudgetRuleDetailed) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.setAccountWithType(null)
        findNavController().navigate(
            BudgetRuleFragmentDirections.actionBudgetRuleFragmentToTransactionAnalysisFragment()
        )
    }

    private fun deleteBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        budgetRuleViewModel.deleteBudgetRule(
            budgetRuleDetailed.budgetRule!!.ruleId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoBudgetRuleAdd() {
        mainViewModel.setBudgetRuleDetailed(null)
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            R.id.action_budgetRuleFragment_to_budgetRuleAddFragment
        )
    }
}