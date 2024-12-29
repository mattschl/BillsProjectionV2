package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_RULE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.BudgetRuleFragmentDirections
import java.util.Random

private const val TAG = ADAPTER_BUDGET_RULE
private const val PARENT_TAG = FRAG_BUDGET_RULES

class BudgetRuleAdapter(
    private val budgetRuleViewModel: BudgetRuleViewModel,
    private val mainViewModel: MainViewModel,
    private val mView: View
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
        Log.d(TAG, "starting $TAG")
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
        val budgetRuleDetailed =
            differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text =
            budgetRuleDetailed.budgetRule!!.budgetRuleName
        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + budgetRuleDetailed.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        val amount =
            cf.displayDollars(budgetRuleDetailed.budgetRule!!.budgetAmount)
        val frequencyTypes =
            mView.context.resources.getStringArray(R.array.frequency_types)
        val frequencyType =
            frequencyTypes[budgetRuleDetailed.budgetRule!!.budFrequencyTypeId]
        val daysOfWeek =
            mView.context.resources.getStringArray(R.array.days_of_week)
        val dayOfWeek =
            daysOfWeek[budgetRuleDetailed.budgetRule!!.budDayOfWeekId]
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
        holder.itemView.setOnClickListener {
            chooseBudgetRule(budgetRuleDetailed)
        }

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
            Toast.makeText(
                mView.context,
                mView.context.getString(R.string.editing_is_not_allowed_right_now),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoCreateBudgetItem(budgetRuleDetailed: BudgetRuleDetailed) {
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() +
                    ", $PARENT_TAG"
        )
        mainViewModel.setBudgetItem(
            BudgetDetailed(
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
                    true,
                    false,
                    false,
                    false,
                    df.getCurrentTimeAsString(),
                    true
                ),
                budgetRuleDetailed.budgetRule!!,
                budgetRuleDetailed.toAccount!!,
                budgetRuleDetailed.fromAccount!!,
            )
        )
        gotoBudgetItemAddFragment()
    }

    private fun chooseBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $PARENT_TAG", "")
        )
        mainViewModel.setBudgetRuleDetailed(
            budgetRuleDetailed
        )
        if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANSACTION_SPLIT)
        ) {
            val mTransactionSplit = mainViewModel.getSplitTransactionDetailed()
            mTransactionSplit?.budgetRule =
                budgetRuleDetailed.budgetRule
            mainViewModel.setSplitTransactionDetailed(mTransactionSplit)
        } else {
            val mTransaction =
                mainViewModel.getTransactionDetailed()
            mTransaction?.budgetRule =
                budgetRuleDetailed.budgetRule
            mainViewModel.setTransactionDetailed(mTransaction)
        }
        if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_ADD) ||
            mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            val mBudgetDetailed =
                mainViewModel.getBudgetItem()
            mBudgetDetailed?.budgetRule =
                budgetRuleDetailed.budgetRule
            mainViewModel.setBudgetItem(mBudgetDetailed)
        }
        gotoCallingFragment()
    }

    private fun gotoAverages(
        budgetRuleDetailed: BudgetRuleDetailed,
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + FRAG_BUDGET_RULES
        )
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.setAccountWithType(null)
        gotoTransactionAverageFragment()
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
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + FRAG_BUDGET_RULES
        )
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        gotoBudgetRuleUpdateFragment()
    }

    private fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun gotoCallingFragment() {
        when {
            mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_SPLIT) -> {
                gotoTransactionSplitFragment()
            }

            mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_ADD) -> {
                gotoTransactionAddFragment()
            }

            mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_UPDATE) -> {
                gotoTransactionUpdateFragment()
            }

            mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_ITEM_ADD) -> {
                gotoBudgetItemAddFragment()
            }

            mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_ITEM_UPDATE) -> {
                gotoBudgetItemUpdateFragment()
            }

            mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS) -> {
                gotoTransactionAverageFragment()
            }
        }
    }

    private fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetItemAddFragment()
        )
    }

    private fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToTransactionUpdateFragment()
        )
    }

    private fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToTransactionAddFragment()
        )
    }

    private fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToTransactionSplitFragment()
        )
    }

    private fun gotoTransactionAverageFragment() {
        mView.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToTransactionAnalysisFragment()
        )
    }
}