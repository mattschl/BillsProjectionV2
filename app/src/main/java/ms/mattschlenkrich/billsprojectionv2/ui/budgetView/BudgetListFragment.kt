package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_LIST

class BudgetListFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        refreshData()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    val budgetDate = df.getCurrentDateAsString()
                    val monthlyRules by budgetRuleViewModel.getBudgetRulesCompleteMonthly(budgetDate)
                        .observeAsState(emptyList())
                    val occasionalRules by budgetRuleViewModel.getBudgetRulesCompletedOccasional(
                        budgetDate
                    )
                        .observeAsState(emptyList())
                    val annualRules by budgetRuleViewModel.getBudgetRulesCompletedAnnually(
                        budgetDate
                    )
                        .observeAsState(emptyList())

                    BudgetSummaryScreen(
                        monthlyRules = monthlyRules,
                        occasionalRules = occasionalRules,
                        annualRules = annualRules,
                        onRuleClick = { chooseOptionsForBudgetItem(it) }
                    )
                }
            }
        }
    }

    override fun refreshData() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.topMenuBar.title = getString(R.string.view_the_complete_budget)
    }

    private fun chooseOptionsForBudgetItem(curRule: BudgetRuleComplete) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + curRule.budgetRule!!.budgetRuleName
        ).setItems(
            arrayOf(
                getString(R.string.view_or_edit_this_budget_rule),
                getString(R.string.delete_this_budget_rule),
                getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
            )
        ) { _, pos ->
            when (pos) {
                0 -> editBudgetRule(curRule)
                1 -> deleteBudgetRule(curRule)
                2 -> gotoAverages(curRule)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun editBudgetRule(curRule: BudgetRuleComplete) {
        val budgetRule = BudgetRuleDetailed(
            curRule.budgetRule!!, curRule.toAccount!!.account, curRule.fromAccount!!.account
        )
        mainViewModel.setBudgetRuleDetailed(budgetRule)
        mainViewModel.setCallingFragments(TAG)
        gotoBudgetRuleUpdateFragment()
    }

    private fun deleteBudgetRule(curRule: BudgetRuleComplete) {
        budgetRuleViewModel.deleteBudgetRule(
            curRule.budgetRule!!.ruleId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoAverages(curRule: BudgetRuleComplete) {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curRule.budgetRule!!, curRule.toAccount!!.account, curRule.fromAccount!!.account
            )
        )
        mainViewModel.setAccountWithType(null)
        gotoTransactionAverageFragment()
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(
            BudgetListFragmentDirections.actionBudgetListFragmentToBudgetRuleUpdateFragment()
        )
    }

    fun gotoTransactionAverageFragment() {
        findNavController().navigate(
            BudgetListFragmentDirections.actionBudgetListFragmentToTransactionAnalysisFragment()
        )
    }
}