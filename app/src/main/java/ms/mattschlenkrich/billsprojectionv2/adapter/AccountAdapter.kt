package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.accounts.AccountsFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import java.text.NumberFormat
import java.util.Locale
import java.util.Random


class AccountAdapter(val callingFragment: String?) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    //    private var dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
//    private var timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    class AccountViewHolder(val itemBinding: AccountLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<AccountWithType>() {
            override fun areItemsTheSame(
                oldItem: AccountWithType,
                newItem: AccountWithType
            ): Boolean {
                return oldItem.account.accountId == newItem.account.accountId &&
                        oldItem.account.accountName == newItem.account.accountName &&
                        oldItem.account.accountNumber == newItem.account.accountNumber
            }

            override fun areContentsTheSame(
                oldItem: AccountWithType,
                newItem: AccountWithType
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountViewHolder {
        return AccountViewHolder(
            AccountLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: AccountViewHolder, position: Int
    ) {

        val currentAccount = differ.currentList[position]

        holder.itemBinding.tvAccountName.text = currentAccount.account.accountName
        var info = if (currentAccount.account.accountNumber.isNotEmpty()) {
            "# ${currentAccount.account.accountNumber}"
        } else {
            ""
        }
        if (currentAccount.account.accountBalance != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Balance " +
                    dollarFormat.format(currentAccount.account.accountBalance)
        }
        if (currentAccount.account.accountOwing != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Owing " +
                    dollarFormat.format(currentAccount.account.accountOwing)
        }
        if (currentAccount.account.budgetAmount != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Budgeted " +
                    dollarFormat.format(currentAccount.account.budgetAmount)
        }
        if (currentAccount.account.isDeleted) {
            info += "${if (info.isNotEmpty()) "\n" else ""}       **Deleted** "
        }
        if (info.isBlank()) {
            info = "No info"
        }
        if (info == "No info") {
            holder.itemBinding.tvAccountInfo.visibility = ViewGroup.GONE
        } else {
            holder.itemBinding.tvAccountInfo.visibility = ViewGroup.VISIBLE
            holder.itemBinding.tvAccountInfo.text = info
        }
        holder.itemBinding.tvAccType.text = currentAccount.accountType.accountType

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibAccountColor.setBackgroundColor(color)

        holder.itemView.setOnLongClickListener {
            val direction = AccountsFragmentDirections
                .actionAccountsFragmentToAccountUpdateFragment(
                    currentAccount.account,
                    currentAccount.accountType,
                    callingFragment
                )
            it.findNavController().navigate(direction)
            false
        }
    }
}