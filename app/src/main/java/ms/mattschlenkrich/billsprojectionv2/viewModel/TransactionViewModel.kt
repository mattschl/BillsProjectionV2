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

    fun getMaxTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        transactionRepository.getMaxTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getMinTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getMinTransactionByBudgetRule(budgetRuleId)

    fun getMinTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        transactionRepository.getMinTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getSumTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getSumTransactionByBudgetRule(budgetRuleId)

    fun getSumTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        transactionRepository.getSumTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getActiveTransactionByAccount(accountId: Long) =
        transactionRepository.getActiveTransactionByAccount(accountId)

    fun getActiveTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getActiveTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getSumTransactionToAccount(accountId: Long) =
        transactionRepository.getSumTransactionToAccount(accountId)

    fun getSumTransactionToAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getSumTransactionToAccount(
            accountId, startDate, endDate
        )

    fun getSumTransactionFromAccount(accountId: Long) =
        transactionRepository.getSumTransactionFromAccount(accountId)

    fun getSumTransactionFromAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getSumTransactionFromAccount(
            accountId, startDate, endDate
        )

    fun getMaxTransactionByAccount(accountId: Long) =
        transactionRepository.getMaxTransactionByAccount(accountId)

    fun getMaxTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getMaxTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getMinTransactionByAccount(accountId: Long) =
        transactionRepository.getMinTransactionByAccount(accountId)

    fun getMinTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        transactionRepository.getMinTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getActiveTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) = transactionRepository.getActiveTransactionBySearch(
        query, startDate, endDate
    )

    fun getActiveTransactionBySearch(query: String?) =
        transactionRepository.getActiveTransactionBySearch(query)

    fun getSumTransactionBySearch(query: String?) =
        transactionRepository.getSumTransactionBySearch(query)

    fun getSumTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        transactionRepository.getSumTransactionBySearch(query, startDate, endDate)

    fun getMaxTransactionBySearch(query: String?) =
        transactionRepository.getMaxTransactionBySearch(query)

    fun getMaxTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        transactionRepository.getMaxTransactionBySearch(query, startDate, endDate)

    fun getMinTransactionBySearch(query: String?) =
        transactionRepository.getMinTransactionBySearch(query)

    fun getMinTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        transactionRepository.getMinTransactionBySearch(query, startDate, endDate)
}