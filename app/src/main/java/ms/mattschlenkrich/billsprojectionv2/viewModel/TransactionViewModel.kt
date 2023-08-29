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

//    fun insertTransaction(
//        transId: Long,
//        transDate: String,
//        bRuleId: Long,
//        toAccountId: Long,
//        toAccountPending: Boolean,
//        fromAccountId: Long,
//        fromAccountPending: Boolean,
//        transName: String,
//        transNote: String,
//        transAmount: Double,
//        isDelete: Boolean,
//        updateTime: String,
//    ) = viewModelScope.launch {
//        transactionRepository.insertTransaction(
//            transId, transDate, bRuleId, toAccountId,
//            toAccountPending, fromAccountId, fromAccountPending,
//            transName, transNote, transAmount, isDelete, updateTime
//        )
//    }

//    fun getTransactionFull(transId: Long) =
//        transactionRepository.getTransactionFull(transId)

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
}