package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.transactions.Transactions

class TransactionRepository(private val db: BillsDatabase) {
    suspend fun insertTransaction(transaction: Transactions) =
        db.getTransactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transactions) =
        db.getTransactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transId: Long, updateTime: String) =
        db.getTransactionDao().deleteTransaction(
            transId, updateTime
        )

    fun getActiveTransactionsDetailed() =
        db.getTransactionDao().getActiveTransactionsDetailed()

    fun getActiveTransactionsDetailed(budgetRuleId: Long) =
        db.getTransactionDao().getActiveTransactionsDetailed(budgetRuleId)

    fun getActiveTransactionsDetailed(
        budgetRuleId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getActiveTransactionsDetailed(
            budgetRuleId, startDate, endDate
        )

    fun getTransactionDetailed(transId: Long) =
        db.getTransactionDao().getTransactionDetailed(transId)

    fun searchActiveTransactionsDetailed(query: String?) =
        db.getTransactionDao().searchActiveTransactionsDetailed(query)

    fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ) =
        db.getTransactionDao().getTransactionFull(
            transId, toAccountID, fromAccountID
        )

    suspend fun updateAccountBalance(
        newBalance: Double,
        accountId: Long,
        updateTime: String
    ) = db.getTransactionDao().updateAccountBalance(
        newBalance, accountId, updateTime
    )

    suspend fun updateAccountOwing(
        newOwing: Double,
        accountId: Long,
        updateTime: String
    ) = db.getTransactionDao().updateAccountOwing(
        newOwing, accountId, updateTime
    )

    fun getPendingTransactionsDetailed(asset: String) =
        db.getTransactionDao().getPendingTransactionsDetailed(asset)

    fun getMaxTransactionByBudgetRule(budgetRuleId: Long) =
        db.getTransactionDao().getMaxTransactionByBudgetRule(budgetRuleId)

    fun getMaxTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        db.getTransactionDao().getMaxTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getMinTransactionByBudgetRule(budgetRuleId: Long) =
        db.getTransactionDao().getMinTransactionByBudgetRule(budgetRuleId)

    fun getMinTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        db.getTransactionDao().getMinTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getSumTransactionByBudgetRule(budgetRuleId: Long) =
        db.getTransactionDao().getSumTransactionByBudgetRule(budgetRuleId)

    fun getSumTransactionByBudgetRule(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) =
        db.getTransactionDao().getSumTransactionByBudgetRule(
            budgetRuleId, startDate, endDate
        )

    fun getActiveTransactionByAccount(accountId: Long) =
        db.getTransactionDao().getActiveTransactionByAccount(accountId)

    fun getActiveTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getActiveTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getSumTransactionToAccount(accountId: Long) =
        db.getTransactionDao().getSumTransactionToAccount(accountId)

    fun getSumTransactionToAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getSumTransactionToAccount(
            accountId, startDate, endDate
        )

    fun getSumTransactionFromAccount(accountId: Long) =
        db.getTransactionDao().getSumTransactionFromAccount(accountId)

    fun getSumTransactionFromAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getSumTransactionFromAccount(
            accountId, startDate, endDate
        )

    fun getMaxTransactionByAccount(accountId: Long) =
        db.getTransactionDao().getMaxTransactionByAccount(accountId)

    fun getMaxTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getMaxTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getMinTransactionByAccount(accountId: Long) =
        db.getTransactionDao().getMinTransactionByAccount(accountId)

    fun getMinTransactionByAccount(
        accountId: Long, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getMinTransactionByAccount(
            accountId, startDate, endDate
        )

    fun getActiveTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) = db.getTransactionDao().getActiveTransactionBySearch(
        query, startDate, endDate
    )

    fun getActiveTransactionBySearch(query: String?) =
        db.getTransactionDao().getActiveTransactionBySearch(query)


    fun getSumTransactionBySearch(query: String?) =
        db.getTransactionDao().getSumTransactionBySearch(query)

    fun getSumTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getSumTransactionBySearch(query, startDate, endDate)

    fun getMaxTransactionBySearch(query: String?) =
        db.getTransactionDao().getMaxTransactionBySearch(query)

    fun getMaxTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getMaxTransactionBySearch(query, startDate, endDate)

    fun getMinTransactionBySearch(query: String?) =
        db.getTransactionDao().getMinTransactionBySearch(query)

    fun getMinTransactionBySearch(
        query: String?, startDate: String, endDate: String
    ) =
        db.getTransactionDao().getMinTransactionBySearch(query, startDate, endDate)
}