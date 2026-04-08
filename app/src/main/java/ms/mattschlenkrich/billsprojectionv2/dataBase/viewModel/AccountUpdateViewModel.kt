package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_1000
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

class AccountUpdateViewModel(
    val transactionViewModel: TransactionViewModel,
    val accountViewModel: AccountViewModel,
    app: Application,
) : AndroidViewModel(app) {

    val df = DateFunctions()

    private suspend fun doAccountUpdates(
        mTransaction: Transactions,
        reverse: Boolean,
    ): Boolean {
        withContext(Dispatchers.Default) {
            val toAcc = accountViewModel.getAccountAndType(mTransaction.transToAccountId)
            if (!mTransaction.transToAccountPending) {
                if (toAcc.accountType!!.keepTotals) {
                    updateAccountBalance(
                        mTransaction.transAmount, mTransaction.transToAccountId, !reverse
                    )
                }
                if (toAcc.accountType!!.tallyOwing) {
                    updateAccountOwing(
                        mTransaction.transAmount, mTransaction.transToAccountId, reverse
                    )
                }
            }
            val fromAcc = accountViewModel.getAccountAndType(mTransaction.transFromAccountId)
            if (!mTransaction.transFromAccountPending) {
                if (fromAcc.accountType!!.keepTotals) {
                    updateAccountBalance(
                        mTransaction.transAmount, mTransaction.transFromAccountId, reverse
                    )
                }
                if (fromAcc.accountType!!.tallyOwing) {
                    updateAccountOwing(
                        mTransaction.transAmount, mTransaction.transFromAccountId, !reverse
                    )
                }
            }
        }
        return true
    }

    suspend fun isTransactionPending(accountId: Long): Boolean {
        val accType = accountViewModel.getAccountAndType(accountId).accountType!!
        return accType.allowPending && accType.tallyOwing
    }


    suspend fun deleteTransaction(mTransaction: Transactions) {
        doAccountUpdates(mTransaction, true)
        delay(WAIT_500)
        transactionViewModel.deleteTransaction(
            mTransaction.transId, df.getCurrentTimeAsString()
        )
    }

    suspend fun performTransaction(
        mTransaction: Transactions
    ) {
        doAccountUpdates(mTransaction, false)
        transactionViewModel.insertTransaction(mTransaction)
    }

    suspend fun updateTransaction(
        oldTransaction: Transactions, newTransaction: Transactions
    ) {
        doAccountUpdates(oldTransaction, true)
        delay(WAIT_1000)
        doAccountUpdates(newTransaction, false)
        transactionViewModel.updateTransaction(newTransaction)
    }

    private suspend fun updateAccountBalance(
        amount: Double, accountId: Long, creditAccount: Boolean
    ) {
        val newBalance =
            accountViewModel.getAccount(accountId).accountBalance + if (creditAccount) amount else -amount
        transactionViewModel.updateAccountBalance(
            newBalance, accountId, df.getCurrentTimeAsString()
        )
    }

    private suspend fun updateAccountOwing(
        amount: Double, accountId: Long, creditAccount: Boolean
    ) {
        val newOwing =
            accountViewModel.getAccount(accountId).accountOwing + if (creditAccount) amount else -amount
        transactionViewModel.updateAccountOwing(newOwing, accountId, df.getCurrentTimeAsString())
    }
}