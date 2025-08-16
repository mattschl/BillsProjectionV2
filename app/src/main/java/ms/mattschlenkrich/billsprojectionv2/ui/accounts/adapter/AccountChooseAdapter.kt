package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
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


//private const val PARENT_TAG = FRAG_ACCOUNTS

class AccountChooseAdapter(
    val mainActivity: MainActivity,
    private val parentTag: String,
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
            holder.itemView.setOnClickListener { chooseAccount(curAccount) }
        }
    }

    private fun chooseAccount(
        curAccount: AccountWithType,
    ) {
        if (mainViewModel.getCallingFragments() != null) {
            if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
                gotoTransactionAverage(curAccount)
            } else {
                val mCallingFragment = mainViewModel.getCallingFragments()!!
                when (mainViewModel.getRequestedAccount()) {
                    REQUEST_TO_ACCOUNT -> {
                        if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)
                        ) {
                            val splitTransactionDetailed = TransactionDetailed(
                                mainViewModel.getTransactionDetailed()?.transaction,
                                mainViewModel.getTransactionDetailed()?.budgetRule,
                                curAccount.account,
                                mainViewModel.getTransactionDetailed()?.fromAccount,
                            )
                            splitTransactionDetailed.transaction!!.transToAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setSplitTransactionDetailed(splitTransactionDetailed)
                        } else if (mCallingFragment.contains(FRAG_TRANS_ADD) ||
                            mCallingFragment.contains(FRAG_TRANS_PERFORM) ||
                            mCallingFragment.contains(FRAG_TRANS_UPDATE)
                        ) {
                            val transactionDetailed = TransactionDetailed(
                                mainViewModel.getTransactionDetailed()?.transaction,
                                mainViewModel.getTransactionDetailed()?.budgetRule,
                                curAccount.account,
                                mainViewModel.getTransactionDetailed()?.fromAccount,
                            )
                            transactionDetailed.transaction!!.transToAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setTransactionDetailed(transactionDetailed)
                        } else if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
                            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
                        ) {
                            mainViewModel.setBudgetItemDetailed(
                                BudgetItemDetailed(
                                    mainViewModel.getBudgetItemDetailed()?.budgetItem,
                                    mainViewModel.getBudgetItemDetailed()?.budgetRule,
                                    curAccount.account,
                                    mainViewModel.getBudgetItemDetailed()?.fromAccount,
                                )
                            )
                        } else if (mCallingFragment.contains(FRAG_BUDGET_RULE_ADD) ||
                            mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)
                        ) {
                            mainViewModel.setBudgetRuleDetailed(
                                BudgetRuleDetailed(
                                    mainViewModel.getBudgetRuleDetailed()?.budgetRule,
                                    curAccount.account,
                                    mainViewModel.getBudgetRuleDetailed()?.fromAccount,
                                )
                            )
                        }
                        gotoCallingFragment()
                    }

                    REQUEST_FROM_ACCOUNT -> {
                        if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                            val splitTransactionDetailed = TransactionDetailed(
                                mainViewModel.getTransactionDetailed()?.transaction,
                                mainViewModel.getTransactionDetailed()?.budgetRule,
                                mainViewModel.getTransactionDetailed()?.toAccount,
                                curAccount.account,
                            )
                            splitTransactionDetailed.transaction?.transFromAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setSplitTransactionDetailed(splitTransactionDetailed)
                        } else if (mCallingFragment.contains(FRAG_TRANS_ADD) ||
                            mCallingFragment.contains(FRAG_TRANS_PERFORM) ||
                            mCallingFragment.contains(FRAG_TRANS_UPDATE)
                        ) {
                            val transactionDetailed = TransactionDetailed(
                                mainViewModel.getTransactionDetailed()?.transaction,
                                mainViewModel.getTransactionDetailed()?.budgetRule,
                                mainViewModel.getTransactionDetailed()?.toAccount,
                                curAccount.account,
                            )
                            transactionDetailed.transaction?.transFromAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setTransactionDetailed(transactionDetailed)
                        } else if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
                            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
                        ) {
                            mainViewModel.setBudgetItemDetailed(
                                BudgetItemDetailed(
                                    mainViewModel.getBudgetItemDetailed()?.budgetItem,
                                    mainViewModel.getBudgetItemDetailed()?.budgetRule,
                                    mainViewModel.getBudgetItemDetailed()?.toAccount,
                                    curAccount.account,
                                )
                            )
                        } else if (mCallingFragment.contains(FRAG_BUDGET_RULE_ADD) ||
                            mCallingFragment.contains(FRAG_BUDGET_RULE_UPDATE)
                        ) {
                            mainViewModel.setBudgetRuleDetailed(
                                BudgetRuleDetailed(
                                    mainViewModel.getBudgetRuleDetailed()?.budgetRule,
                                    mainViewModel.getBudgetRuleDetailed()?.toAccount,
                                    curAccount.account
                                )
                            )
                        }
                        gotoCallingFragment()
                    }
                }
            }
        }
    }

    private fun gotoTransactionAverage(curAccount: AccountWithType) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setAccountWithType(curAccount)
        mainViewModel.setBudgetRuleDetailed(null)
        accountChooseFragment.gotoTransactionAverageFragment()
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragments()!!
        if (callingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
            accountChooseFragment.gotoTransactionSplitFragment()
        } else if (callingFragment.contains(FRAG_TRANS_ADD)) {
            accountChooseFragment.gotoTransactionAddFragment()
        } else if (callingFragment.contains(FRAG_TRANS_UPDATE)) {
            accountChooseFragment.gotoTransactionUpdateFragment()
        } else if (callingFragment.contains(FRAG_TRANS_PERFORM)) {
            accountChooseFragment.gotoTransactionPerformFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_RULE_ADD)) {
            accountChooseFragment.gotoBudgetRuleAddFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_RULE_UPDATE)) {
            accountChooseFragment.gotoBudgetRuleUpdateFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_ADD)) {
            accountChooseFragment.gotoBudgetItemAddFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)) {
            accountChooseFragment.gotoBudgetItemUpdateFragment()
        }
    }
}