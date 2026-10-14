package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalVisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.projections.ProjectBudgetDates
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed

@Composable
fun BudgetRuleItem(
    budgetRuleDetailed: BudgetRuleDetailed,
    onClick: () -> Unit
) {
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
    val vf = LocalVisualsFunctions.current
    val nf = LocalNumberFunctions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                text = stringResource(R.string.label_to_colon) + (budgetRuleDetailed.toAccount?.accountName
                    ?: ""),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(R.string.label_from_colon) + (budgetRuleDetailed.fromAccount?.accountName
                    ?: ""),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            val amount = nf.displayDollars(rule.budgetAmount)
            val frequencyTypes = stringArrayResource(R.array.frequency_types)
            val frequencyType = frequencyTypes.getOrElse(rule.budFrequencyTypeId) { "" }
            val daysOfWeek = stringArrayResource(R.array.days_of_week)
            val dayOfWeek = daysOfWeek.getOrElse(rule.budDayOfWeekId) { "" }

            val df = LocalDateFunctions.current
            val projectBudgetDates = remember(frequencyTypes, daysOfWeek) {
                ProjectBudgetDates(
                    frequencyTypes,
                    daysOfWeek
                )
            }
            val nextDateStr = remember(rule) {
                projectBudgetDates.getNextScheduledDate(rule)
            }
            val nextDateDisplay = if (!nextDateStr.isNullOrEmpty()) {
                df.getDisplayDateWithYear(nextDateStr)
            } else {
                ""
            }

            val isStartZero =
                rule.budStartDate.trim() == "0" || rule.budStartDate.trim() == "0000-00-00" || rule.budStartDate.isBlank()
            val isNotScheduled = isStartZero && nextDateDisplay.isEmpty()

            val info = if (isNotScheduled) {
                "$amount - " + stringResource(R.string.label_not_scheduled)
            } else {
                "$amount $frequencyType X ${rule.budFrequencyCount}\nOn $dayOfWeek"
            }

            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (nextDateDisplay.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.label_next_scheduled) + ": " + nextDateDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (isDeleted) {
                Text(
                    text = stringResource(R.string.label_deleted_suffix),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}