package ms.mattschlenkrich.billsprojectionv2

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityMainBinding
import ms.mattschlenkrich.billsprojectionv2.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetViewRepository
import ms.mattschlenkrich.billsprojectionv2.repository.TransactionRepository
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetViewViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetViewViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.TransactionViewModelFactory

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel
    lateinit var transactionViewModel: TransactionViewModel
    lateinit var budgetViewViewModel: BudgetViewViewModel
    lateinit var mView: View
//    private val timeFormatter: SimpleDateFormat =
//        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mView = binding.root
        Log.d(TAG, "MainActivity is started")
//        val navView: BottomNavigationView = binding.bottomNavView
//        Log.d(TAG, "navController is ${R.id.fragment_container_view}")
//        val navController =
//            findNavController(R.id.fragment_container_view)
//
//        val appBarConfiguration =
//            AppBarConfiguration(
//                setOf(
//                    R.id.transactionViewFragment,
//                    R.id.accountsFragment,
//                    R.id.budgetRuleFragment
//                )
//            )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        setupAccountViewModel()
        setupBudgetRuleViewModel()
        setupTransactionViewModel()
        setupBudgetViewViewModel()

//        binding.bottomNavView.apply {
//            setOnClickListener {
//                Log.d(TAG, "selectedItemId = $selectedItemId")
//                when (selectedItemId) {
//                    0 -> {
//                        //not in operation
//                        Log.d(TAG, "0 is selected")
//                    }
//                    1 -> {
//                        findNavController().navigate(R.id.transactionViewFragment)
//                    }
//                    2 -> {
//                        findNavController().navigate(R.id.accountsFragment)
//                    }
//                    3 -> {
//                        findNavController().navigate(R.id.budgetRuleFragment)
//                    }
//                }
//
//            }
//        }


//        BillsDatabase.testDb(this.baseContext)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(R.string.transactions)
        menu?.add(R.string.accounts)
        menu?.add(R.string.budget_rules)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {

            resources.getString(R.string.transactions) -> {
                findNavController(R.id.fragment_container_view)
                    .navigate(
                        R.id.action_global_transactionViewFragment
                    )
            }

            resources.getString(R.string.accounts) -> {
                val direction =
                    NavGraphDirections.actionGlobalAccountsFragment(
                        null,
                        null,
                        null,
                        null
                    )

                findNavController(R.id.fragment_container_view)
                    .navigate(direction)
            }

            resources.getString(R.string.budget_rules) -> {
                val direction =
                    NavGraphDirections.actionGlobalBudgetRuleFragment(
                        null,
                        null
                    )
                findNavController(R.id.fragment_container_view)
                    .navigate(direction)
            }
        }
        return false
    }

    private fun setupBudgetViewViewModel() {
        val budgetViewRepository = BudgetViewRepository(
            BillsDatabase(this)
        )
        val budgetViewViewModelFactory =
            BudgetViewViewModelFactory(
                application, budgetViewRepository
            )
        budgetViewViewModel = ViewModelProvider(
            this,
            budgetViewViewModelFactory
        )[BudgetViewViewModel::class.java]
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