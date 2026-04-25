package ms.mattschlenkrich.billsprojectionv2.common.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
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

    private val _budgetItemDetailed = mutableStateOf<BudgetItemDetailed?>(null)
    private val _oldTransactionFull = mutableStateOf<TransactionFull?>(null)
    private val _transactionDetailed = mutableStateOf<TransactionDetailed?>(null)
    private val _splitTransactionDetailed = mutableStateOf<TransactionDetailed?>(null)
    private val _budgetRuleDetailed = mutableStateOf<BudgetRuleDetailed?>(null)
    private val _account = mutableStateOf<AccountWithType?>(null)
    private val _accountType = mutableStateOf<AccountType?>(null)
    private val _requestedAccount = mutableStateOf<String?>("")
    private val _callingFragments = mutableStateOf<String?>("")
    private val _returnTo = mutableStateOf<String?>("")
    private val _returnToAsset = mutableStateOf<String?>(null)
    private val _returnToPayDay = mutableStateOf<String?>(null)
    private val _transferNum = mutableStateOf<Double?>(0.0)
    private val _updatingTransaction = mutableStateOf(false)

    fun setReturnToAsset(newAsset: String?) {
        if (_returnToAsset.value != newAsset) {
            _returnToAsset.value = newAsset
            _returnToPayDay.value = null
        }
    }

    fun getReturnToAsset(): String? {
        return _returnToAsset.value
    }

    fun setReturnToPayDay(newPayDay: String?) {
        _returnToPayDay.value = newPayDay
    }

    fun getReturnToPayDay(): String? {
        return _returnToPayDay.value
    }

    fun eraseAll() {
        _budgetItemDetailed.value = null
        _oldTransactionFull.value = null
        _transactionDetailed.value = null
        _splitTransactionDetailed.value = null
        _budgetRuleDetailed.value = null
        _account.value = null
        _accountType.value = null
        _requestedAccount.value = null
        _callingFragments.value = null
        _returnTo.value = null
        _returnToAsset.value = null
        _returnToPayDay.value = null
        _transferNum.value = 0.0
    }

    fun setOldTransaction(newTransactionFull: TransactionFull?) {
        _oldTransactionFull.value = newTransactionFull
    }

    fun getOldTransaction(): TransactionFull? {
        return _oldTransactionFull.value
    }

    fun setUpdatingTransaction(update: Boolean) {
        _updatingTransaction.value = update
    }

    fun getUpdatingTransaction(): Boolean {
        return _updatingTransaction.value
    }

    fun setSplitTransactionDetailed(newTransaction: TransactionDetailed?) {
        _splitTransactionDetailed.value = newTransaction
    }

    fun getSplitTransactionDetailed(): TransactionDetailed? {
        return _splitTransactionDetailed.value
    }

    fun setReturnTo(newReturnTo: String?) {
        _returnTo.value = newReturnTo
    }

    fun getReturnTo(): String? {
        return _returnTo.value
    }

    fun setTransferNum(newNum: Double?) {
        _transferNum.value = newNum
    }

    fun getTransferNum(): Double? {
        return _transferNum.value
    }

    fun setCallingFragments(newCallingFragments: String?) {
        _callingFragments.value = newCallingFragments
    }

    fun getCallingFragments(): String? {
        return _callingFragments.value
    }

    fun addCallingFragment(newFragment: String) {
        _callingFragments.value = "${_callingFragments.value}, $newFragment"
    }

    fun removeCallingFragment(oldFragment: String) {
        _callingFragments.value = _callingFragments.value?.replace(", $oldFragment", "")
        _callingFragments.value = _callingFragments.value?.replace(oldFragment, "")
    }

    fun setRequestedAccount(newRequestedAccount: String?) {
        _requestedAccount.value = newRequestedAccount
    }

    fun getRequestedAccount(): String? {
        return _requestedAccount.value
    }

    fun setAccountType(newAccountType: AccountType?) {
        _accountType.value = newAccountType
    }

    fun getAccountType(): AccountType? {
        return _accountType.value
    }

    fun setAccountWithType(newAccount: AccountWithType?) {
        _account.value = newAccount
    }

    fun getAccountWithType(): AccountWithType? {
        return _account.value
    }

    fun setBudgetRuleDetailed(newBudgetRule: BudgetRuleDetailed?) {
        _budgetRuleDetailed.value = newBudgetRule
    }

    fun getBudgetRuleDetailed(): BudgetRuleDetailed? {
        return _budgetRuleDetailed.value
    }

    fun setTransactionDetailed(newTransaction: TransactionDetailed?) {
        _transactionDetailed.value = newTransaction
    }

    fun getTransactionDetailed(): TransactionDetailed? {
        return _transactionDetailed.value
    }

    fun setBudgetItemDetailed(newBudgetDetailed: BudgetItemDetailed?) {
        _budgetItemDetailed.value = newBudgetDetailed
    }

    fun getBudgetItemDetailed(): BudgetItemDetailed? {
        return _budgetItemDetailed.value
    }

}