package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_CHOOSE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
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
                    BudgetRuleChooseScreen()
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

    @Composable
    fun BudgetRuleChooseScreen() {
        var searchQuery by remember { mutableStateOf("") }
        val budgetRulesDetailed by if (searchQuery.isEmpty()) {
            budgetRuleViewModel.getActiveBudgetRulesDetailed().observeAsState(emptyList())
        } else {
            budgetRuleViewModel.searchBudgetRules("%$searchQuery%").observeAsState(emptyList())
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { gotoBudgetRuleAdd() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_budget_rule),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp)
            ) {
                ProjectTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search)) },
                    placeholder = { Text(stringResource(R.string.enter_criteria)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (budgetRulesDetailed.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.no_budget_rules_to_view),
                                modifier = Modifier.padding(32.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp
                        ) {
                            items(budgetRulesDetailed) { budgetRuleDetailed ->
                                BudgetRuleChooseItem(budgetRuleDetailed)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BudgetRuleChooseItem(budgetRuleDetailed: BudgetRuleDetailed) {
        val rule = budgetRuleDetailed.budgetRule!!
        val isDeleted = rule.budIsDeleted
        val containerColor = if (isDeleted) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
        val contentColor = if (isDeleted) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseBudgetRule(budgetRuleDetailed) },
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rule.budgetRuleName + if (isDeleted) " " + stringResource(R.string.deleted) else "",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(15.dp, 10.dp)
                        .background(Color(vf.getRandomColorInt()))
                )
            }
        }
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