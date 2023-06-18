package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.transactions.TransactionViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import java.text.NumberFormat
import java.util.Locale
import java.util.Random

class TransactionAdapter(
//    private val transaction: TransactionDetailed?,
//    private val context: Context,
    private val callingFragment: String,
) : RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder>() {

    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)

    class TransactionsViewHolder(
        val itemBinding: TransactionLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem.transaction!!.transId ==
                        newItem.transaction!!.transId
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
            TransactionLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )

        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: TransactionsViewHolder, position: Int
    ) {
        val transactionDetailed =
            differ.currentList[position]
        holder.itemBinding.tvTransDescription.text =
            transactionDetailed.transaction!!.transName
        holder.itemBinding.tvDate.text =
            transactionDetailed.transaction.transDate
        var info = "To: " +
                transactionDetailed.toAccount!!
                    .accountName
        holder.itemBinding.tvToAccount.text = info
        info = "From: " +
                transactionDetailed.fromAccount!!
                    .accountName
        holder.itemBinding.tvFromAccount.text = info
        info = dollarFormat.format(
            transactionDetailed.transaction.amount
        )
        info += "\nNote: " +
                transactionDetailed.transaction.transNote
        holder.itemBinding.tvTransInfo.text = info
        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibColor.setBackgroundColor(color)

        holder.itemView.setOnLongClickListener {
            val direction = TransactionViewFragmentDirections
                .actionTransactionViewFragmentToTransactionUpdateFragment(
                    transactionDetailed,
                    callingFragment
                )
            it.findNavController().navigate(direction)
            false
        }

    }
}