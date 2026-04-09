package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_BUDGET_VIEW

class BudgetViewFragment : Fragment(), RefreshableFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var budgetItemViewModel: BudgetItemViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val vf = VisualsFunctions()

    private var selectedAsset = mutableStateOf("")
    private var selectedPayDay = mutableStateOf("")
    private var curAsset = mutableStateOf<AccountWithType?>(null)
    private val budgetList = mutableStateListOf<BudgetItemDetailed>()
    private val pendingList = mutableStateListOf<TransactionDetailed>()
    private var pendingAmount = mutableDoubleStateOf(0.0)

    private var refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainActivity.topMenuBar.title = getString(R.string.view_the_budget)
        updateViewModels()

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    BudgetViewScreen()
                }
            }
        }
    }

    private fun updateViewModels() {
        mainViewModel = mainActivity.mainViewModel
        accountViewModel = mainActivity.accountViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        budgetItemViewModel = mainActivity.budgetItemViewModel
        transactionViewModel = mainActivity.transactionViewModel
    }

    @Preview
    @Composable
    fun BudgetViewScreen() {
        val configuration = LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        val currentRefreshKey = refreshKey.intValue

        val assetList by remember(currentRefreshKey) {
            if (::budgetItemViewModel.isInitialized) {
                budgetItemViewModel.getAssetsForBudget()
            } else {
                MutableLiveData(emptyList())
            }
        }.observeAsState(emptyList())

        LaunchedEffect(assetList, currentRefreshKey) {
            if (::mainViewModel.isInitialized &&
                selectedAsset.value.isEmpty() && assetList.isNotEmpty()
            ) {
                val returnAsset = mainViewModel.getReturnToAsset()
                if (returnAsset != null && assetList.contains(returnAsset)) {
                    selectedAsset.value = returnAsset
                } else {
                    selectedAsset.value = assetList[0]
                }
            }
        }

        val curAssetDetail by remember(selectedAsset.value, currentRefreshKey) {
            if (::accountViewModel.isInitialized && selectedAsset.value.isNotEmpty()) {
                accountViewModel.getAccountDetailed(selectedAsset.value)
            } else {
                MutableLiveData(null)
            }
        }.observeAsState()

        LaunchedEffect(curAssetDetail, currentRefreshKey) {
            curAsset.value = curAssetDetail
        }

        LaunchedEffect(selectedAsset.value, currentRefreshKey) {
            if (::mainViewModel.isInitialized && selectedAsset.value.isNotEmpty()) {
                mainViewModel.setReturnToAsset(selectedAsset.value)
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onAddButtonPress() },
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
                SummaryCard(assetList, currentRefreshKey)

                if (pendingList.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.pending_lined) + " " + nf.displayDollars(
                            pendingAmount.doubleValue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center,
                        color = if (pendingAmount.doubleValue < 0) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isTablet) 150.dp else 100.dp)
                    ) {
                        items(pendingList) { pending ->
                            PendingItem(pending)
                        }
                    }
                }

                if (budgetList.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.budgeted_lined),
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
                            BudgetItemRow(budgetItem)
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
        currentRefreshKey: Int,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(2.dp)) {
                DropdownSelector(
                    label = stringResource(R.string.asset_account),
                    options = assetList,
                    selectedOption = selectedAsset.value,
                    onOptionSelected = {
                        selectedAsset.value = it
                    },
                )

                val payDayList by remember(selectedAsset.value, currentRefreshKey) {
                    if (::budgetItemViewModel.isInitialized) {
                        budgetItemViewModel.getPayDays(selectedAsset.value)
                    } else {
                        MutableLiveData(emptyList())
                    }
                }.observeAsState(emptyList())

                LaunchedEffect(payDayList, currentRefreshKey) {
                    if (payDayList.isNotEmpty()) {
                        val returnPayDay = if (::mainViewModel.isInitialized) {
                            mainViewModel.getReturnToPayDay()
                        } else null
                        if (returnPayDay != null && payDayList.any {
                                it == returnPayDay.replace(
                                    getString(R.string.__current),
                                    ""
                                )
                            }) {
                            selectedPayDay.value =
                                returnPayDay.replace(getString(R.string.__current), "")
                        } else {
                            selectedPayDay.value = payDayList[0]
                        }
                    } else {
                        selectedPayDay.value = ""
                    }
                }

                if (payDayList.isNotEmpty()) {
                    DropdownSelector(
                        label = stringResource(R.string.pay_day),
                        options = payDayList.mapIndexed { index, s ->
                            if (index == 0) "$s${
                                getString(
                                    R.string.__current
                                )
                            }" else s
                        },
                        selectedOption = if (payDayList.indexOf(selectedPayDay.value) == 0) "${selectedPayDay.value}${
                            getString(
                                R.string.__current
                            )
                        }" else selectedPayDay.value,
                        onOptionSelected = {
                            selectedPayDay.value = it.replace(getString(R.string.__current), "")
                            if (::mainViewModel.isInitialized) {
                                mainViewModel.setReturnToPayDay(it)
                            }
                        },
                    )
                }

                val pendingTransactions by remember(selectedAsset.value, currentRefreshKey) {
                    if (::transactionViewModel.isInitialized && selectedAsset.value.isNotEmpty()) {
                        transactionViewModel.getPendingTransactionsDetailed(selectedAsset.value)
                    } else {
                        MutableLiveData(emptyList())
                    }
                }.observeAsState(emptyList())

                val currentBudgetItems by remember(
                    selectedAsset.value,
                    selectedPayDay.value,
                    currentRefreshKey
                ) {
                    if (::budgetItemViewModel.isInitialized && selectedAsset.value.isNotEmpty()) {
                        budgetItemViewModel.getBudgetItems(
                            selectedAsset.value,
                            selectedPayDay.value
                        )
                    } else {
                        MutableLiveData(emptyList())
                    }
                }.observeAsState(emptyList())

                LaunchedEffect(pendingTransactions, currentRefreshKey) {
                    pendingList.clear()
                    pendingList.addAll(pendingTransactions)
                    updatePendingTotal(pendingTransactions)
                }

                LaunchedEffect(currentBudgetItems, currentRefreshKey) {
                    budgetList.clear()
                    budgetList.addAll(currentBudgetItems)
                }

                curAsset.value?.let { asset ->
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
                            modifier = Modifier.clickable { gotoAccount() }
                        )

                        Text(
                            text = nf.displayDollars(amount),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!asset.accountType!!.keepTotals && asset.account.accountOwing >= 0.0) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Black,
                            modifier = Modifier.clickable { gotoAccount() }
                        )

                        SurplusDeficitInfo(asset, payDayList)
                    }

                    if (asset.accountType!!.tallyOwing) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val creditLimit = asset.account.accountCreditLimit
                            val available =
                                creditLimit + pendingAmount.doubleValue - asset.account.accountOwing
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
                    color = androidx.compose.ui.graphics.Color.Black
                )

                TotalsSection()
            }
        }
    }

    @Composable
    fun TotalsSection() {
        var credits = 0.0
        var debits = 0.0
        var fixedExpenses = 0.0
        var otherExpenses = 0.0

        budgetList.forEach { details ->
            if (details.toAccount!!.accountName == selectedAsset.value) {
                credits += details.budgetItem!!.biProjectedAmount
            } else {
                debits += details.budgetItem!!.biProjectedAmount
            }
            if (details.fromAccount!!.accountName == selectedAsset.value) {
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
                color = if (credits > 0.0) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (debits > 0.0) stringResource(R.string.debits_) + nf.displayDollars(debits) else stringResource(
                    R.string.no_debits
                ),
                color = if (debits > 0.0) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = if (fixedExpenses > 0.0) stringResource(R.string.fixed_expenses) + nf.displayDollars(
                    fixedExpenses
                ) else stringResource(R.string.no_fixed_expenses),
                color = if (fixedExpenses > 0.0) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (otherExpenses > 0.0) stringResource(R.string.discretionary_) + nf.displayDollars(
                    otherExpenses
                ) else stringResource(R.string.no_discretionary_expenses),
                color = if (otherExpenses > 0.0) androidx.compose.ui.graphics.Color.Blue else androidx.compose.ui.graphics.Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    @Composable
    fun SurplusDeficitInfo(
        asset: AccountWithType?,
        payDayList: List<String>,
    ) {
        var credits = 0.0
        var debits = 0.0
        budgetList.forEach { details ->
            if (details.toAccount!!.accountName == selectedAsset.value) {
                credits += details.budgetItem!!.biProjectedAmount
            } else {
                debits += details.budgetItem!!.biProjectedAmount
            }
        }

        var surplus = credits - debits
        if (asset != null && payDayList.isNotEmpty() && selectedPayDay.value == payDayList[0]) {
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
            color = if (surplus >= 0.0) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Red,
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

    @Composable
    fun PendingItem(pending: TransactionDetailed) {
        val color = remember { androidx.compose.ui.graphics.Color(vf.getRandomColorInt()) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptionsForTransaction(pending) }
                .padding(vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 5.dp)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = df.getDisplayDate(pending.transaction!!.transDate),
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.bodySmall
            )
            val isCredit = pending.toAccount!!.accountName == selectedAsset.value
            Text(
                text = nf.displayDollars(pending.transaction.transAmount),
                modifier = Modifier.width(90.dp),
                fontWeight = FontWeight.Bold,
                color = if (isCredit) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = pending.transaction.transName + if (pending.transaction.transNote.isNotBlank()) " - ${pending.transaction.transNote}" else "",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = if (isCredit) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Red,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }

    @Composable
    fun BudgetItemRow(
        budgetItem: BudgetItemDetailed,
    ) {
        val color = remember { androidx.compose.ui.graphics.Color(vf.getRandomColorInt()) }
        val isCredit = budgetItem.toAccount!!.accountName == selectedAsset.value

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptionsForBudget(budgetItem) }
                .padding(vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = df.getDisplayDate(budgetItem.budgetItem!!.biActualDate),
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budgetItem.budgetItem!!.biBudgetName + if (budgetItem.budgetItem!!.biIsFixed) getString(
                            R.string._fixed_
                        ) else "",
                        fontWeight = FontWeight.Bold,
                        color = if (budgetItem.budgetItem!!.biIsFixed) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.to_) + budgetItem.toAccount!!.accountName,
                            modifier = Modifier.weight(1f),
                            color = androidx.compose.ui.graphics.Color.Blue,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.from_) + budgetItem.fromAccount!!.accountName,
                            modifier = Modifier.weight(1f),
                            color = androidx.compose.ui.graphics.Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }
                Text(
                    text = nf.displayDollars(budgetItem.budgetItem!!.biProjectedAmount),
                    modifier = Modifier.width(85.dp),
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Red,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall
                )
                Image(
                    painter = painterResource(id = if (budgetItem.budgetItem!!.biLocked) R.drawable.ic_liocked_foreground else R.drawable.ic_unlocked_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(24.dp)
                        .clickable { chooseLockUnlock(budgetItem) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color)
                    .padding(horizontal = 8.dp)
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun NoBudgetItemsCardPreview() {
        BillsProjectionTheme {
            NoBudgetItemsCard()
        }
    }

    @Composable
    fun NoBudgetItemsCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.no_budget_items),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.instructions_budget_view),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    private fun updatePendingTotal(transactions: List<TransactionDetailed>) {
        var total = 0.0
        for (item in transactions) {
            if (item.transaction!!.transToAccountPending) {
                total += item.transaction.transAmount
            } else {
                total -= item.transaction.transAmount
            }
        }
        pendingAmount.doubleValue = total
    }

    private fun onAddButtonPress() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_an_action)).setItems(
                arrayOf(
                    getString(R.string.schedule_a_new_budget_item),
                    getString(R.string.add_an_unscheduled_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> addNewBudgetItem()
                    1 -> addNewTransaction()
                }
            }.show()
    }

    private fun chooseLockUnlock(budgetItemDetailed: BudgetItemDetailed) {
        val budgetItem = budgetItemDetailed.budgetItem!!
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.lock_or_unlock)).setItems(
                arrayOf(
                    getString(R.string.lock) + budgetItem.biBudgetName,
                    getString(R.string.un_lock) + budgetItem.biBudgetName,
                    getString(R.string.lock_all_items_for_this_payday),
                    getString(R.string.un_lock_all_items_for_this_payday)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> budgetItemViewModel.lockUnlockBudgetItem(
                        true,
                        budgetItem.biRuleId,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    1 -> budgetItemViewModel.lockUnlockBudgetItem(
                        false,
                        budgetItem.biRuleId,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    2 -> budgetItemViewModel.lockUnlockBudgetItem(
                        true,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )

                    3 -> budgetItemViewModel.lockUnlockBudgetItem(
                        false,
                        budgetItem.biPayDay,
                        df.getCurrentTimeAsString()
                    )
                }
            }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun chooseOptionsForBudget(curBudgetDetailed: BudgetItemDetailed) {
        val curBudget = curBudgetDetailed.budgetItem!!
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + curBudget.biBudgetName
        ).setItems(
            arrayOf(
                getString(R.string.perform_a_transaction_on_) + " \"${curBudget.biBudgetName}\" ",
                if (curBudget.biProjectedAmount == 0.0) "" else getString(R.string.perform_action) + "\"${curBudget.biBudgetName}\" " + getString(
                    R.string.for_amount_of_the_full_amount_
                ) + nf.displayDollars(curBudget.biProjectedAmount),
                getString(R.string.adjust_the_projections_for_this_item),
                getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                getString(R.string.cancel_this_projected_item),
            )
        ) { _, pos ->
            when (pos) {
                0 -> performTransaction(curBudgetDetailed)
                1 -> confirmTransaction(curBudgetDetailed)
                2 -> gotoBudgetItem(curBudgetDetailed)
                3 -> gotoBudgetRule(curBudgetDetailed)
                4 -> confirmCancelBudgetItem(curBudgetDetailed)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun chooseOptionsForTransaction(pendingTransaction: TransactionDetailed) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_an_action_for) + nf.displayDollars(pendingTransaction.transaction!!.transAmount) + getString(
                R.string._to_
            ) + pendingTransaction.transaction.transName
        ).setItems(
            arrayOf(
                getString(R.string.complete_this_pending_transaction),
                getString(R.string.open_the_transaction_to_edit_it),
                getString(R.string.delete_this_pending_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> confirmPendingTransaction(pendingTransaction)
                1 -> editTransaction(pendingTransaction)
                2 -> deleteTransaction(pendingTransaction)
            }
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun confirmPendingTransaction(pendingTransaction: TransactionDetailed) {
        val display =
            getString(R.string.this_will_apply_the_amount_of) + nf.displayDollars(pendingTransaction.transaction!!.transAmount) + (if (pendingTransaction.transaction.transToAccountPending) getString(
                R.string._to_
            ) else getString(R.string._From_)) + selectedAsset.value
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                completePendingTransaction(
                    pendingTransaction
                )
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun completePendingTransaction(transaction: TransactionDetailed) {
        lifecycleScope.launch {
            val trans = transaction.transaction!!
            if (transaction.toAccount!!.accountName == selectedAsset.value) {
                transactionViewModel.updateTransaction(
                    trans.copy(
                        transToAccountPending = false,
                        transUpdateTime = df.getCurrentTimeAsString()
                    )
                )
                curAsset.value?.let { asset ->
                    if (asset.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            asset.account.accountBalance + trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (asset.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            asset.account.accountOwing - trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                }
            } else {
                transactionViewModel.updateTransaction(
                    trans.copy(
                        transFromAccountPending = false,
                        transUpdateTime = df.getCurrentTimeAsString()
                    )
                )
                curAsset.value?.let { asset ->
                    if (asset.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            asset.account.accountBalance - trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (asset.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            asset.account.accountOwing + trans.transAmount,
                            asset.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                }
            }
            delay(WAIT_500)
        }
    }

    private fun editTransaction(transaction: TransactionDetailed) {
        setReturnVariables()
        mainViewModel.setTransactionDetailed(transaction)
        lifecycleScope.launch {
            val trans = transaction.transaction!!
            val transactionFull = transactionViewModel.getTransactionFull(
                trans.transId,
                trans.transToAccountId,
                trans.transFromAccountId
            )
            mainViewModel.setOldTransaction(transactionFull)
            delay(WAIT_250)
            gotoTransactionUpdateFragment()
        }
    }

    private fun deleteTransaction(transaction: TransactionDetailed) {
        transactionViewModel.deleteTransaction(
            transaction.transaction!!.transId,
            df.getCurrentTimeAsString()
        )
    }

    private fun performTransaction(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetItemDetailed(curBudget)
        mainViewModel.setTransactionDetailed(null)
        setReturnVariables()
        gotoTransactionPerformFragment()
    }

    private fun confirmTransaction(curBudgetDetailed: BudgetItemDetailed) {
        val curBudget = curBudgetDetailed.budgetItem!!
        if (curBudget.biProjectedAmount > 0.0) {
            lifecycleScope.launch {
                val display = getConfirmationsDisplay(curBudget, curBudgetDetailed)
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_completing_transaction))
                    .setMessage(display)
                    .setPositiveButton(getString(R.string.perform_action)) { _, _ ->
                        completeBudgetTransaction(
                            curBudgetDetailed
                        )
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }
        }
    }

    private suspend fun getConfirmationsDisplay(
        budgetItem: BudgetItem,
        curBudgetDetailed: BudgetItemDetailed
    ): String {
        var display =
            getString(R.string.this_will_perform) + budgetItem.biBudgetName + getString(R.string.applying_the_amount_of) + nf.displayDollars(
                budgetItem.biProjectedAmount
            ) + getString(R.string.from) + curBudgetDetailed.fromAccount!!.accountName
        display += if (accountUpdateViewModel.isTransactionPending(budgetItem.biFromAccountId)) getString(
            R.string._pending
        ) else ""
        delay(WAIT_100)
        display += getString(R.string._to) + curBudgetDetailed.toAccount!!.accountName
        display += if (accountUpdateViewModel.isTransactionPending(budgetItem.biToAccountId)) getString(
            R.string._pending
        ) else ""
        return display
    }

    private fun completeBudgetTransaction(curBudgetDetailed: BudgetItemDetailed) {
        val budgetItem = curBudgetDetailed.budgetItem!!
        lifecycleScope.launch {
            val toPending = accountUpdateViewModel.isTransactionPending(budgetItem.biToAccountId)
            val fromPending =
                accountUpdateViewModel.isTransactionPending(budgetItem.biFromAccountId)
            accountUpdateViewModel.performTransaction(
                Transactions(
                    nf.generateId(),
                    df.getCurrentDateAsString(),
                    budgetItem.biBudgetName,
                    "",
                    budgetItem.biRuleId,
                    budgetItem.biToAccountId,
                    toPending,
                    budgetItem.biFromAccountId,
                    fromPending,
                    budgetItem.biProjectedAmount,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            budgetItemViewModel.updateBudgetItem(
                budgetItem.copy(
                    biActualDate = df.getCurrentDateAsString(),
                    biProjectedAmount = 0.0,
                    biIsCompleted = true,
                    biUpdateTime = df.getCurrentTimeAsString()
                )
            )
            delay(WAIT_500)
        }
    }

    private fun confirmCancelBudgetItem(curBudgetDetailed: BudgetItemDetailed) {
        val budgetItem = curBudgetDetailed.budgetItem!!
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_cancelling_budget_item)).setMessage(
                getString(R.string.this_will_cancel) + budgetItem.biBudgetName + getString(R.string.with_the_amount_of) + nf.displayDollars(
                    budgetItem.biProjectedAmount
                ) + getString(R.string._remaining)
            ).setPositiveButton(getString(R.string.cancel_now)) { _, _ ->
                budgetItemViewModel.cancelBudgetItem(
                    budgetItem.biRuleId,
                    budgetItem.biProjectedDate,
                    df.getCurrentTimeAsString()
                )
            }.setNegativeButton(getString(R.string.ignore_this), null).show()
    }

    private fun gotoBudgetItem(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetItemDetailed(curBudget)
        setReturnVariables()
        gotoBudgetItemUpdateFragment()
    }

    private fun gotoBudgetRule(curBudget: BudgetItemDetailed) {
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curBudget.budgetRule,
                curBudget.toAccount,
                curBudget.fromAccount
            )
        )
        setReturnVariables()
        gotoBudgetRuleUpdateFragment()
    }

    private fun addNewTransaction() {
        setReturnVariables()
        mainViewModel.setTransactionDetailed(null)
        gotoTransactionAddFragment()
    }

    private fun addNewBudgetItem() {
        setReturnVariables()
        gotoBudgetItemAddFragment()
    }

    private fun setReturnVariables() {
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setReturnToAsset(selectedAsset.value)
        mainViewModel.setReturnToPayDay(selectedPayDay.value)
    }

    private fun gotoAccount() {
        setReturnVariables()
        mainViewModel.setAccountWithType(curAsset.value)
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToAccountUpdateFragment())
    }

    fun gotoBudgetItemUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemUpdateFragment())
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetRuleUpdateFragment())
    }

    private fun gotoTransactionAddFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionAddFragment())
    }

    fun gotoTransactionPerformFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionPerformFragment())
    }

    private fun gotoBudgetItemAddFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToBudgetItemAddFragment())
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(BudgetViewFragmentDirections.actionBudgetViewFragmentToTransactionUpdateFragment())
    }

    override fun refreshData() {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.budget_view)
        refreshKey.intValue++
    }
}