package ms.mattschlenkrich.billsprojectionv2.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel

class AccountUpdateViewModel(
    val transactionViewModel: TransactionViewModel,
    val accountViewModel: AccountViewModel,
    app: Application,
) : AndroidViewModel(app) {

    val df = DateFunctions()

    private fun doTransaction(
        mTransaction: Transactions,
        reverse: Boolean,
    ): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            if (!mTransaction.transToAccountPending) {
                if (accountViewModel.getAccountAndType(
                        mTransaction.transToAccountId
                    ).accountType!!.keepTotals
                ) {
                    updateAccountBalance(
                        mTransaction.transAmount,
                        mTransaction.transToAccountId,
                        !reverse
                    )
                }
                if (accountViewModel.getAccountAndType(
                        mTransaction.transToAccountId
                    ).accountType!!.tallyOwing
                ) {
                    updateAccountOwing(
                        mTransaction.transAmount,
                        mTransaction.transToAccountId,
                        reverse
                    )
                }
            }
            if (!mTransaction.transFromAccountPending) {
                if (accountViewModel.getAccountAndType(
                        mTransaction.transFromAccountId
                    ).accountType!!.keepTotals
                ) {
                    updateAccountBalance(
                        mTransaction.transAmount,
                        mTransaction.transFromAccountId,
                        reverse
                    )
                }
                if (accountViewModel.getAccountAndType(
                        mTransaction.transFromAccountId
                    ).accountType!!.tallyOwing
                ) {
                    updateAccountOwing(
                        mTransaction.transAmount,
                        mTransaction.transFromAccountId,
                        !reverse
                    )
                }
            }
        }
        return true
    }

    fun isTransactionPending(accountId: Long): Boolean {
        return accountViewModel.getAccountAndType(
            accountId
        ).accountType!!.allowPending
    }

    fun deleteTransaction(mTransaction: Transactions) {
        doTransaction(mTransaction, true)
        transactionViewModel.deleteTransaction(
            mTransaction.transId,
            df.getCurrentTimeAsString()
        )
    }

    fun performTransaction(
        mTransaction: Transactions
    ) {
        doTransaction(mTransaction, false)
        transactionViewModel.insertTransaction(
            mTransaction
        )
    }

    fun updateTransaction(
        oldTransaction: Transactions,
        newTransaction: Transactions
    ) {
        //TODO:
    }

    private fun updateAccountBalance(
        amount: Double,
        accountId: Long,
        creditAccount: Boolean
    ) {
        val newBalance =
            accountViewModel.getAccount(accountId).accountBalance +
                    if (creditAccount) amount else -amount
        transactionViewModel.updateAccountBalance(
            newBalance,
            accountId,
            df.getCurrentTimeAsString()
        )
    }

    private fun updateAccountOwing(
        amount: Double,
        accountId: Long,
        creditAccount: Boolean
    ) {
        val newOwing =
            accountViewModel.getAccount(accountId).accountOwing +
                    if (creditAccount) amount else -amount
        transactionViewModel.updateAccountOwing(
            newOwing,
            accountId,
            df.getCurrentTimeAsString()
        )
    }

    private suspend fun getAccountAndType(accountId: Long): AccountAndType {
        return accountViewModel.getAccountAndType(accountId)
    }
}