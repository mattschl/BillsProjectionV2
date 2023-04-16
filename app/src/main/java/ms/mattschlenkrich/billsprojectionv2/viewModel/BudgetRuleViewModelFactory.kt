package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetRuleRepository

class BudgetRuleViewModelFactory(
    val app: Application,
    private val budgetRuleRepository: BudgetRuleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetRuleViewModel(app, budgetRuleRepository) as T
    }
}