package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_100
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetViewItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragment
import java.util.Random

private const val PARENT_TAG = FRAG_BUDGET_VIEW


class BudgetViewAdapter(
    private val budgetViewFragment: BudgetViewFragment,
    private val mainActivity: MainActivity,
    private val curAccount: String,
    private val curPayDay: String,
    private val mView: View,
) : RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    var message = ""

    class BudgetViewHolder(val itemBinding: BudgetViewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetItemDetailed>() {
            override fun areContentsTheSame(
                oldItem: BudgetItemDetailed,
                newItem: BudgetItemDetailed
            ): Boolean {
                return oldItem.budgetItem!!.biProjectedDate == newItem.budgetItem!!.biProjectedDate &&
                        oldItem.budgetItem.biRuleId == newItem.budgetItem.biRuleId &&
                        oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId &&
                        oldItem.toAccount?.accountId == newItem.toAccount?.accountId &&
                        oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId
            }

            override fun areItemsTheSame(
                oldItem: BudgetItemDetailed,
                newItem: BudgetItemDetailed
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

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val curBudget = differ.currentList[position]

        holder.itemBinding.apply {
            tvDate.text =
                df.getDisplayDate(curBudget.budgetItem!!.biActualDate)
            tvName.text = curBudget.budgetItem.biBudgetName
            if (curBudget.budgetItem.biIsFixed) {
                val newText = curBudget.budgetItem.biBudgetName +
                        mView.context.getString(R.string._fixed_)
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
            var info =
                mView.context.getString(R.string.to_) + curBudget.toAccount!!.accountName
            tvToAccount.text = info
            info =
                mView.context.getString(R.string.from_) + curBudget.fromAccount!!.accountName
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

    private fun chooseLockUnlock(budgetItem: BudgetItemDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.lock_or_unlock))
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.lock) +
                            budgetItem.budgetItem!!.biBudgetName,
                    mView.context.getString(R.string.un_lock) +
                            budgetItem.budgetItem.biBudgetName,
                    mView.context.getString(R.string.lock_all_items_for_this_payday),
                    mView.context.getString(R.string.un_lock_all_items_for_this_payday)
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
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun chooseOptionsForBudget(curBudget: BudgetItemDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_an_action_for) +
                        curBudget.budgetItem!!.biBudgetName
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.perform_a_transaction_on_) +
                            " \"${curBudget.budgetItem.biBudgetName}\" ",
                    if (curBudget.budgetItem.biProjectedAmount == 0.0) {
                        ""
                    } else {
                        mView.context.getString(R.string.perform_action) +
                                "\"${curBudget.budgetItem.biBudgetName}\" " +
                                mView.context.getString(R.string.for_amount_of_the_full_amount_) +
                                nf.displayDollars(curBudget.budgetItem.biProjectedAmount)
                    },
                    mView.context.getString(R.string.adjust_the_projections_for_this_item),
                    mView.context.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                    mView.context.getString(R.string.cancel_this_projected_item),
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
                        gotoBudgetItem(curBudget)
                    }

                    3 -> {
                        gotoBudgetRule(curBudget)
                    }

                    4 -> {
                        confirmCancelBudgetItem(curBudget)
                    }
                }
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun performTransaction(curBudget: BudgetItemDetailed) {
        mainActivity.mainViewModel.setBudgetItemDetailed(curBudget)
        mainActivity.mainViewModel.setTransactionDetailed(null)
        setReturnVariables()
        budgetViewFragment.gotoTransactionPerformFragment()
    }

    private fun confirmCompleteTransaction(curBudget: BudgetItemDetailed) {
        if (curBudget.budgetItem!!.biProjectedAmount > 0.0) {
            var display = ""
            CoroutineScope(Dispatchers.IO).launch {
                display = getConfirmationsDisplay(curBudget.budgetItem, curBudget)
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                AlertDialog.Builder(mView.context)
                    .setTitle(mView.context.getString(R.string.confirm_completing_transaction))
                    .setMessage(
                        display
                    )
                    .setPositiveButton(mView.context.getString(R.string.complete_now)) { _, _ ->
                        completeTransaction(curBudget)
                    }
                    .setNegativeButton(mView.context.getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    private suspend fun getConfirmationsDisplay(
        budgetItem: BudgetItem, curBudget: BudgetItemDetailed
    ): String {
        var display = mView.context.getString(R.string.this_will_perform) +
                budgetItem.biBudgetName +
                mView.context.getString(R.string.applying_the_amount_of) +
                nf.displayDollars(curBudget.budgetItem!!.biProjectedAmount) +
                mView.context.getString(R.string.from) +
                curBudget.fromAccount!!.accountName
        display += if (mainActivity.accountUpdateViewModel.isTransactionPending(
                budgetItem.biFromAccountId
            )
        ) {
            mView.context.getString(R.string._pending)
        } else {
            ""
        }
        delay(WAIT_100)
        display += mView.context.getString(R.string._to) +
                curBudget.toAccount!!.accountName
        display += if (mainActivity.accountUpdateViewModel.isTransactionPending(
                budgetItem.biToAccountId
            )
        ) {
            mView.context.getString(R.string._pending)
        } else {
            ""
        }
        delay(WAIT_100)
        return display
    }

    private fun completeTransaction(curBudget: BudgetItemDetailed) {
        updateAccountsAndTransaction(curBudget)
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            budgetViewFragment.populateBudgetList()
        }
    }

    private fun updateAccountsAndTransaction(curBudget: BudgetItemDetailed): Boolean {
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

    private fun confirmCancelBudgetItem(curBudget: BudgetItemDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.confirm_cancelling_budget_item))
            .setMessage(
                mView.context.getString(R.string.this_will_cancel) +
                        curBudget.budgetItem!!.biBudgetName +
                        mView.context.getString(R.string.with_the_amount_of) +
                        nf.displayDollars(curBudget.budgetItem.biProjectedAmount) +
                        mView.context.getString(R.string._remaining)
            )
            .setPositiveButton(mView.context.getString(R.string.cancel_now)) { _, _ ->
                cancelBudgetItem(curBudget)
            }
            .setNegativeButton(mView.context.getString(R.string.ignore_this), null)
            .show()
    }

    private fun cancelBudgetItem(curBudget: BudgetItemDetailed) {
        mainActivity.budgetItemViewModel.cancelBudgetItem(
            curBudget.budgetItem!!.biRuleId,
            curBudget.budgetItem.biProjectedDate,
            df.getCurrentTimeAsString()
        )
        budgetViewFragment.populateBudgetList()
    }

    private fun gotoBudgetRule(curBudget: BudgetItemDetailed) {
        mainActivity.mainViewModel.setBudgetRuleDetailed(
            BudgetRuleDetailed(
                curBudget.budgetRule,
                curBudget.toAccount,
                curBudget.fromAccount
            )
        )
        setReturnVariables()
        budgetViewFragment.gotoBudgetRuleUpdateFragment()
    }

    private fun gotoBudgetItem(curBudget: BudgetItemDetailed) {
        mainActivity.mainViewModel.setBudgetItemDetailed(curBudget)
        setReturnVariables()
        budgetViewFragment.gotoBudgetItemUpdateFragment()
    }

    private fun setReturnVariables() {
        mainActivity.mainViewModel.setCallingFragments(
            PARENT_TAG
        )
        mainActivity.mainViewModel.setReturnToAsset(curAccount)
        mainActivity.mainViewModel.setReturnToPayDay(curPayDay)
    }
}