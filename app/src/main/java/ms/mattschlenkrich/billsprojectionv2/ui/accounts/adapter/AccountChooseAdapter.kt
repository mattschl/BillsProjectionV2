package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.ListChooseItemsBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountChooseFragment


private const val TAG = "AccountChoose"

class AccountChooseAdapter(
    val mainActivity: MainActivity,
    private val accountChooseFragment: AccountChooseFragment,
) : RecyclerView.Adapter<AccountChooseAdapter.AccountViewHolder>() {

    private val mainViewModel = mainActivity.mainViewModel

    class AccountViewHolder(val itemBinding: ListChooseItemsBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<AccountWithType>() {
        override fun areItemsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem.account.accountId == newItem.account.accountId && oldItem.account.accountName == newItem.account.accountName && oldItem.account.accountNumber == newItem.account.accountNumber
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AccountViewHolder {
        return AccountViewHolder(
            ListChooseItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: AccountViewHolder, position: Int
    ) {
        val curAccount = differ.currentList[position]
        holder.itemBinding.apply {
            tvItemName.text = curAccount.account.accountName
            if ((curAccount.accountType!!.tallyOwing || curAccount.accountType.keepTotals) &&
                curAccount.accountType.displayAsAsset
            ) {
                tvItemName.setTextColor(Color.RED)
            } else {
                tvItemName.setTextColor(Color.BLACK)
            }
            holder.itemView.setOnClickListener { chooseAccountAndPopulateCache(curAccount) }
        }
    }

    private fun chooseAccountAndPopulateCache(curAccount: AccountWithType) {
        if (mainViewModel.getCallingFragments() != null) {
            val mCallingFragment = mainViewModel.getCallingFragments()!!
            if (mCallingFragment.contains(FRAG_BUDGET_RULE_ADD) ||
                mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)
            ) {
                populateBudgetRuleDetailed(curAccount)
            } else if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
                mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
            ) {
                populateBudgetItemDetailed(curAccount)
            } else if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                populateSplitTransaction(curAccount)
            } else if (mCallingFragment.contains(FRAG_TRANS_ADD) ||
                mCallingFragment.contains(FRAG_TRANS_PERFORM) ||
                mCallingFragment.contains(FRAG_TRANS_UPDATE)
            ) {
                populateTransactionDetailed(curAccount)
            }
            gotoCallingFragment()
        }
    }

    private fun populateSplitTransaction(curAccount: AccountWithType) {
        val splitTrans = mainViewModel.getSplitTransactionDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT
        val splitTransactionDetailed = TransactionDetailed(
            splitTrans.transaction,
            splitTrans.budgetRule,
            if (isToAccount) curAccount.account else splitTrans.toAccount,
            if (!isToAccount) curAccount.account else splitTrans.fromAccount,
        )
        splitTransactionDetailed.transaction!!.transToAccountPending =
            curAccount.accountType!!.tallyOwing
        mainViewModel.setSplitTransactionDetailed(splitTransactionDetailed)
    }

    private fun populateTransactionDetailed(curAccount: AccountWithType) {
        val tempTrans = mainViewModel.getTransactionDetailed()!!

        tempTrans.transaction!!.transToAccountPending =
            curAccount.accountType!!.allowPending && curAccount.accountType.tallyOwing &&
                    mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT
        tempTrans.transaction.transFromAccountPending =
            curAccount.accountType.allowPending && curAccount.accountType.tallyOwing &&
                    mainViewModel.getRequestedAccount()!! == REQUEST_FROM_ACCOUNT
        Log.d(
            TAG, "ToAccountPending = ${
                curAccount.accountType.allowPending && curAccount.accountType.tallyOwing && mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT
            } \n" +
                    "FromAccountPending =  ${curAccount.accountType.allowPending && curAccount.accountType.tallyOwing && mainViewModel.getRequestedAccount()!! == REQUEST_FROM_ACCOUNT}"
        )
        val transactionDetailed = TransactionDetailed(
            tempTrans.transaction,
            tempTrans.budgetRule,
            if (mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT) curAccount.account else tempTrans.toAccount,
            if (mainViewModel.getRequestedAccount()!! == REQUEST_FROM_ACCOUNT) curAccount.account else tempTrans.fromAccount,
        )
        transactionDetailed.transaction!!.transToAccountPending =
            curAccount.accountType.tallyOwing
        mainViewModel.setTransactionDetailed(transactionDetailed)
    }

    private fun populateBudgetItemDetailed(curAccount: AccountWithType) {
        val tempBudgetItem = mainViewModel.getBudgetItemDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT
        mainViewModel.setBudgetItemDetailed(
            BudgetItemDetailed(
                tempBudgetItem.budgetItem,
                tempBudgetItem.budgetRule,
                if (isToAccount) curAccount.account else tempBudgetItem.toAccount,
                if (!isToAccount) curAccount.account else tempBudgetItem.fromAccount,
            )
        )
    }

    private fun populateBudgetRuleDetailed(curAccount: AccountWithType) {
        val tempBudgetRule = mainViewModel.getBudgetRuleDetailed()!!
        val isToAccount = mainViewModel.getRequestedAccount()!! == REQUEST_TO_ACCOUNT
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                tempBudgetRule.budgetRule,
                if (isToAccount) curAccount.account else tempBudgetRule.toAccount,
                if (!isToAccount) curAccount.account else tempBudgetRule.fromAccount,
            )
        )
        Log.d(
            TAG,
            "toAccount is ${mainViewModel.getBudgetRuleDetailed()?.toAccount?.accountName} " +
                    "\nfromAccount is ${mainViewModel.getBudgetRuleDetailed()?.fromAccount?.accountName}"
        )
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragments()!!
        if (callingFragment.contains(FRAG_BUDGET_RULE_ADD)) {
            accountChooseFragment.gotoBudgetRuleAddFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_ADD)) {
            accountChooseFragment.gotoBudgetItemAddFragment()
        } else if (callingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
            accountChooseFragment.gotoTransactionSplitFragment()
        } else if (callingFragment.contains(FRAG_TRANS_ADD)) {
            accountChooseFragment.gotoTransactionAddFragment()
        } else if (callingFragment.contains(FRAG_TRANS_UPDATE)) {
            accountChooseFragment.gotoTransactionUpdateFragment()
        } else if (callingFragment.contains(FRAG_TRANS_PERFORM)) {
            accountChooseFragment.gotoTransactionPerformFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_RULE_UPDATE)) {
            accountChooseFragment.gotoBudgetRuleUpdateFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)) {
            accountChooseFragment.gotoBudgetItemUpdateFragment()
        }
    }
}