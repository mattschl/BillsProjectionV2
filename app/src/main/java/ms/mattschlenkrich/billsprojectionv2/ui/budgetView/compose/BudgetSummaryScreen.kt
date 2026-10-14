package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionBottomSheet
import ms.mattschlenkrich.billsprojectionv2.common.components.ActionOption
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSummaryScreen(
    monthlyRules: List<BudgetRuleComplete>,
    occasionalRules: List<BudgetRuleComplete>,
    annualRules: List<BudgetRuleComplete>,
    onRuleClick: (BudgetRuleComplete) -> Unit,
    sheetTitle: String = "",
    sheetOptions: List<ActionOption> = emptyList(),
    onSheetDismiss: () -> Unit = {}
) {
    val df = LocalDateFunctions.current
    val nf = LocalNumberFunctions.current
    val sheetState = rememberModalBottomSheetState()

    if (sheetOptions.isNotEmpty()) {
        ActionBottomSheet(
            title = sheetTitle,
            options = sheetOptions,
            sheetState = sheetState,
            onDismissRequest = onSheetDismiss
        )
    }

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
                title = stringResource(R.string.label_regular_monthly_budget),
                isExpanded = monthlyExpanded,
                onToggle = { monthlyExpanded = !monthlyExpanded }
            )
        }
        if (monthlyExpanded) {
            item {
                BudgetSummary(monthlyRules, type = "monthly")
            }
            items(monthlyRules) { rule ->
                BudgetListItem(
                    curRule = rule,
                    onRuleClick = onRuleClick,
                )
            }
        }

        item {
            BudgetSectionHeader(
                title = stringResource(R.string.label_occasional_budget),
                isExpanded = occasionalExpanded,
                onToggle = { occasionalExpanded = !occasionalExpanded }
            )
        }
        if (occasionalExpanded) {
            item {
                BudgetSummary(occasionalRules, type = "occasional")
            }
            items(occasionalRules) { rule ->
                BudgetListItem(
                    curRule = rule,
                    onRuleClick = onRuleClick,
                )
            }
        }

        item {
            BudgetSectionHeader(
                title = stringResource(R.string.label_annual_budget),
                isExpanded = annualExpanded,
                onToggle = { annualExpanded = !annualExpanded }
            )
        }
        if (annualExpanded) {
            item {
                BudgetSummary(annualRules, type = "annual")
            }
            items(annualRules) { rule ->
                BudgetListItem(
                    curRule = rule,
                    showFullDetails = true,
                    onRuleClick = onRuleClick,
                )
            }
        }
    }
}