package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun TransactionAnalysisSummaryCard(
    transactionList: List<TransactionDetailed>,
    sumToAccount: Double?,
    sumFromAccount: Double?,
    sumCredits: Double?,
    maxVal: Double?,
    minVal: Double?,
    effectiveEndDate: String,
    selectedSum: Double = 0.0,
    showSelectedSum: Boolean = false
) {
    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.title_analysis_help),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.action_add)} ${transactionList.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${stringResource(R.string.label_end_date)} ${
                        df.getDisplayDate(
                            effectiveEndDate
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (sumCredits != null) {
                Text(
                    text = "${stringResource(R.string.label_total_credits)} ${
                        nf.displayDollars(
                            sumCredits
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (sumToAccount != null || sumFromAccount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (sumToAccount != null) {
                        Text(
                            text = "${stringResource(R.string.label_to_colon)} ${
                                nf.displayDollars(
                                    sumToAccount
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (sumFromAccount != null) {
                        Text(
                            text = "${stringResource(R.string.label_from_colon)} ${
                                nf.displayDollars(
                                    sumFromAccount
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (maxVal != null || minVal != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (maxVal != null) {
                        Text(
                            text = "${stringResource(R.string.label_highest)} ${
                                nf.displayDollars(
                                    maxVal
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (minVal != null) {
                        Text(
                            text = "${stringResource(R.string.label_lowest)} ${
                                nf.displayDollars(
                                    minVal
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (showSelectedSum) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${stringResource(R.string.label_selected_colon)} ${
                            nf.displayDollars(
                                selectedSum
                            )
                        }",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}