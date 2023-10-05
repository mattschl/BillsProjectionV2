package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.repository.BudgetItemRepository

class BudgetItemViewModel(
    app: Application, private val budgetItemRepository: BudgetItemRepository
) : AndroidViewModel(app) {

    fun insertBudgetItem(budgetItem: BudgetItem) = viewModelScope.launch {
        budgetItemRepository.insertBudgetItem(budgetItem)
    }

    fun updateBudgetItem(budgetItem: BudgetItem) = viewModelScope.launch {
        budgetItemRepository.updateBudgetItem(budgetItem)
    }

    fun deleteBudgetItem(
        budgetRulId: Long, projectedDate: String, updateTime: String
    ) = viewModelScope.launch {
        budgetItemRepository.deleteBudgetItem(
            budgetRulId, projectedDate, updateTime
        )
    }

    fun getPayDaysActive() = budgetItemRepository.getPayDaysActive()

    fun deleteFutureBudgetItems(currentDate: String, updateTime: String) = viewModelScope.launch {
        budgetItemRepository.deleteFutureBudgetItems(
            currentDate, updateTime
        )
    }

    fun getAssetsForBudget() = budgetItemRepository.getAssetsForBudget()

    fun getPayDays(asset: String) = budgetItemRepository.getPayDays(asset)

    fun getBudgetItems(asset: String, payDay: String) =
        budgetItemRepository.getBudgetItems(asset, payDay)

    fun getPayDays() = budgetItemRepository.getPayDays()

    fun cancelBudgetItem(
        budgetRuleId: Long, projectedDate: String, updateTime: String
    ) = viewModelScope.launch {
        budgetItemRepository.cancelBudgetItem(
            budgetRuleId, projectedDate, updateTime
        )
    }

    fun getBudgetItemWritable(budgetRuleId: Long, projectedDate: String) =
        budgetItemRepository.getBudgetItemWritable(budgetRuleId, projectedDate)

    fun rewriteBudgetItem(
        budgetRuleId: Long, projectedDate: String, actualDate: String, payDay: String,
        budgetName: String, isPayDay: Boolean, toAccountId: Long, fromAccountId: Long,
        projectedAmount: Double, isFixed: Boolean, isAutomatic: Boolean, updateTime: String
    ) =
        budgetItemRepository.rewriteBudgetItem(
            budgetRuleId, projectedDate, actualDate, payDay, budgetName, isPayDay, toAccountId,
            fromAccountId, projectedAmount, isFixed, isAutomatic, updateTime
        )

    fun lockUnlockBudgetItem(
        lock: Boolean, budgetRuleId: Long, payDay: String, updateTime: String
    ) =
        viewModelScope.launch {
            budgetItemRepository.lockUnlockBudgetItem(
                lock, budgetRuleId, payDay, updateTime
            )
        }

    fun lockUnlockBudgetItem(
        lock: Boolean, payDay: String, updateTime: String
    ) =
        viewModelScope.launch {
            budgetItemRepository.lockUnlockBudgetItem(
                lock, payDay, updateTime
            )
        }
}