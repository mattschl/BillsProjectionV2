package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

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
}