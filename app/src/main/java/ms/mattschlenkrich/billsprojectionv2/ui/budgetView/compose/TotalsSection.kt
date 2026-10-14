package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions

@Composable
fun TotalsSection(
    budgetTotals: BudgetTotals,
) {
    val nf = LocalNumberFunctions.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (budgetTotals.credits > 0.0) "${stringResource(R.string.label_credits_colon)}${
                nf.displayDollars(
                    budgetTotals.credits
                )
            }" else stringResource(R.string.msg_no_credits),
            color = if (budgetTotals.credits > 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (budgetTotals.debits > 0.0) "${stringResource(R.string.label_debits_colon)}${
                nf.displayDollars(
                    budgetTotals.debits
                )
            }" else stringResource(R.string.msg_no_debits),
            color = if (budgetTotals.debits > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (budgetTotals.fixedExpenses > 0.0) "${stringResource(R.string.title_fixed_expenses)}${
                nf.displayDollars(
                    budgetTotals.fixedExpenses
                )
            }" else stringResource(R.string.msg_no_fixed),
            color = if (budgetTotals.fixedExpenses > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (budgetTotals.otherExpenses > 0.0) "${stringResource(R.string.label_discretionary_colon)}${
                nf.displayDollars(
                    budgetTotals.otherExpenses
                )
            }" else stringResource(R.string.msg_no_discretionary),
            color = if (budgetTotals.otherExpenses > 0.0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}