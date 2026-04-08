package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
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

class BudgetRuleFragment : Fragment() {

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
                    BudgetRulesScreen(refreshKey.intValue)
                }
            }
        }
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
    }

    fun refreshData() {
        updateViewModels()
        lifecycleScope.launch {
            delay(100)
            refreshKey.intValue++
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.removeCallingFragment(TAG)
    }

    @Composable
    fun BudgetRulesScreen(refreshKey: Int) {
        var searchQuery by remember(refreshKey) { mutableStateOf("") }
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
                                BudgetRuleItem(budgetRuleDetailed)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BudgetRuleItem(budgetRuleDetailed: BudgetRuleDetailed) {
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
                .clickable { chooseOptionsForBudgetRule(budgetRuleDetailed) },
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rule.budgetRuleName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(vf.getRandomColorInt()))
                    )
                }

                Text(
                    text = stringResource(R.string.to_) + (budgetRuleDetailed.toAccount?.accountName
                        ?: ""),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.from_) + (budgetRuleDetailed.fromAccount?.accountName
                        ?: ""),
                    style = MaterialTheme.typography.labelMedium
                )

                val amount = nf.displayDollars(rule.budgetAmount)
                val frequencyTypes = stringArrayResource(R.array.frequency_types)
                val frequencyType = frequencyTypes.getOrElse(rule.budFrequencyTypeId) { "" }
                val daysOfWeek = stringArrayResource(R.array.days_of_week)
                val dayOfWeek = daysOfWeek.getOrElse(rule.budDayOfWeekId) { "" }

                val info = "$amount $frequencyType X ${rule.budFrequencyCount}\nOn $dayOfWeek"
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (isDeleted) {
                    Text(
                        text = stringResource(R.string.deleted),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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