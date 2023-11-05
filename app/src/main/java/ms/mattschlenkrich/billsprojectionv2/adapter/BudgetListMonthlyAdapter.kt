package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetListItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetListFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

//private const val TAG = ADAPTER_BUDGET_LIST
//private const val PARENT_TAG = FRAG_BUDGET_LIST

class BudgetListMonthlyAdapter(
    private val mainViewModel: MainViewModel,
    private val parentView: View,
) : RecyclerView.Adapter<BudgetListMonthlyAdapter.BudgetListHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()

    class BudgetListHolder(val itemBinding: BudgetListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleComplete>() {
            override fun areContentsTheSame(
                oldItem: BudgetRuleComplete,
                newItem: BudgetRuleComplete
            ): Boolean {
                return oldItem.budgetRule!!.ruleId == newItem.budgetRule!!.ruleId &&
                        oldItem.budgetRule!!.budgetRuleName == newItem.budgetRule!!.budgetRuleName &&
                        oldItem.budgetRule!!.budgetAmount == newItem.budgetRule!!.budgetAmount &&
                        oldItem.toAccount!!.accountType!!.isAsset ==
                        newItem.toAccount!!.accountType!!.isAsset &&
                        oldItem.fromAccount!!.accountType!!.isAsset ==
                        newItem.fromAccount!!.accountType!!.isAsset
            }

            override fun areItemsTheSame(
                oldItem: BudgetRuleComplete,
                newItem: BudgetRuleComplete
            ): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetListHolder {
        return BudgetListHolder(
            (
                    BudgetListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BudgetListHolder, position: Int) {
        val curRule = differ.currentList[position]
        var info = ""
        curRule.apply {
            holder.itemBinding.llOthers.visibility = View.GONE
            holder.itemBinding.tvBudgetName.text =
                budgetRule!!.budgetRuleName
            val amt = when (budgetRule!!.budFrequencyTypeId) {
                FREQ_WEEKLY -> {
                    budgetRule!!.budgetAmount * 4 / budgetRule!!.budFrequencyCount
                }

                FREQ_MONTHLY -> {
                    budgetRule!!.budgetAmount
                }

                else -> {
                    0.0
                }
            }
            if (toAccount!!.accountType!!.displayAsAsset && fromAccount!!.accountType!!.displayAsAsset) {
                info = "NA"
            } else if (toAccount!!.accountType!!.isAsset) {
                holder.itemBinding.tvAmount.setTextColor(Color.BLACK)
                info = cf.displayDollars(amt)
            } else if (fromAccount!!.accountType!!.isAsset || fromAccount!!.accountType!!.displayAsAsset) {
                holder.itemBinding.tvAmount.setTextColor(Color.RED)
                info = cf.displayDollars(amt)
            }
            if (curRule.budgetRule!!.budFixedAmount) {
                info = "(f)   $info"
            }
            holder.itemBinding.tvAmount.text = info
            val random = Random()
            val color = Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
            holder.itemBinding.ibColor.setBackgroundColor(color)
            holder.itemView.setOnLongClickListener {
                gotoBudgetRule(curRule)
                false
            }
        }
    }

    private fun gotoBudgetRule(curRule: BudgetRuleComplete) {
        val budgetRule = BudgetRuleDetailed(
            curRule.budgetRule!!,
            curRule.toAccount!!.account,
            curRule.fromAccount!!.account
        )
        mainViewModel.setBudgetRuleDetailed(budgetRule)
        parentView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToBudgetRuleUpdateFragment()

        )
    }
}