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

class BudgetListOccasionalAdapter(
    private val mainViewModel: MainViewModel,
    private val parentView: View,
) : RecyclerView.Adapter<BudgetListOccasionalAdapter.BudgetListHolder>() {

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
        var info: String
        curRule.apply {
            holder.itemBinding.llOthers.visibility = View.VISIBLE
            holder.itemBinding.tvBudgetName.text =
                budgetRule!!.budgetRuleName
            info = cf.displayDollars(budgetRule!!.budgetAmount)
            holder.itemBinding.tvAmount.text = info
            val random = Random()
            val color = Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
            holder.itemBinding.ibColor.setBackgroundColor(color)
            info = when (budgetRule!!.budFrequencyTypeId) {
                FREQ_WEEKLY -> {
                    "Weekly x " + budgetRule!!.budFrequencyCount
                }

                FREQ_MONTHLY -> {
                    "Monthly x " + budgetRule!!.budFrequencyCount
                }

                else -> {
                    ""
                }
            }
            holder.itemBinding.tvFrequency.text = info
            info = when (budgetRule!!.budFrequencyTypeId) {
                FREQ_WEEKLY -> {
                    "Average: " + cf.displayDollars(
                        budgetRule!!.budgetAmount * 4 /
                                budgetRule!!.budFrequencyCount
                    )
                }

                FREQ_MONTHLY -> {
                    "Average/month: " + cf.displayDollars(
                        budgetRule!!.budgetAmount /
                                budgetRule!!.budFrequencyCount
                    )
                }

                else -> {
                    ""
                }
            }
            holder.itemBinding.tvAverage.text = info
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