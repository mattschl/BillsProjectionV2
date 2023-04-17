package ms.mattschlenkrich.billsprojectionv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityMainBinding
import ms.mattschlenkrich.billsprojectionv2.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var accountViewModel: AccountViewModel
    lateinit var budgetRuleViewModel: BudgetRuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAccountViewModel()
        setupBudgetRuleViewModel()
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


}