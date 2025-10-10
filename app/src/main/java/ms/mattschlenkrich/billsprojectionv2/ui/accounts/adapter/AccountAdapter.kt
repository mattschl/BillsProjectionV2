package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountsFragment


//private const val PARENT_TAG = FRAG_ACCOUNTS

class AccountAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
    private val accountsFragment: AccountsFragment,
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private val mainViewModel = mainActivity.mainViewModel
    private val vf = VisualsFunctions()

    private val accountViewModel = mainActivity.accountViewModel

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class AccountViewHolder(val itemBinding: AccountLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<AccountWithType>() {
        override fun areItemsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem.account.accountId == newItem.account.accountId && oldItem.account.accountName == newItem.account.accountName && oldItem.account.accountNumber == newItem.account.accountNumber
        }

        override fun areContentsTheSame(
            oldItem: AccountWithType, newItem: AccountWithType
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AccountViewHolder {
        return AccountViewHolder(
            AccountLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: AccountViewHolder, position: Int
    ) {
        val curAccount = differ.currentList[position]

        holder.itemBinding.apply {
            tvAccountName.text = curAccount.account.accountName
            var info = if (curAccount.account.accountNumber.isNotEmpty()) {
                "# ${curAccount.account.accountNumber} "
            } else {
                ""
            }
            if (curAccount.account.accountBalance != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.balance) + cf.displayDollars(
                    curAccount.account.accountBalance
                )
            }
            if (curAccount.account.accountOwing != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.owing) + cf.displayDollars(
                    curAccount.account.accountOwing
                )
            }
            if (curAccount.account.accBudgetedAmount != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.budgeted) + cf.displayDollars(
                    curAccount.account.accBudgetedAmount
                )
            }
            if (curAccount.account.accountCreditLimit != 0.0) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.credit_limit) + cf.displayDollars(
                    curAccount.account.accountCreditLimit
                )
            }
            if (curAccount.account.accIsDeleted) {
                info += (if (info.isNotEmpty()) "\n" else "") + mView.context.getString(R.string.deleted)
                holder.itemView.setBackgroundColor(mView.context.getColor(R.color.deep_red))
            } else {
                holder.itemView.setBackgroundColor(mView.context.getColor(R.color.white))
            }
            if (info.isBlank()) {
                info = mView.context.getString(R.string.no_info)
            }
            if (info == mView.context.getString(R.string.no_info)) {
                tvAccountInfo.visibility = ViewGroup.GONE
            } else {
                tvAccountInfo.visibility = ViewGroup.VISIBLE
                tvAccountInfo.text = info
            }
            tvAccType.text = curAccount.accountType?.accountType
            ibAccountColor.setBackgroundColor(vf.getRandomColorInt())
            holder.itemView.setOnClickListener { chooseOptionsForAccount(curAccount) }
        }
    }

    private fun chooseOptionsForAccount(
        curAccount: AccountWithType
    ) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_an_action_for) + curAccount.account.accountName
        ).setItems(
            arrayOf(
                mView.context.getString(R.string.edit_this_account),
                mView.context.getString(R.string.delete_this_account),
                mView.context.getString(R.string.view_a_summary_of_transactions_using_this_account)
            )
        ) { _, pos ->
            when (pos) {
                0 -> gotoUpdateAccount(curAccount)
                1 -> deleteAccount(curAccount)
                2 -> gotoTransactionAverage(curAccount)
            }

        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun gotoUpdateAccount(
        curAccount: AccountWithType,
    ) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setAccountWithType(curAccount)
        accountsFragment.gotoAccountUpdateFragment()
    }

    private fun deleteAccount(curAccount: AccountWithType) {
        accountViewModel.deleteAccount(
            curAccount.account.accountId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoTransactionAverage(curAccount: AccountWithType) {
        mainViewModel.addCallingFragment(parentTag)
        mainViewModel.setAccountWithType(curAccount)
        mainViewModel.setBudgetRuleDetailed(null)
        accountsFragment.gotoTransactionAverageFragment()
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }
}