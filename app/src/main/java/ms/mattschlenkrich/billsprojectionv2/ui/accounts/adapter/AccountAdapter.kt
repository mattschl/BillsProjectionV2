package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
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
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountsFragment
import java.util.Random


//private const val PARENT_TAG = FRAG_ACCOUNTS

class AccountAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
    private val accountsFragment: AccountsFragment,
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private val mainViewModel = mainActivity.mainViewModel

    private var mBudgetItem = BudgetItemDetailed(
        mainViewModel.getBudgetItemDetailed()?.budgetItem,
        mainViewModel.getBudgetItemDetailed()?.budgetRule,
        mainViewModel.getBudgetItemDetailed()?.toAccount,
        mainViewModel.getBudgetItemDetailed()?.fromAccount
    )

    private var mBudgetRuleDetailed = BudgetRuleDetailed(
        mainViewModel.getBudgetRuleDetailed()?.budgetRule,
        mainViewModel.getBudgetRuleDetailed()?.toAccount,
        mainViewModel.getBudgetRuleDetailed()?.fromAccount
    )
    private var mTransactionDetailed = TransactionDetailed(
        mainViewModel.getTransactionDetailed()?.transaction,
        mainViewModel.getTransactionDetailed()?.budgetRule,
        mainViewModel.getTransactionDetailed()?.toAccount,
        mainViewModel.getTransactionDetailed()?.fromAccount
    )
    private var mSplitTransactionDetailed = TransactionDetailed(
        mainViewModel.getSplitTransactionDetailed()?.transaction,
        mainViewModel.getSplitTransactionDetailed()?.budgetRule,
        mainViewModel.getSplitTransactionDetailed()?.toAccount,
        mainViewModel.getSplitTransactionDetailed()?.fromAccount
    )
    private val accountViewModel = mainActivity.accountViewModel

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class AccountViewHolder(val itemBinding: AccountLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<AccountWithType>() {
        override fun areItemsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem.account.accountId == newItem.account.accountId && oldItem.account.accountName == newItem.account.accountName && oldItem.account.accountNumber == newItem.account.accountNumber
        }

        override fun areContentsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AccountViewHolder {
        return AccountViewHolder(
            AccountLayoutBinding.inflate(
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
            tvAccountName.text = curAccount.account.accountName
            var info = if (curAccount.account.accountNumber.isNotEmpty()) {
                "# ${curAccount.account.accountNumber} "
            } else {
                ""
            }
            if (curAccount.account.accountBalance != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.balance) + cf.displayDollars(
                    curAccount.account.accountBalance
                )
            }
            if (curAccount.account.accountOwing != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.owing) + cf.displayDollars(
                    curAccount.account.accountOwing
                )
            }
            if (curAccount.account.accBudgetedAmount != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.budgeted) + cf.displayDollars(
                    curAccount.account.accBudgetedAmount
                )
            }
            if (curAccount.account.accountCreditLimit != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.credit_limit) + cf.displayDollars(
                    curAccount.account.accountCreditLimit
                )
            }
            if (curAccount.account.accIsDeleted) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.deleted)
            }
            if (info.isBlank()) {
                info = mView.context.getString(R.string.no_info)
            }
            if (info == mView.context.getString(R.string.no_info)) {
                tvAccountInfo.visibility = ViewGroup.GONE
            } else {
                tvAccountInfo.visibility = ViewGroup.VISIBLE
                tvAccountInfo.text = info
            }
            tvAccType.text = curAccount.accountType?.accountType

            val random = Random()
            val color = Color.argb(
                255, random.nextInt(256), random.nextInt(256), random.nextInt(256)
            )
            ibAccountColor.setBackgroundColor(color)

            holder.itemView.setOnClickListener { chooseAccount(curAccount) }
            holder.itemView.setOnLongClickListener {
                chooseOptionsForAccount(curAccount)
                false
            }
        }
    }

    private fun chooseOptionsForAccount(
        curAccount: AccountWithType
    ) {
        val callingFragment = mainViewModel.getCallingFragments()
        if (callingFragment == null || callingFragment.contains("BudgetRule") ||
            callingFragment.contains("Account")
        ) {
            AlertDialog.Builder(mView.context).setTitle(
                mView.context.getString(R.string.choose_an_action_for) + curAccount.account.accountName
            ).setItems(
                arrayOf(
                    mView.context.getString(R.string.edit_this_account),
                    mView.context.getString(R.string.delete_this_account),
                    mView.context.getString(R.string.view_a_summary_of_transactions_using_this_account)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> gotoUpdateAccount(curAccount)
                    1 -> deleteAccount(curAccount)
                    2 -> gotoTransactionAverage(curAccount)
                }

            }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
        } else {
            showMessage(mView.context.getString(R.string.editing_is_not_allowed_right_now))
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun deleteAccount(curAccount: AccountWithType) {
        accountViewModel.deleteAccount(
            curAccount.account.accountId, df.getCurrentTimeAsString()
        )
    }

    private fun chooseAccount(
        curAccount: AccountWithType,
    ) {
        if (mainViewModel.getCallingFragments() != null) {
            if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
                gotoTransactionAverage(curAccount)
            } else {
                when (mainViewModel.getRequestedAccount()) {
                    REQUEST_TO_ACCOUNT -> {
                        mBudgetRuleDetailed.toAccount = curAccount.account
                        mainViewModel.setBudgetRuleDetailed(mBudgetRuleDetailed)
                        if (mainViewModel.getCallingFragments()!!
                                .contains(FRAG_TRANSACTION_SPLIT)
                        ) {
                            mSplitTransactionDetailed.toAccount = curAccount.account
                            mSplitTransactionDetailed.transaction!!.transToAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setSplitTransactionDetailed(
                                mSplitTransactionDetailed
                            )
                        } else {
                            mTransactionDetailed.toAccount = curAccount.account
                            mTransactionDetailed.transaction!!.transToAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setTransactionDetailed(mTransactionDetailed)
                        }
                        mBudgetItem.toAccount = curAccount.account
                        mainViewModel.setBudgetItemDetailed(mBudgetItem)
                        gotoCallingFragment()

                    }

                    REQUEST_FROM_ACCOUNT -> {
                        mBudgetRuleDetailed.fromAccount = curAccount.account
                        mainViewModel.setBudgetRuleDetailed(mBudgetRuleDetailed)
                        val callingFragment = mainViewModel.getCallingFragments()!!
                        if (callingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                            mSplitTransactionDetailed.fromAccount = curAccount.account
                            mSplitTransactionDetailed.transaction?.transFromAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setSplitTransactionDetailed(
                                mSplitTransactionDetailed
                            )
                        } else if (callingFragment.contains(FRAG_TRANS_ADD) || callingFragment.contains(
                                FRAG_TRANS_PERFORM
                            ) || callingFragment.contains(FRAG_TRANS_UPDATE)
                        ) {
                            mTransactionDetailed.fromAccount = curAccount.account
                            mTransactionDetailed.transaction?.transFromAccountPending =
                                curAccount.accountType!!.tallyOwing
                            mainViewModel.setTransactionDetailed(mTransactionDetailed)
                        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_ADD) || callingFragment.contains(
                                FRAG_BUDGET_ITEM_UPDATE
                            )
                        ) {
                            mBudgetItem.fromAccount = curAccount.account
                            mainViewModel.setBudgetItemDetailed(mBudgetItem)
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
        accountsFragment.gotoTransactionAverageFragment()
    }

    private fun gotoUpdateAccount(
        curAccount: AccountWithType,
    ) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setAccountWithType(curAccount)
        accountsFragment.gotoAccountUpdateFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.removeCallingFragment(FRAG_ACCOUNTS)
        val callingFragment = mainViewModel.getCallingFragments()!!
        if (callingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
            accountsFragment.gotoTransactionSplitFragment()
        }
        if (callingFragment.contains(FRAG_BUDGET_RULE_ADD)) {
            accountsFragment.gotoBudgetRuleAddFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_RULE_UPDATE)) {
            accountsFragment.gotoBudgetRuleUpdateFragment()
        } else if (callingFragment.contains(FRAG_TRANS_ADD)) {
            accountsFragment.gotoTransactionAddFragment()
        } else if (callingFragment.contains(FRAG_TRANS_UPDATE)) {
            accountsFragment.gotoTransactionUpdateFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_ADD)) {
            accountsFragment.gotoBudgetItemAddFragment()
        } else if (callingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)) {
            accountsFragment.gotoBudgetItemUpdateFragment()
        } else if (callingFragment.contains(FRAG_TRANS_PERFORM)) {
            accountsFragment.gotoTransactionPerformFragment()
        }
    }
}