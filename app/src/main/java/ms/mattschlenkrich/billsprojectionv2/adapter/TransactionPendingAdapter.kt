package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.PendingTransactionItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed

class TransactionPendingAdapter(
    private val curAccount: String,
    private val context: Context
) : RecyclerView.Adapter<TransactionPendingAdapter.TransactionPendingHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()

    class TransactionPendingHolder(val itemBinding: PendingTransactionItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem.transaction!!.transId == newItem.transaction!!.transId &&
                        oldItem.transaction.transName == newItem.transaction.transName
            }

            override fun areContentsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionPendingHolder {
        return TransactionPendingHolder(
            PendingTransactionItemBinding.inflate(
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
        holder: TransactionPendingHolder, position: Int
    ) {
        val pendingTransaction = differ.currentList[position]
        holder.itemBinding.tvPendingDate.text =
            df.getDisplayDate(pendingTransaction.transaction!!.transDate)
        holder.itemBinding.tvPendingAmount.text =
            cf.displayDollars(pendingTransaction.transaction.transAmount)
        var display =
            pendingTransaction.transaction.transName
        display += if (pendingTransaction.transaction.transNote.isNotBlank()) {
            " - " + pendingTransaction.transaction.transNote
        } else {
            ""
        }
        holder.itemBinding.tvPendingDescription.text = display
    }
}