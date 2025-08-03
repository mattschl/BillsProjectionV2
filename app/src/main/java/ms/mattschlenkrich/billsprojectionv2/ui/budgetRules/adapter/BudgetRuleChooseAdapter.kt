package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.ListBudgetRuleChooseBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleChooseFragment

//private const val TAG = ADAPTER_BUDGET_RULE
//private const val PARENT_TAG = FRAG_BUDGET_RULES

class BudgetRuleChooseAdapter(
    val mainActivity: MainActivity,
    private val parentTag: String,
    private val budgetRuleChooseFragment: BudgetRuleChooseFragment,
) : RecyclerView.Adapter<BudgetRuleChooseAdapter.BudgetRuleViewHolder>() {
    private val mainViewModel = mainActivity.mainViewModel
    private val vf = VisualsFunctions()

    class BudgetRuleViewHolder(
        val itemBinding: ListBudgetRuleChooseBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
        override fun areItemsTheSame(
            oldItem: BudgetRuleDetailed, newItem: BudgetRuleDetailed
        ): Boolean {
            return oldItem.budgetRule!!.ruleId == newItem.budgetRule!!.ruleId
        }

        override fun areContentsTheSame(
            oldItem: BudgetRuleDetailed, newItem: BudgetRuleDetailed
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BudgetRuleViewHolder {
        return BudgetRuleViewHolder(
            ListBudgetRuleChooseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetRuleViewHolder, position: Int
    ) {
        val budgetRuleDetailed = differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text = budgetRuleDetailed.budgetRule!!.budgetRuleName
//        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
//        holder.itemBinding.tvToAccount.text = info
//        info = "From: " + budgetRuleDetailed.fromAccount!!.accountName
//        holder.itemBinding.tvFromAccount.text = info
        holder.itemBinding.ibColor.setBackgroundColor(vf.getRandomColorInt())
        holder.itemView.setOnClickListener { chooseBudgetRule(budgetRuleDetailed) }
    }

    private fun chooseBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed
    ) {
        if (mainViewModel.getCallingFragments() != null) {
            mainViewModel.removeCallingFragment(parentTag)
            mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
            val mCallingFragment = mainViewModel.getCallingFragments()!!
            if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)) {
                val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
                mTransactionSplit?.budgetRule = budgetRuleDetailed.budgetRule
                mainViewModel.setSplitTransactionDetailed(mTransactionSplit)
            } else {
                val mTransaction = mainViewModel.getTransactionDetailed()
                mTransaction?.budgetRule = budgetRuleDetailed.budgetRule
                mainViewModel.setTransactionDetailed(mTransaction)
            }
            if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) || mCallingFragment.contains(
                    FRAG_BUDGET_ITEM_UPDATE
                )
            ) {
                val mBudgetDetailed = mainViewModel.getBudgetItemDetailed()
                mBudgetDetailed?.budgetRule = budgetRuleDetailed.budgetRule
                mainViewModel.setBudgetItemDetailed(mBudgetDetailed)
            }
            gotoCallingFragment()
        }
    }

    private fun gotoCallingFragment() {
        val mCallingFragment = mainViewModel.getCallingFragments()!!
        when {
            mCallingFragment.contains(FRAG_TRANSACTION_SPLIT) -> {
                budgetRuleChooseFragment.gotoTransactionSplitFragment()
            }

            mCallingFragment.contains(FRAG_TRANS_ADD) -> {
                budgetRuleChooseFragment.gotoTransactionAddFragment()
            }

            mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) -> {
                budgetRuleChooseFragment.gotoBudgetItemAddFragment()
            }

            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE) -> {
                budgetRuleChooseFragment.gotoBudgetItemUpdateFragment()
            }
        }
    }
}