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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.transactions.TransactionViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = "TransactionAdapter"

class TransactionAdapter(
    private val mainActivity: MainActivity,
    private val mainViewModel: MainViewModel,
    private val context: Context,
) : RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder>() {

    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private val transactionViewModel =
        mainActivity.transactionViewModel
    private val accountViewModel =
        mainActivity.accountViewModel


    class TransactionsViewHolder(
        val itemBinding: TransactionLinearItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                Log.d(
                    TAG, "transId = ${oldItem.transaction?.transId} \n" +
                            "rulId = ${oldItem.budgetRule?.ruleId} \n" +
                            "toAccountId = ${oldItem.toAccount?.accountId} \n" +
                            "fromAccountId = ${oldItem.fromAccount?.accountId}"
                )
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
                Log.d(TAG, "'in areContentsTheSame ")
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
        Log.d(TAG, "position = $position")
        val transaction =
            differ.currentList[
                position
            ]
        holder.itemBinding.tvDate.text =
            df.getDisplayDate(transaction.transaction!!.transDate)
        holder.itemBinding.tvTransDescription.text =
            transaction.transaction.transName
        holder.itemBinding.tvTransAmount.text =
            cf.displayDollars(transaction.transaction.transAmount)
        var info = "To: " +
                transaction.toAccount!!
                    .accountName
        if (transaction.transaction.transToAccountPending) {
            info += " *PENDING*"
            holder.itemBinding.tvToAccount.setTextColor(
                Color.RED
            )
        } else {
            holder.itemBinding.tvToAccount.setTextColor(
                Color.BLACK
            )
        }
        holder.itemBinding.tvToAccount.text = info
        info = "From: " +
                transaction.fromAccount!!
                    .accountName
        if (transaction.transaction.transFromAccountPending) {
            info += " *PENDING*"
            holder.itemBinding.tvFromAccount.setTextColor(
                Color.RED
            )
        } else {
            holder.itemBinding.tvFromAccount.setTextColor(
                Color.BLACK
            )
        }
        holder.itemBinding.tvFromAccount.text = info
        if (transaction.transaction.transNote.isEmpty()) {
            holder.itemBinding.tvTransInfo.visibility = View.GONE
        } else {
            info = "Note: " +
                    transaction.transaction.transNote
            holder.itemBinding.tvTransInfo.text = info
            holder.itemBinding.tvTransInfo.visibility = View.VISIBLE
        }
        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)

        holder.itemView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(
                    "Choose action for " +
                            transaction.transaction.transName
                )
                .setItems(
                    arrayOf(
                        "Edit this transaction",
                        "Delete this transaction"
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> {
                            val direction = TransactionViewFragmentDirections
                                .actionTransactionViewFragmentToTransactionUpdateFragment()
                            it.findNavController().navigate(direction)
                        }

                        1 -> {
                            AlertDialog.Builder(context)
                                .setTitle(
                                    "Are you sure you want to delete " +
                                            transaction.transaction.transName
                                )
                                .setPositiveButton("Delete") { _, _ ->
                                    deleteTransaction(transaction.transaction)
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun deleteTransaction(transaction: Transactions) {
        CoroutineScope(Dispatchers.IO).launch {
            transactionViewModel.deleteTransaction(
                transaction.transId,
                df.getCurrentTimeAsString()
            )
            val oldTransaction =
                transactionViewModel.getTransactionFull(
                    transaction.transId,
                    transaction.transToAccountId,
                    transaction.transFromAccountId
                )
            if (!oldTransaction.transaction.transToAccountPending) {
                if (oldTransaction.toAccountAndType.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        oldTransaction.toAccountAndType.accountBalance -
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (oldTransaction.toAccountAndType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        oldTransaction.toAccountAndType.accountOwing +
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (!oldTransaction.transaction.transFromAccountPending) {
                    if (oldTransaction.fromAccountAndType.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            oldTransaction.fromAccountAndType.accountBalance +
                                    oldTransaction.transaction.transAmount,
                            oldTransaction.transaction.transFromAccountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                    if (oldTransaction.fromAccountAndType.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            oldTransaction.fromAccountAndType.accountOwing -
                                    oldTransaction.transaction.transAmount,
                            oldTransaction.transaction.transFromAccountId,
                            df.getCurrentTimeAsString()
                        )

                    }
                }
            }
        }
    }
}