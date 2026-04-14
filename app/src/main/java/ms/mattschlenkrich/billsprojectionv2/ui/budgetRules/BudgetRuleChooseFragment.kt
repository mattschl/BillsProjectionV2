package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_RULE_CHOOSE

class BudgetRuleChooseFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private val vf = VisualsFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    var searchQuery by remember { mutableStateOf("") }
                    val budgetRulesDetailed by if (searchQuery.isEmpty()) {
                        budgetRuleViewModel.getActiveBudgetRulesDetailed()
                            .observeAsState(emptyList())
                    } else {
                        budgetRuleViewModel.searchBudgetRules("%$searchQuery%")
                            .observeAsState(emptyList())
                    }
                    BudgetRuleChooseScreen(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        budgetRulesDetailed = budgetRulesDetailed,
                        onAddBudgetRule = { gotoBudgetRuleAdd() },
                        onBudgetRuleClick = { chooseBudgetRule(it) }
                    )
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.topMenuBar.title = getString(R.string.choose_a_budget_rule)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.removeCallingFragment(TAG)
    }

    private fun chooseBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        if (mainViewModel.getCallingFragments() != null) {
            val mCallingFragment = mainViewModel.getCallingFragments()!!
            if (mCallingFragment.contains(FRAG_TRANSACTION_ANALYSIS)) {
                mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
                findNavController().popBackStack()
                return
            }
            mainViewModel.removeCallingFragment(TAG)
            mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
            if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
                mainViewModel.setSplitTransactionDetailed(
                    mTransactionSplit?.copy(budgetRule = budgetRuleDetailed.budgetRule)
                )
            } else {
                val mTransaction = mainViewModel.getTransactionDetailed()
                mainViewModel.setTransactionDetailed(
                    mTransaction?.copy(budgetRule = budgetRuleDetailed.budgetRule)
                )
            }
            if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) || mCallingFragment.contains(
                    FRAG_BUDGET_ITEM_UPDATE
                )
            ) {
                val mBudgetDetailed = mainViewModel.getBudgetItemDetailed()
                mainViewModel.setBudgetItemDetailed(
                    mBudgetDetailed?.copy(budgetRule = budgetRuleDetailed.budgetRule)
                )
            }
            findNavController().popBackStack()
        }
    }

    private fun gotoBudgetRuleAdd() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(null)
        findNavController().navigate(
            R.id.action_budgetRuleChooseFragment_to_budgetRuleAddFragment
        )
    }
}