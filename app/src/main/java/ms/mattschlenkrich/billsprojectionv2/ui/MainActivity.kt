package ms.mattschlenkrich.billsprojectionv2.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults
import ms.mattschlenkrich.billsprojectionv2.common.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.common.sync.SyncActivity
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
import ms.mattschlenkrich.billsprojectionv2.ui.auth.LoginScreen
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.NavGraph
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme
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

    interface MenuHostProxy {
        var title: String
        fun setTitle(titleResId: Int)
    }

    inner class TopMenuBarProxy : MenuHostProxy {
        override var title: String
            get() = topMenuBarState.value.title
            set(value) {
                topMenuBarState.value = topMenuBarState.value.copy(title = value)
            }

        override fun setTitle(titleResId: Int) {
            title = getString(titleResId)
        }
    }

    val topMenuBar: MenuHostProxy by lazy { TopMenuBarProxy() }

    data class TopBarState(val title: String = "")

    private var isUpdating = mutableStateOf(false)
    private var isAuthenticated = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setTheme(R.style.Theme_BillsProjectionV2)
        super.onCreate(savedInstanceState)

        setupViewModels()

        val settingsManager = SettingsManager(this)
        val settings = settingsManager.getSettings()
        val isFirstRun = settings.isFirstRun
        if (isFirstRun) {
            settingsManager.saveSettings(settings.copy(isFirstRun = false))
        }

        isAuthenticated.value = !settings.usePasswordProtection || settings.passwordHash == null

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val s = SettingsManager(this).getSettings()
                if (s.usePasswordProtection && s.passwordHash != null) {
                    isAuthenticated.value = false
                }
            }
        })

        setContent {
            val s = remember { SettingsManager(this).getSettings() }
            val fontScale = when (s.fontSize) {
                "small" -> 0.8f
                "large" -> 1.2f
                "extra_large" -> 1.5f
                else -> 1.0f
            }
            BillsProjectionTheme(fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated.value) {
                        MainScreen(isFirstRun)
                    } else {
                        LoginScreen(
                            passwordHash = s.passwordHash!!,
                            onAuthenticated = { isAuthenticated.value = true },
                            onPasswordChanged = { newHash ->
                                val currentSettings = settingsManager.getSettings()
                                settingsManager.saveSettings(currentSettings.copy(passwordHash = newHash))
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setupViewModels(clearExisting: Boolean = false) {
        if (clearExisting) {
            BillsDatabase.resetInstance()
        }
        setupMainViewModel()
        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetItemViewModel()
        setupAccountUpdateViewModel()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(isFirstRun: Boolean) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val pagerState = rememberPagerState(pageCount = { 5 })
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            if (isFirstRun) {
                navController.navigate(Screen.Help.route)
            }
        }

        val isTopLevel = currentRoute == Screen.MainPager.route || currentRoute in listOf(
            Screen.BudgetView.route,
            Screen.Transactions.route,
            Screen.Accounts.route,
            Screen.Analysis.route,
            Screen.BudgetRules.route
        )

        val syncLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                mainViewModel.eraseAll()
                setupViewModels(clearExisting = true)
                navController.navigate(Screen.MainPager.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                MainTopBar(
                    title = topMenuBarState.value.title.ifEmpty { stringResource(R.string.app_name) },
                    showBackButton = !isTopLevel,
                    onBackClick = { navController.popBackStack() },
                    onSyncClick = { syncLauncher.launch(Intent(this, SyncActivity::class.java)) },
                    onMenuItemClick = { actionId ->
                        handleMenuAction(actionId, navController)
                    }
                )
            },
            bottomBar = {
                if (isTopLevel) {
                    MainBottomBar(
                        pagerState = pagerState,
                        currentRoute = currentRoute,
                        onItemSelected = { pageIndex ->
                            coroutineScope.launch {
                                if (currentRoute != Screen.MainPager.route) {
                                    navController.navigate(Screen.MainPager.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                pagerState.animateScrollToPage(pageIndex)
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                NavGraph(
                    navController = navController,
                    activity = this@MainActivity,
                    pagerState = pagerState
                )

                if (isUpdating.value) {
                    Dialog(
                        onDismissRequest = { },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color.Transparent
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainTopBar(
        title: String,
        showBackButton: Boolean = false,
        onBackClick: () -> Unit = {},
        onSyncClick: () -> Unit,
        onMenuItemClick: (Int) -> Unit
    ) {
        var showMenu by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                            modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.more_options),
                        modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.update_budget_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_update_predictions)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.view_budget_summary)) },
                        onClick = {
                            onMenuItemClick(R.id.action_view_summary)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_future_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_delete_predictions)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sync)) },
                        onClick = {
                            onSyncClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        onClick = {
                            onMenuItemClick(R.id.action_settings)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.help)) },
                        onClick = {
                            onMenuItemClick(R.id.action_help)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.privacy_policy)) },
                        onClick = {
                            onMenuItemClick(R.id.action_privacy_policy)
                            showMenu = false
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.ic_bills_projection_background),
                titleContentColor = Color.Black,
                actionIconContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )
    }

    @Composable
    fun MainBottomBar(
        pagerState: PagerState,
        currentRoute: String?,
        onItemSelected: (Int) -> Unit
    ) {
        val items = listOf(
            Triple(R.string.budget_view, R.drawable.ic_budget_view, Screen.BudgetView.route),
            Triple(R.string.transactions, R.drawable.ic_transactions, Screen.Transactions.route),
            Triple(R.string.accounts, R.drawable.ic_accounts, Screen.Accounts.route),
            Triple(R.string.analysis, R.drawable.ic_analysis, Screen.Analysis.route),
            Triple(R.string.budget_rules, R.drawable.ic_budget_rules, Screen.BudgetRules.route),
        )

        NavigationBar {
            items.forEachIndexed { index, (labelRes, iconRes, route) ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(iconRes),
                            contentDescription = stringResource(labelRes),
                            modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                        )
                    },
                    label = { Text(stringResource(labelRes), softWrap = false) },
                    selected = currentRoute == route || (currentRoute == Screen.MainPager.route && pagerState.currentPage == index),
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }

    private fun handleMenuAction(actionId: Int, navController: NavHostController) {
        when (actionId) {
            R.id.action_update_predictions -> updateBudget()
            R.id.action_view_summary -> navController.navigate(Screen.BudgetList.route)
            R.id.action_delete_predictions -> chooseDeleteFuturePredictions()
            R.id.action_settings -> navController.navigate(Screen.Settings.route)
            R.id.action_help -> navController.navigate(Screen.Help.route)
            R.id.action_privacy_policy -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = getString(R.string.https_www_mschlenkrich_ca_privacy_policy).toUri()
                }
                startActivity(intent)
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
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun updateBudget() {
        val defaultDate = LocalDate.now().plusMonths(3)
        val picker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                performBudgetUpdate(selectedDate)
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        )
        picker.setTitle(getString(R.string.choose_a_date_to_project_bills_to))
        picker.show()
    }

    private fun performBudgetUpdate(stopDate: String) {
        isUpdating.value = true
        val updateBudgetPredictions = UpdateBudgetPredictions(this)
        CoroutineScope(Dispatchers.IO).launch {
            updateBudgetPredictions.updatePredictions(stopDate)
            isUpdating.value = false
            doTheUpdate(getString(R.string.budget_updated))
        }
    }

    private fun doTheUpdate(msg: String) {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.update_results))
                setMessage(msg)
                setPositiveButton(getString(android.R.string.ok)) { _, _ -> }
                show()
            }
        }
    }

    private fun deleteFuturePredictions() {
        val updateBudgetPredictions =
            UpdateBudgetPredictions(this)
        lifecycleScope.launch {
            updateBudgetPredictions.killPredictions()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        setupViewModels(clearExisting = true)
    }

    private fun setupBudgetItemViewModel() {
        val budgetItemRepository = BudgetItemRepository(
            BillsDatabase(this)
        )
        val budgetItemViewModelFactory =
            BudgetItemViewModelFactory(application, budgetItemRepository)
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
            BudgetRuleViewModelFactory(application, budgetRuleRepository)
        budgetRuleViewModel = ViewModelProvider(
            this,
            budgetRuleViewModelFactory
        )[BudgetRuleViewModel::class.java]
    }

    private fun setupAccountViewModel() {
        val accountRepository = AccountRepository(
            BillsDatabase(this)
        )
        val accountViewModelFactory =
            AccountViewModelFactory(application, accountRepository)
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
            TransactionViewModelFactory(application, transactionRepository)
        transactionViewModel = ViewModelProvider(
            this,
            transactionViewModelFactory
        )[TransactionViewModel::class.java]
    }

    private fun setupMainViewModel() {
        val mainViewModelFactory = MainViewModelFactory(application)
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