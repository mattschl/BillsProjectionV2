package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetDateItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleUpdateFragment

class BudgetRuleDatesAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
    private val budgetRuleUpdateFragment: BudgetRuleUpdateFragment,
) : RecyclerView.Adapter<BudgetRuleDatesAdapter.DateViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel

    class DateViewHolder(val itemBinding: BudgetDateItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCalBack = object : DiffUtil.ItemCallback<BudgetItemDetailed>() {
        override fun areContentsTheSame(
            oldItem: BudgetItemDetailed, newItem: BudgetItemDetailed
        ): Boolean {
            return oldItem.budgetItem!!.biProjectedDate == newItem.budgetItem!!.biProjectedDate && oldItem.budgetItem.biRuleId == newItem.budgetItem.biRuleId && oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId && oldItem.toAccount?.accountId == newItem.toAccount?.accountId && oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId
        }

        override fun areItemsTheSame(
            oldItem: BudgetItemDetailed, newItem: BudgetItemDetailed
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
            tvActualDate.text = df.getDisplayDate(curItem.budgetItem!!.biActualDate)
            var display = " (pay day ${df.getDisplayDate(curItem.budgetItem.biPayDay)})"
            tvPayDay.text = display
            display = " for ${nf.displayDollars(curItem.budgetItem.biProjectedAmount)}"
            tvAmount.text = display
        }
        holder.itemView.setOnClickListener { confirmGotoBudgetItem(curItem) }
    }

    private fun confirmGotoBudgetItem(curItem: BudgetItemDetailed) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.would_you_like_to_go_to_this_budget_item_on) + " ${
                df.getDisplayDate(
                    curItem.budgetItem!!.biActualDate
                )
            }?"
        ).setPositiveButton(mView.context.getString(R.string.yes)) { _, _ ->
            gotoBudgetItem(curItem)
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun gotoBudgetItem(curItem: BudgetItemDetailed) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setBudgetItemDetailed(curItem)
        budgetRuleUpdateFragment.gotoBudgetItemUpdateFragment()
    }
}