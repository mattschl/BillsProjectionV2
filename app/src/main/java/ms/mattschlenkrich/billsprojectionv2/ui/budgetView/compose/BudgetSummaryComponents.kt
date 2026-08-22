package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.DropdownSelector
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType

@Composable
fun SummaryCard(
    assetList: List<String>,
    selectedAsset: String,
    onAssetSelected: (String) -> Unit,
    payDayList: List<String>,
    selectedPayDay: String,
    onPayDaySelected: (String) -> Unit,
    curAsset: AccountWithType?,
    budgetTotals: BudgetTotals,
    pendingAmount: Double,
    onAccountClick: () -> Unit,
) {
    val nf = LocalNumberFunctions.current
    val currentTag = stringResource(R.string.__current)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            DropdownSelector(
                label = stringResource(R.string.asset_account),
                options = assetList,
                selectedOption = selectedAsset,
                onOptionSelected = onAssetSelected,
            )

            if (payDayList.isNotEmpty()) {
                DropdownSelector(
                    label = stringResource(R.string.pay_day),
                    options = payDayList.mapIndexed { index, s ->
                        if (index == 0) "$s$currentTag" else s
                    },
                    selectedOption = if (payDayList.indexOf(selectedPayDay) == 0) "$selectedPayDay$currentTag" else selectedPayDay,
                    onOptionSelected = { selected ->
                        onPayDaySelected(selected.replace(currentTag, ""))
                    },
                )
            }

            curAsset?.let { asset ->
                val accountType = asset.accountType
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = if (accountType?.keepTotals == true) {
                        stringResource(R.string.balance_in_account)
                    } else if (asset.account.accountOwing >= 0.0) {
                        stringResource(R.string.balance_owing)
                    } else {
                        stringResource(R.string.credit_of)
                    }

                    val amount = if (accountType?.keepTotals == true) {
                        asset.account.accountBalance
                    } else if (asset.account.accountOwing >= 0.0) {
                        asset.account.accountOwing
                    } else {
                        -asset.account.accountOwing
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { onAccountClick() }
                    )

                    Text(
                        text = nf.displayDollars(amount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (accountType?.keepTotals != true && asset.account.accountOwing >= 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onAccountClick() }
                    )

                    SurplusDeficitInfo(
                        asset = asset,
                        payDayList = payDayList,
                        selectedPayDay = selectedPayDay,
                        budgetTotals = budgetTotals,
                    )
                }

                if (accountType?.tallyOwing == true) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val creditLimit = asset.account.accountCreditLimit
                        val available =
                            creditLimit + pendingAmount - asset.account.accountOwing
                        val availableReal =
                            if (available > creditLimit) creditLimit else available

                        Text(
                            text = stringResource(R.string.available_credit),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = nf.displayDollars(availableReal),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 1.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )

            TotalsSection(
                budgetTotals = budgetTotals,
            )
        }
    }
}

@Composable
fun TotalsSection(
    budgetTotals: BudgetTotals,
) {
    val nf = LocalNumberFunctions.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (budgetTotals.credits > 0.0) "${stringResource(R.string.credits_)}${
                nf.displayDollars(
                    budgetTotals.credits
                )
            }" else stringResource(R.string.no_credits),
            color = if (budgetTotals.credits > 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (budgetTotals.debits > 0.0) "${stringResource(R.string.debits_)}${
                nf.displayDollars(
                    budgetTotals.debits
                )
            }" else stringResource(R.string.no_debits),
            color = if (budgetTotals.debits > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (budgetTotals.fixedExpenses > 0.0) "${stringResource(R.string.fixed_expenses)}${
                nf.displayDollars(
                    budgetTotals.fixedExpenses
                )
            }" else stringResource(R.string.no_fixed_expenses),
            color = if (budgetTotals.fixedExpenses > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (budgetTotals.otherExpenses > 0.0) "${stringResource(R.string.discretionary_)}${
                nf.displayDollars(
                    budgetTotals.otherExpenses
                )
            }" else stringResource(R.string.no_discretionary_expenses),
            color = if (budgetTotals.otherExpenses > 0.0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SurplusDeficitInfo(
    asset: AccountWithType?,
    payDayList: List<String>,
    selectedPayDay: String,
    budgetTotals: BudgetTotals,
) {
    val nf = LocalNumberFunctions.current
    var surplus = budgetTotals.credits - budgetTotals.debits
    if (asset != null && payDayList.isNotEmpty() && selectedPayDay == payDayList[0]) {
        val accountType = asset.accountType
        if (accountType?.keepTotals == true) {
            surplus += asset.account.accountBalance
        } else {
            surplus -= asset.account.accountOwing
        }
    }

    Text(
        text = if (surplus >= 0.0) "${stringResource(R.string.surplus_of)}${
            nf.displayDollars(
                surplus
            )
        }"
        else "${stringResource(R.string.deficit_of)}${nf.displayDollars(-surplus)}",
        fontWeight = FontWeight.Bold,
        color = if (surplus >= 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp)
    )
}