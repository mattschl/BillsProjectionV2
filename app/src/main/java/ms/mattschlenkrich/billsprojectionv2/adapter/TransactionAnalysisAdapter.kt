package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.transactions.TransactionDetailed
import java.util.Random

private const val TAG = ADAPTER_TRANSACTION_ANALYSIS
//private const val PARENT_TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisAdapter(
//    val mainActivity: MainActivity,
//    private val mainViewModel: MainViewModel,
//    private val context: Context,
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
    }
}