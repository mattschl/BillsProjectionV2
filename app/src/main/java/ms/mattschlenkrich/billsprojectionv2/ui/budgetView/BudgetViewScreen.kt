package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.components.BudgetItemDisplay
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun BudgetViewScreen(
    assetList: List<String>,
    selectedAsset: String,
    onAssetSelected: (String) -> Unit,
    payDayList: List<String>,
    selectedPayDay: String,
    onPayDaySelected: (String) -> Unit,
    curAsset: AccountWithType?,
    pendingList: List<TransactionDetailed>,
    pendingAmount: Double,
    budgetList: List<BudgetItemDetailed>,
    onAddClick: () -> Unit,
    onBudgetItemClick: (BudgetItemDetailed) -> Unit,
    onBudgetItemLockClick: (BudgetItemDetailed) -> Unit,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onAccountClick: () -> Unit,
) {
    val nf = NumberFunctions()
    val df = DateFunctions()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(if (isTablet) 8.dp else 4.dp)
        ) {
            SummaryCard(
                assetList = assetList,
                selectedAsset = selectedAsset,
                onAssetSelected = onAssetSelected,
                payDayList = payDayList,
                selectedPayDay = selectedPayDay,
                onPayDaySelected = onPayDaySelected,
                curAsset = curAsset,
                budgetList = budgetList,
                pendingAmount = pendingAmount,
                onAccountClick = onAccountClick,
                nf = nf
            )

            if (pendingList.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.pending_items) + " " + nf.displayDollars(
                        pendingAmount
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    color = if (pendingAmount < 0) Color.Red else Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isTablet) 150.dp else 100.dp)
                ) {
                    items(pendingList) { pending ->
                        PendingItem(
                            pending = pending,
                            selectedAsset = selectedAsset,
                            assetList = assetList,
                            onTransactionClick = onTransactionClick,
                            df = df,
                            nf = nf
                        )
                    }
                }
            }

            if (budgetList.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.budgeted_expenses),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(budgetList) { budgetItem ->
                        BudgetItemDisplay(
                            budgetItemDetailed = budgetItem,
                            isCredit = if (selectedAsset == ALL_ITEMS) {
                                assetList.contains(budgetItem.toAccount?.accountName)
                            } else {
                                budgetItem.toAccount?.accountName == selectedAsset
                            },
                            onClick = { onBudgetItemClick(budgetItem) },
                            onLockClick = { onBudgetItemLockClick(budgetItem) }
                        )
                    }
                }
            } else {
                NoBudgetItemsCard()
            }
        }
    }
}

@Composable
fun SummaryCard(
    assetList: List<String>,
    selectedAsset: String,
    onAssetSelected: (String) -> Unit,
    payDayList: List<String>,
    selectedPayDay: String,
    onPayDaySelected: (String) -> Unit,
    curAsset: AccountWithType?,
    budgetList: List<BudgetItemDetailed>,
    pendingAmount: Double,
    onAccountClick: () -> Unit,
    nf: NumberFunctions,
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = if (asset.accountType!!.keepTotals) {
                        stringResource(R.string.balance_in_account)
                    } else if (asset.account.accountOwing >= 0.0) {
                        stringResource(R.string.balance_owing)
                    } else {
                        stringResource(R.string.credit_of)
                    }

                    val amount = if (asset.accountType!!.keepTotals) {
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
                        color = if (!asset.accountType!!.keepTotals && asset.account.accountOwing >= 0.0) Color.Red else Color.Black,
                        modifier = Modifier.clickable { onAccountClick() }
                    )

                    SurplusDeficitInfo(
                        asset = asset,
                        payDayList = payDayList,
                        selectedAsset = selectedAsset,
                        selectedPayDay = selectedPayDay,
                        budgetList = budgetList,
                        nf = nf
                    )
                }

                if (asset.accountType!!.tallyOwing) {
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
                color = Color.Black
            )

            TotalsSection(
                budgetList = budgetList,
                selectedAsset = selectedAsset,
                assetList = assetList,
                nf = nf
            )
        }
    }
}

@Composable
fun TotalsSection(
    budgetList: List<BudgetItemDetailed>,
    selectedAsset: String,
    assetList: List<String>,
    nf: NumberFunctions,
) {
    var credits = 0.0
    var debits = 0.0
    var fixedExpenses = 0.0
    var otherExpenses = 0.0

    budgetList.forEach { details ->
        val isCredit = if (selectedAsset == ALL_ITEMS) {
            assetList.contains(details.toAccount?.accountName)
        } else {
            details.toAccount?.accountName == selectedAsset
        }

        if (isCredit) {
            credits += details.budgetItem!!.biProjectedAmount
        } else {
            debits += details.budgetItem!!.biProjectedAmount
        }

        val isAssetRelated = if (selectedAsset == ALL_ITEMS) {
            assetList.contains(details.fromAccount?.accountName)
        } else {
            details.fromAccount?.accountName == selectedAsset
        }

        if (isAssetRelated) {
            if (details.budgetItem!!.biIsFixed) {
                fixedExpenses += details.budgetItem!!.biProjectedAmount
            } else {
                otherExpenses += details.budgetItem!!.biProjectedAmount
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (credits > 0.0) stringResource(R.string.credits_) + nf.displayDollars(
                credits
            ) else stringResource(R.string.no_credits),
            color = if (credits > 0.0) Color.Black else Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (debits > 0.0) stringResource(R.string.debits_) + nf.displayDollars(debits) else stringResource(
                R.string.no_debits
            ),
            color = if (debits > 0.0) Color.Red else Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (fixedExpenses > 0.0) stringResource(R.string.fixed_expenses) + nf.displayDollars(
                fixedExpenses
            ) else stringResource(R.string.no_fixed_expenses),
            color = if (fixedExpenses > 0.0) Color.Red else Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (otherExpenses > 0.0) stringResource(R.string.discretionary_) + nf.displayDollars(
                otherExpenses
            ) else stringResource(R.string.no_discretionary_expenses),
            color = if (otherExpenses > 0.0) Color.Blue else Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SurplusDeficitInfo(
    asset: AccountWithType?,
    payDayList: List<String>,
    selectedAsset: String,
    selectedPayDay: String,
    budgetList: List<BudgetItemDetailed>,
    nf: NumberFunctions,
) {
    var credits = 0.0
    var debits = 0.0
    budgetList.forEach { details ->
        if (details.toAccount!!.accountName == selectedAsset) {
            credits += details.budgetItem!!.biProjectedAmount
        } else {
            debits += details.budgetItem!!.biProjectedAmount
        }
    }

    var surplus = credits - debits
    if (asset != null && payDayList.isNotEmpty() && selectedPayDay == payDayList[0]) {
        if (asset.accountType!!.keepTotals) {
            surplus += asset.account.accountBalance
        } else {
            surplus -= asset.account.accountOwing
        }
    }

    Text(
        text = if (surplus >= 0.0) stringResource(R.string.surplus_of) + nf.displayDollars(
            surplus
        )
        else stringResource(R.string.deficit_of) + nf.displayDollars(-surplus),
        fontWeight = FontWeight.Bold,
        color = if (surplus >= 0.0) Color.Black else Color.Red,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp)
    )
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selectedOption,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}