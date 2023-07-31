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

    fun insertBudgetItem(budgetItem: BudgetItem) =
        viewModelScope.launch {
            budgetItemRepository.insertBudgetItem(budgetItem)
        }

    fun updateBudgetItem(budgetItem: BudgetItem) =
        viewModelScope.launch {
            budgetItemRepository.updateBudgetItem(budgetItem)
        }

    fun deleteBudgetItem(
        budgetRulId: Long,
        projectedDate: String,
        updateTime: String
    ) =
        viewModelScope.launch {
            budgetItemRepository.deleteBudgetItem(
                budgetRulId,
                projectedDate,
                updateTime
            )
        }

}