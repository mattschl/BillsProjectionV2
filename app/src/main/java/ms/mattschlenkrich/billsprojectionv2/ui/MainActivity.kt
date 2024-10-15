package ms.mattschlenkrich.billsprojectionv2.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.NavGraphDirections
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.AccountUpdateViewModel
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.AccountUpdateViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetItemRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.TransactionRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetItemViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityMainBinding
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetItemViewModel: BudgetItemViewModel
    lateinit var accountUpdateViewModel: AccountUpdateViewModel
    lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mView = binding.root
        Log.d(TAG, "MainActivity is started")

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add(getString(R.string.update_budget_predictions))
                menu.add(getString(R.string.view_current_budget_summary))
                menu.add(getString(R.string.delete_future_predictions))
                menu.add(getString(R.string.help))
//                menu.add(getString(R.string.pay_check))
                menu.add("${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.title) {

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

                    getString(R.string.help) -> {
                        gotoHelp()
                        true
                    }

//                    getString(R.string.pay_check) -> {
//                        //future module
//                        true
//                    }

                    else -> {
                        Log.d(TAG, "other was called")
                        false
                    }
                }

            }
        })
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottom_nav_view)
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
            setTitle("*Warning*! Confirm Delete?")
            setMessage(
                "This action should only be done when you do a drastic change. " +
                        "Such as a change in payday source. " +
                        "Any manually entered items will be overwritten!! " +
                        "This action cannot be undone!"
            )
            setPositiveButton("Continue") { _, _ ->
                deleteFuturePredictions()
            }
            setNegativeButton("Cancel", null)
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

    private fun gotoBudgetList() {
        val direction = NavGraphDirections
            .actionGlobalBudgetListFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoAnalysis() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections
                .actionGlobalTransactionAverageFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoBudgetView() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections.actionGlobalBudgetViewFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoBudgetRules() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections.actionGlobalBudgetRuleFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoAccounts() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections.actionGlobalAccountsFragment()

        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoTransactions() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections.actionGlobalTransactionViewFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
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