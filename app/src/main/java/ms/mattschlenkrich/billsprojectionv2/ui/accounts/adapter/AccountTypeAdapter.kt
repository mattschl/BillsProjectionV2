package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountTypeLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountTypesFragment
import java.util.Random


private const val PARENT_TAG = FRAG_ACCOUNT_TYPES

class AccountTypeAdapter(
    private val mainViewModel: MainViewModel,
    private val accountTypesFragment: AccountTypesFragment,
    private val mView: View,
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
        val curAccountType = differ.currentList[position]
        holder.itemBinding.apply {
            tvAccountType.text = curAccountType.accountType
            var info = if (curAccountType.keepTotals)
                mView.context.getString(R.string.balance_will_be_updated) else ""
            if (curAccountType.tallyOwing) {
                info += (if (info.isNotEmpty()) "\n" else "") +
                        mView.context.getString(R.string.will_calculate_amount_owing)
            }
            if (curAccountType.isAsset) {
                info += (if (info.isNotEmpty()) "\n" else "") +
                        mView.context.getString(R.string.this_is_an_asset)
            }
            if (curAccountType.displayAsAsset) {
                info += (if (info.isNotEmpty()) "\n" else "") +
                        mView.context.getString(R.string.this_will_be_used_for_the_budget)
            }
            if (curAccountType.allowPending) {
                info += (if (info.isNotEmpty()) "\n" else "") +
                        mView.context.getString(R.string.allow_transactions_pending)
            }
            if (curAccountType.acctIsDeleted) {
                info += "${if (info.isNotEmpty()) "\n" else ""} " +
                        mView.context.getString(R.string.deleted)
            }
            if (info.isBlank()) {
                info =
                    mView.context.getString(R.string.this_account_does_not_keep_a_balance_owing_amount)
            }
            tvAccountTypeInfo.text = info

            val random = Random()
            val color = Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
            ibAccountTypeColor.setBackgroundColor(color)

            holder.itemView.setOnLongClickListener {
                gotoAccountTypeUpdate(curAccountType)
                false
            }
            holder.itemView.setOnClickListener {
                chooseAccountType(curAccountType)
            }
        }
    }

    private fun gotoAccountTypeUpdate(
        curAccountType: AccountType?,
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setAccountType(
            curAccountType
        )
        accountTypesFragment.gotoAccountUpdateFragment()
    }

    private fun chooseAccountType(
        curAccountType: AccountType?,
    ) {
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
            accountTypesFragment.gotoAccountUpdateFragment()
        } else if (mainViewModel.getCallingFragments()!!.contains(FRAG_ACCOUNT_ADD)) {
            accountTypesFragment.gotoAccountAddFragment()
        }
    }
}