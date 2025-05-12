package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleFragment
import java.util.Random

//private const val TAG = ADAPTER_BUDGET_RULE
//private const val PARENT_TAG = FRAG_BUDGET_RULES

class BudgetRuleAdapter(
    private val budgetRuleViewModel: BudgetRuleViewModel,
    private val mainViewModel: MainViewModel,
    private val mView: View,
    private val parentTag: String,
    private val budgetRuleFragment: BudgetRuleFragment,
) : RecyclerView.Adapter<BudgetRuleAdapter.BudgetRuleViewHolder>() {

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class BudgetRuleViewHolder(
        val itemBinding: BudgetRuleLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule!!.ruleId ==
                        newItem.budgetRule!!.ruleId
            }

            override fun areContentsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BudgetRuleViewHolder {
        return BudgetRuleViewHolder(
            BudgetRuleLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetRuleViewHolder, position: Int
    ) {
        val budgetRuleDetailed = differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text = budgetRuleDetailed.budgetRule!!.budgetRuleName
        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + budgetRuleDetailed.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        val amount = cf.displayDollars(budgetRuleDetailed.budgetRule!!.budgetAmount)
        val frequencyTypes = mView.context.resources.getStringArray(R.array.frequency_types)
        val frequencyType = frequencyTypes[budgetRuleDetailed.budgetRule!!.budFrequencyTypeId]
        val daysOfWeek = mView.context.resources.getStringArray(R.array.days_of_week)
        val dayOfWeek = daysOfWeek[budgetRuleDetailed.budgetRule!!.budDayOfWeekId]
        info = "$amount " + frequencyType +
                " X " + budgetRuleDetailed.budgetRule!!.budFrequencyCount +
                "\nOn " + dayOfWeek
        holder.itemBinding.tvInfo.text = info
        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)
        holder.itemView.setOnClickListener { chooseBudgetRule(budgetRuleDetailed) }

        holder.itemView.setOnLongClickListener {
            chooseOptionsForBudgetRule(budgetRuleDetailed)
            false
        }
    }

    private fun chooseOptionsForBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed,
    ) {
        if (!mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    mView.context.getString(R.string.choose_an_action_for) +
                            budgetRuleDetailed.budgetRule!!.budgetRuleName
                )
                .setItems(
                    arrayOf(
                        mView.context.getString(R.string.view_or_edit_this_budget_rule),
                        mView.context.getString(R.string.delete_this_budget_rule),
                        mView.context.getString(R.string.view_a_summary_of_transactions_for_this_budget_rule),
                        mView.context.getString(R.string.create_a_scheduled_item_with_this_budget_rule)
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> editBudgetRule(budgetRuleDetailed)
                        1 -> deleteBudgetRule(budgetRuleDetailed)
                        2 -> gotoAverages(budgetRuleDetailed)
                        3 -> gotoCreateBudgetItem(budgetRuleDetailed)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            showMessage(mView.context.getString(R.string.editing_is_not_allowed_right_now))
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCreateBudgetItem(budgetRuleDetailed: BudgetRuleDetailed) {
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setBudgetItemDetailed(
            BudgetItemDetailed(
                BudgetItem(
                    budgetRuleDetailed.budgetRule!!.ruleId,
                    df.getCurrentDateAsString(),
                    df.getCurrentDateAsString(),
                    "",
                    budgetRuleDetailed.budgetRule!!.budgetRuleName,
                    budgetRuleDetailed.budgetRule!!.budIsPayDay,
                    budgetRuleDetailed.toAccount!!.accountId,
                    budgetRuleDetailed.fromAccount!!.accountId,
                    budgetRuleDetailed.budgetRule!!.budgetAmount,
                    false,
                    budgetRuleDetailed.budgetRule!!.budFixedAmount,
                    budgetRuleDetailed.budgetRule!!.budIsAutoPay,
                    biManuallyEntered = true,
                    biIsCompleted = false,
                    biIsCancelled = false,
                    biIsDeleted = false,
                    biUpdateTime = df.getCurrentTimeAsString(),
                    biLocked = true
                ),
                budgetRuleDetailed.budgetRule!!,
                budgetRuleDetailed.toAccount!!,
                budgetRuleDetailed.fromAccount!!,
            )
        )
        budgetRuleFragment.gotoBudgetItemAddFragment()
    }

    private fun chooseBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed
    ) {
        mainViewModel.removeCallingFragment(parentTag)
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        val mCallingFragment = mainViewModel.getCallingFragments()!!
        if (mCallingFragment.contains(FRAG_TRANSACTION_SPLIT)
        ) {
            val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
            mTransactionSplit?.budgetRule = budgetRuleDetailed.budgetRule
            mainViewModel.setSplitTransactionDetailed(mTransactionSplit)
        } else {
            val mTransaction = mainViewModel.getTransactionDetailed()
            mTransaction?.budgetRule = budgetRuleDetailed.budgetRule
            mainViewModel.setTransactionDetailed(mTransaction)
        }
        if (mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) ||
            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            val mBudgetDetailed = mainViewModel.getBudgetItemDetailed()
            mBudgetDetailed?.budgetRule = budgetRuleDetailed.budgetRule
            mainViewModel.setBudgetItemDetailed(mBudgetDetailed)
        }
        gotoCallingFragment()
    }

    private fun gotoAverages(
        budgetRuleDetailed: BudgetRuleDetailed,
    ) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.setAccountWithType(null)
        budgetRuleFragment.gotoTransactionAverageFragment()
    }

    private fun deleteBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        budgetRuleViewModel.deleteBudgetRule(
            budgetRuleDetailed.budgetRule!!.ruleId,
            df.getCurrentTimeAsString()
        )
    }

    private fun editBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed?,
    ) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        budgetRuleFragment.gotoBudgetRuleUpdateFragment()
    }

    private fun gotoCallingFragment() {
        val mCallingFragment = mainViewModel.getCallingFragments()!!
        when {
            mCallingFragment.contains(FRAG_TRANSACTION_SPLIT) -> {
                budgetRuleFragment.gotoTransactionSplitFragment()
            }

            mCallingFragment.contains(FRAG_TRANS_ADD) -> {
                budgetRuleFragment.gotoTransactionAddFragment()
            }

            mCallingFragment.contains(FRAG_TRANS_UPDATE) -> {
                budgetRuleFragment.gotoTransactionUpdateFragment()
            }

            mCallingFragment.contains(FRAG_BUDGET_ITEM_ADD) -> {
                budgetRuleFragment.gotoBudgetItemAddFragment()
            }

            mCallingFragment.contains(FRAG_BUDGET_ITEM_UPDATE) -> {
                budgetRuleFragment.gotoBudgetItemUpdateFragment()
            }

            mCallingFragment.contains(FRAG_TRANSACTION_ANALYSIS) -> {
                budgetRuleFragment.gotoTransactionAverageFragment()
            }
        }
    }
}