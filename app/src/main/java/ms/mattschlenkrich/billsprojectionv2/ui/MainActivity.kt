package ms.mattschlenkrich.billsprojectionv2.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.NavGraphDirections
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.common.sync.NewActivity
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetItemRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.TransactionRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountUpdateViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountsFragment
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleFragment
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragment
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAddFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionSplitFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionUpdateFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionViewFragment
import java.time.LocalDate


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var mainViewModel: MainViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetItemViewModel: BudgetItemViewModel
    lateinit var accountUpdateViewModel: AccountUpdateViewModel

    private val topMenuBarState = mutableStateOf(TopBarState())

    interface MenuHostProxy : MenuHost {
        var title: String
        fun setTitle(titleResId: Int)
    }

    private inner class TopMenuBarProxy : MenuHostProxy {
        override var title: String
            get() = topMenuBarState.value.title
            set(value) {
                topMenuBarState.value = topMenuBarState.value.copy(title = value)
            }

        override fun setTitle(titleResId: Int) {
            title = getString(titleResId)
        }

        override fun addMenuProvider(provider: MenuProvider) {
            (this@MainActivity as MenuHost).addMenuProvider(provider)
        }

        override fun addMenuProvider(provider: MenuProvider, owner: LifecycleOwner) {
            (this@MainActivity as MenuHost).addMenuProvider(provider, owner)
        }

        override fun addMenuProvider(
            provider: MenuProvider,
            owner: LifecycleOwner,
            state: Lifecycle.State
        ) {
            (this@MainActivity as MenuHost).addMenuProvider(provider, owner, state)
        }

        override fun removeMenuProvider(provider: MenuProvider) {
            (this@MainActivity as MenuHost).removeMenuProvider(provider)
        }

        override fun invalidateMenu() {
            (this@MainActivity as MenuHost).invalidateMenu()
        }
    }

    val topMenuBar: MenuHostProxy by lazy { TopMenuBarProxy() }

    data class TopBarState(
        var title: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsManager(this)
        val fontSize = settingsManager.getSettings().fontSize ?: "medium"
        when (fontSize) {
            "small" -> setTheme(R.style.Theme_BillsProjectionV2_Small)
            "large" -> setTheme(R.style.Theme_BillsProjectionV2_Large)
            "extra_large" -> setTheme(R.style.Theme_BillsProjectionV2_ExtraLarge)
            else -> setTheme(R.style.Theme_BillsProjectionV2_Medium)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupViewModels()

        setContent {
            BillsProjectionTheme {
                MainScreen()
            }
        }
    }

    private fun setupViewModels() {
        setupMainViewModel()
        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetItemViewModel()
        setupAccountUpdateViewModel()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var selectedItem by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                MainTopBar(
                    title = topMenuBarState.value.title.ifEmpty { stringResource(R.string.app_name) },
                    onSyncClick = { startActivity(Intent(this, NewActivity::class.java)) },
                    onMenuItemClick = { actionId ->
                        handleMenuAction(actionId)
                    }
                )
            },
            bottomBar = {
                MainBottomBar(
                    selectedItem = selectedItem,
                    onItemSelected = { index, actionId ->
                        selectedItem = index
                        handleNavigation(actionId)
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    factory = { ctx ->
                        FragmentContainerView(ctx).apply {
                            id = R.id.fragment_container_view
                            val navHostFragment =
                                androidx.navigation.fragment.NavHostFragment.create(R.navigation.nav_graph)
                            supportFragmentManager.beginTransaction()
                                .replace(id, navHostFragment)
                                .setPrimaryNavigationFragment(navHostFragment)
                                .commit()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainTopBar(
        title: String,
        onSyncClick: () -> Unit,
        onMenuItemClick: (Int) -> Unit
    ) {
        var showMenu by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text(title) },
            actions = {
                IconButton(onClick = onSyncClick) {
                    Icon(Icons.Default.Sync, contentDescription = stringResource(R.string.sync))
                }
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.update_budget_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_update_predictions); showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.view_current_budget_summary)) },
                        onClick = { onMenuItemClick(R.id.action_view_summary); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_future_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_delete_predictions); showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        onClick = { onMenuItemClick(R.id.action_settings); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.help)) },
                        onClick = { onMenuItemClick(R.id.action_help); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.privacy_policy)) },
                        onClick = { onMenuItemClick(R.id.action_privacy_policy); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("${stringResource(R.string.app_name)} ${BuildConfig.VERSION_NAME}") },
                        onClick = { showMenu = false }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.ic_bills_projection_background),
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }

    @Composable
    fun MainBottomBar(
        selectedItem: Int,
        onItemSelected: (Int, Int) -> Unit
    ) {
        val items = listOf(
            Triple(R.string.budget_view, R.drawable.ic_budget_view, R.id.navigation_budget_view),
            Triple(R.string.transactions, R.drawable.ic_transactions, R.id.navigation_transactions),
            Triple(R.string.accounts, R.drawable.ic_accounts, R.id.navigation_accounts),
            Triple(R.string.analysis, R.drawable.ic_analysis, R.id.navigation_analysis),
            Triple(R.string.budget_rules, R.drawable.ic_budget_rules, R.id.navigation_budget_rules),
        )

        NavigationBar(
            containerColor = Color.White
        ) {
            items.forEachIndexed { index, (labelRes, iconRes, actionId) ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(iconRes),
                            contentDescription = stringResource(labelRes)
                        )
                    },
                    label = { Text(stringResource(labelRes), softWrap = false) },
                    selected = selectedItem == index,
                    onClick = { onItemSelected(index, actionId) }
                )
            }
        }
    }

    private fun handleMenuAction(actionId: Int) {
        when (actionId) {
            R.id.action_update_predictions -> updateBudget()
            R.id.action_view_summary -> gotoBudgetList()
            R.id.action_delete_predictions -> chooseDeleteFuturePredictions()
            R.id.action_settings -> gotoSettings()
            R.id.action_help -> gotoHelp()
            R.id.action_privacy_policy -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = getString(R.string.https_www_mschlenkrich_ca_privacy_policy).toUri()
                }
                startActivity(intent)
            }
        }
    }

    private fun handleNavigation(actionId: Int) {
        when (actionId) {
            R.id.navigation_budget_view -> gotoBudgetView()
            R.id.navigation_accounts -> gotoAccounts()
            R.id.navigation_transactions -> gotoTransactions()
            R.id.navigation_budget_rules -> gotoBudgetRules()
            R.id.navigation_analysis -> gotoAnalysis()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        BillsProjectionTheme {
            Scaffold(
                topBar = {
                    MainTopBar(
                        title = "Bills Projection V2",
                        onSyncClick = {},
                        onMenuItemClick = {}
                    )
                },
                bottomBar = {
                    MainBottomBar(selectedItem = 0, onItemSelected = { _, _ -> })
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Content Area", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    private fun chooseDeleteFuturePredictions() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.warning_confirm_delete))
            setMessage(
                getString(R.string.this_action_should_only_be_done_when_you_do_a_drastic_change)
            )
            setPositiveButton(getString(R.string._continue)) { _, _ ->
                deleteFuturePredictions()
            }
            setNegativeButton(getString(R.string.cancel), null)
            create()
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    private fun gotoHelp() {
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections.actionGlobalHelpFragment()
        )
    }

    private fun gotoSettings() {
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections.actionGlobalSettingsFragment()
        )
    }

    private fun gotoBudgetList() {
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections
                .actionGlobalBudgetListFragment()
        )
    }

    private fun gotoAnalysis() {
        mainViewModel.eraseAll()
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections
                .actionGlobalTransactionAnalysisFragment()
        )
    }

    private fun gotoBudgetView() {
        mainViewModel.eraseAll()
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections
                .actionGlobalBudgetViewFragment()
        )
    }

    private fun gotoBudgetRules() {
        mainViewModel.eraseAll()
        findNavController(R.id.fragment_container_view)
            .navigate(
                NavGraphDirections
                    .actionGlobalBudgetRuleFragment()
            )
    }

    private fun gotoAccounts() {
        mainViewModel.eraseAll()
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections
                .actionGlobalAccountsFragment()
        )
    }

    private fun gotoTransactions() {
        mainViewModel.eraseAll()
        findNavController(R.id.fragment_container_view).navigate(
            NavGraphDirections
                .actionGlobalTransactionViewFragment()
        )
    }

    private fun updateBudget() {
        val stopDateAll = LocalDate.now()
            .plusMonths(4).toString()
            .split("-")
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${month.toString().padStart(2, '0')}-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                doTheUpdate(display)
            },
            stopDateAll[0].toInt(),
            stopDateAll[1].toInt() - 1,
            stopDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.pick_a_date_to_project_forward_to))
        datePickerDialog.show()
    }

    private fun doTheUpdate(
        display: String
    ) {
        val updateBudgetPredictions =
            UpdateBudgetPredictions(this)
        CoroutineScope(Dispatchers.IO).launch {
            updateBudgetPredictions.updatePredictions(display)
        }
    }

    private fun deleteFuturePredictions() {
        val updateBudgetPredictions =
            UpdateBudgetPredictions(this)
        CoroutineScope(Dispatchers.IO).launch {
            updateBudgetPredictions.killPredictions()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        // Reset database instance and re-initialize view models to ensure fresh data after sync
        BillsDatabase.resetInstance()
        setupBudgetItemViewModel()
        setupBudgetRuleViewModel()
        setupAccountViewModel()
        setupTransactionViewModel()
        setupMainViewModel()
        setupAccountUpdateViewModel()

        // Trigger an update for the current fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        navHostFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            when (fragment) {
                is BudgetViewFragment -> {
                    fragment.populateAssets()
                }

                is AccountsFragment -> {
                    // Compose refresh happens automatically if needed
                }

                is BudgetRuleFragment -> {
                    // Compose refresh happens automatically if needed
                }

                is TransactionViewFragment -> {
                    // Compose refresh happens automatically if needed, 
                    // but we can add a manual trigger if state is not in ViewModel
                }

                is TransactionAddFragment -> {
                    fragment.populateValues()
                }

                is TransactionUpdateFragment -> {
                    fragment.populateValues()
                }

                is TransactionSplitFragment -> {
                    fragment.populateValues()
                }
            }
        }
    }

    private fun setupBudgetItemViewModel() {
        val budgetItemRepository = BudgetItemRepository(
            BillsDatabase(this)
        )
        val budgetItemViewModelFactory =
            BudgetItemViewModelFactory(
                application, budgetItemRepository
            )
        budgetItemViewModel = ViewModelProvider(
            this,
            budgetItemViewModelFactory
        )[BudgetItemViewModel::class.java]
    }

    private fun setupBudgetRuleViewModel() {
        val budgetRuleRepository = BudgetRuleRepository(
            BillsDatabase(this)
        )
        val budgetRuleViewModelFactory =
            BudgetRuleViewModelFactory(
                application, budgetRuleRepository
            )
        budgetRuleViewModel = ViewModelProvider(
            this,
            budgetRuleViewModelFactory
        )[BudgetRuleViewModel::class.java]
    }

    private fun setupAccountViewModel() {
        val accountRepository = AccountRepository(
            BillsDatabase(this.applicationContext)
        )
        val accountViewModelFactory =
            AccountViewModelFactory(
                application, accountRepository
            )
        accountViewModel = ViewModelProvider(
            this,
            accountViewModelFactory
        )[AccountViewModel::class.java]
    }

    private fun setupTransactionViewModel() {
        val transactionRepository = TransactionRepository(
            BillsDatabase(this)
        )
        val transactionViewModelFactory =
            TransactionViewModelFactory(
                application, transactionRepository
            )
        transactionViewModel = ViewModelProvider(
            this,
            transactionViewModelFactory
        )[TransactionViewModel::class.java]
    }

    private fun setupMainViewModel() {
        val mainViewModelFactory =
            MainViewModelFactory(application)
        mainViewModel = ViewModelProvider(
            this,
            mainViewModelFactory
        )[MainViewModel::class.java]
    }

    private fun setupAccountUpdateViewModel() {
        val accountUpdateViewModelFactory =
            AccountUpdateViewModelFactory(
                this,
                transactionViewModel,
                accountViewModel,
                application
            )
        accountUpdateViewModel = ViewModelProvider(
            this,
            accountUpdateViewModelFactory
        )[AccountUpdateViewModel::class.java]
    }
}