package ms.mattschlenkrich.billsprojectionv2.ui.compose

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.NavGraph
import ms.mattschlenkrich.billsprojectionv2.ui.navigation.Screen
import ms.mattschlenkrich.billsprojectionv2.ui.sync.SyncActivity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    activity: MainActivity,
    isFirstRun: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pagerState = rememberPagerState { 5 }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isFirstRun) {
            navController.navigate(Screen.Help.route)
        }
    }

    val settings = remember { SettingsManager(activity).getSettings() }
    var showSyncWarningDialog by remember { mutableStateOf(value = false) }
    var daysSinceLastSync by remember { mutableIntStateOf(0) }
    var hasDismissedSyncWarning by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        if ((settings.syncWarningDays > 0) && !hasDismissedSyncWarning) {
            withContext(Dispatchers.IO) {
                try {
                    val db = BillsDatabase(activity.applicationContext)
                    val lastSyncUtc = db.getSyncHistoryDao().getLastSyncTime(settings.deviceId)
                    if (lastSyncUtc.isNullOrBlank()) {
                        daysSinceLastSync = -1
                        showSyncWarningDialog = true
                    } else if (!isFirstRun) {
                        val date = DateFunctions().parseUtcTimestamp(lastSyncUtc)
                        if (date != null) {
                            val diffMs = System.currentTimeMillis() - date.time
                            val diffDays = (diffMs / (1000L * 60 * 60 * 24)).toInt()
                            if (diffDays >= settings.syncWarningDays) {
                                daysSinceLastSync = diffDays
                                showSyncWarningDialog = true
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    var showBudgetUpdateWarningDialog by remember { mutableStateOf(value = false) }
    var latestProjectedDate by remember { mutableStateOf<String?>(null) }
    var weeksRemaining by remember { mutableIntStateOf(0) }
    var hasDismissedBudgetUpdateWarning by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        if (!isFirstRun && (settings.budgetUpdateWarningWeeks > 0) && !hasDismissedBudgetUpdateWarning) {
            withContext(Dispatchers.IO) {
                try {
                    val latestHistory =
                        activity.budgetItemViewModel.getLatestPredictionUpdateHistory()
                    if (latestHistory == null) {
                        latestProjectedDate = null
                        weeksRemaining = -1
                        showBudgetUpdateWarningDialog = true
                    } else {
                        val updateDate =
                            DateFunctions().parseUtcTimestamp(latestHistory.puhTime)
                        if (updateDate != null) {
                            val diffMs = System.currentTimeMillis() - updateDate.time
                            val diffWeeks = (diffMs / (1000L * 60 * 60 * 24 * 7)).toInt()
                            if (diffWeeks >= settings.budgetUpdateWarningWeeks) {
                                latestProjectedDate = latestHistory.puhLastPredictionDate
                                weeksRemaining = diffWeeks
                                showBudgetUpdateWarningDialog = true
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    LaunchedEffect(currentRoute, pagerState.currentPage) {
        val titleResId = when (currentRoute) {
            Screen.MainPager.route -> when (pagerState.currentPage) {
                0 -> R.string.title_view_budget
                1 -> R.string.title_view_transaction_history
                2 -> R.string.title_accounts
                3 -> R.string.title_transaction_analysis
                4 -> R.string.title_budget_rules
                else -> R.string.title_view_budget
            }

            Screen.BudgetView.route -> R.string.title_view_budget
            Screen.Transactions.route -> R.string.title_view_transaction_history
            Screen.Accounts.route -> R.string.title_accounts
            Screen.Analysis.route -> R.string.title_transaction_analysis
            Screen.BudgetRules.route -> R.string.title_budget_rules
            Screen.Help.route -> R.string.label_help
            Screen.Settings.route -> R.string.nav_settings
            Screen.BudgetList.route -> R.string.title_budget_summary
            Screen.AccountAdd.route -> R.string.title_add_account
            Screen.AccountUpdate.route -> R.string.action_update_account
            Screen.TransactionAdd.route -> R.string.title_add_transaction
            Screen.TransactionUpdate.route -> R.string.action_update_transaction
            Screen.TransactionPerform.route -> R.string.title_perform_transaction
            Screen.TransactionSplit.route -> R.string.title_splitting_transaction
            Screen.BudgetItemAdd.route -> R.string.title_add_budget_item
            Screen.BudgetItemUpdate.route -> R.string.action_update_budget_item
            Screen.BudgetRuleAdd.route -> R.string.action_add_budget_rule
            Screen.BudgetRuleUpdate.route -> R.string.action_update_budget_rule
            Screen.AccountTypes.route -> R.string.label_account_type
            Screen.AccountTypeAdd.route -> R.string.action_add_account_type
            Screen.AccountTypeUpdate.route -> R.string.action_update_account_type
            Screen.AccountChoose.route -> R.string.title_choose_account
            Screen.BudgetRuleChoose.route -> R.string.title_choose_budget_rule
            Screen.Calculator.route -> R.string.title_calculator
            else -> null
        }
        titleResId?.let { activity.topMenuBar.setTitle(it) }
    }

    val isTopLevel = (currentRoute == Screen.MainPager.route) || (currentRoute in listOf(
        Screen.BudgetView.route,
        Screen.Transactions.route,
        Screen.Accounts.route,
        Screen.Analysis.route,
        Screen.BudgetRules.route,
    ))

    var shouldResetNavigation by remember { mutableStateOf(value = false) }

    val syncLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Re-setup view models just in case of simple connection changes,
        // but full data refreshes are handled by activity restarts in SyncActivity.
        activity.setupViewModels(clearExisting = false)
    }

    if (shouldResetNavigation) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.MainPager.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
            shouldResetNavigation = false
        }
    }

    val isImeVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                title = activity.topMenuBarState.value.title.ifEmpty { stringResource(R.string.app_name) },
                showBackButton = !isTopLevel,
                onBackClick = { navController.popBackStack() },
                onSyncClick = { syncLauncher.launch(Intent(activity, SyncActivity::class.java)) },
            ) { actionId ->
                activity.handleMenuAction(actionId, navController)
            }
        },
        bottomBar = {
            if (!isImeVisible) {
                MainBottomBar(
                    pagerState = pagerState,
                    currentRoute = currentRoute,
                ) { pageIndex ->
                    coroutineScope.launch {
                        if (currentRoute != Screen.MainPager.route) {
                            activity.mainViewModel.eraseAll()
                            pagerState.scrollToPage(pageIndex)
                            navController.navigate(Screen.MainPager.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            pagerState.animateScrollToPage(pageIndex)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavGraph(
                navController = navController,
                activity = activity,
                pagerState = pagerState
            )

            if (activity.isUpdating.value) {
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

            if (showSyncWarningDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSyncWarningDialog = false
                        hasDismissedSyncWarning = true
                    },
                    title = { Text(stringResource(R.string.title_sync_reminder)) },
                    text = {
                        val msg = if (daysSinceLastSync < 0) {
                            stringResource(R.string.msg_sync_warning_never)
                        } else {
                            stringResource(R.string.msg_sync_warning_days, daysSinceLastSync)
                        }
                        Text(msg)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSyncWarningDialog = false
                                hasDismissedSyncWarning = true
                                syncLauncher.launch(
                                    Intent(
                                        activity,
                                        SyncActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(R.string.action_go_to_sync))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSyncWarningDialog = false
                                hasDismissedSyncWarning = true
                            }
                        ) {
                            val labelRes = if (daysSinceLastSync < 0) {
                                R.string.action_start_fresh
                            } else {
                                R.string.action_remind_later
                            }
                            Text(stringResource(labelRes))
                        }
                    }
                )
            }

            if (showBudgetUpdateWarningDialog && !showSyncWarningDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showBudgetUpdateWarningDialog = false
                        hasDismissedBudgetUpdateWarning = true
                    },
                    title = { Text(stringResource(R.string.title_budget_update_reminder)) },
                    text = {
                        val msg = if (latestProjectedDate == null) {
                            stringResource(R.string.msg_budget_update_warning_never)
                        } else {
                            stringResource(
                                R.string.msg_budget_update_warning_weeks,
                                latestProjectedDate!!,
                                weeksRemaining,
                            )
                        }
                        Text(msg)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBudgetUpdateWarningDialog = false
                                hasDismissedBudgetUpdateWarning = true
                                activity.updateBudget()
                            }
                        ) {
                            Text(stringResource(R.string.action_update_now))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showBudgetUpdateWarningDialog = false
                                hasDismissedBudgetUpdateWarning = true
                            }
                        ) {
                            Text(stringResource(R.string.action_remind_later))
                        }
                    }
                )
            }
        }
    }
}