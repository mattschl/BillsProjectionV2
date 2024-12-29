package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_LIST
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_MONTHLY
import ms.mattschlenkrich.billsprojectionv2.common.FREQ_WEEKLY
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetListItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetListFragmentDirections
import java.util.Random

private const val PARENT_TAG = FRAG_BUDGET_LIST


class BudgetListOccasionalAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<BudgetListOccasionalAdapter.BudgetListHolder>() {

    private val cf = NumberFunctions()
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
            holder.itemBinding.apply {
                llOthers.visibility = View.VISIBLE
                if (budgetRule!!.budFrequencyTypeId != FREQ_MONTHLY) {
                    if (budgetRule!!.budFrequencyTypeId == FREQ_WEEKLY) {
                        info = budgetRule!!.budgetRuleName + " - " + df.getNextWeeklyDate(
                            budgetRule!!.budStartDate,
                            budgetRule!!.budFrequencyCount
                        )
                    }
                } else {
                    info = budgetRule!!.budgetRuleName + " - " + df.getNextMonthlyDate(
                        budgetRule!!.budStartDate,
                        budgetRule!!.budFrequencyCount
                    )
                }
                tvBudgetName.text = info
                info = cf.displayDollars(budgetRule!!.budgetAmount)
                tvAmount.text = info
                val random = Random()
                val color = Color.argb(
                    255,
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
                ibColor.setBackgroundColor(color)
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
                tvFrequency.text = info
                info = when (budgetRule!!.budFrequencyTypeId) {
                    FREQ_WEEKLY -> {
                        mView.context.getString(R.string.average_per_month) +
                                cf.displayDollars(
                                    budgetRule!!.budgetAmount * 4 /
                                            budgetRule!!.budFrequencyCount
                                )
                    }

                    FREQ_MONTHLY -> {
                        mView.context.getString(R.string.average_per_month) +
                                cf.displayDollars(
                                    budgetRule!!.budgetAmount /
                                            budgetRule!!.budFrequencyCount
                                )
                    }

                    else -> {
                        ""
                    }
                }
                tvAverage.text = info
                holder.itemView.setOnLongClickListener {
                    chooseOptionsForBudgetItem(curRule)
                    false
                }
            }
        }
    }

    private fun chooseOptionsForBudgetItem(curRule: BudgetRuleComplete) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_an_action_for) +
                        curRule.budgetRule!!.budgetRuleName
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.view_or_edit_this_budget_rule),
                    mView.context.getString(R.string.delete_this_budget_rule),
                    mView.context.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule)
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

    private fun editBudgetRule(curRule: BudgetRuleComplete) {
        val budgetRule = BudgetRuleDetailed(
            curRule.budgetRule!!,
            curRule.toAccount!!.account,
            curRule.fromAccount!!.account
        )
        mainActivity.mainViewModel.setBudgetRuleDetailed(budgetRule)
        mainActivity.mainViewModel.setCallingFragments(PARENT_TAG)
        gotoBudgetRuleUpdateFragment()
    }

    private fun deleteBudgetRule(curRule: BudgetRuleComplete) {
        mainActivity.budgetRuleViewModel.deleteBudgetRule(
            curRule.budgetRule!!.ruleId,
            df.getCurrentTimeAsString()
        )
    }

    private fun gotoAverages(curRule: BudgetRuleComplete) {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainActivity.mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curRule.budgetRule!!,
                curRule.toAccount!!.account,
                curRule.fromAccount!!.account
            )
        )
        mainActivity.mainViewModel.setAccountWithType(null)
        gotoTransactionAverageFragment()
    }

    private fun gotoTransactionAverageFragment() {
        mView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToTransactionAnalysisFragment()
        )
    }

    private fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            BudgetListFragmentDirections
                .actionBudgetListFragmentToBudgetRuleUpdateFragment()
        )
    }
}