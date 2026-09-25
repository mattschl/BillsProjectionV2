package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

object BudgetViewCalculations {

    fun calculatePendingAmount(
        pendingList: List<TransactionDetailed>,
        selectedAsset: String,
        assetList: List<String>,
    ): Double {
        var amount = 0.0
        pendingList.forEach { details ->
            if (details.toAccount?.accountName == selectedAsset) {
                amount += details.transaction?.transAmount ?: 0.0
            } else if (details.fromAccount?.accountName == selectedAsset) {
                amount -= details.transaction?.transAmount ?: 0.0
            } else if (selectedAsset == ALL_ITEMS) {
                if (assetList.contains(details.toAccount?.accountName)) {
                    amount += details.transaction?.transAmount ?: 0.0
                } else if (assetList.contains(details.fromAccount?.accountName)) {
                    amount -= details.transaction?.transAmount ?: 0.0
                }
            }
        }
        return amount
    }

    fun calculateSelectedSum(
        selectedItems: Set<String>,
        allBudgetList: List<BudgetItemDetailed>,
        selectedAsset: String,
        assetList: List<String>,
    ): Double {
        if (selectedItems.isEmpty()) return 0.0
        var netChange = 0.0
        allBudgetList.forEach { details ->
            val item = details.budgetItem ?: return@forEach
            val key = "${item.biRuleId}_${item.biProjectedDate}"
            if (selectedItems.contains(key)) {
                val isCredit = if (selectedAsset == ALL_ITEMS) {
                    assetList.contains(details.toAccount?.accountName)
                } else {
                    details.toAccount?.accountName == selectedAsset
                }

                if (isCredit) {
                    netChange += item.biProjectedAmount
                } else {
                    val isDebit = if (selectedAsset == ALL_ITEMS) {
                        assetList.contains(details.fromAccount?.accountName)
                    } else {
                        details.fromAccount?.accountName == selectedAsset
                    }
                    if (isDebit) {
                        netChange -= item.biProjectedAmount
                    }
                }
            }
        }
        return netChange
    }

    fun calculateSelectedPendingSum(
        selectedPendingItems: Set<Long>,
        pendingList: List<TransactionDetailed>,
        selectedAsset: String,
        assetList: List<String>,
    ): Double {
        if (selectedPendingItems.isEmpty()) return 0.0
        var netChange = 0.0
        pendingList.forEach { details ->
            val trans = details.transaction ?: return@forEach
            if (selectedPendingItems.contains(trans.transId)) {
                val isCredit = if (selectedAsset == ALL_ITEMS) {
                    assetList.contains(details.toAccount?.accountName)
                } else {
                    details.toAccount?.accountName == selectedAsset
                }

                if (isCredit) {
                    netChange += trans.transAmount
                } else {
                    val isDebit = if (selectedAsset == ALL_ITEMS) {
                        assetList.contains(details.fromAccount?.accountName)
                    } else {
                        details.fromAccount?.accountName == selectedAsset
                    }
                    if (isDebit) {
                        netChange -= trans.transAmount
                    }
                }
            }
        }
        return netChange
    }
}