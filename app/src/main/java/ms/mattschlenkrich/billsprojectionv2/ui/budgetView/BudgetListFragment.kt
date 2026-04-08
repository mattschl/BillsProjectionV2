package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_LIST

class BudgetListFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val vf = VisualsFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
        mainActivity.topMenuBar.title = getString(R.string.view_the_complete_budget)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    BudgetListScreen()
                }
            }
        }
    }

    @Composable
    fun BudgetListScreen() {
        val budgetDate = df.getCurrentDateAsString()
        val monthlyRules by budgetRuleViewModel.getBudgetRulesCompleteMonthly(budgetDate)
            .observeAsState(emptyList())
        val occasionalRules by budgetRuleViewModel.getBudgetRulesCompletedOccasional(budgetDate)
            .observeAsState(emptyList())
        val annualRules by budgetRuleViewModel.getBudgetRulesCompletedAnnually(budgetDate)
            .observeAsState(emptyList())

        var monthlyExpanded by remember { mutableStateOf(false) }
        var occasionalExpanded by remember { mutableStateOf(false) }
        var annualExpanded by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            item {
                BudgetSectionHeader(
                    title = stringResource(R.string.regular_monthly_budget),
                    isExpanded = monthlyExpanded,
                    onToggle = { monthlyExpanded = !monthlyExpanded }
                )
            }
            if (monthlyExpanded) {
                item {
                    BudgetSummary(monthlyRules, type = "monthly")
                }
                items(monthlyRules) { rule ->
                    BudgetListItem(rule)
                }
            }

            item {
                BudgetSectionHeader(
                    title = stringResource(R.string.occasional_budget),
                    isExpanded = occasionalExpanded,
                    onToggle = { occasionalExpanded = !occasionalExpanded }
                )
            }
            if (occasionalExpanded) {
                item {
                    BudgetSummary(occasionalRules, type = "occasional")
                }
                items(occasionalRules) { rule ->
                    BudgetListItem(rule)
                }
            }

            item {
                BudgetSectionHeader(
                    title = stringResource(R.string.annual_budget),
                    isExpanded = annualExpanded,
                    onToggle = { annualExpanded = !annualExpanded }
                )
            }
            if (annualExpanded) {
                item {
                    BudgetSummary(annualRules, type = "annual")
                }
                items(annualRules) { rule ->
                    BudgetListItem(rule, showFullDetails = true)
                }
            }
        }
    }

    @Composable
    fun BudgetSectionHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        HorizontalDivider()
    }

    @Composable
    fun BudgetSummary(rules: List<BudgetRuleComplete>, type: String) {
        var totalCredits = 0.0
        var totalDebits = 0.0
        var totalFixed = 0.0

        for (budget in rules) {
            val amt = when (type) {
                "monthly", "occasional" -> {
                    when (budget.budgetRule!!.budFrequencyTypeId) {
                        FREQ_WEEKLY -> budget.budgetRule!!.budgetAmount * 4 / budget.budgetRule!!.budFrequencyCount
                        FREQ_MONTHLY -> budget.budgetRule!!.budgetAmount / budget.budgetRule!!.budFrequencyCount
                        else -> 0.0
                    }
                }

                "annual" -> budget.budgetRule!!.budgetAmount / 12 / budget.budgetRule!!.budFrequencyCount
                else -> 0.0
            }

            if (budget.toAccount!!.accountType!!.displayAsAsset) {
                totalCredits += amt
                if (type == "monthly" && budget.budgetRule!!.budFixedAmount) totalFixed += amt
            }
            if (budget.fromAccount!!.accountType!!.displayAsAsset) {
                totalDebits += amt
                if (type == "monthly" && budget.budgetRule!!.budFixedAmount) totalFixed -= amt
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.credits_) + nf.displayDollars(totalCredits),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.debits_) + nf.displayDollars(totalDebits),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (type == "monthly") {
                        Text(
                            text = stringResource(R.string.fixed_) + nf.displayDollars(totalFixed),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                val diff = totalCredits - totalDebits
                Text(
                    text = if (diff >= 0) stringResource(R.string.surplus_of) + nf.displayDollars(
                        diff
                    )
                    else stringResource(R.string.deficit_of) + nf.displayDollars(-diff),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (diff >= 0) Color.Unspecified else Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun BudgetListItem(curRule: BudgetRuleComplete, showFullDetails: Boolean = false) {
        val budgetRule = curRule.budgetRule!!
        val toAccount = curRule.toAccount!!
        val fromAccount = curRule.fromAccount!!

        var nameInfo = budgetRule.budgetRuleName + " - "
        nameInfo += when (budgetRule.budFrequencyTypeId) {
            FREQ_WEEKLY -> {
                if (showFullDetails) df.getDisplayDateInComingYear(
                    budgetRule.budStartDate,
                    budgetRule.budFrequencyCount.toLong()
                )
                else stringResource(R.string.weekly_x) + budgetRule.budFrequencyCount
            }

            FREQ_MONTHLY -> {
                if (showFullDetails) df.getDisplayDateInComingYear(budgetRule.budStartDate)
                else stringResource(R.string.monthly)
            }

            else -> ""
        }

        val amt = when (budgetRule.budFrequencyTypeId) {
            FREQ_WEEKLY -> budgetRule.budgetAmount * 4 / budgetRule.budFrequencyCount
            FREQ_MONTHLY -> budgetRule.budgetAmount / budgetRule.budFrequencyCount
            else -> budgetRule.budgetAmount
        }

        val displayAmt =
            if (toAccount.accountType!!.displayAsAsset && fromAccount.accountType!!.displayAsAsset) {
                stringResource(R.string.na)
            } else nf.displayDollars(if (showFullDetails) budgetRule.budgetAmount else amt)

        val textColor =
            if (fromAccount.accountType!!.isAsset || fromAccount.accountType!!.displayAsAsset) Color.Red else Color.Black

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptionsForBudgetItem(curRule) }
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(vf.getRandomColorInt()))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nameInfo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (budgetRule.budFixedAmount) "(f) $displayAmt" else displayAmt,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
            if (showFullDetails) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.annually_every) + budgetRule.budFrequencyCount + stringResource(
                            R.string._years
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.average_per_month) + nf.displayDollars(
                            budgetRule.budgetAmount / 12 / budgetRule.budFrequencyCount
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (budgetRule.budFrequencyTypeId != FREQ_MONTHLY || budgetRule.budFrequencyCount > 1) {
                Text(
                    text = when (budgetRule.budFrequencyTypeId) {
                        FREQ_WEEKLY -> "Weekly x " + budgetRule.budFrequencyCount
                        FREQ_MONTHLY -> "Monthly x " + budgetRule.budFrequencyCount
                        else -> ""
                    } + " " + stringResource(R.string.average_per_month) + nf.displayDollars(amt),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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