package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetViewRepository

class BudgetViewViewModelFactory(
    val app: Application,
    private val budgetViewRepository: BudgetViewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetViewViewModel(app, budgetViewRepository) as T
    }
}