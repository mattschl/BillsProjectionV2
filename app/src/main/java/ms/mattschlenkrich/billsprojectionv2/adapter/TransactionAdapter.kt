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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.transactions.TransactionViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_TRANSACTION
private const val PARENT_TAG = FRAG_TRANSACTION_VIEW

class TransactionAdapter(
    val mainActivity: MainActivity,
    private val mainViewModel: MainViewModel,
    private val context: Context,
) : RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder>() {

    private val cf = NumberFunctions()
    private val df = DateFunctions()
    private val transactionViewModel =
        mainActivity.transactionViewModel
//    private val accountViewModel =
//        mainActivity.accountViewModel


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
                cf.displayDollars(transaction.transaction.transAmount)
            var info = "To: " +
                    transaction.toAccount!!
                        .accountName
            if (transaction.transaction.transToAccountPending) {
                info += " *PENDING*"
                tvToAccount.setTextColor(
                    Color.RED
                )
            } else {
                tvToAccount.setTextColor(
                    Color.BLACK
                )
            }
            tvToAccount.text = info
            info = "From: " +
                    transaction.fromAccount!!
                        .accountName
            if (transaction.transaction.transFromAccountPending) {
                info += " *PENDING*"
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
                            gotoTransactionUpdate(transaction, transaction.transaction, it)
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
    }

    private fun gotoTransactionUpdate(
        transaction: TransactionDetailed?,
        transaction0: Transactions,
        it: View
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setTransactionDetailed(transaction)
        CoroutineScope(Dispatchers.IO).launch {
            val oldTransactionFull = async {
                transactionViewModel.getTransactionFull(
                    transaction0.transId,
                    transaction0.transToAccountId,
                    transaction0.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(oldTransactionFull.await())
        }
        it.findNavController().navigate(
            TransactionViewFragmentDirections
                .actionTransactionViewFragmentToTransactionUpdateFragment()
        )
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
                if (oldTransaction.toAccountAndType.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        oldTransaction.toAccountAndType.account.accountBalance -
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (oldTransaction.toAccountAndType.accountType!!.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        oldTransaction.toAccountAndType.account.accountOwing +
                                oldTransaction.transaction.transAmount,
                        oldTransaction.transaction.transToAccountId,
                        df.getCurrentTimeAsString()
                    )
                }
                if (!oldTransaction.transaction.transFromAccountPending) {
                    if (oldTransaction.fromAccountAndType.accountType!!.keepTotals) {
                        transactionViewModel.updateAccountBalance(
                            oldTransaction.fromAccountAndType.account.accountBalance +
                                    oldTransaction.transaction.transAmount,
                            oldTransaction.transaction.transFromAccountId,
                            df.getCurrentTimeAsString()
                        )
                    }
                    if (oldTransaction.fromAccountAndType.accountType!!.tallyOwing) {
                        transactionViewModel.updateAccountOwing(
                            oldTransaction.fromAccountAndType.account.accountOwing -
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