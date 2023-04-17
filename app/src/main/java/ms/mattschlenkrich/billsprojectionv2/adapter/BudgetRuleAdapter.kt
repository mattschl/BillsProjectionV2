package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import java.util.Random

class BudgetRuleAdapter :
    RecyclerView.Adapter<BudgetRuleAdapter.BudgetRuleViewHolder>() {


    class BudgetRuleViewHolder(val itemBinding: BudgetRuleLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule.RuleId == newItem.budgetRule.RuleId &&
                        oldItem.budgetRule.budgetRuleName == newItem.budgetRule.budgetRuleName
            }

            override fun areContentsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetRuleViewHolder {
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

    override fun onBindViewHolder(holder: BudgetRuleViewHolder, position: Int) {
        val curBudgetRule = differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text =
            curBudgetRule.budgetRule.budgetRuleName
        var info = "To: " + curBudgetRule.toAccount.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + curBudgetRule.fromAccount.accountName
        holder.itemBinding.tvFromAccount.text = info
        info = "Frequency: " + curBudgetRule.frequencyTypes.frequencyType +
                "\nOn " + curBudgetRule.daysOfWeek.dayOfWeek

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)

        holder.itemView.setOnLongClickListener {
            //TODO: set up the other fragment to update a budget rule
            false
        }
    }

}