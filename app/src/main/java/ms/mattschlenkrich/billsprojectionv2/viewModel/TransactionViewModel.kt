package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.repository.TransactionRepository

class TransactionViewModel(
    app: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(app) {
    fun insertTransaction(transaction: Transaction) =
        viewModelScope.launch {
            transactionRepository.insertTransactions(transaction)
        }

    fun updateTransaction(transaction: Transaction) =
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
}