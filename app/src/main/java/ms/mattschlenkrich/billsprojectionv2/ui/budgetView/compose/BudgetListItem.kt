package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalVisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete

@Composable
fun BudgetListItem(
    curRule: BudgetRuleComplete,
    showFullDetails: Boolean = false,
    onRuleClick: (BudgetRuleComplete) -> Unit,
) {
    val df = LocalDateFunctions.current
    val nf = LocalNumberFunctions.current
    val vf = LocalVisualsFunctions.current
    val budgetRule = curRule.budgetRule ?: return
    val toAccount = curRule.toAccount ?: return
    val fromAccount = curRule.fromAccount ?: return
    val toAccountType = toAccount.accountType
    val fromAccountType = fromAccount.accountType

    val isStartZero =
        budgetRule.budStartDate.trim() == "0" || budgetRule.budStartDate.trim() == "0000-00-00" || budgetRule.budStartDate.isBlank()

    var nameInfo = budgetRule.budgetRuleName + " - "
    if (isStartZero) {
        nameInfo += stringResource(R.string.label_not_scheduled)
    } else {
        nameInfo += when (budgetRule.budFrequencyTypeId) {
            FREQ_WEEKLY -> {
                if (showFullDetails) df.getDisplayDateInComingYear(
                    budgetRule.budStartDate,
                    budgetRule.budFrequencyCount.toLong()
                )
                else stringResource(R.string.label_weekly_x) + budgetRule.budFrequencyCount
            }

            FREQ_MONTHLY -> {
                if (showFullDetails) df.getDisplayDateInComingYear(budgetRule.budStartDate)
                else stringResource(R.string.label_monthly)
            }

            else -> ""
        }
    }

    val amt = when (budgetRule.budFrequencyTypeId) {
        FREQ_WEEKLY -> budgetRule.budgetAmount * 4 / budgetRule.budFrequencyCount
        FREQ_MONTHLY -> budgetRule.budgetAmount / budgetRule.budFrequencyCount
        else -> budgetRule.budgetAmount
    }

    val displayAmt =
        if (toAccountType?.displayAsAsset == true && fromAccountType?.displayAsAsset == true) {
            stringResource(R.string.label_na)
        } else nf.displayDollars(if (showFullDetails) budgetRule.budgetAmount else amt)

    val textColor =
        if (fromAccountType?.isAsset == true || fromAccountType?.displayAsAsset == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    val context = androidx.compose.ui.platform.LocalContext.current
    val densityPref = remember {
        ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager(context)
            .getSettings().listDensityPreference
    }
    val verticalPadding =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ListItemDensityHelper.getVerticalPadding(
            densityPref
        )
    val cardSpacing =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ListItemDensityHelper.getCardVerticalSpacing(
            densityPref
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = cardSpacing)
            .clickable { onRuleClick(curRule) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = verticalPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(vf.getRandomColorInt()))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = verticalPadding)
            ) {
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
                            text = stringResource(R.string.label_annually_every) + budgetRule.budFrequencyCount + stringResource(
                                R.string.text_years_suffix
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.label_average_per_month) + nf.displayDollars(
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
                        } + " " + stringResource(R.string.label_average_per_month) + nf.displayDollars(
                            amt
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

object BudgetListItemDefaults {
    const val WIDTH_FACTOR = 6
    const val FULL_DETAILS_WIDTH_FACTOR = 5
}