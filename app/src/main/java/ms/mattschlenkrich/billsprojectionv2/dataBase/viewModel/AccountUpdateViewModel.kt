@file:Suppress(
    "unused"
)

package ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    ) {
        withContext(Dispatchers.IO) {
            // Update TO account
            val toAcc = accountViewModel.getAccountAndType(mTransaction.transToAccountId)
            if (!mTransaction.transToAccountPending) {
                applyAtomicUpdate(
                    toAcc.account.accountId,
                    mTransaction.transAmount,
                    !reverse,
                    toAcc.accountType!!.keepTotals,
                    toAcc.accountType.tallyOwing
                )
            }

            // Update FROM account
            val fromAcc = accountViewModel.getAccountAndType(mTransaction.transFromAccountId)
            if (!mTransaction.transFromAccountPending) {
                applyAtomicUpdate(
                    fromAcc.account.accountId,
                    mTransaction.transAmount,
                    reverse,
                    fromAcc.accountType!!.keepTotals,
                    fromAcc.accountType.tallyOwing
                )
            }
        }
    }

    private suspend fun applyAtomicUpdate(
        accountId: Long,
        amount: Double,
        isCredit: Boolean,
        keepTotals: Boolean,
        tallyOwing: Boolean
    ) {
        val account = accountViewModel.getAccount(accountId)
        var newBalance = account.accountBalance
        var newOwing = account.accountOwing

        if (keepTotals) {
            newBalance += if (isCredit) amount else -amount
        }
        if (tallyOwing) {
            newOwing += if (isCredit) -amount else amount
        }

        transactionViewModel.updateAccountBalanceAndOwing(
            newBalance,
            newOwing,
            accountId,
            df.getCurrentTimeAsString()
        )
    }

    suspend fun isTransactionPending(accountId: Long): Boolean {
        val accType = accountViewModel.getAccountAndType(accountId).accountType!!
        return accType.allowPending && accType.tallyOwing
    }


    suspend fun deleteTransaction(mTransaction: Transactions) {
        doAccountUpdates(mTransaction, true)
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
        withContext(Dispatchers.IO) {
            // Check To Account changes
            if (oldTransaction.transToAccountId != newTransaction.transToAccountId ||
                oldTransaction.transAmount != newTransaction.transAmount ||
                oldTransaction.transToAccountPending != newTransaction.transToAccountPending
            ) {
                // Undo old To if it was not pending
                if (!oldTransaction.transToAccountPending) {
                    val oldToAcc =
                        accountViewModel.getAccountAndType(oldTransaction.transToAccountId)
                    applyAtomicUpdate(
                        oldTransaction.transToAccountId,
                        oldTransaction.transAmount,
                        false,
                        oldToAcc.accountType!!.keepTotals,
                        oldToAcc.accountType.tallyOwing
                    )
                }
                // Apply new To if it is not pending
                if (!newTransaction.transToAccountPending) {
                    val newToAcc =
                        accountViewModel.getAccountAndType(newTransaction.transToAccountId)
                    applyAtomicUpdate(
                        newTransaction.transToAccountId,
                        newTransaction.transAmount,
                        true,
                        newToAcc.accountType!!.keepTotals,
                        newToAcc.accountType.tallyOwing
                    )
                }
            }

            // Check From Account changes
            if (oldTransaction.transFromAccountId != newTransaction.transFromAccountId ||
                oldTransaction.transAmount != newTransaction.transAmount ||
                oldTransaction.transFromAccountPending != newTransaction.transFromAccountPending
            ) {
                // Undo old From if it was not pending
                if (!oldTransaction.transFromAccountPending) {
                    val oldFromAcc =
                        accountViewModel.getAccountAndType(oldTransaction.transFromAccountId)
                    applyAtomicUpdate(
                        oldTransaction.transFromAccountId,
                        oldTransaction.transAmount,
                        true,
                        oldFromAcc.accountType!!.keepTotals,
                        oldFromAcc.accountType.tallyOwing
                    )
                }
                // Apply new From if it is not pending
                if (!newTransaction.transFromAccountPending) {
                    val newFromAcc =
                        accountViewModel.getAccountAndType(newTransaction.transFromAccountId)
                    applyAtomicUpdate(
                        newTransaction.transFromAccountPendingId(),
                        newTransaction.transAmount,
                        false,
                        newFromAcc.accountType!!.keepTotals,
                        newFromAcc.accountType.tallyOwing
                    )
                }
            }
            transactionViewModel.updateTransaction(newTransaction)
        }
    }

    private fun Transactions.transFromAccountPendingId(): Long = this.transFromAccountId

    suspend fun updateTransactionWithoutAccountUpdate(
        transaction: Transactions
    ) {
        transactionViewModel.updateTransaction(transaction)
    }

    suspend fun updateAccountBalance(
        amount: Double, accountId: Long
    ) {
        transactionViewModel.updateAccountBalance(
            amount, accountId, df.getCurrentTimeAsString()
        )
    }

    suspend fun updateAccountOwing(
        amount: Double, accountId: Long
    ) {
        transactionViewModel.updateAccountOwing(amount, accountId, df.getCurrentTimeAsString())
    }
}