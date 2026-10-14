package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType

@Composable
fun SurplusDeficitInfo(
    asset: AccountWithType?,
    payDayList: List<String>,
    selectedPayDay: String,
    budgetTotals: BudgetTotals,
) {
    val nf = LocalNumberFunctions.current
    var surplus = budgetTotals.credits - budgetTotals.debits
    if ((asset != null) && payDayList.isNotEmpty() && (selectedPayDay == payDayList[0])) {
        val accountType = asset.accountType
        if (accountType?.keepTotals == true) {
            surplus += asset.account.accountBalance
        } else {
            surplus -= asset.account.accountOwing
        }
    }

    Text(
        text = if (surplus >= 0.0) "${stringResource(R.string.label_surplus_of)}${
            nf.displayDollars(
                surplus
            )
        }"
        else "${stringResource(R.string.label_deficit_of)}${nf.displayDollars(-surplus)}",
        fontWeight = FontWeight.Bold,
        color = if (surplus >= 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp)
    )
}