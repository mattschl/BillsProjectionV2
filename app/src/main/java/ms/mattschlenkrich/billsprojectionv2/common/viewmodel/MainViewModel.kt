package ms.mattschlenkrich.billsprojectionv2.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionFull

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {

    private var budgetItemDetailed: BudgetItemDetailed? = null
    private var oldTransactionFull: TransactionFull? = null
    private var transactionDetailed: TransactionDetailed? = null
    private var splitTransactionDetailed: TransactionDetailed? = null
    private var budgetRuleDetailed: BudgetRuleDetailed? = null
    private var account: AccountWithType? = null
    private var accountType: AccountType? = null
    private var requestedAccount: String? = ""
    private var callingFragments: String? = ""
    private var returnTo: String? = ""
    private var returnToAsset: String? = ""
    private var returnToPayDay: String? = null
    private var transferNum: Double? = 0.0
    private var updatingTransaction: Boolean = false

    fun setReturnToAsset(newAsset: String?) {
        returnToAsset = newAsset
    }

    fun getReturnToAsset(): String? {
        return returnToAsset
    }

    fun setReturnToPayDay(newPayDay: String?) {
        returnToPayDay = newPayDay
    }

    fun getReturnToPayDay(): String? {
        return returnToPayDay
    }

    fun eraseAll() {
        budgetItemDetailed = null
        oldTransactionFull = null
        transactionDetailed = null
        splitTransactionDetailed = null
        budgetRuleDetailed = null
        account = null
        accountType = null
        requestedAccount = null
        callingFragments = null
        returnTo = null
        transferNum = 0.0
    }

    fun setOldTransaction(newTransactionFull: TransactionFull?) {
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

    fun addCallingFragment(newFragment: String) {
        callingFragments = "$callingFragments, $newFragment"
    }

    fun removeCallingFragment(oldFragment: String) {
        callingFragments = callingFragments?.replace(", $oldFragment", "")
        callingFragments = callingFragments?.replace(oldFragment, "")
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

    fun setBudgetItemDetailed(newBudgetDetailed: BudgetItemDetailed?) {
        budgetItemDetailed = newBudgetDetailed
    }

    fun getBudgetItemDetailed(): BudgetItemDetailed? {
        return budgetItemDetailed
    }

}