package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_TRANSACTION_ANALYSIS
private const val PARENT_TAG = FRAG_TRANSACTION_ANALYSIS

class TransactionAnalysisAdapter(
    val mainActivity: MainActivity,
    private val mainViewModel: MainViewModel,
    private val context: Context,
) : RecyclerView.Adapter<TransactionAnalysisAdapter.TransViewHolder>() {

    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private val transactionViewModel =
        mainActivity.transactionViewModel

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
    ): TransactionAnalysisAdapter.TransViewHolder {
        return TransactionAnalysisAdapter.TransViewHolder(
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
        holder: TransactionAnalysisAdapter.TransViewHolder,
        position: Int,
    ) {
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
        if (transaction.transaction.transToAccountPending &&
            transaction.transaction.transFromAccountPending
        ) {
            holder.itemBinding.tvToAccount.setLines(2)
            holder.itemBinding.tvFromAccount.setLines(2)

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
    }
}