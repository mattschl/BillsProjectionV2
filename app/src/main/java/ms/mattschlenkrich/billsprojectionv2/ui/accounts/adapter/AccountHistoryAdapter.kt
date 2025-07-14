package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.TransactionLinearItemBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountUpdateFragment
import java.util.Random

class AccountHistoryAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentFragment: String,
    private val accountUpdateFragment: AccountUpdateFragment,
) : RecyclerView.Adapter<AccountHistoryAdapter.HistoryHolder>() {

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    class HistoryHolder(
        val itemBinding: TransactionLinearItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<TransactionDetailed>() {
        override fun areItemsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem.transaction?.transId == newItem.transaction?.transId && oldItem.budgetRule?.ruleId == newItem.budgetRule?.ruleId && oldItem.toAccount?.accountId == newItem.toAccount?.accountId && oldItem.fromAccount?.accountId == newItem.fromAccount?.accountId
        }

        override fun areContentsTheSame(
            oldItem: TransactionDetailed, newItem: TransactionDetailed
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HistoryHolder {
        return HistoryHolder(
            TransactionLinearItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val mTransactionDetailed = differ.currentList[position]
        holder.itemBinding.apply {
            tvDate.text = df.getDisplayDate(mTransactionDetailed.transaction!!.transDate)
            tvTransDescription.text = mTransactionDetailed.transaction.transName
            tvTransAmount.text = nf.displayDollars(mTransactionDetailed.transaction.transAmount)
            var info =
                mView.context.getString(R.string.to_) + mTransactionDetailed.toAccount!!.accountName
            if (mTransactionDetailed.transaction.transToAccountPending) {
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
            info =
                mView.context.getString(R.string.from_) + mTransactionDetailed.fromAccount!!.accountName
            if (mTransactionDetailed.transaction.transFromAccountPending) {
                info += mView.context.getString(R.string._pending)
                tvFromAccount.setTextColor(
                    Color.RED
                )
            } else {
                tvFromAccount.setTextColor(
                    Color.BLACK
                )
            }
            if (mTransactionDetailed.transaction.transToAccountPending && mTransactionDetailed.transaction.transFromAccountPending) {
                tvToAccount.setLines(2)
                tvFromAccount.setLines(2)

            }
            tvFromAccount.text = info
            if (mTransactionDetailed.transaction.transNote.isEmpty()) {
                tvTransInfo.visibility = View.GONE
            } else {
                info =
                    mView.context.getString(R.string.note_) + mTransactionDetailed.transaction.transNote
                tvTransInfo.text = info
                tvTransInfo.visibility = View.VISIBLE
            }
            val random = Random()
            val color = Color.argb(
                255, random.nextInt(256), random.nextInt(256), random.nextInt(256)
            )
            ibColor.setBackgroundColor(color)
            holder.itemView.setOnClickListener {
                chooseOptions(mTransactionDetailed)
            }
        }
    }

    private fun chooseOptions(mTransactionDetailed: TransactionDetailed) {

    }

}