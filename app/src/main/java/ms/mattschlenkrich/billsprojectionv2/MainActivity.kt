package ms.mattschlenkrich.billsprojectionv2

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
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityMainBinding
import ms.mattschlenkrich.billsprojectionv2.projections.UpdateBudgetPredictions
import ms.mattschlenkrich.billsprojectionv2.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetItemRepository
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.repository.TransactionRepository
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModelFactory
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetItemViewModel: BudgetItemViewModel
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
                menu.add("Bills Projection ${BuildConfig.VERSION_NAME}")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.title) {

                    getString(R.string.update_budget_predictions) -> {
                        updateBudget()
                        true
                    }

                    else -> {
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
        setMainViewModel()
        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetItemViewModel()

    }

    private fun gotoAnalysis() {
        mainViewModel.eraseAll()
        val direction =
            NavGraphDirections
                .actionGlobalTransactionAverageFragment()
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun setMainViewModel() {
        val mainViewModelFactory =
            MainViewModelFactory(application)
        mainViewModel = ViewModelProvider(
            this,
            mainViewModelFactory
        )[MainViewModel::class.java]
        mainViewModel.eraseAll()
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
        datePickerDialog.setTitle("Pick a date to project forward to.")
        datePickerDialog.show()
    }

    private fun doTheUpdate(
        display: String
    ) {
        val updateBudgetPredictions =
            UpdateBudgetPredictions(this)
        CoroutineScope(Dispatchers.IO).launch { updateBudgetPredictions.updatePredictions(display) }
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
}