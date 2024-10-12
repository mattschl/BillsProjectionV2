package ms.mattschlenkrich.billsprojectionv2.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    fun deleteTransaction(
        mTransaction: Transactions
    ): Boolean {
        if (!mTransaction.transToAccountPending) {
            if (getAccountAndType(
                    mTransaction.transToAccountId
                ).accountType!!.keepTotals
            ) {
                updateAccountBalance(
                    mTransaction.transAmount,
                    mTransaction.transToAccountId,
                    false
                )
            }
            if (getAccountAndType(
                    mTransaction.transToAccountId
                ).accountType!!.tallyOwing
            ) {
                updateAccountOwing(
                    mTransaction.transAmount,
                    mTransaction.transToAccountId,
                    true
                )
            }
        }
        if (!mTransaction.transFromAccountPending) {
            if (getAccountAndType(
                    mTransaction.transFromAccountId
                ).accountType!!.keepTotals
            ) {
                updateAccountBalance(
                    mTransaction.transAmount,
                    mTransaction.transFromAccountId,
                    true
                )
            }
            if (getAccountAndType(
                    mTransaction.transFromAccountId
                ).accountType!!.tallyOwing
            ) {
                updateAccountOwing(
                    mTransaction.transAmount,
                    mTransaction.transFromAccountId,
                    false
                )
            }
        }
        return true
    }

    fun performTransaction(
        mTransaction: Transactions
    ) {
        //TODO:
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

    private fun getAccountAndType(accountId: Long): AccountAndType {
        return accountViewModel.getAccountAndType(accountId)
    }
}