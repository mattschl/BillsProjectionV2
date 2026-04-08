package ms.mattschlenkrich.billsprojectionv2.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAG_TRANSACTION_VIEW

class TransactionViewFragment : Fragment(),
    SearchView.OnQueryTextListener, MenuProvider {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var accountUpdateViewModel: AccountUpdateViewModel
    private lateinit var budgetRuleViewModel: BudgetRuleViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val vf = VisualsFunctions()

    private val searchQueryState = mutableStateOf("")
    private val refreshKey = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        updateViewModels()
        mainActivity.topMenuBar.title = getString(R.string.view_transaction_history)

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    TransactionViewScreen(refreshKey.intValue)
                }
            }
        }
    }

    private fun updateViewModels() {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        transactionViewModel = mainActivity.transactionViewModel
        accountUpdateViewModel = mainActivity.accountUpdateViewModel
        budgetRuleViewModel = mainActivity.budgetRuleViewModel
    }

    fun refreshData() {
        updateViewModels()
        lifecycleScope.launch {
            delay(100)
            refreshKey.intValue++
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Composable
    fun TransactionViewScreen(refreshKey: Int) {
        val query by searchQueryState
        val transactionList by if (query.isBlank()) {
            transactionViewModel.getActiveTransactionsDetailed()
        } else {
            transactionViewModel.searchActiveTransactionsDetailed("%$query%")
        }.observeAsState(emptyList())

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { gotoAddTransactionFragment() },
                    containerColor = Color(0xFFB00020) // Deep Red
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.image),
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (transactionList.isEmpty()) {
                    EmptyState()
                } else {
                    TransactionList(transactionList)
                }
            }
        }
    }

    @Composable
    fun EmptyState() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions_to_view),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.instructions_transaction_view),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }

    @Composable
    fun TransactionList(transactions: List<TransactionDetailed>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(transactions) { transactionDetailed ->
                TransactionItemView(transactionDetailed)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    @Composable
    fun TransactionItemView(transactionDetailed: TransactionDetailed) {
        val trans = transactionDetailed.transaction!!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chooseOptions(transactionDetailed) }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        Color(vf.getRandomColorInt()),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = trans.transName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = nf.displayDollars(trans.transAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${transactionDetailed.fromAccount?.accountName} → ${transactionDetailed.toAccount?.accountName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = df.getDisplayDate(trans.transDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (trans.transToAccountPending || trans.transFromAccountPending) {
                    Text(
                        text = stringResource(R.string.pending),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (trans.transNote.isNotEmpty()) {
                    Text(
                        text = trans.transNote,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    private fun chooseOptions(transactionDetailed: TransactionDetailed) {
        val context = requireContext()
        var display = ""
        val trans = transactionDetailed.transaction!!
        if (trans.transToAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                trans.transAmount
            ) + getString(R.string._to_) + transactionDetailed.toAccount!!.accountName + " " + getString(
                R.string.pending
            )
        }
        if (display.isNotEmpty() && trans.transFromAccountPending) {
            display += " " + getString(R.string._and) + " "
        }
        if (trans.transFromAccountPending) {
            display += getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                trans.transAmount
            ) + getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName + " " + getString(
                R.string.pending
            )
        }

        val items = mutableListOf(
            getString(R.string.edit_this_transaction),
            display,
            getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
            getString(R.string.delete_this_transaction)
        )

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.choose_an_action_for) + " " + trans.transName)
            .setItems(items.toTypedArray()) { _, pos ->
                when (pos) {
                    0 -> gotoTransactionUpdate(transactionDetailed)
                    1 -> if (trans.transToAccountPending || trans.transFromAccountPending) {
                        completePendingTransactions(transactionDetailed)
                    }

                    2 -> gotoBudgetRuleUpdate(transactionDetailed)
                    3 -> confirmDeleteTransaction(trans)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        val trans = transactionDetailed.transaction!!
        val newTransaction = trans.copy(
            transToAccountPending = false,
            transFromAccountPending = false
        )
        lifecycleScope.launch {
            accountUpdateViewModel.updateTransaction(trans, newTransaction)
        }
    }

    private fun confirmDeleteTransaction(transaction: Transactions) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.are_you_sure_you_want_to_delete) + " " + transaction.transName)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    accountUpdateViewModel.deleteTransaction(transaction)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(TAG)
        mainViewModel.setTransactionDetailed(transactionDetailed)
        lifecycleScope.launch {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transactionDetailed.transaction!!.transId,
                    transactionDetailed.transaction.transToAccountId,
                    transactionDetailed.transaction.transFromAccountId
                )
            }.await()
            mainViewModel.setOldTransaction(oldTransactionFull)
            gotoTransactionUpdateFragment()
        }
    }

    private fun gotoBudgetRuleUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(TAG)
        budgetRuleViewModel.getBudgetRuleFullLive(
            transactionDetailed.transaction!!.transRuleId
        ).observe(viewLifecycleOwner) { bRuleDetailed ->
            if (bRuleDetailed != null) {
                mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
                gotoBudgetRuleUpdateFragment()
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val searchItem = menu.add(Menu.NONE, R.id.action_search, Menu.NONE, R.string.search).apply {
            setIcon(android.R.drawable.ic_menu_search)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        }
        val searchView = SearchView(requireContext())
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)
        searchItem.actionView = searchView
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        searchQueryState.value = newText ?: ""
        return true
    }

    private fun gotoAddTransactionFragment() {
        mainViewModel.addCallingFragment(TAG)
        mainViewModel.setTransactionDetailed(null)
        gotoTransactionAddFragment()
    }

    private fun gotoTransactionAddFragment() {
        findNavController().navigate(
            TransactionViewFragmentDirections.actionTransactionViewFragmentToTransactionAddFragment()
        )
    }

    fun gotoTransactionUpdateFragment() {
        findNavController().navigate(
            TransactionViewFragmentDirections.actionTransactionViewFragmentToTransactionUpdateFragment()
        )
    }

    fun gotoBudgetRuleUpdateFragment() {
        findNavController().navigate(
            TransactionViewFragmentDirections.actionTransactionViewFragmentToBudgetRuleUpdateFragment()
        )
    }
}