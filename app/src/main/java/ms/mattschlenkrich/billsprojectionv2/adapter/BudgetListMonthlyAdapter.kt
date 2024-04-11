package ms.mattschlenkrich.billsprojectionv2.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetListItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetListFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

//private const val TAG = ADAPTER_BUDGET_LIST
private const val PARENT_TAG = FRAG_BUDGET_LIST

class BudgetListMonthlyAdapter(
    private val mainViewModel: MainViewModel,
    private val budgetRuleViewModel: BudgetRuleViewModel,
    private val parentView: View,
) : RecyclerView.Adapter<BudgetListMonthlyAdapter.BudgetListHolder>() {

    val cf = NumberFunctions()
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
            if (budgetRule!!.budFrequencyTypeId == FREQ_MONTHLY) {
                info = budgetRule!!.budgetRuleName +
                        " - monthly"
            } else if (budgetRule!!.budFrequencyTypeId == FREQ_WEEKLY) {
                info = budgetRule!!.budgetRuleName + " - weekly x " +
                        budgetRule!!.budFrequencyCount

            }
            holder.itemBinding.tvBudgetName.text = info
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
                chooseOptionsForBudgetItem(curRule)
                false
            }
        }
    }

    private fun chooseOptionsForBudgetItem(curRule: BudgetRuleComplete) {
        AlertDialog.Builder(parentView.context)
            .setTitle(
                "Choose an action for " +
                        curRule.budgetRule!!.budgetRuleName
            )
            .setItems(
                arrayOf(
                    "View or Edit this Budget Rule",
                    "Delete this Budget Rule",
                    "View a summary of transactions for this rule"
                )
            ) { _, pos ->
                when (pos) {
                    0 -> editBudgetRule(curRule)
                    1 -> deleteBudgetRule(curRule)
                    2 -> gotoAverages(curRule)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun gotoAverages(curRule: BudgetRuleComplete) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curRule.budgetRule!!,
                curRule.toAccount!!.account,
                curRule.fromAccount!!.account
            )
        )
        mainViewModel.setAccountWithType(null)
        parentView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToTransactionAverageFragment()
        )
    }

    private fun deleteBudgetRule(curRule: BudgetRuleComplete) {
        budgetRuleViewModel.deleteBudgetRule(
            curRule.budgetRule!!.ruleId,
            df.getCurrentTimeAsString()
        )
    }

    private fun editBudgetRule(curRule: BudgetRuleComplete) {
        val budgetRule = BudgetRuleDetailed(
            curRule.budgetRule!!,
            curRule.toAccount!!.account,
            curRule.fromAccount!!.account
        )
        mainViewModel.setBudgetRuleDetailed(budgetRule)
        mainViewModel.setCallingFragments(PARENT_TAG)
        parentView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToBudgetRuleUpdateFragment()

        )
    }
}