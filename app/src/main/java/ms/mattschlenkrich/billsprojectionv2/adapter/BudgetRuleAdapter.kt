package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_RULE
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules.BudgetRuleFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = ADAPTER_BUDGET_RULE
private const val PARENT_TAG = FRAG_BUDGET_RULES

class BudgetRuleAdapter(
    private val mainViewModel: MainViewModel,
    private val context: Context
) : RecyclerView.Adapter<BudgetRuleAdapter.BudgetRuleViewHolder>() {

    private val cf = CommonFunctions()

    class BudgetRuleViewHolder(
        val itemBinding: BudgetRuleLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule!!.ruleId ==
                        newItem.budgetRule!!.ruleId
            }

            override fun areContentsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BudgetRuleViewHolder {
        return BudgetRuleViewHolder(
            BudgetRuleLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetRuleViewHolder, position: Int
    ) {
        val budgetRuleDetailed =
            differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text =
            budgetRuleDetailed.budgetRule!!.budgetRuleName
        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + budgetRuleDetailed.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        val amount =
            cf.displayDollars(budgetRuleDetailed.budgetRule!!.budgetAmount)
        val frequencyTypes =
            context.resources.getStringArray(R.array.frequency_types)
        val frequencyType =
            frequencyTypes[budgetRuleDetailed.budgetRule!!.budFrequencyTypeId]
        val daysOfWeek =
            context.resources.getStringArray(R.array.days_of_week)
        val dayOfWeek =
            daysOfWeek[budgetRuleDetailed.budgetRule!!.budDayOfWeekId]
        info = "$amount " + frequencyType +
                " X " + budgetRuleDetailed.budgetRule!!.budFrequencyCount +
                "\nOn " + dayOfWeek
        holder.itemBinding.tvInfo.text = info

        holder.itemView.setOnClickListener {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments()!!
                    .replace(", $PARENT_TAG", "")
            )
            val mTransaction =
                mainViewModel.getTransactionDetailed()
            mTransaction?.budgetRule =
                budgetRuleDetailed.budgetRule
            mainViewModel.setTransactionDetailed(mTransaction)
            val mBudgetDetailed =
                mainViewModel.getBudgetItem()
            mBudgetDetailed?.budgetRule =
                budgetRuleDetailed!!.budgetRule
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments()!!
                    .replace(", $PARENT_TAG", "")
            )
            mainViewModel.setBudgetItem(mBudgetDetailed)
            gotoCallingFragment(it)
        }

        holder.itemView.setOnLongClickListener {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments() + ", " + FRAG_BUDGET_RULES
            )
            mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
            val direction = BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetRuleUpdateFragment()
            it.findNavController().navigate(direction)
            false
        }
    }

    private fun gotoCallingFragment(it: View) {
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_ADD)) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionAddFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANS_UPDATE)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionUpdateFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_ADD)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToBudgetItemAddFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToBudgetItemUpdateFragment()
            it.findNavController().navigate(direction)
        }
    }
}