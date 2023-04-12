package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.AccountsFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import java.text.NumberFormat
import java.util.*


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
        val info = if (currentAccount.account.accountNumber.isNotEmpty()) {
            "# ${currentAccount.account.accountNumber}\n"
        } else {
            ""
        } + if (currentAccount.account.accountBalance != 0.0) {
            "Balance " +
                    "${dollarFormat.format(currentAccount.account.accountBalance)}\n"
        } else {
            ""
        } + if (currentAccount.account.accountOwing != 0.0) {
            "Owing " +
                    "${dollarFormat.format(currentAccount.account.accountOwing)}\n"
        } else {
            ""
        } + if (currentAccount.account.budgetAmount != 0.0) {
            "Budgeted " +
                    "${dollarFormat.format(currentAccount.account.budgetAmount)}\n"
        } else {
            ""
        } + if (currentAccount.account.isDeleted) {
            "**Deleted**"
        } else {
            ""
        } + if (currentAccount.account.accountNumber.isEmpty() &&
            currentAccount.account.accountBalance == 0.0 &&
            currentAccount.account.accountOwing == 0.0 &&
            currentAccount.account.budgetAmount == 0.0 &&
            !currentAccount.account.isDeleted
        ) {
            "No info"
        } else {
            ""
        }
        if (info == "No info") {
            holder.itemBinding.tvAccountInfo.visibility = ViewGroup.GONE
        } else {
            holder.itemBinding.tvAccountInfo.visibility = ViewGroup.VISIBLE
            holder.itemBinding.tvAccountInfo.text = info
        }

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