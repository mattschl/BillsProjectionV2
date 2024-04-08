package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_ACCOUNT_TYPE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountTypeLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.accounts.AccountTypesFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel
import java.util.Random

private const val TAG = ADAPTER_ACCOUNT_TYPE
private const val PARENT_TAG = FRAG_ACCOUNT_TYPES

class AccountTypeAdapter(
    private val mainViewModel: MainViewModel
) :
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
        Log.d(TAG, "$TAG is entered")
        val curAccountType = differ.currentList[position]

        holder.itemBinding.tvAccountType.text = curAccountType.accountType
        var info = if (curAccountType.keepTotals)
            "Balance will be updated" else ""
        if (curAccountType.tallyOwing) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Will calculate amount owing"
        }
        if (curAccountType.isAsset) {
            info += "${if (info.isNotEmpty()) "\n" else ""}This is an asset"
        }
        if (curAccountType.displayAsAsset) {
            info += "${if (info.isNotEmpty()) "\n" else ""}This will display in the budget"
        }
        if (curAccountType.allowPending) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Transactions can be delayed"
        }
        if (curAccountType.acctIsDeleted) {
            info += "${if (info.isNotEmpty()) "\n" else ""}      **DELETED**"
        }
        if (info.isBlank()) {
            info = "This account does not keep a balance/owing amount"
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
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments() + ", " + PARENT_TAG
            )
            mainViewModel.setAccountType(
                curAccountType
            )
            it.findNavController().navigate(
                AccountTypesFragmentDirections
                    .actionAccountTypesFragmentToAccountTypeUpdateFragment()
            )
            false
        }
        holder.itemView.setOnClickListener {
            mainViewModel.setAccountType(
                curAccountType
            )
            mainViewModel.setAccountWithType(
                AccountWithType(
                    mainViewModel.getAccountWithType()!!.account,
                    curAccountType
                )
            )
            if (mainViewModel.getCallingFragments()!!.contains(FRAG_ACCOUNT_UPDATE)) {
                it.findNavController().navigate(
                    AccountTypesFragmentDirections
                        .actionAccountTypesFragmentToAccountUpdateFragment()
                )
            } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_ACCOUNT_ADD)) {
                it.findNavController().navigate(
                    AccountTypesFragmentDirections
                        .actionAccountTypesFragmentToAccountAddFragment()
                )
            }
        }
    }
}