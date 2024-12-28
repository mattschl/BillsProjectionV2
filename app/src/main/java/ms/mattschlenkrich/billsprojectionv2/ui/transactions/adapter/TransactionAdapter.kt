package ms.mattschlenkrich.billsprojectionv2.ui.transactions.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionViewFragmentDirections
import java.util.Random

private const val PARENT_TAG = FRAG_TRANSACTION_VIEW


class TransactionAdapter(
    val mainActivity: MainActivity,
    private val mainViewModel: MainViewModel,
    private val mView: View,
) : RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val transactionViewModel =
        mainActivity.transactionViewModel
    private val accountUpdateViewModel =
        mainActivity.accountUpdateViewModel


    class TransactionsViewHolder(
        val itemBinding: TransactionLinearItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem.transaction?.transId ==
                        newItem.transaction?.transId &&
                        oldItem.budgetRule?.ruleId ==
                        newItem.budgetRule?.ruleId &&
                        oldItem.toAccount?.accountId ==
                        newItem.toAccount?.accountId &&
                        oldItem.fromAccount?.accountId ==
                        newItem.fromAccount?.accountId
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
    ): TransactionsViewHolder {
        return TransactionsViewHolder(
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
        holder: TransactionsViewHolder,
        position: Int,
    ) {
        val transaction =
            differ.currentList[
                position
            ]
        holder.itemBinding.apply {
            tvDate.text =
                df.getDisplayDate(transaction.transaction!!.transDate)
            tvTransDescription.text =
                transaction.transaction.transName
            tvTransAmount.text =
                nf.displayDollars(transaction.transaction.transAmount)
            var info = mView.context.getString(R.string.to_) +
                    transaction.toAccount!!
                        .accountName
            if (transaction.transaction.transToAccountPending) {
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
                    transaction.fromAccount!!
                        .accountName
            if (transaction.transaction.transFromAccountPending) {
                info += mView.context.getString(R.string._pending)
                tvFromAccount.setTextColor(
                    Color.RED
                )
            } else {
                tvFromAccount.setTextColor(
                    Color.BLACK
                )
            }
            if (transaction.transaction.transToAccountPending &&
                transaction.transaction.transFromAccountPending
            ) {
                tvToAccount.setLines(2)
                tvFromAccount.setLines(2)

            }
            tvFromAccount.text = info
            if (transaction.transaction.transNote.isEmpty()) {
                tvTransInfo.visibility = View.GONE
            } else {
                info = "Note: " +
                        transaction.transaction.transNote
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

            holder.itemView.setOnClickListener {
                chooseOptions(transaction)
            }
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
                        transaction.transaction.transName
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
                        AlertDialog.Builder(mView.context)
                            .setTitle(
                                mView.context.getString(R.string.are_you_sure_you_want_to_delete) +
                                        transaction.transaction.transName
                            )
                            .setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
                                deleteTransaction(transaction.transaction)
                            }
                            .setNegativeButton(mView.context.getString(R.string.cancel), null)
                            .show()
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
            accountUpdateViewModel.updateTransaction(
                transaction.transaction,
                newTransaction
            )
        }
    }

    private fun gotoTransactionUpdate(
        transaction: TransactionDetailed
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setTransactionDetailed(transaction)
        CoroutineScope(Dispatchers.IO).launch {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transaction.transaction!!.transId,
                    transaction.transaction.transToAccountId,
                    transaction.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(oldTransactionFull.await())
        }
        gotoTransactionUpdateFragment()
    }

    private fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            TransactionViewFragmentDirections
                .actionTransactionViewFragmentToTransactionUpdateFragment()
        )
    }

    private fun deleteTransaction(transaction: Transactions) {
        mainActivity.accountUpdateViewModel.deleteTransaction(
            transaction
        )
    }
}