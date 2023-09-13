package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.transactions.TransactionViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import java.util.Random

private const val TAG = "TransactionAdapter"

class TransactionAdapter(
    private val asset: String?,
    private val payDay: String?,
//    private val transaction: TransactionDetailed?,
//    private val context: Context,
    private val callingFragment: String,
) : RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder>() {

    private val cf = CommonFunctions()
    private val df = DateFunctions()

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
        holder.itemBinding.tvTransDescription.text =
            transaction.transaction!!.transName
        holder.itemBinding.tvDate.text =
            df.getDisplayDate(transaction.transaction.transDate)
        var info = "To: " +
                transaction.toAccount!!
                    .accountName
        holder.itemBinding.tvTransAmount.text =
            cf.displayDollars(transaction.transaction.transAmount)
        holder.itemBinding.tvToAccount.text = info
        info = "From: " +
                transaction.fromAccount!!
                    .accountName
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

        holder.itemView.setOnLongClickListener {
            val direction = TransactionViewFragmentDirections
                .actionTransactionViewFragmentToTransactionUpdateFragment(
                    asset,
                    payDay,
                    transaction,
                    callingFragment
                )
            it.findNavController().navigate(direction)
            false
        }
    }
}