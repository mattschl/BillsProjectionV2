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
}