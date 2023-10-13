package ms.mattschlenkrich.billsprojectionv2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetDateItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed

class BudgetRuleDatesAdapter :
    RecyclerView.Adapter<BudgetRuleDatesAdapter.DateViewHolder>() {

    private val df = DateFunctions()

    class DateViewHolder(val itemBinding: BudgetDateItemBinding) :
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        return DateViewHolder(
            BudgetDateItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val curItem = differ.currentList[position]
        holder.itemBinding.tvActualDate.text =
            df.getDisplayDate(curItem.budgetItem!!.biActualDate)
        holder.itemBinding.tvPayDay.text =
            df.getDisplayDate(curItem.budgetItem.biPayDay)

    }
}