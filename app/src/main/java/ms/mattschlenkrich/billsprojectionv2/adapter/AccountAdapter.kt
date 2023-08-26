package ms.mattschlenkrich.billsprojectionv2.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.accounts.AccountsFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import java.util.Random

private const val TAG = ADAPTER_ACCOUNT

class AccountAdapter(
    private val budgetItem: BudgetDetailed?,
    private val transaction: TransactionDetailed?,
    private val budgetRuleDetailed: BudgetRuleDetailed?,
    private val requestedAccount: String?,
    private val callingFragments: String?,
) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var mBudgetRuleDetailed = budgetRuleDetailed
    private var mTransactionDetailed =
        transaction
            ?: TransactionDetailed(
                null,
                null,
                null,
                null
            )

    private val cf = CommonFunctions()

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

        Log.d(TAG, "$TAG is entered")
        val curAccount = differ.currentList[position]

        holder.itemBinding.tvAccountName.text = curAccount.account.accountName
        var info = if (curAccount.account.accountNumber.isNotEmpty()) {
            "# ${curAccount.account.accountNumber}"
        } else {
            ""
        }
        if (curAccount.account.accountBalance != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Balance " +
                    cf.displayDollars(curAccount.account.accountBalance)
        }
        if (curAccount.account.accountOwing != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Owing " +
                    cf.displayDollars(curAccount.account.accountOwing)
        }
        if (curAccount.account.accBudgetedAmount != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Budgeted " +
                    cf.displayDollars(curAccount.account.accBudgetedAmount)
        }
        if (curAccount.account.accountCreditLimit != 0.0) {
            info += "${if (info.isNotEmpty()) "\n" else ""}Credit Limit " +
                    cf.displayDollars(curAccount.account.accBudgetedAmount)
        }
        if (curAccount.account.accIsDeleted) {
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
        holder.itemBinding.tvAccType.text = curAccount.accountType.accountType

        val random = Random()
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
        holder.itemBinding.ibAccountColor.setBackgroundColor(color)

        holder.itemView.setOnClickListener {
            Log.d(
                TAG, "onClick requested Account is $requestedAccount," +
                        "  calling Fragment is $callingFragments"
            )
            when (requestedAccount) {
                REQUEST_TO_ACCOUNT -> {
                    if (callingFragments!!.contains(FRAG_BUDGET_RULE_ADD) ||
                        callingFragments.contains(FRAG_BUDGET_RULE_UPDATE)
                    ) {
                        Log.d(TAG, "going to budget fragment: $callingFragments")
                        mBudgetRuleDetailed!!.toAccount = curAccount.account
                        gotoCallingFragment(it)
                    } else if (callingFragments.contains(FRAG_TRANS_ADD) ||
                        callingFragments.contains(FRAG_TRANS_UPDATE)
                    ) {
                        mTransactionDetailed.toAccount = curAccount.account
                        gotoCallingFragment(it)
                    }
                }

                REQUEST_FROM_ACCOUNT -> {
                    if (callingFragments!!.contains(FRAG_BUDGET_RULE_ADD) ||
                        callingFragments.contains(FRAG_BUDGET_RULE_UPDATE)
                    ) {
                        mBudgetRuleDetailed!!.fromAccount = curAccount.account
                        gotoCallingFragment(it)
                    } else if (callingFragments.contains(FRAG_TRANS_ADD) ||
                        callingFragments.contains(FRAG_TRANS_UPDATE)
                    ) {
                        mTransactionDetailed.fromAccount = curAccount.account
                        gotoCallingFragment(it)
                    }
                }
            }
        }

        holder.itemView.setOnLongClickListener {

            gotoUpdateUpdateAccount(curAccount, it)
            false
        }
    }

    private fun gotoUpdateUpdateAccount(
        curAccount: AccountWithType,
        it: View
    ) {
        val fragmentChain = "$callingFragments, $FRAG_ACCOUNTS"
        val direction = AccountsFragmentDirections
            .actionAccountsFragmentToAccountUpdateFragment(
                budgetItem,
                mTransactionDetailed,
                mBudgetRuleDetailed,
                curAccount.account,
                curAccount.accountType,
                requestedAccount,
                fragmentChain
            )
        it.findNavController().navigate(direction)
    }

    private fun gotoCallingFragment(it: View) {
        val fragmentChain = callingFragments!!
            .replace(", $FRAG_ACCOUNTS", "")
        Log.d(
            TAG, "Fragment Chain is \n" +
                    fragmentChain
        )
        if (callingFragments.contains(FRAG_BUDGET_RULE_ADD)) {
            val direction = AccountsFragmentDirections
                .actionAccountsFragmentToBudgetRuleAddFragment(
                    budgetItem,
                    mTransactionDetailed,
                    mBudgetRuleDetailed,
                    fragmentChain
                )
//            Log.d(
//                TAG, "in gotoCallingFragment mTransactionDetailed is " +
//                        mTransactionDetailed.transaction!!.transName
//            )
            it.findNavController().navigate(direction)
        } else if (callingFragments.contains(FRAG_BUDGET_RULE_UPDATE)
        ) {
            val direction = AccountsFragmentDirections
                .actionAccountsFragmentToBudgetRuleUpdateFragment(
                    budgetItem,
                    mTransactionDetailed,
                    mBudgetRuleDetailed,
                    fragmentChain
                )
            it.findNavController().navigate(direction)
        } else if (callingFragments.contains(FRAG_TRANS_ADD)
        ) {
            val direction =
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionAddFragment(
                        mTransactionDetailed,
                        fragmentChain
                    )
            it.findNavController().navigate(direction)
        } else if (callingFragments.contains(FRAG_TRANS_UPDATE)
        ) {
            val direction =
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionUpdateFragment(
                        mTransactionDetailed,
                        fragmentChain
                    )
            it.findNavController().navigate(direction)
        }
    }
}