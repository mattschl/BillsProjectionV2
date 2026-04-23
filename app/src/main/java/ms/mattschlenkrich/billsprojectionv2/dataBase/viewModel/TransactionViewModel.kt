package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.TransactionRepository

class TransactionViewModel(
    app: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(app) {
    suspend fun insertTransaction(transaction: Transactions) =
        transactionRepository.insertTransaction(transaction)

    suspend fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ) =
        transactionRepository.getTransactionFull(
            transId, toAccountID, fromAccountID
        )

    suspend fun getTransactionDetailed(transId: Long) =
        transactionRepository.getTransactionDetailed(transId)

    suspend fun updateTransaction(transaction: Transactions) =
        transactionRepository.updateTransaction(transaction)

    suspend fun deleteTransaction(transId: Long, updateTime: String) =
        transactionRepository.deleteTransaction(
            transId, updateTime
        )

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

    suspend fun updateAccountBalance(
        newBalance: Double,
        accountId: Long,
        updateTime: String
    ) = transactionRepository.updateAccountBalance(
        newBalance, accountId, updateTime
    )

    suspend fun updateAccountOwing(
        newOwing: Double,
        accountId: Long,
        updateTime: String
    ) = transactionRepository.updateAccountOwing(
        newOwing, accountId, updateTime
    )

    suspend fun updateAccountBalanceAndOwing(
        newBalance: Double,
        newOwing: Double,
        accountId: Long,
        updateTime: String
    ) = transactionRepository.updateAccountBalanceAndOwing(
        newBalance, newOwing, accountId, updateTime
    )

    fun getPendingTransactionsDetailed(asset: String) =
        transactionRepository.getPendingTransactionsDetailed(asset)

    fun getMaxTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getMaxTransactionByBudgetRule(budgetRuleId)

    fun getMaxTransactionByBudgetRule(
        budgetRuleId: Long, startDate: String, endDate: String
    ) = transactionRepository.getMaxTransactionByBudgetRule(
        budgetRuleId, startDate, endDate
    )

    fun getMinTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getMinTransactionByBudgetRule(budgetRuleId)

    fun getMinTransactionByBudgetRule(
        budgetRuleId: Long, startDate: String, endDate: String
    ) = transactionRepository.getMinTransactionByBudgetRule(
        budgetRuleId, startDate, endDate
    )

    fun getSumTransactionByBudgetRule(budgetRuleId: Long) =
        transactionRepository.getSumTransactionByBudgetRule(budgetRuleId)

    fun getSumTransactionByBudgetRule(
        budgetRuleId: Long, startDate: String, endDate: String
    ) = transactionRepository.getSumTransactionByBudgetRule(
        budgetRuleId, startDate, endDate
    )

    fun getActiveTransactionByAccount(accountId: Long) =
        transactionRepository.getActiveTransactionByAccount(accountId)

    fun getActiveTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) = transactionRepository.getActiveTransactionByAccount(
        accountId, startDate, endDate
    )

    fun getSumTransactionToAccount(accountId: Long) =
        transactionRepository.getSumTransactionToAccount(accountId)

    fun getSumTransactionToAccount(
        accountId: Long, startDate: String, endDate: String
    ) = transactionRepository.getSumTransactionToAccount(
        accountId, startDate, endDate
    )

    fun getSumTransactionFromAccount(accountId: Long) =
        transactionRepository.getSumTransactionFromAccount(accountId)

    fun getSumTransactionFromAccount(
        accountId: Long, startDate: String, endDate: String
    ) = transactionRepository.getSumTransactionFromAccount(
        accountId, startDate, endDate
    )

    fun getMaxTransactionByAccount(accountId: Long) =
        transactionRepository.getMaxTransactionByAccount(accountId)

    fun getMaxTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) = transactionRepository.getMaxTransactionByAccount(
        accountId, startDate, endDate
    )

    fun getMinTransactionByAccount(accountId: Long) =
        transactionRepository.getMinTransactionByAccount(accountId)

    fun getMinTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) = transactionRepository.getMinTransactionByAccount(
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
    ) = transactionRepository.getSumTransactionBySearch(query, startDate, endDate)

    fun getMaxTransactionBySearch(query: String?) =
        transactionRepository.getMaxTransactionBySearch(query)

    fun getMaxTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) = transactionRepository.getMaxTransactionBySearch(query, startDate, endDate)

    fun getMinTransactionBySearch(query: String?) =
        transactionRepository.getMinTransactionBySearch(query)

    fun getMinTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) = transactionRepository.getMinTransactionBySearch(query, startDate, endDate)

    fun getTransactionsFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getTransactionsFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getSumFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getSumFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getSumToAccountFiltered(
        accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getSumToAccountFiltered(
        accountId, query, startDate, endDate
    )

    fun getSumFromAccountFiltered(
        accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getSumFromAccountFiltered(
        accountId, query, startDate, endDate
    )

    fun getMaxFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getMaxFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getMinFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = transactionRepository.getMinFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )
}