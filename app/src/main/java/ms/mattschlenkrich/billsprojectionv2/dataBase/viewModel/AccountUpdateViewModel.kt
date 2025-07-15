package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private fun doAccountUpdates(
        mTransaction: Transactions,
        reverse: Boolean,
    ): Boolean {
        CoroutineScope(Dispatchers.Default).launch {
            if (!mTransaction.transToAccountPending) {
                if (accountViewModel.getAccountAndType(
                        mTransaction.transToAccountId
                    ).accountType!!.keepTotals
                ) {
                    updateAccountBalance(
                        mTransaction.transAmount, mTransaction.transToAccountId, !reverse
                    )
                }
                if (accountViewModel.getAccountAndType(mTransaction.transToAccountId).accountType!!.tallyOwing) {
                    updateAccountOwing(
                        mTransaction.transAmount, mTransaction.transToAccountId, reverse
                    )
                }
            }
            if (!mTransaction.transFromAccountPending) {
                if (accountViewModel.getAccountAndType(mTransaction.transFromAccountId).accountType!!.keepTotals) {
                    updateAccountBalance(
                        mTransaction.transAmount, mTransaction.transFromAccountId, reverse
                    )
                }
                if (accountViewModel.getAccountAndType(mTransaction.transFromAccountId).accountType!!.tallyOwing) {
                    updateAccountOwing(
                        mTransaction.transAmount, mTransaction.transFromAccountId, !reverse
                    )
                }
            }
        }
        return true
    }

    fun isTransactionPending(accountId: Long): Boolean {
        val accType = accountViewModel.getAccountAndType(accountId).accountType!!
        return accType.allowPending && accType.tallyOwing
    }


    fun deleteTransaction(mTransaction: Transactions) {
        CoroutineScope(Dispatchers.Main).launch {
            doAccountUpdates(mTransaction, true)
            delay(WAIT_500)
            transactionViewModel.deleteTransaction(
                mTransaction.transId, df.getCurrentTimeAsString()
            )
        }
    }

    fun performTransaction(
        mTransaction: Transactions
    ) {
        doAccountUpdates(mTransaction, false)
        transactionViewModel.insertTransaction(mTransaction)
    }

    fun updateTransaction(
        oldTransaction: Transactions, newTransaction: Transactions
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            doAccountUpdates(oldTransaction, true)
            delay(WAIT_1000)
            doAccountUpdates(newTransaction, false)
            withContext(Dispatchers.Default) { transactionViewModel.updateTransaction(newTransaction) }
        }
    }

    private fun updateAccountBalance(
        amount: Double, accountId: Long, creditAccount: Boolean
    ) {
        val newBalance =
            accountViewModel.getAccount(accountId).accountBalance + if (creditAccount) amount else -amount
        transactionViewModel.updateAccountBalance(
            newBalance, accountId, df.getCurrentTimeAsString()
        )
    }

    private fun updateAccountOwing(
        amount: Double, accountId: Long, creditAccount: Boolean
    ) {
        val newOwing =
            accountViewModel.getAccount(accountId).accountOwing + if (creditAccount) amount else -amount
        transactionViewModel.updateAccountOwing(newOwing, accountId, df.getCurrentTimeAsString())
    }
}