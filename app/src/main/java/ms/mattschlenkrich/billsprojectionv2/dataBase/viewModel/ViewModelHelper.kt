package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModelFactory
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.AccountRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetItemRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetRuleRepository
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.TransactionRepository
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

object ViewModelHelper {
    fun setupMainViewModel(activity: MainActivity): MainViewModel {
        val factory = MainViewModelFactory(activity.application)
        return ViewModelProvider(activity, factory)[MainViewModel::class.java]
    }

    fun setupAccountViewModel(activity: MainActivity): AccountViewModel {
        val repository = AccountRepository(BillsDatabase(activity))
        val factory = AccountViewModelFactory(activity.application, repository)
        return ViewModelProvider(activity, factory)[AccountViewModel::class.java]
    }

    fun setupBudgetRuleViewModel(activity: MainActivity): BudgetRuleViewModel {
        val repository = BudgetRuleRepository(BillsDatabase(activity))
        val factory = BudgetRuleViewModelFactory(activity.application, repository)
        return ViewModelProvider(activity, factory)[BudgetRuleViewModel::class.java]
    }

    fun setupTransactionViewModel(activity: MainActivity): TransactionViewModel {
        val repository = TransactionRepository(BillsDatabase(activity))
        val factory = TransactionViewModelFactory(activity.application, repository)
        return ViewModelProvider(activity, factory)[TransactionViewModel::class.java]
    }

    fun setupBudgetItemViewModel(activity: MainActivity): BudgetItemViewModel {
        val repository = BudgetItemRepository(BillsDatabase(activity))
        val factory = BudgetItemViewModelFactory(activity.application, repository)
        return ViewModelProvider(activity, factory)[BudgetItemViewModel::class.java]
    }

    fun setupAccountUpdateViewModel(
        activity: MainActivity,
        transactionViewModel: TransactionViewModel,
        accountViewModel: AccountViewModel
    ): AccountUpdateViewModel {
        val factory = AccountUpdateViewModelFactory(
            activity,
            transactionViewModel,
            accountViewModel,
            activity.application
        )
        return ViewModelProvider(activity, factory)[AccountUpdateViewModel::class.java]
    }
}