package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter

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
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleComplete
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetListItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetListFragment

//private const val PARENT_TAG = FRAG_BUDGET_LIST

class BudgetListAnnualAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
    private val budgetListFragment: BudgetListFragment,
) : RecyclerView.Adapter<BudgetListAnnualAdapter.BudgetListHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val vf = VisualsFunctions()

    class BudgetListHolder(val itemBinding: BudgetListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<BudgetRuleComplete>() {
        override fun areContentsTheSame(
            oldItem: BudgetRuleComplete, newItem: BudgetRuleComplete
        ): Boolean {
            return oldItem.budgetRule!!.ruleId == newItem.budgetRule!!.ruleId && oldItem.budgetRule!!.budgetRuleName == newItem.budgetRule!!.budgetRuleName && oldItem.budgetRule!!.budgetAmount == newItem.budgetRule!!.budgetAmount && oldItem.toAccount!!.accountType!!.isAsset == newItem.toAccount!!.accountType!!.isAsset && oldItem.fromAccount!!.accountType!!.isAsset == newItem.fromAccount!!.accountType!!.isAsset
        }

        override fun areItemsTheSame(
            oldItem: BudgetRuleComplete, newItem: BudgetRuleComplete
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetListHolder {
        return BudgetListHolder(
            (BudgetListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ))
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BudgetListHolder, position: Int) {
        val curRule = differ.currentList[position]
        curRule.apply {
            holder.itemBinding.apply {
                clBottom.visibility = View.VISIBLE
                var info = if (curRule.budgetRule!!.budFrequencyCount > 1) {
                    budgetRule!!.budgetRuleName + " - " + df.getDisplayDateInComingYear(
                        budgetRule!!.budStartDate, budgetRule!!.budFrequencyCount.toLong()
                    )
                } else {
                    budgetRule!!.budgetRuleName + " - " + df.getDisplayDateInComingYear(budgetRule!!.budStartDate)
                }
                tvBudgetName.text = info
                info = nf.displayDollars(budgetRule!!.budgetAmount)
                tvAmount.text = info
                ibColor.setBackgroundColor(vf.getRandomColorInt())
                info =
                    mView.context.getString(R.string.annually_every) + budgetRule!!.budFrequencyCount + mView.context.getString(
                        R.string._years
                    )
                tvFrequency.text = info
                info = mView.context.getString(R.string.average_per_month) + nf.displayDollars(
                    budgetRule!!.budgetAmount / 12 / budgetRule!!.budFrequencyCount
                )
                tvAverage.text = info
                holder.itemView.setOnLongClickListener {
                    chooseOptionsForBudgetItem(curRule)
                    false
                }
            }
        }
    }

    private fun chooseOptionsForBudgetItem(curRule: BudgetRuleComplete) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_an_action_for) + curRule.budgetRule!!.budgetRuleName
        ).setItems(
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
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun editBudgetRule(curRule: BudgetRuleComplete) {
        val budgetRule = BudgetRuleDetailed(
            curRule.budgetRule!!, curRule.toAccount!!.account, curRule.fromAccount!!.account
        )
        mainViewModel.setBudgetRuleDetailed(budgetRule)
        mainViewModel.setCallingFragments(parentTag)
        budgetListFragment.gotoBudgetRuleUpdateFragment()
    }

    private fun deleteBudgetRule(curRule: BudgetRuleComplete) {
        mainActivity.budgetRuleViewModel.deleteBudgetRule(
            curRule.budgetRule!!.ruleId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoAverages(curRule: BudgetRuleComplete) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curRule.budgetRule!!, curRule.toAccount!!.account, curRule.fromAccount!!.account
            )
        )
        mainViewModel.setAccountWithType(null)
        budgetListFragment.gotoTransactionAverageFragment()
    }
}