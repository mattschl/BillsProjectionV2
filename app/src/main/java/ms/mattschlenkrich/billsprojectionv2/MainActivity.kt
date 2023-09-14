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
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModelFactory
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetItemViewModel: BudgetItemViewModel
    lateinit var mView: View
//    private val timeFormatter: SimpleDateFormat =
//        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mView = binding.root
        Log.d(TAG, "MainActivity is started")
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menu.add(R.string.budget_view)
//                menu.add(R.string.transactions)
//                menu.add(R.string.accounts)
//                menu.add(R.string.budget_rules)
                menu.add(getString(R.string.update_budget_predictions))
                menu.add("Bills Projection ${BuildConfig.VERSION_NAME}")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.title) {

//                    resources.getString(R.string.budget_view) -> {
//                        gotoBudgetView()
//                        return true
//                    }
//
//                    resources.getString(R.string.transactions) -> {
//                        gotoTransactions()
//                        return true
//                    }
//
//                    resources.getString(R.string.accounts) -> {
//                        gotoAccounts()
//                        return true
//                    }
//
//                    resources.getString(R.string.budget_rules) -> {
//                        gotoBudgetRules()
//                        return true
//                    }

                    getString(R.string.update_budget_predictions) -> {
                        updateBudget()
                        return true
                    }

                    else -> {
                        return false
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

                else -> {
                    false
                }
            }
        }

        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetItemViewModel()

    }

    private fun gotoBudgetView() {
        val direction =
            NavGraphDirections.actionGlobalBudgetViewFragment(
                null,
                null,
                null
            )
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoBudgetRules() {
        val direction =
            NavGraphDirections.actionGlobalBudgetRuleFragment(
                null,
                null,
                null,
                null,
                null
            )
        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoAccounts() {
        val direction =
            NavGraphDirections.actionGlobalAccountsFragment(
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )

        findNavController(R.id.fragment_container_view)
            .navigate(direction)
    }

    private fun gotoTransactions() {
        val direction =
            NavGraphDirections.actionGlobalTransactionViewFragment(
                null,
                null,
                null,
                null
            )
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
        Log.d(TAG, "Doing the update passing the date $display")
        updateBudgetPredictions.updatePredictions(display)
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