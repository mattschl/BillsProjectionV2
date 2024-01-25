package ms.mattschlenkrich.billsprojectionv2.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetViewItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetViewFragment
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.BudgetItemViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_BUDGET_VIEW
private const val PARENT_TAG = FRAG_BUDGET_VIEW

class BudgetViewAdapter(
    private val budgetViewFragment: BudgetViewFragment,
    private val budgetItemViewModel: BudgetItemViewModel,
    private val mainViewModel: MainViewModel,
    private val curAccount: String,
    private val curPayDay: String,
    private val context: Context
) : RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()

    class BudgetViewHolder(val itemBinding: BudgetViewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem.budgetItem!!.biProjectedDate == newItem.budgetItem!!.biProjectedDate &&
                        oldItem.budgetItem.biRuleId == newItem.budgetItem.biRuleId
            }

            override fun areItemsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return BudgetViewHolder(
            BudgetViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetViewHolder, position: Int
    ) {
        val curBudget = differ.currentList[position]
        holder.itemBinding.tvDate.text =
            df.getDisplayDate(curBudget.budgetItem!!.biActualDate)
        holder.itemBinding.tvName.text = curBudget.budgetItem.biBudgetName
        if (curBudget.budgetItem.biIsFixed) {
            val newText = curBudget.budgetItem.biBudgetName + "\n*Fixed*"
            holder.itemBinding.tvName.text = newText
            holder.itemBinding.tvName.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvName.text = curBudget.budgetItem.biBudgetName
            holder.itemBinding.tvName.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvAmount.text =
            cf.displayDollars(curBudget.budgetItem.biProjectedAmount)
        if (curBudget.toAccount!!.accountName == curAccount) {
            holder.itemBinding.tvAmount.setTextColor(Color.BLACK)
        } else {
            holder.itemBinding.tvAmount.setTextColor(Color.RED)
        }
        var info = "To: " + curBudget.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " + curBudget.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        if (curBudget.budgetItem.biLocked) {
            holder.itemBinding.imgLocked.setImageResource(
                R.drawable.ic_liocked_foreground
            )
        } else {
            holder.itemBinding.imgLocked.setImageResource(
                R.drawable.ic_unlocked_foreground
            )
        }
        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)
        holder.itemBinding.imgLocked.setOnClickListener {
            chooseLockUnlock(curBudget)
        }
        holder.itemView.setOnClickListener {
            chooseOptionsForBudget(
                curBudget, it
            )
        }
    }

    private fun chooseLockUnlock(budgetItem: BudgetDetailed) {
        AlertDialog.Builder(context)
            .setTitle("Lock or Unlock")
            .setItems(
                arrayOf(
                    "LOCK ${budgetItem.budgetItem!!.biBudgetName}",
                    "UN Lock  ${budgetItem.budgetItem.biBudgetName}",
                    "LOCK all items for this payday",
                    "UN Lock all items for this payday"
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            true, budgetItem.budgetItem.biRuleId,
                            budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    1 -> {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            false, budgetItem.budgetItem.biRuleId,
                            budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    2 -> {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            true, budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    3 -> {
                        budgetItemViewModel.lockUnlockBudgetItem(
                            false, budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun chooseOptionsForBudget(
        curBudget: BudgetDetailed,
        it: View
    ) {
        Log.d(TAG, "Entering options for $TAG")
        AlertDialog.Builder(context)
            .setTitle("Choose action for ${curBudget.budgetItem!!.biBudgetName}")
            .setItems(
                arrayOf(
                    "Perform a TRANSACTION on this item.",
                    "ADJUST this item.",
                    "CANCEL this item.",
                    "Go to the RULES for this item."
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        performTransaction(curBudget, it)
                    }

                    1 -> {
                        openBudgetItem(curBudget, it)
                    }

                    2 -> {
                        cancelBudgetItem(curBudget)
                    }

                    3 -> {
                        gotoBudgetRule(curBudget, it)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun gotoBudgetRule(curBudget: BudgetDetailed, it: View) {
        mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curBudget.budgetRule,
                curBudget.toAccount,
                curBudget.fromAccount
            )
        )
        setToReturn()
        it.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun cancelBudgetItem(curBudget: BudgetDetailed) {
        budgetItemViewModel.cancelBudgetItem(
            curBudget.budgetItem!!.biRuleId,
            curBudget.budgetItem.biProjectedDate,
            df.getCurrentTimeAsString()
        )
        budgetViewFragment.fillBudgetTotals()
    }

    private fun openBudgetItem(curBudget: BudgetDetailed, it: View) {
        mainViewModel.setBudgetItem(curBudget)
        setToReturn()
        it.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun performTransaction(curBudget: BudgetDetailed, it: View) {
        mainViewModel.setBudgetItem(curBudget)
        mainViewModel.setTransactionDetailed(null)
        setToReturn()
        it.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToTransactionPerformFragment()
        )

    }

    private fun setToReturn() {
        mainViewModel.setCallingFragments(
            PARENT_TAG
        )
        mainViewModel.setReturnToAsset(curAccount)
        mainViewModel.setReturnToPayDay(curPayDay)
    }
}