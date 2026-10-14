package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.DropdownSelector
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import kotlin.math.abs

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
    selectedSum: Double = 0.0,
    showSelectedSum: Boolean = false,
    earliestPayDay: String? = null,
) {
    val nf = LocalNumberFunctions.current
    val currentTag = stringResource(R.string.text_current_suffix)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            DropdownSelector(
                label = stringResource(R.string.label_asset_account),
                options = assetList,
                selectedOption = selectedAsset,
                onOptionSelected = onAssetSelected,
            )

            if (payDayList.isNotEmpty()) {
                DropdownSelector(
                    label = stringResource(R.string.label_pay_day),
                    options = payDayList.map { s ->
                        if ((earliestPayDay != null) && (s == earliestPayDay)) "$s$currentTag" else s
                    },
                    selectedOption = if ((earliestPayDay != null) && (selectedPayDay == earliestPayDay)) "$selectedPayDay$currentTag" else selectedPayDay,
                ) { selected ->
                    onPayDaySelected(selected.replace(currentTag, ""))
                }
            }

            curAsset?.let { asset ->
                val accountType = asset.accountType
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val label = if (accountType?.keepTotals == true) {
                        stringResource(R.string.label_balance_in_account)
                    } else if (asset.account.accountOwing >= 0.0) {
                        stringResource(R.string.label_balance_owing)
                    } else {
                        stringResource(R.string.label_credit_of)
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
                        modifier = Modifier.clickable { onAccountClick() },
                    )

                    Text(
                        text = nf.displayDollars(amount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if ((accountType?.keepTotals != true) && (asset.account.accountOwing >= 0.0)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onAccountClick() },
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val creditLimit = asset.account.accountCreditLimit
                        val available = (creditLimit + pendingAmount) - asset.account.accountOwing
                        val availableReal =
                            if (available > creditLimit) creditLimit else available

                        Text(
                            text = stringResource(R.string.label_available_credit),
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

            if (showSelectedSum) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(R.string.label_selected_colon)} ${
                            nf.displayDollars(
                                abs(selectedSum)
                            )
                        }",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    curAsset?.let { asset ->
                        val isAsset = asset.accountType?.keepTotals == true
                        val label = if (isAsset) stringResource(R.string.label_remainder)
                        else stringResource(R.string.label_owing)
                        val projected = if (isAsset) asset.account.accountBalance + selectedSum
                        else asset.account.accountOwing - selectedSum

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$label: ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = nf.displayDollars(projected),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!isAsset && (projected > 0)) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
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