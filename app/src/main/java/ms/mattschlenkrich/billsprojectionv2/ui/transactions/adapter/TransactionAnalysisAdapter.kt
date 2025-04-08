package ms.mattschlenkrich.billsprojectionv2.ui.transactions.adapter

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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAnalysisFragment
import java.util.Random

//private const val TAG = ADAPTER_TRANSACTION_ANALYSIS
//private const val PARENT_TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val transactionAnalysisFragment: TransactionAnalysisFragment,
    private val parentTag: String,
) : RecyclerView.Adapter<TransactionAnalysisAdapter.TransViewHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
//    private val transactionViewModel =
//        mainActivity.transactionViewModel

    class TransViewHolder(
        val itemBinding: TransactionLinearItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem.transaction?.transId == newItem.transaction?.transId &&
                        oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId &&
                        oldItem.toAccount?.accountId == newItem.toAccount?.accountId &&
                        oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId
            }

            override fun areContentsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransViewHolder {
        return TransViewHolder(
            TransactionLinearItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: TransViewHolder,
        position: Int,
    ) {
        val transactionDetailed =
            differ.currentList[
                position
            ]
        holder.itemBinding.apply {
            tvDate.text =
                df.getDisplayDate(transactionDetailed.transaction!!.transDate)
            tvTransDescription.text =
                transactionDetailed.transaction.transName
            tvTransAmount.text =
                nf.displayDollars(transactionDetailed.transaction.transAmount)
            var info = mView.context.getString(R.string._to_) +
                    transactionDetailed.toAccount!!
                        .accountName
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
            info = mView.context.getString(R.string.from_) +
                    transactionDetailed.fromAccount!!
                        .accountName
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
            if (transactionDetailed.transaction.transToAccountPending &&
                transactionDetailed.transaction.transFromAccountPending
            ) {
                tvToAccount.setLines(2)
                tvFromAccount.setLines(2)

            }
            tvFromAccount.text = info
            if (transactionDetailed.transaction.transNote.isEmpty()) {
                tvTransInfo.visibility = View.GONE
            } else {
                info = mView.context.getString(R.string.note_) +
                        transactionDetailed.transaction.transNote
                tvTransInfo.text = info
                tvTransInfo.visibility = View.VISIBLE
            }
            val random = Random()
            val color = Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
            ibColor.setBackgroundColor(color)
        }
        holder.itemView.setOnClickListener {
            chooseOptions(transactionDetailed)
        }
    }

    private fun chooseOptions(transaction: TransactionDetailed) {
        var display = ""
        if (transaction.transaction!!.transToAccountPending) {
            display += mView.context.getString(R.string.complete_the_pending_amount_of) +
                    nf.displayDollars(transaction.transaction.transAmount) +
                    mView.context.getString(R.string._to_) +
                    transaction.toAccount!!.accountName
        }
        if (transaction.transaction.transToAccountPending) {
            display += mView.context.getString(R.string._pending)
        }
        if (display != "" && transaction.transaction.transFromAccountPending) {
            display += mView.context.getString(R.string._and)
        }
        if (transaction.transaction.transFromAccountPending) {
            display += mView.context.getString(R.string.complete_the_pending_amount_of) +
                    nf.displayDollars(transaction.transaction.transAmount) +
                    mView.context.getString(R.string._From_) +
                    transaction.fromAccount!!.accountName
        }
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_an_action_for) +
                        transaction.transaction!!.transName
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.edit_this_transaction),
                    display,
                    mView.context.getString(R.string.delete_this_transaction)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        gotoTransactionUpdate(transaction)
                    }

                    1 -> {
                        if (transaction.transaction.transToAccountPending ||
                            transaction.transaction.transFromAccountPending
                        ) {
                            completePendingTransactions(transaction)
                        }
                    }

                    2 -> {
                        confirmDeleteTransaction(transaction)

                    }
                }
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun completePendingTransactions(transaction: TransactionDetailed) {
        transaction.transaction!!.apply {
            val newTransaction =
                Transactions(
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
                    transUpdateTime
                )
            mainActivity.accountUpdateViewModel.updateTransaction(
                transaction.transaction,
                newTransaction
            )
        }
    }

    private fun confirmDeleteTransaction(transactionDetailed: TransactionDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.are_you_sure_you_want_to_delete) +
                        transactionDetailed.transaction!!.transName
            )
            .setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
                deleteTransaction(transactionDetailed.transaction)
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()

    }

    private fun deleteTransaction(transaction: Transactions) {
        mainActivity.accountUpdateViewModel.deleteTransaction(
            transaction
        )
    }

    private fun gotoTransactionUpdate(transactionDetailed: TransactionDetailed) {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() +
                    ", " + parentTag
        )
        mainActivity.mainViewModel.setTransactionDetailed(transactionDetailed)
        CoroutineScope(Dispatchers.IO).launch {
            val oldTransactionFull = async {
                mainActivity.transactionViewModel.getTransactionFull(
                    transactionDetailed.transaction!!.transId,
                    transactionDetailed.transaction.transToAccountId,
                    transactionDetailed.transaction.transFromAccountId
                )
            }
            mainActivity.mainViewModel.setOldTransaction(oldTransactionFull.await())
        }
        transactionAnalysisFragment.gotoTransactionUpdateFragment()
    }
}