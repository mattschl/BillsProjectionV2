package ms.mattschlenkrich.billsprojectionv2.dataBase.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

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
        db.getTransactionDao().getTransactionsFiltered(-1L, -1L, "", "", "")

    suspend fun getTransactionDetailed(transId: Long) =
        db.getTransactionDao().getTransactionDetailed(transId)

    fun searchActiveTransactionsDetailed(query: String?) =
        db.getTransactionDao().getTransactionsFiltered(-1L, -1L, query ?: "", "", "")

    suspend fun getTransactionFull(
        transId: Long,
        toAccountID: Long,
        fromAccountID: Long,
    ) = db.getTransactionDao().getTransactionFull(
        transId, toAccountID, fromAccountID
    )

    fun getSumTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) = db.getTransactionDao().getSumTransactionByBudgetRuleSync(
        budgetRuleId, startDate, endDate
    )

    fun getCountTransactionByBudgetRuleSync(
        budgetRuleId: Long,
        startDate: String,
        endDate: String
    ) = db.getTransactionDao().getCountTransactionByBudgetRuleSync(
        budgetRuleId, startDate, endDate
    )

    suspend fun updateAccountBalance(
        newBalance: Double, accountId: Long, updateTime: String
    ) = db.getTransactionDao().updateAccountBalance(
        newBalance, accountId, updateTime
    )

    suspend fun updateAccountOwing(
        newOwing: Double, accountId: Long, updateTime: String
    ) = db.getTransactionDao().updateAccountOwing(
        newOwing, accountId, updateTime
    )

    suspend fun updateAccountBalanceAndOwing(
        newBalance: Double, newOwing: Double, accountId: Long, updateTime: String
    ) = db.getTransactionDao().updateAccountBalanceAndOwing(
        newBalance, newOwing, accountId, updateTime
    )

    fun getPendingTransactionsDetailed(asset: String) =
        db.getTransactionDao().getPendingTransactionsDetailed(asset)

    fun getActiveTransactionByAccount(accountId: Long): LiveData<List<TransactionDetailed>> =
        if (accountId == 0L) MutableLiveData(emptyList())
        else db.getTransactionDao().getTransactionsByAccount(accountId)

    fun getTransactionsFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getTransactionsFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getSumFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getSumFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getSumToAccountFiltered(
        accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getSumToAccountFiltered(
        accountId, query, startDate, endDate
    )

    fun getSumFromAccountFiltered(
        accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getSumFromAccountFiltered(
        accountId, query, startDate, endDate
    )

    fun getMaxFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getMaxFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )

    fun getMinFiltered(
        budgetRuleId: Long, accountId: Long, query: String, startDate: String, endDate: String
    ) = db.getTransactionDao().getMinFiltered(
        budgetRuleId, accountId, query, startDate, endDate
    )
}