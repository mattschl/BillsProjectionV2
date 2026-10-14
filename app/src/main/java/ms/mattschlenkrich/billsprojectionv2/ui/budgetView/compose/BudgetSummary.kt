package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete

@Composable
fun BudgetSummary(
    rules: List<BudgetRuleComplete>,
    type: String,
) {
    val nf = LocalNumberFunctions.current
    var totalCredits = 0.0
    var totalDebits = 0.0
    var totalFixed = 0.0

    for (budget in rules) {
        val budgetRule = budget.budgetRule ?: continue
        val toAccount = budget.toAccount ?: continue
        val fromAccount = budget.fromAccount ?: continue
        val toAccountType = toAccount.accountType
        val fromAccountType = fromAccount.accountType

        val amt = when (type) {
            "monthly", "occasional" -> {
                when (budgetRule.budFrequencyTypeId) {
                    FREQ_WEEKLY -> budgetRule.budgetAmount * 4 / budgetRule.budFrequencyCount
                    FREQ_MONTHLY -> budgetRule.budgetAmount / budgetRule.budFrequencyCount
                    else -> 0.0
                }
            }

            "annual" -> budgetRule.budgetAmount / 12 / budgetRule.budFrequencyCount
            else -> 0.0
        }

        if (toAccountType?.displayAsAsset == true) {
            totalCredits += amt
            if (type == "monthly" && budgetRule.budFixedAmount) totalFixed += amt
        }
        if (fromAccountType?.displayAsAsset == true) {
            totalDebits += amt
            if (type == "monthly" && budgetRule.budFixedAmount) totalFixed -= amt
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
                    text = stringResource(R.string.label_credits_colon) + nf.displayDollars(
                        totalCredits
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.label_debits_colon) + nf.displayDollars(
                        totalDebits
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                if (type == "monthly") {
                    Text(
                        text = stringResource(R.string.label_fixed_colon) + nf.displayDollars(
                            totalFixed
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            val diff = totalCredits - totalDebits
            Text(
                text = if (diff >= 0) stringResource(R.string.label_surplus_of) + nf.displayDollars(
                    diff
                )
                else stringResource(R.string.label_deficit_of) + nf.displayDollars(-diff),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (diff >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}