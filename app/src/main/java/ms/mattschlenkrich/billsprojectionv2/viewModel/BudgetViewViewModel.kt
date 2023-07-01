package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.BudgetView
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetViewRepository

class BudgetViewViewModel(
    app: Application,
    private val budgetViewRepository: BudgetViewRepository
) : AndroidViewModel(app) {
    suspend fun insertBudgetView(budgetView: BudgetView) =
        viewModelScope.launch {
            budgetViewRepository.insertBudgetView(budgetView)
        }
}