package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.repository.TransactionRepository

class TransactionViewModel(
    app: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(app) {
    fun insertTransaction(transaction: Transactions) =
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }

    fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ) =
        transactionRepository.getTransactionFull(
            transId, toAccountID, fromAccountID
        )

    fun getTransactionDetailed(transId: Long) =
        transactionRepository.getTransactionDetailed(transId)

    fun updateTransaction(transaction: Transactions) =
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }

    fun deleteTransaction(transId: Long, updateTime: String) =
        viewModelScope.launch {
            transactionRepository.deleteTransaction(
                transId, updateTime
            )
        }

    fun getActiveTransactionsDetailed() =
        transactionRepository.getActiveTransactionsDetailed()

    fun getActiveTransactionsDetailed(budgetRuleId: Long) =
        transactionRepository.getActiveTransactionsDetailed(budgetRuleId)

    fun getActiveTransactionsDetailed(
        budgetRuleId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getActiveTransactionsDetailed(
            budgetRuleId, startDate, endDate
        )

    fun searchActiveTransactionsDetailed(query: String?) =
        transactionRepository.searchActiveTransactionsDetailed(query)

    fun updateAccountBalance(
        newBalance: Double,
        accountId: Long,
        updateTime: String
    ) = viewModelScope.launch {
        transactionRepository.updateAccountBalance(
            newBalance, accountId, updateTime
        )
    }

    fun updateAccountOwing(
        newOwing: Double,
        accountId: Long,
        updateTime: String
    ) = viewModelScope.launch {
        transactionRepository.updateAccountOwing(
            newOwing, accountId, updateTime
        )
    }

    fun getPendingTransactionsDetailed(asset: String) =
        transactionRepository.getPendingTransactionsDetailed(asset)

    fun getMaxTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getMaxTransactionByBudgetRule(budgetRuleId)

    fun getMinTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getMinTransactionByBudgetRule(budgetRuleId)

    fun getSumTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getSumTransactionByBudgetRule(budgetRuleId)
}