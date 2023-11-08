package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.TransactionFull

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {

    private var asset: String? = ""
    private var payDay: String? = ""
    private var budgetItem: BudgetDetailed? = null
    private var oldTransactionFull: TransactionFull? = null
    private var transactionDetailed: TransactionDetailed? = null
    private var splitTransactionDetailed: TransactionDetailed? = null
    private var budgetRuleDetailed: BudgetRuleDetailed? = null
    private var account: AccountWithType? = null
    private var accountType: AccountType? = null
    private var requestedAccount: String? = ""
    private var callingFragments: String? = ""
    private var returnTo: String? = ""
    private var transferNum: Double? = 0.0
    private var updatingTransaction: Boolean = false

    fun eraseAll() {
        budgetItem = null
        oldTransactionFull = null
        transactionDetailed = null
        splitTransactionDetailed = null
        budgetRuleDetailed = null
        account = null
        accountType = null
        requestedAccount = ""
        callingFragments = ""
        returnTo = ""
        transferNum = 0.0
    }

    fun setOldTransaction(newTransactionFull: TransactionFull) {
        oldTransactionFull = newTransactionFull
    }

    fun getOldTransaction(): TransactionFull? {
        return oldTransactionFull
    }

    fun setUpdatingTransaction(update: Boolean) {
        updatingTransaction = update
    }

    fun getUpdatingTransaction(): Boolean {
        return updatingTransaction
    }

    fun setSplitTransactionDetailed(newTransaction: TransactionDetailed?) {
        splitTransactionDetailed = newTransaction
    }

    fun getSplitTransactionDetailed(): TransactionDetailed? {
        return splitTransactionDetailed
    }

    fun setReturnTo(newReturnTo: String?) {
        returnTo = newReturnTo
    }

    fun getReturnTo(): String? {
        return returnTo
    }

    fun setTransferNum(newNum: Double?) {
        transferNum = newNum
    }

    fun getTransferNum(): Double? {
        return transferNum
    }

    fun setCallingFragments(newCallingFragments: String?) {
        callingFragments = newCallingFragments
    }

    fun getCallingFragments(): String? {
        return callingFragments
    }

    fun setRequestedAccount(newRequestedAccount: String?) {
        requestedAccount = newRequestedAccount
    }

    fun getRequestedAccount(): String? {
        return requestedAccount
    }

    fun setAccountType(newAccountType: AccountType?) {
        accountType = newAccountType
    }

    fun getAccountType(): AccountType? {
        return accountType
    }

    fun setAccountWithType(newAccount: AccountWithType?) {
        account = newAccount
    }

    fun getAccountWithType(): AccountWithType? {
        return account
    }

    fun setBudgetRuleDetailed(newBudgetRule: BudgetRuleDetailed?) {
        budgetRuleDetailed = newBudgetRule
    }

    fun getBudgetRuleDetailed(): BudgetRuleDetailed? {
        return budgetRuleDetailed
    }

    fun setTransactionDetailed(newTransaction: TransactionDetailed?) {
        transactionDetailed = newTransaction
    }

    fun getTransactionDetailed(): TransactionDetailed? {
        return transactionDetailed
    }

    fun setBudgetItem(newBudgetDetailed: BudgetDetailed?) {
        budgetItem = newBudgetDetailed
    }

    fun getBudgetItem(): BudgetDetailed? {
        return budgetItem
    }

    fun setPayDay(newPayDay: String?) {
        payDay = newPayDay
    }

    fun getPayDay(): String? {
        return payDay
    }

    fun setAsset(newAsset: String?) {
        asset = newAsset
    }

    fun getAsset(): String? {
        return asset
    }
}