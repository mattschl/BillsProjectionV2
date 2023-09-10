package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetViewItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed

class BudgetViewAdapter(private val curAccount: String) :
    RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()

    class BudgetViewHolder(val itemBinding: BudgetViewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCalBack =
        object : DiffUtil.ItemCallback<BudgetDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem.budgetItem!!.biProjectedDate == newItem.budgetItem!!.biProjectedDate &&
                        oldItem.budgetItem.biRuleId == newItem.budgetItem.biRuleId
            }

            override fun areItemsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCalBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return BudgetViewHolder(
            BudgetViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetViewHolder, position: Int
    ) {
        val curBudget = differ.currentList[position]

        holder.itemBinding.tvDate.text =
            df.getDisplayDate(curBudget.budgetItem!!.biActualDate)
        holder.itemBinding.tvName.text = curBudget.budgetItem.biBudgetName
        holder.itemBinding.tvAmount.text =
            cf.displayDollars(curBudget.budgetItem.biProjectedAmount)
        if (curBudget.toAccount!!.accountName == curAccount) {
            holder.itemBinding.tvAmount.setTextColor(Color.BLACK)
        } else {
            holder.itemBinding.tvAmount.setTextColor(Color.RED)
        }
        var info = "To: " + curBudget.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + curBudget.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
    }

}