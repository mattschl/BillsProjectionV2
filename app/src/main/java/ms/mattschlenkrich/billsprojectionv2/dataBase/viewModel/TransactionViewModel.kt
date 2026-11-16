package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.repository.TransactionRepository

class TransactionViewModel(
    app: Application,
    private val transactionRepository: TransactionRepository,
) : AndroidViewModel(app) {
    suspend fun insertTransaction(transaction: Transactions) =
        transactionRepository.insertTransaction(transaction)

    suspend fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ) =
        transactionRepository.getTransactionFull(
            transId, toAccountID, fromAccountID,
        )

    fun getSumTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String,
    ) = transactionRepository.getSumTransactionByBudgetRuleSync(
        budgetRuleId, startDate, endDate,
    )

    fun getCountTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String,
    ) = transactionRepository.getCountTransactionByBudgetRuleSync(
        budgetRuleId, startDate, endDate,
    )

    suspend fun getTransactionDetailed(transId: Long) =
        transactionRepository.getTransactionDetailed(transId)

    suspend fun updateTransaction(transaction: Transactions) =
        transactionRepository.updateTransaction(transaction)

    suspend fun deleteTransaction(transId: Long, updateTime: String) =
        transactionRepository.deleteTransaction(
            transId,
            updateTime,
        )

    fun getActiveTransactionsDetailed() =
        transactionRepository.getActiveTransactionsDetailed()

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

    fun getActiveTransactionByAccount(accountId: Long) =
        transactionRepository.getActiveTransactionByAccount(accountId)

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

    fun createTransactionDetailedFromBudgetItem(budgetItemDetailed: BudgetItemDetailed): TransactionDetailed {
        val item = budgetItemDetailed.budgetItem ?: return TransactionDetailed(
            null, null, null, null
        )
        return TransactionDetailed(
            transaction = Transactions(
                transId = 0,
                transDate = item.biActualDate,
                transName = item.biBudgetName,
                transNote = "",
                transRuleId = item.biRuleId,
                transToAccountId = item.biToAccountId,
                transToAccountPending = item.biIsPending,
                transFromAccountId = item.biFromAccountId,
                transFromAccountPending = item.biIsPending,
                transAmount = item.biProjectedAmount,
                transIsDeleted = false,
                transUpdateTime = ""
            ),
            budgetRule = budgetItemDetailed.budgetRule,
            toAccount = budgetItemDetailed.toAccount,
            fromAccount = budgetItemDetailed.fromAccount
        )
    }
}