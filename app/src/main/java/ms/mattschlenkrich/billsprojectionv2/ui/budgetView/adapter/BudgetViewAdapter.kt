package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
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
    private val context: Context
) : RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder>() {

    val nf = NumberFunctions()
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
        curBudget: BudgetDetailed,
        it: View
    ) {
        Log.d(TAG, "Entering options for $TAG")
        AlertDialog.Builder(context)
            .setTitle("Choose action for ${curBudget.budgetItem!!.biBudgetName}")
            .setItems(
                arrayOf(
                    "Perform a TRANSACTION on this budget item.",
                    "COMPLETE ${curBudget.budgetItem.biBudgetName} for amount of " +
                            nf.displayDollars(curBudget.budgetItem.biProjectedAmount),
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
                        confirmCompleteTransaction(curBudget)
                    }

                    2 -> {
                        openBudgetItem(curBudget, it)
                    }

                    3 -> {
                        cancelBudgetItem(curBudget)
                    }

                    4 -> {
                        gotoBudgetRule(curBudget, it)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmCompleteTransaction(curBudget: BudgetDetailed) {
        AlertDialog.Builder(context)
            .setTitle("'Confirm Completing transaction")
            .setItems(
                arrayOf(
                    "This will complete ${curBudget.budgetItem!!.biBudgetName} for amount of\n" +
                            nf.displayDollars(curBudget.budgetItem.biProjectedAmount)
                ),
                null
            )
            .setPositiveButton("Complete Now") { _, _ ->
                completeTransaction(curBudget)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun completeTransaction(curBudget: BudgetDetailed) {
        CoroutineScope(Dispatchers.IO).launch {
            val mToAccountWithType =
                mainActivity.accountViewModel.getAccountWithType(
                    curBudget.budgetItem!!.biToAccountId
                )
            val mFromAccountWithType =
                mainActivity.accountViewModel.getAccountWithType(
                    curBudget.budgetItem.biFromAccountId
                )
            delay(WAIT_100)
            val curTransaction = getCurTransactionObject(
                curBudget.budgetItem,
                updateAccountBalance(
                    mToAccountWithType,
                    curBudget.budgetItem.biProjectedAmount,
                ),
                updateAccountBalance(
                    mFromAccountWithType,
                    curBudget.budgetItem.biProjectedAmount,
                )
            )
            delay(WAIT_250)
            mainActivity.transactionViewModel.insertTransaction(curTransaction)
            delay(WAIT_100)
            curBudget.budgetItem.apply {
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
        }
        budgetViewFragment.populateBudgetTotals()
    }

    private fun updateAccountBalance(accountWithType: AccountWithType, transAmount: Double):
            Boolean {
        accountWithType.apply {
            if (accountType!!.keepTotals) {
                mainActivity.transactionViewModel.updateAccountBalance(
                    account.accountBalance - transAmount,
                    account.accountId,
                    df.getCurrentTimeAsString()
                )
                return false
            } else if (!accountType.allowPending && accountType.tallyOwing) {
                mainActivity.transactionViewModel.updateAccountOwing(
                    account.accountOwing + transAmount,
                    account.accountId,
                    df.getCurrentTimeAsString()
                )
                return false
            } else if (!accountType.keepTotals && !accountType.tallyOwing) {
                return false
            }
        }
        return true
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

    private fun gotoBudgetRule(curBudget: BudgetDetailed, it: View) {
        mainActivity.mainViewModel.setBudgetRuleDetailed(
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
        mainActivity.budgetItemViewModel.cancelBudgetItem(
            curBudget.budgetItem!!.biRuleId,
            curBudget.budgetItem.biProjectedDate,
            df.getCurrentTimeAsString()
        )
        budgetViewFragment.populateBudgetTotals()
    }

    private fun openBudgetItem(curBudget: BudgetDetailed, it: View) {
        mainActivity.mainViewModel.setBudgetItem(curBudget)
        setToReturn()
        it.findNavController().navigate(
            BudgetViewFragmentDirections
                .actionBudgetViewFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun performTransaction(curBudget: BudgetDetailed, it: View) {
        mainActivity.mainViewModel.setBudgetItem(curBudget)
        mainActivity.mainViewModel.setTransactionDetailed(null)
        setToReturn()
        it.findNavController().navigate(
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