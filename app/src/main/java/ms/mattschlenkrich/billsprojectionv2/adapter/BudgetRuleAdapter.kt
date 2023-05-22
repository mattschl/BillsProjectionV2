package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.ADAPTER_BUDGET_RULE
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules.BudgetRuleFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import java.text.NumberFormat
import java.util.Locale
import java.util.Random

private const val TAG = ADAPTER_BUDGET_RULE

class BudgetRuleAdapter(
    private val context: Context,
    private val callingFragments: Array<String>
) :
    RecyclerView.Adapter<BudgetRuleAdapter.BudgetRuleViewHolder>() {

    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)

    class BudgetRuleViewHolder(val itemBinding: BudgetRuleLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule.RuleId ==
                        newItem.budgetRule.RuleId
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
        Log.d(TAG, "$TAG is entered")
        return BudgetRuleViewHolder(
            BudgetRuleLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "differ count is ${differ.currentList.size}")
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BudgetRuleViewHolder, position: Int) {
        val budgetRuleDetailed = differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text =
            budgetRuleDetailed.budgetRule.budgetRuleName
        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + budgetRuleDetailed.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        val amount =
            dollarFormat.format(budgetRuleDetailed.budgetRule.amount)
        val frequencyTypes =
            context.resources.getStringArray(R.array.frequency_types)
        val frequencyType =
            frequencyTypes[budgetRuleDetailed.budgetRule.frequencyTypeId.toInt()]
        val daysOfWeek =
            context.resources.getStringArray(R.array.days_of_week)
        val dayOfWeek =
            daysOfWeek[budgetRuleDetailed.budgetRule.dayOfWeekId.toInt()]
        info = "$amount " + frequencyType +
                " X " + budgetRuleDetailed.budgetRule.frequencyCount +
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
                    budgetRuleDetailed,
                    callingFragments
                )
            it.findNavController().navigate(direction)
            false
        }
    }

}