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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_500
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.PendingTransactionItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.BudgetViewFragment
import java.util.Random

//private const val PARENT_TAG = FRAG_BUDGET_VIEW

class TransactionPendingAdapter(
    val mainActivity: MainActivity,
    private val curAccount: String,
    private val mView: View,
    private val parentTag: String,
    private val budgetViewFragment: BudgetViewFragment,
) : RecyclerView.Adapter<TransactionPendingAdapter.TransactionPendingHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val accountViewModel = mainActivity.accountViewModel
    private val transactionViewModel = mainActivity.transactionViewModel

    class TransactionPendingHolder(val itemBinding: PendingTransactionItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<TransactionDetailed>() {
        override fun areItemsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem.transaction!!.transId == newItem.transaction!!.transId &&
                    oldItem.transaction.transName == newItem.transaction.transName &&
                    oldItem.toAccount?.accountId == newItem.toAccount?.accountId &&
                    oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId &&
                    oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionPendingHolder {
        return TransactionPendingHolder(
            PendingTransactionItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TransactionPendingHolder, position: Int) {
        val pendingTransaction = differ.currentList[position]
        val widthPixels = mView.context.resources.displayMetrics.widthPixels

        holder.itemBinding.apply {
            tvPendingDate.text = df.getDisplayDate(pendingTransaction.transaction!!.transDate)
            tvPendingDate.width = widthPixels * 85 / 360
            tvPendingDate.setTextColor(Color.BLACK)
            tvPendingAmount.text = nf.displayDollars(pendingTransaction.transaction.transAmount)
            if (pendingTransaction.toAccount!!.accountName == curAccount) {
                tvPendingAmount.setTextColor(Color.BLACK)
                tvPendingDescription.setTextColor(Color.BLACK)
            } else {
                tvPendingAmount.setTextColor(Color.RED)
                tvPendingDescription.setTextColor(Color.RED)
            }
            tvPendingDescription.maxWidth = widthPixels * 200 / 360
            var display = pendingTransaction.transaction.transName
            display += if (pendingTransaction.transaction.transNote.isNotBlank()) {
                " - " + pendingTransaction.transaction.transNote
            } else {
                ""
            }
            tvPendingDescription.text = display
            val random = Random()
            val color = Color.argb(
                255, random.nextInt(256), random.nextInt(256), random.nextInt(256)
            )
            vColor.setBackgroundColor(color)
            holder.itemView.setOnClickListener { chooseOptionsForTransaction(pendingTransaction) }

        }
    }

    private fun chooseOptionsForTransaction(pendingTransaction: TransactionDetailed) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_an_action_for) + nf.displayDollars(
                pendingTransaction.transaction!!.transAmount
            ) + mView.context.getString(R.string._to_) + pendingTransaction.transaction.transName
        ).setItems(
            arrayOf(
                mView.context.getString(R.string.complete_this_pending_transaction),
                mView.context.getString(R.string.open_the_transaction_to_edit_it),
                mView.context.getString(R.string.delete_this_pending_transaction)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    confirmTransaction(pendingTransaction, curAccount)
                }

                1 -> {
                    editTransaction(pendingTransaction)
                }

                2 -> {
                    deleteTransaction(pendingTransaction)
                }
            }
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun confirmTransaction(
        pendingTransaction: TransactionDetailed, curAccount: String
    ) {
        var display =
            mView.context.getString(R.string.this_will_apply_the_amount_of) + nf.displayDollars(
                pendingTransaction.transaction!!.transAmount
            )
        display += if (pendingTransaction.transaction.transToAccountPending) {
            mView.context.getString(R.string._to_)
        } else {
            mView.context.getString(R.string._From_)
        }
        display += curAccount
        confirmCompleteTransaction(display, pendingTransaction, curAccount)
    }

    private fun confirmCompleteTransaction(
        display: String, pendingTransaction: TransactionDetailed, curAccount: String
    ) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.confirm_completing_transaction))
            .setMessage(display)
            .setPositiveButton(mView.context.getString(R.string.confirm)) { _, _ ->
                completeTransaction(
                    pendingTransaction,
                    curAccount
                )
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun deleteTransaction(transaction: TransactionDetailed) {
        transactionViewModel.deleteTransaction(
            transaction.transaction!!.transId, df.getCurrentTimeAsString()
        )
        budgetViewFragment.populatePendingList()
    }

    private fun editTransaction(transaction: TransactionDetailed) {
        mainViewModel.setCallingFragments(parentTag)
        mainViewModel.setTransactionDetailed(transaction)
        CoroutineScope(Dispatchers.IO).launch {
            val transactionFull = async {
                transactionViewModel.getTransactionFull(
                    transaction.transaction!!.transId,
                    transaction.transaction.transToAccountId,
                    transaction.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(transactionFull.await())

        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            budgetViewFragment.gotoTransactionUpdateFragment()
        }
    }

    private fun completeTransaction(transaction: TransactionDetailed?, curAccount: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (transaction!!.toAccount!!.accountName == curAccount) {
                CoroutineScope(Dispatchers.Default).launch {
                    transactionViewModel.updateTransaction(
                        Transactions(
                            transaction.transaction!!.transId,
                            transaction.transaction.transDate,
                            transaction.transaction.transName,
                            transaction.transaction.transNote,
                            transaction.transaction.transRuleId,
                            transaction.transaction.transToAccountId,
                            false,
                            transaction.transaction.transFromAccountId,
                            transaction.transaction.transFromAccountPending,
                            transaction.transaction.transAmount,
                            transaction.transaction.transIsDeleted,
                            df.getCurrentTimeAsString()
                        )
                    )
                    val account = async {
                        accountViewModel.getAccountWithType(
                            curAccount
                        )
                    }
                    val accountDetailed = account.await()
                    if (accountDetailed.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            accountDetailed.account.accountBalance + transaction.transaction.transAmount,
                            accountDetailed.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (accountDetailed.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            accountDetailed.account.accountOwing - transaction.transaction.transAmount,
                            accountDetailed.account.accountId,
                            df.getCurrentTimeAsString()

                        )
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Default).launch {
                    transactionViewModel.updateTransaction(
                        Transactions(
                            transaction.transaction!!.transId,
                            transaction.transaction.transDate,
                            transaction.transaction.transName,
                            transaction.transaction.transNote,
                            transaction.transaction.transRuleId,
                            transaction.transaction.transToAccountId,
                            transaction.transaction.transToAccountPending,
                            transaction.transaction.transFromAccountId,
                            false,
                            transaction.transaction.transAmount,
                            transaction.transaction.transIsDeleted,
                            df.getCurrentTimeAsString()
                        )
                    )
                    val account = async {
                        accountViewModel.getAccountWithType(curAccount)
                    }
                    val accountDetailed = account.await()
                    if (accountDetailed.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            accountDetailed.account.accountBalance - transaction.transaction.transAmount,
                            accountDetailed.account.accountId,
                            df.getCurrentTimeAsString()
                        )
                    } else if (accountDetailed.accountType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            accountDetailed.account.accountOwing + transaction.transaction.transAmount,
                            accountDetailed.account.accountId,
                            df.getCurrentTimeAsString()

                        )
                    }
                }
            }
            delay(WAIT_500)
            budgetViewFragment.populatePendingList()
        }
    }
}