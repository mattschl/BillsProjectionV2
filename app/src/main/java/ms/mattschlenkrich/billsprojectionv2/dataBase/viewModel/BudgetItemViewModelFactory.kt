package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.BudgetItemRepository

class BudgetItemViewModelFactory(
    val app: Application,
    private val budgetItemRepository: BudgetItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetItemViewModel(app, budgetItemRepository) as T
    }
}