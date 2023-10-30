package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetListItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_BUDGET_LIST
private const val PARENT_TAG = FRAG_BUDGET_LIST

class BudgetListAdapter(
    val mainActivity: MainActivity,
    val accountViewModel: AccountViewModel,
    private val mainViewModel: MainViewModel,
    private val parentView: View,
) : RecyclerView.Adapter<BudgetListAdapter.BudgetListHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()

    class BudgetListHolder(val itemBinding: BudgetListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule!!.ruleId == newItem.budgetRule!!.ruleId &&
                        oldItem.budgetRule!!.budgetRuleName == newItem.budgetRule!!.budgetRuleName
            }

            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
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
        holder.itemBinding.tvBudgetName.text =
            curRule.budgetRule!!.budgetRuleName
        parentView.findViewTreeLifecycleOwner()?.let {
            accountViewModel.getAccountDetailed(curRule.budgetRule!!.budToAccountId).observe(
                it
            ) { toAccountWithType ->
                if (toAccountWithType.accountType!!.isAsset) {
                    info = if (curRule.budgetRule!!.budFrequencyTypeId == FREQ_MONTHLY) {
                        "Credit: " + cf.displayDollars(curRule.budgetRule!!.budgetAmount)
                    } else {
                        "Credit: " + cf.displayDollars(
                            curRule.budgetRule!!.budgetAmount *
                                    4 / curRule.budgetRule!!.budFrequencyCount
                        )
                    }
                    holder.itemBinding.tvAmount.setTextColor(Color.BLACK)
                    holder.itemBinding.tvAmount.text = info
                }
            }
        }
        parentView.findViewTreeLifecycleOwner()?.let {
            accountViewModel.getAccountDetailed(curRule.budgetRule!!.budFromAccountId).observe(
                it
            ) { fromAccountWithType ->
                if (fromAccountWithType.accountType!!.isAsset) {
                    info = if (curRule.budgetRule!!.budFrequencyTypeId == FREQ_MONTHLY) {
                        "Debit: " + cf.displayDollars(curRule.budgetRule!!.budgetAmount)
                    } else {
                        "Debit: " + cf.displayDollars(
                            curRule.budgetRule!!.budgetAmount *
                                    4 / curRule.budgetRule!!.budFrequencyCount
                        )
                    }
                    holder.itemBinding.tvAmount.setTextColor(Color.RED)
                    holder.itemBinding.tvAmount.text = info
                }
            }
        }

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)
    }
}