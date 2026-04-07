package ms.mattschlenkrich.billsprojectionv2.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationBarView
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
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityMainBinding
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountsFragment
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleFragment
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAddFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionUpdateFragment
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionViewFragment
import java.time.LocalDate


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var topMenuBar: Toolbar
    lateinit var mainViewModel: MainViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetItemViewModel: BudgetItemViewModel
    lateinit var accountUpdateViewModel: AccountUpdateViewModel
    lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsManager(this)
        val fontSize = settingsManager.getSettings().fontSize
        when (fontSize) {
            "small" -> setTheme(R.style.Theme_BillsProjectionV2_Small)
            "large" -> setTheme(R.style.Theme_BillsProjectionV2_Large)
            else -> setTheme(R.style.Theme_BillsProjectionV2_Medium)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mView = binding.root
        Log.d(TAG, "MainActivity is started")
        topMenuBar = binding.topMenu

        topMenuBar.menu.apply {
            add(Menu.NONE, 1, Menu.NONE, R.string.sync)
            add(R.string.update_budget_predictions)
            add(R.string.view_current_budget_summary)
            add(R.string.delete_future_predictions)
            add(R.string.settings)
            add(R.string.help)
            add(R.string.privacy_policy)
            add("${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
        }
        topMenuBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                getString(R.string.sync) -> {
                    startActivity(Intent(this, NewActivity::class.java))
                    true
                }

                getString(R.string.update_budget_predictions) -> {
                    updateBudget()
                    true
                }

                getString(R.string.view_current_budget_summary) -> {
                    gotoBudgetList()
                    true
                }

                getString(R.string.delete_future_predictions) -> {
                    chooseDeleteFuturePredictions()
                    true
                }

                getString(R.string.settings) -> {
                    gotoSettings()
                    true
                }

                getString(R.string.help) -> {
                    gotoHelp()
                    true
                }

                getString(R.string.privacy_policy) -> {
                    val defaultBrowser = Intent.makeMainSelectorActivity(
                        Intent.ACTION_MAIN,
                        Intent.CATEGORY_APP_BROWSER
                    )
                    defaultBrowser.data =
                        getString(R.string.https_www_mschlenkrich_ca_privacy_policy).toUri()
                    startActivity(defaultBrowser)
                    true
                }

                else -> {
                    Log.d(TAG, "other was called")
                    false
                }
            }
        }


        val bottomNav =
            findViewById<NavigationBarView>(R.id.bottom_nav_view)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_budget_view -> {
                    gotoBudgetView()
                    true
                }

                R.id.navigation_accounts -> {
                    gotoAccounts()
                    true
                }

                R.id.navigation_transactions -> {
                    gotoTransactions()
                    true
                }

                R.id.navigation_budget_rules -> {
                    gotoBudgetRules()
                    true
                }

                R.id.navigation_analysis -> {
                    gotoAnalysis()
                    true
                }

                else -> {
                    false
                }
            }
        }
        setupMainViewModel()
        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetItemViewModel()
        setupAccountUpdateViewModel()
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
        menuInflater.inflate(R.menu.main_menu, menu)
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
                    fragment.setUpRecyclerView()
                }

                is BudgetRuleFragment -> {
                    fragment.setupRecyclerView()
                }

                is TransactionViewFragment -> {
                    fragment.setupRecyclerView()
                }

                is TransactionAddFragment -> {
                    fragment.populateValues()
                }

                is TransactionUpdateFragment -> {
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