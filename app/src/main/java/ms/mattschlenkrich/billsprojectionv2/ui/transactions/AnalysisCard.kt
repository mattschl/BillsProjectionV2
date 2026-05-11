package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.AnalysisMode
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun AnalysisCard(
    mode: AnalysisMode,
    transactionList: List<TransactionDetailed>,
    sumToAccount: Double?,
    sumFromAccount: Double?,
    sumCredits: Double?,
    maxVal: Double?,
    minVal: Double?,
    effectiveEndDate: String,
    nf: NumberFunctions,
    df: DateFunctions
) {
    if (mode == AnalysisMode.NONE) return

    val analysisData = remember(transactionList, effectiveEndDate) {
        var totalAmount = 0.0
        transactionList.forEach { totalAmount += it.transaction?.transAmount ?: 0.0 }

        val monthsCount = if (transactionList.isNotEmpty()) {
            val startDateStr = transactionList.last().transaction?.transDate ?: effectiveEndDate
            df.getMonthsBetween(startDateStr, effectiveEndDate) + 1
        } else 1
        Pair(totalAmount, monthsCount)
    }

    val totals = analysisData.first
    val months = analysisData.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (mode == AnalysisMode.ACCOUNT) {
                AnalysisRow(
                    label1 = stringResource(R.string.credit_average),
                    value1 = nf.displayDollars((sumToAccount ?: 0.0) / months) + " / $months",
                    label2 = stringResource(R.string.debit_average),
                    value2 = nf.displayDollars((sumFromAccount ?: 0.0) / months),
                    value2Color = MaterialTheme.colorScheme.error
                )
            } else {
                AnalysisRow(
                    label1 = stringResource(R.string.average),
                    value1 = nf.displayDollars(totals / months) + " / $months",
                    label2 = stringResource(R.string.highest),
                    value2 = nf.displayDollars(maxVal ?: 0.0)
                )
            }

            AnalysisRow(
                label1 = stringResource(R.string.lowest),
                value1 = nf.displayDollars(minVal ?: 0.0),
                label2 = stringResource(R.string.most_recent),
                value2 = nf.displayDollars(
                    transactionList.firstOrNull()?.transaction?.transAmount ?: 0.0
                )
            )

            if (mode == AnalysisMode.ACCOUNT) {
                AnalysisRow(
                    label1 = stringResource(R.string.total_credits),
                    value1 = nf.displayDollars(sumToAccount ?: 0.0),
                    label2 = stringResource(R.string.total_debits),
                    value2 = nf.displayDollars(sumFromAccount ?: 0.0),
                    value2Color = MaterialTheme.colorScheme.error
                )
            } else {
                AnalysisRow(
                    label1 = "Total (${transactionList.size})",
                    value1 = nf.displayDollars(sumCredits ?: 0.0),
                    label2 = "",
                    value2 = ""
                )
            }
        }
    }
}