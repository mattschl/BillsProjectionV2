package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountTypeLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.AccountTypesFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import java.util.*

class AccountTypeAdapter(val account: Account?, val callingFragment: String?) :
    RecyclerView.Adapter<AccountTypeAdapter.AccountTypeViewHolder>() {

    class AccountTypeViewHolder(val itemBinding: AccountTypeLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<AccountType>() {
            override fun areItemsTheSame(oldItem: AccountType, newItem: AccountType): Boolean {
                return oldItem.typeId == newItem.typeId &&
                        oldItem.accountType == newItem.accountType
            }

            override fun areContentsTheSame(oldItem: AccountType, newItem: AccountType): Boolean {
                return oldItem == newItem
            }

        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    )
            : AccountTypeViewHolder {
        return AccountTypeViewHolder(
            AccountTypeLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: AccountTypeViewHolder, position: Int
    ) {
        val currAccountType = differ.currentList[position]

        holder.itemBinding.tvAccountType.text = currAccountType.accountType
        val info = if (currAccountType.keepTotals) {
            "Transactions will be calculated\n"
        } else {
            ""
        } + if (currAccountType.tallyOwing) {
            "Will calculate amount owing\n"
        } else {
            ""
        } + if (currAccountType.isAsset) {
            "This is an asset\n"
        } else {
            ""
        } + if (currAccountType.displayAsAsset) {
            "This will display in the budget\n"
        } else {
            ""
        } + if (currAccountType.isDeleted) {
            "   **DELETED**"
        } else {
            ""
        } + if (!currAccountType.keepTotals &&
            !currAccountType.tallyOwing &&
            !currAccountType.isAsset &&
            currAccountType.displayAsAsset &&
            !currAccountType.isDeleted
        ) {
            "This is a dummy account and will not effect other accounts"
        } else {
            ""
        }
        holder.itemBinding.tvAccountTypeInfo.text = info

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibAccountTypeColor.setBackgroundColor(color)

        holder.itemView.setOnLongClickListener {
            val direction = AccountTypesFragmentDirections
                .actionAccountTypesFragmentToAccountTypeUpdateFragment(
                    currAccountType,
                    account,
                    callingFragment
                )
            it.findNavController().navigate(direction)
            false
        }
        holder.itemView.setOnClickListener {
            if (callingFragment == FRAG_ACCOUNT_UPDATE) {
                val direction = AccountTypesFragmentDirections
                    .actionAccountTypesFragmentToAccountUpdateFragment(
                        account,
                        currAccountType,
                        callingFragment
                    )
                it.findNavController().navigate(direction)
            } else if (callingFragment == FRAG_ACCOUNT_ADD) {
                val direction = AccountTypesFragmentDirections
                    .actionAccountTypesFragmentToAccountAddFragment(
                        account, currAccountType,
                        callingFragment
                    )
                it.findNavController().navigate(direction)
            }
        }

    }

}