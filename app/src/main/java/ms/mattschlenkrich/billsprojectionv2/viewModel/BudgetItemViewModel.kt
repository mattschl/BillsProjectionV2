package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetItemRepository

class BudgetItemViewModel(
    app: Application,
    private val budgetItemRepository: BudgetItemRepository
) : AndroidViewModel(app) {

    suspend fun insertBudgetItem(budgetItem: BudgetItem) =
        viewModelScope.launch {
            budgetItemRepository.insertBudgetItem(budgetItem)
        }

    suspend fun updateBudgetItem(budgetItem: BudgetItem) =
        viewModelScope.launch {
            budgetItemRepository.updateBudgetItem(budgetItem)
        }
}