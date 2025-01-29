package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetDateItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleUpdateFragment

class BudgetRuleDatesAdapter(
    private val mainViewModel: MainViewModel,
    private val budgetRuleUpdateFragment: BudgetRuleUpdateFragment,
    private val parentTag: String,
) :
    RecyclerView.Adapter<BudgetRuleDatesAdapter.DateViewHolder>() {

    private val df = DateFunctions()

    class DateViewHolder(val itemBinding: BudgetDateItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCalBack =
        object : DiffUtil.ItemCallback<BudgetItemDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetItemDetailed,
                newItem: BudgetItemDetailed
            ): Boolean {
                return oldItem.budgetItem!!.biProjectedDate == newItem.budgetItem!!.biProjectedDate &&
                        oldItem.budgetItem.biRuleId == newItem.budgetItem.biRuleId
            }

            override fun areItemsTheSame(
                oldItem: BudgetItemDetailed,
                newItem: BudgetItemDetailed
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
        holder.itemBinding.apply {
            tvActualDate.text =
                df.getDisplayDate(curItem.budgetItem!!.biActualDate)
            tvPayDay.text =
                df.getDisplayDate(curItem.budgetItem.biPayDay)
        }
        holder.itemView.setOnClickListener {
            gotoBudgetItem(curItem)
        }
    }

    private fun gotoBudgetItem(curItem: BudgetItemDetailed?) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + parentTag
        )
        mainViewModel.setBudgetItemDetailed(curItem)
        budgetRuleUpdateFragment.gotoBudgetItemUpdateFragment()
    }


}