package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import java.util.*

class AccountAdapter : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(val itemBinding: AccountLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)


    private val differCallBack = object : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.accountId == newItem.accountId &&
                    oldItem.accountName == newItem.accountName &&
                    oldItem.accountNumber == newItem.accountNumber
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountAdapter.AccountViewHolder {
        return AccountAdapter.AccountViewHolder(
            AccountLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AccountAdapter.AccountViewHolder, position: Int) {
        val currentAccount = differ.currentList[position]

        holder.itemBinding.tvAccountName.text = currentAccount.accountName
        val info = if (currentAccount.accountNumber.isNotEmpty()) {
            "Account Number is ${currentAccount.accountNumber} /n"
        } else {
            ""
        } + if (currentAccount.accountBalance != 0.0) {
            "Balance is ${currentAccount.accountBalance} /n"
        } else {
            ""
        } + if (currentAccount.accountOwing != 0.0) {
            "Balance Owing is ${currentAccount.accountOwing}"
        } else {
            ""
        } + if (currentAccount.accountNumber.isEmpty() &&
            currentAccount.accountBalance == 0.0 &&
            currentAccount.accountOwing == 0.0
        )
            "No account info" else ""
        holder.itemBinding.tvAccountInfo.text = info

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibAccountColor.setBackgroundColor(color)


    }
}