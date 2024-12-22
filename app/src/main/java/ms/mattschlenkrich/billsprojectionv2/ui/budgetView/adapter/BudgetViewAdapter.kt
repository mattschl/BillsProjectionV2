package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetViewItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragment
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragmentDirections
import java.util.Random

private const val TAG = ADAPTER_BUDGET_VIEW
private const val PARENT_TAG = FRAG_BUDGET_VIEW


class BudgetViewAdapter(
    private val budgetViewFragment: BudgetViewFragment,
    private val mainActivity: MainActivity,
    private val curAccount: String,
    private val curPayDay: String,
    private val mView: View,
) : RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder>() {

    val nf = NumberFunctions()
    val df = DateFunctions()
    var message = ""

    class BudgetViewHolder(val itemBinding: BudgetViewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem.budgetItem!!.biProjectedDate ==
                        newItem.budgetItem!!.biProjectedDate &&
                        oldItem.budgetItem.biRuleId ==
                        newItem.budgetItem.biRuleId
            }

            override fun areItemsTheSame(
                oldItem: BudgetDetailed,
                newItem: BudgetDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BudgetViewHolder {
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

        holder.itemBinding.apply {
            tvDate.text =
                df.getDisplayDate(curBudget.budgetItem!!.biActualDate)
            tvName.text = curBudget.budgetItem.biBudgetName
            if (curBudget.budgetItem.biIsFixed) {
                val newText = curBudget.budgetItem.biBudgetName + "\n*Fixed*"
                tvName.text = newText
                tvName.setTextColor(Color.RED)
            } else {
                tvName.text = curBudget.budgetItem.biBudgetName
                tvName.setTextColor(Color.BLACK)
            }
            tvAmount.text =
                nf.displayDollars(curBudget.budgetItem.biProjectedAmount)
            if (curBudget.toAccount!!.accountName == curAccount) {
                tvAmount.setTextColor(Color.BLACK)
            } else {
                tvAmount.setTextColor(Color.RED)
            }
            var info = "To: " + curBudget.toAccount!!.accountName
            tvToAccount.text = info
            info = "From: " + curBudget.fromAccount!!.accountName
            tvFromAccount.text = info
            if (curBudget.budgetItem.biLocked) {
                imgLocked.setImageResource(
                    R.drawable.ic_liocked_foreground
                )
            } else {
                imgLocked.setImageResource(
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
            ibColor.setBackgroundColor(color)
            imgLocked.setOnClickListener {
                chooseLockUnlock(curBudget)
            }
        }
        holder.itemView.setOnClickListener {
            chooseOptionsForBudget(
                curBudget
            )
        }
    }

    private fun chooseLockUnlock(budgetItem: BudgetDetailed) {
        AlertDialog.Builder(mView.context)
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
                        mainActivity.budgetItemViewModel.lockUnlockBudgetItem(
                            true, budgetItem.budgetItem.biRuleId,
                            budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    1 -> {
                        mainActivity.budgetItemViewModel.lockUnlockBudgetItem(
                            false, budgetItem.budgetItem.biRuleId,
                            budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    2 -> {
                        mainActivity.budgetItemViewModel.lockUnlockBudgetItem(
                            true, budgetItem.budgetItem.biPayDay,
                            df.getCurrentTimeAsString()
                        )
                    }

                    3 -> {
                        mainActivity.budgetItemViewModel.lockUnlockBudgetItem(
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
        curBudget: BudgetDetailed
    ) {
        Log.d(TAG, "Entering options for $TAG")
        AlertDialog.Builder(mView.context)
            .setTitle("Choose action for ${curBudget.budgetItem!!.biBudgetName}")
            .setItems(
                arrayOf(
                    "Perform a TRANSACTION on this budget item.",
                    if (curBudget.budgetItem.biProjectedAmount == 0.0) {
                        ""
                    } else {
                        "PERFORM action \"${curBudget.budgetItem.biBudgetName}\" " +
                                "\nfor amount of " +
                                nf.displayDollars(curBudget.budgetItem.biProjectedAmount)
                    },
                    "ADJUST this item.",
                    "CANCEL this item.",
                    "Go to the RULES for this item."
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        performTransaction(curBudget)
                    }

                    1 -> {
                        confirmCompleteTransaction(curBudget)
                    }

                    2 -> {
                        openBudgetItem(curBudget)
                    }

                    3 -> {
                        confirmCancelBudgetItem(curBudget)
                    }

                    4 -> {
                        gotoBudgetRule(curBudget)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmCompleteTransaction(curBudget: BudgetDetailed) {
        if (curBudget.budgetItem!!.biProjectedAmount > 0.0) {
            var display = ""
            CoroutineScope(Dispatchers.IO).launch {
                display = getConfirmationsDisplay(curBudget.budgetItem, curBudget)
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                AlertDialog.Builder(mView.context)
                    .setTitle("'Confirm Completing transaction")
                    .setMessage(
                        display
                    )
                    .setPositiveButton("Complete Now") { _, _ ->
                        completeTransaction(curBudget)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private suspend fun getConfirmationsDisplay(
        budgetItem: BudgetItem,
        curBudget: BudgetDetailed
    ): String {
        var display = "This will perform ${budgetItem.biBudgetName} \n" +
                "applying the amount of " +
                nf.displayDollars(curBudget.budgetItem!!.biProjectedAmount) +
                "\n\nFROM:   ${curBudget.fromAccount!!.accountName} "
        display += if (mainActivity.accountUpdateViewModel.isTransactionPending(
                budgetItem.biFromAccountId
            )
        ) {
            "  *pending"
        } else {
            ""
        }
        delay(WAIT_100)
        display += "\nTO:   ${curBudget.toAccount!!.accountName}"
        display += if (mainActivity.accountUpdateViewModel.isTransactionPending(
                budgetItem.biToAccountId
            )
        ) {
            "  *pending"
        } else {
            ""
        }
        delay(WAIT_100)
        return display
    }

    private fun completeTransaction(curBudget: BudgetDetailed) {
        updateAccountsAndTransaction(curBudget)
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            budgetViewFragment.populateBudgetList()
        }
    }

    private fun updateAccountsAndTransaction(curBudget: BudgetDetailed): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            mainActivity.accountUpdateViewModel.performTransaction(
                getCurTransactionObject(
                    curBudget.budgetItem!!,
                    mainActivity.accountUpdateViewModel.isTransactionPending(
                        curBudget.budgetItem.biToAccountId
                    ),
                    mainActivity.accountUpdateViewModel.isTransactionPending(
                        curBudget.budgetItem.biFromAccountId
                    )
                )
            )
            updateBudgetItem(curBudget.budgetItem)
            delay(WAIT_500)
            budgetViewFragment.populateBudgetList()
        }
        return true
    }

    private fun updateBudgetItem(budgetItem: BudgetItem) {
        budgetItem.apply {
            mainActivity.budgetItemViewModel.updateBudgetItem(
                BudgetItem(
                    biRuleId,
                    biProjectedDate,
                    df.getCurrentDateAsString(),
                    biPayDay,
                    biBudgetName,
                    biIsPayDayItem,
                    biToAccountId,
                    biFromAccountId,
                    0.0,
                    biIsPending,
                    biIsFixed,
                    biIsAutomatic,
                    biManuallyEntered,
                    true,
                    biIsCancelled,
                    biIsDeleted,
                    df.getCurrentTimeAsString(),
                    biLocked
                )
            )
        }
        budgetViewFragment.populateBudgetList()
    }

    private fun getCurTransactionObject(
        budgetItem: BudgetItem,
        toAccountPending: Boolean,
        fromAccountPending: Boolean
    ): Transactions {
        budgetItem.apply {
            return Transactions(
                nf.generateId(),
                df.getCurrentDateAsString(),
                biBudgetName,
                "",
                biRuleId,
                biToAccountId,
                toAccountPending,
                biFromAccountId,
                fromAccountPending,
                biProjectedAmount,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoBudgetRule(curBudget: BudgetDetailed) {
        mainActivity.mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curBudget.budgetRule,
                curBudget.toAccount,
                curBudget.fromAccount
            )
        )
        setToReturn()
        mView.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun confirmCancelBudgetItem(curBudget: BudgetDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle("Confirm Cancelling Budget Item ")
            .setMessage(
                "This will Cancel ${curBudget.budgetItem!!.biBudgetName} with the amount of\n" +
                        nf.displayDollars(curBudget.budgetItem.biProjectedAmount) +
                        " remaining"
            )
            .setPositiveButton("Cancel Now") { _, _ ->
                cancelBudgetItem(curBudget)
            }
            .setNegativeButton("Ignore this", null)
            .show()
    }

    private fun cancelBudgetItem(curBudget: BudgetDetailed) {
        mainActivity.budgetItemViewModel.cancelBudgetItem(
            curBudget.budgetItem!!.biRuleId,
            curBudget.budgetItem.biProjectedDate,
            df.getCurrentTimeAsString()
        )
        budgetViewFragment.populateBudgetList()
    }

    private fun openBudgetItem(curBudget: BudgetDetailed) {
        mainActivity.mainViewModel.setBudgetItem(curBudget)
        setToReturn()
        mView.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun performTransaction(curBudget: BudgetDetailed) {
        mainActivity.mainViewModel.setBudgetItem(curBudget)
        mainActivity.mainViewModel.setTransactionDetailed(null)
        setToReturn()
        mView.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToTransactionPerformFragment()
        )

    }

    private fun setToReturn() {
        mainActivity.mainViewModel.setCallingFragments(
            PARENT_TAG
        )
        mainActivity.mainViewModel.setReturnToAsset(curAccount)
        mainActivity.mainViewModel.setReturnToPayDay(curPayDay)
    }
}