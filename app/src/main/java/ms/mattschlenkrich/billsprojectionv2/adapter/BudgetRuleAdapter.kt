package ms.mattschlenkrich.billsprojectionv2.adapter

import android.app.AlertDialog
import android.content.Context
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
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules.BudgetRuleFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetRuleViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_BUDGET_RULE
private const val PARENT_TAG = FRAG_BUDGET_RULES

class BudgetRuleAdapter(
    private val budgetRuleViewModel: BudgetRuleViewModel,
    private val mainViewModel: MainViewModel,
    private val context: Context
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
            context.resources.getStringArray(R.array.frequency_types)
        val frequencyType =
            frequencyTypes[budgetRuleDetailed.budgetRule!!.budFrequencyTypeId]
        val daysOfWeek =
            context.resources.getStringArray(R.array.days_of_week)
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
            chooseBudgetRule(budgetRuleDetailed, it)
        }

        holder.itemView.setOnLongClickListener {
            chooseOptionsForBudgetRule(budgetRuleDetailed, it)
            false
        }
    }

    private fun chooseOptionsForBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed,
        it: View
    ) {
        if (!mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
            AlertDialog.Builder(context)
                .setTitle(
                    "Choose an action for " +
                            budgetRuleDetailed.budgetRule!!.budgetRuleName
                )
                .setItems(
                    arrayOf(
                        "View or Edit this Budget Rule",
                        "Delete this Budget Rule",
                        "View a summary of transactions for this rule"
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> editBudgetRule(budgetRuleDetailed, it)
                        1 -> deleteBudgetRule(budgetRuleDetailed)
                        2 -> gotoAverages(budgetRuleDetailed, it)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(
                context,
                "Editing is not allowed right now",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun chooseBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed,
        it: View
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
                budgetRuleDetailed!!.budgetRule
            mainViewModel.setBudgetItem(mBudgetDetailed)
        }
        gotoCallingFragment(it)
    }

    private fun gotoAverages(
        budgetRuleDetailed: BudgetRuleDetailed,
        it: View
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + FRAG_BUDGET_RULES
        )
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        mainViewModel.setAccountWithType(null)
        it.findNavController().navigate(
            BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToTransactionAverageFragment()
        )
    }

    private fun deleteBudgetRule(budgetRuleDetailed: BudgetRuleDetailed) {
        budgetRuleViewModel.deleteBudgetRule(
            budgetRuleDetailed.budgetRule!!.ruleId,
            df.getCurrentTimeAsString()
        )
    }

    private fun editBudgetRule(
        budgetRuleDetailed: BudgetRuleDetailed?,
        it: View
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + FRAG_BUDGET_RULES
        )
        mainViewModel.setBudgetRuleDetailed(budgetRuleDetailed)
        val direction = BudgetRuleFragmentDirections
            .actionBudgetRuleFragmentToBudgetRuleUpdateFragment()
        it.findNavController().navigate(direction)
    }

    private fun gotoCallingFragment(it: View) {
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_SPLIT)) {
            it.findNavController().navigate(
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionSplitFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_ADD)) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionAddFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANS_UPDATE)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionUpdateFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_ITEM_ADD)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToBudgetItemAddFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            val direction =
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToBudgetItemUpdateFragment()
            it.findNavController().navigate(direction)
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)
        ) {
            it.findNavController().navigate(
                BudgetRuleFragmentDirections
                    .actionBudgetRuleFragmentToTransactionAverageFragment()
            )
        }
    }
}