package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountUpdateFragment
import java.util.Random

class AccountUpdateHistoryAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentFragment: String,
    private val accountUpdateFragment: AccountUpdateFragment,
) : RecyclerView.Adapter<AccountUpdateHistoryAdapter.HistoryHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val transactionViewModel = mainActivity.transactionViewModel
    private val accountUpdateViewModel = mainActivity.accountUpdateViewModel
    private val budgetRuleViewModel = mainActivity.budgetRuleViewModel

    class HistoryHolder(
        val itemBinding: TransactionLinearItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<TransactionDetailed>() {
        override fun areItemsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem.transaction?.transId == newItem.transaction?.transId && oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId && oldItem.toAccount?.accountId == newItem.toAccount?.accountId && oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId
        }

        override fun areContentsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HistoryHolder {
        return HistoryHolder(
            TransactionLinearItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val transactionDetailed = differ.currentList[position]
        holder.itemBinding.apply {
            tvDate.text = df.getDisplayDate(transactionDetailed.transaction!!.transDate)
            tvTransDescription.text = transactionDetailed.transaction.transName
            tvTransAmount.text = nf.displayDollars(transactionDetailed.transaction.transAmount)
            var info =
                mView.context.getString(R.string.to_) + transactionDetailed.toAccount!!.accountName
            if (transactionDetailed.transaction.transToAccountPending) {
                info += mView.context.getString(R.string._pending)
                tvToAccount.setTextColor(
                    Color.RED
                )
            } else {
                tvToAccount.setTextColor(
                    Color.BLACK
                )
            }
            tvToAccount.text = info
            info =
                mView.context.getString(R.string.from_) + transactionDetailed.fromAccount!!.accountName
            if (transactionDetailed.transaction.transFromAccountPending) {
                info += mView.context.getString(R.string._pending)
                tvFromAccount.setTextColor(
                    Color.RED
                )
            } else {
                tvFromAccount.setTextColor(
                    Color.BLACK
                )
            }
            if (transactionDetailed.transaction.transToAccountPending && transactionDetailed.transaction.transFromAccountPending) {
                tvToAccount.setLines(2)
                tvFromAccount.setLines(2)

            }
            tvFromAccount.text = info
            if (transactionDetailed.transaction.transNote.isEmpty()) {
                tvTransInfo.visibility = View.GONE
            } else {
                info =
                    mView.context.getString(R.string.note_) + transactionDetailed.transaction.transNote
                tvTransInfo.text = info
                tvTransInfo.visibility = View.VISIBLE
            }
            val random = Random()
            val color = Color.argb(
                255, random.nextInt(256), random.nextInt(256), random.nextInt(256)
            )
            ibColor.setBackgroundColor(color)
            holder.itemView.setOnClickListener {
                chooseOptions(transactionDetailed)
            }
        }
    }

    private fun chooseOptions(transactionDetailed: TransactionDetailed) {
        var display = ""
        if (transactionDetailed.transaction!!.transToAccountPending) {
            display += mView.context.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + mView.context.getString(R.string._to_) + transactionDetailed.toAccount!!.accountName
        }
        if (transactionDetailed.transaction.transToAccountPending) {
            display += mView.context.getString(R.string._pending)
        }
        if (display != "" && transactionDetailed.transaction.transFromAccountPending) {
            display += mView.context.getString(R.string._and)
        }
        if (transactionDetailed.transaction.transFromAccountPending) {
            display += mView.context.getString(R.string.complete_the_pending_amount_of) + nf.displayDollars(
                transactionDetailed.transaction.transAmount
            ) + mView.context.getString(R.string._From_) + transactionDetailed.fromAccount!!.accountName
        }
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_an_action_for) + transactionDetailed.transaction.transName
        ).setItems(
            arrayOf(
                mView.context.getString(R.string.edit_this_transaction),
                display,
                mView.context.getString(R.string.go_to_the_rules_for_future_budgets_of_this_kind),
                mView.context.getString(R.string.delete_this_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    gotoTransactionUpdate(transactionDetailed)
                }

                1 -> {
                    if (transactionDetailed.transaction.transToAccountPending || transactionDetailed.transaction.transFromAccountPending) {
                        confirmCompletePendingTransactions(transactionDetailed)
                    }
                }

                2 -> {
                    gotoBudgetRuleUpdate(transactionDetailed)
                }

                3 -> {
                    confirmDeleteTransaction(transactionDetailed)
                }
            }
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(parentFragment)
        mainViewModel.setTransactionDetailed(transactionDetailed)
        CoroutineScope(Dispatchers.IO).launch {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transactionDetailed.transaction!!.transId,
                    transactionDetailed.transaction.transToAccountId,
                    transactionDetailed.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(oldTransactionFull.await())
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            accountUpdateFragment.gotoTransactionUpdate()
        }
    }

    private fun confirmCompletePendingTransactions(transactionDetailed: TransactionDetailed) {
        var display = mView.context.getString(R.string.this_will_apply_the_amount_of) +
                nf.displayDollars(transactionDetailed.transaction!!.transAmount)
        display += if (transactionDetailed.transaction.transToAccountPending) {
            mView.context.getString(R.string.to_) + transactionDetailed.toAccount!!.accountName
        } else {
            ""
        }
        display += if (transactionDetailed.transaction.transToAccountPending && transactionDetailed.transaction.transFromAccountPending) {
            mView.context.getString(R.string._and)
        } else {
            ""
        }
        display += if (transactionDetailed.transaction.transFromAccountPending) {
            mView.context.getString(R.string.from) + transactionDetailed.fromAccount!!.accountName
        } else {
            ""
        }
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(mView.context.getString(R.string.confirm)) { _, _ ->
                completePendingTransactions(
                    transactionDetailed
                )
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun completePendingTransactions(transactionDetailed: TransactionDetailed) {
        CoroutineScope(Dispatchers.Main).launch {
            transactionDetailed.transaction!!.apply {
                val newTransaction = Transactions(
                    transId,
                    transDate,
                    transName,
                    transNote,
                    transRuleId,
                    transToAccountId,
                    false,
                    transFromAccountId,
                    false,
                    transAmount,
                    transIsDeleted,
                    df.getCurrentTimeAsString()
                )
                accountUpdateViewModel.updateTransaction(
                    transactionDetailed.transaction, newTransaction
                )
            }
            delay(WAIT_500)
            accountUpdateFragment.updateBalances()
        }
    }

    private fun gotoBudgetRuleUpdate(transactionDetailed: TransactionDetailed) {
        mainViewModel.setCallingFragments(parentFragment)
        budgetRuleViewModel.getBudgetRuleFullLive(
            transactionDetailed.transaction!!.transRuleId
        ).observe(mView.findViewTreeLifecycleOwner()!!) { bRuleDetailed ->
            mainViewModel.setBudgetRuleDetailed(bRuleDetailed)
            accountUpdateFragment.gotoBudgetRuleUpdateFragment()
        }
    }

    private fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.are_you_sure_you_want_to_delete) + transactionDetailed.transaction!!.transName
        ).setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
            deleteTransaction(transactionDetailed.transaction)
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun deleteTransaction(transaction: Transactions) {
        CoroutineScope(Dispatchers.Main).launch {
            accountUpdateViewModel.deleteTransaction(transaction)
            delay(WAIT_500)
            accountUpdateFragment.updateBalances()
        }
    }

}