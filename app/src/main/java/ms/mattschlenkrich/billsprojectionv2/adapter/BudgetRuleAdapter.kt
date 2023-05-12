package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules.BudgetRuleFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import java.util.Random

class BudgetRuleAdapter(val callingFragment: String) :
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
        val frequencyTypes =
            Resources.getSystem().getStringArray(R.array.frequency_type)
        val frequencyType =
            frequencyTypes[curBudgetRule.budgetRule.frequencyTypeId.toInt()]
        val daysOfWeek =
            Resources.getSystem().getStringArray(R.array.day_of_week)
        val dayOfWeek =
            daysOfWeek[curBudgetRule.budgetRule.dayOfWeekId.toInt()]
        info = "Frequency: " + frequencyType +
                " X " + curBudgetRule.budgetRule.frequencyCount +
                "\nOn " + dayOfWeek
        holder.itemBinding.tvInfo.text = info

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)

        holder.itemView.setOnLongClickListener {
            val direction = BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetRuleUpdateFragment(
                    curBudgetRule,
                    callingFragment
                )
            false
        }
    }

}