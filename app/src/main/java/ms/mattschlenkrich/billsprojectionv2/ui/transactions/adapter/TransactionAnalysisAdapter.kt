package ms.mattschlenkrich.billsprojectionv2.ui.transactions.adapter

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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.transactions.TransactionAnalysisFragmentDirections
import java.util.Random

private const val TAG = ADAPTER_TRANSACTION_ANALYSIS
private const val PARENT_TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisAdapter(
    val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<TransactionAnalysisAdapter.TransViewHolder>() {

    private val cf = NumberFunctions()
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
        }
        holder.itemView.setOnClickListener {
            chooseOptions(transaction)
        }
    }

    private fun chooseOptions(transaction: TransactionDetailed) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                "Choose action for " +
                        transaction.transaction!!.transName
            )
            .setItems(
                arrayOf(
                    "Edit this transaction",
                    "Delete this transaction"
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        gotoTransactionUpdate(transaction)
                    }

                    1 -> {
                        AlertDialog.Builder(mView.context)
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

    private fun deleteTransaction(transaction: Transactions) {
        mainActivity.accountUpdateViewModel.deleteTransaction(
            transaction
        )
    }

    private fun gotoTransactionUpdate(transaction: TransactionDetailed) {
        mainActivity.mainViewModel.setCallingFragments(
            mainActivity.mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainActivity.mainViewModel.setTransactionDetailed(transaction)
        CoroutineScope(Dispatchers.IO).launch {
            val oldTransactionFull = async {
                mainActivity.transactionViewModel.getTransactionFull(
                    transaction.transaction!!.transId,
                    transaction.transaction.transToAccountId,
                    transaction.transaction.transFromAccountId
                )
            }
            mainActivity.mainViewModel.setOldTransaction(oldTransactionFull.await())
        }
        mView.findNavController().navigate(
            TransactionAnalysisFragmentDirections
                .actionTransactionAnalysisFragmentToTransactionUpdateFragment()
        )
    }
}