package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

class TransactionRepository(private val db: BillsDatabase) {
    suspend fun insertTransaction(transaction: Transactions) =
        db.getTransactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transactions) =
        db.getTransactionDao().updateTransaction(transaction)
}