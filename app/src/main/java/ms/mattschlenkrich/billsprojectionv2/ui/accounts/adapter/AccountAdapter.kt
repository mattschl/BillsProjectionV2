package ms.mattschlenkrich.billsprojectionv2.ui.accounts.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_ANALYSIS
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_FROM_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.REQUEST_TO_ACCOUNT
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.databinding.AccountLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.accounts.AccountsFragmentDirections
import java.util.Random


private const val PARENT_TAG = FRAG_ACCOUNTS

class AccountAdapter(
    val mainActivity: MainActivity,
    private val mainViewModel: MainViewModel,
    private val context: Context
) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var mBudgetItem =
        BudgetDetailed(
            mainViewModel.getBudgetItem()?.budgetItem,
            mainViewModel.getBudgetItem()?.budgetRule,
            mainViewModel.getBudgetItem()?.toAccount,
            mainViewModel.getBudgetItem()?.fromAccount
        )

    private var mBudgetRuleDetailed =
        BudgetRuleDetailed(
            mainViewModel.getBudgetRuleDetailed()?.budgetRule,
            mainViewModel.getBudgetRuleDetailed()?.toAccount,
            mainViewModel.getBudgetRuleDetailed()?.fromAccount
        )
    private var mTransactionDetailed =
        TransactionDetailed(
            mainViewModel.getTransactionDetailed()?.transaction,
            mainViewModel.getTransactionDetailed()?.budgetRule,
            mainViewModel.getTransactionDetailed()?.toAccount,
            mainViewModel.getTransactionDetailed()?.fromAccount
        )
    private var mSplitTransactionDetailed =
        TransactionDetailed(
            mainViewModel.getSplitTransactionDetailed()?.transaction,
            mainViewModel.getSplitTransactionDetailed()?.budgetRule,
            mainViewModel.getSplitTransactionDetailed()?.toAccount,
            mainViewModel.getSplitTransactionDetailed()?.fromAccount
        )
    private val accountViewModel = mainActivity.accountViewModel

    private val cf = NumberFunctions()
    private val df = DateFunctions()

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
        val curAccount = differ.currentList[position]

        holder.itemBinding.apply {
            tvAccountName.text =
                curAccount.account.accountName
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
                        cf.displayDollars(curAccount.account.accountCreditLimit)
            }
            if (curAccount.account.accIsDeleted) {
                info += "${if (info.isNotEmpty()) "\n" else ""}       **Deleted** "
            }
            if (info.isBlank()) {
                info = "No info"
            }
            if (info == "No info") {
                tvAccountInfo.visibility = ViewGroup.GONE
            } else {
                tvAccountInfo.visibility = ViewGroup.VISIBLE
                tvAccountInfo.text = info
            }
            tvAccType.text = curAccount.accountType?.accountType

            val random = Random()
            val color = Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
            ibAccountColor.setBackgroundColor(color)

            holder.itemView.setOnClickListener {
                chooseAccount(curAccount, it)
            }
            holder.itemView.setOnLongClickListener {
                chooseOptionsForAccount(curAccount, it)
                false
            }
        }
    }

    private fun chooseOptionsForAccount(
        curAccount: AccountWithType,
        it: View
    ) {
        if (!mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
            AlertDialog.Builder(context)
                .setTitle(
                    "Choose an action for" +
                            curAccount.account.accountName
                )
                .setItems(
                    arrayOf(
                        "Edit this Account",
                        "Delete this Account",
                        "View a summary of Transactions using this Account"
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> gotoUpdateAccount(curAccount, it)
                        1 -> deleteAccount(curAccount)
                        2 -> gotoAverage(curAccount, it)
                    }

                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(
                context,
                "Editing is not allowed right now",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun chooseAccount(
        curAccount: AccountWithType,
        it: View
    ) {
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_ANALYSIS)) {
            gotoTransactionAverage(curAccount, it)
        } else {
            when (mainViewModel.getRequestedAccount()) {
                REQUEST_TO_ACCOUNT -> {
                    mBudgetRuleDetailed.toAccount = curAccount.account
                    mainViewModel.setBudgetRuleDetailed(mBudgetRuleDetailed)
                    if (mainViewModel.getCallingFragments()!!
                            .contains(FRAG_TRANSACTION_SPLIT)
                    ) {
                        mSplitTransactionDetailed.toAccount = curAccount.account
                        mSplitTransactionDetailed.transaction!!.transToAccountPending =
                            curAccount.accountType!!.tallyOwing
                        mainViewModel.setSplitTransactionDetailed(mSplitTransactionDetailed)
                    } else {
                        mTransactionDetailed.toAccount = curAccount.account
                        mTransactionDetailed.transaction!!.transToAccountPending =
                            curAccount.accountType!!.tallyOwing
                        mainViewModel.setTransactionDetailed(mTransactionDetailed)
                    }
                    mBudgetItem.toAccount = curAccount.account
                    mainViewModel.setBudgetItem(mBudgetItem)
                    gotoCallingFragment(it)

                }

                REQUEST_FROM_ACCOUNT -> {
                    mBudgetRuleDetailed.fromAccount = curAccount.account
                    mainViewModel.setBudgetRuleDetailed(mBudgetRuleDetailed)
                    if (mainViewModel.getCallingFragments()!!
                            .contains(FRAG_TRANSACTION_SPLIT)
                    ) {
                        mSplitTransactionDetailed.fromAccount = curAccount.account
                        mSplitTransactionDetailed.transaction!!.transFromAccountPending =
                            curAccount.accountType!!.tallyOwing
                        mainViewModel.setSplitTransactionDetailed(mSplitTransactionDetailed)
                    } else {
                        mTransactionDetailed.fromAccount = curAccount.account
                        mTransactionDetailed.transaction!!.transFromAccountPending =
                            curAccount.accountType!!.tallyOwing
                        mainViewModel.setTransactionDetailed(mTransactionDetailed)
                    }
                    mBudgetItem.fromAccount = curAccount.account
                    mainViewModel.setBudgetItem(mBudgetItem)
                    gotoCallingFragment(it)

                }
            }
        }
    }

    private fun gotoTransactionAverage(curAccount: AccountWithType, it: View) {
        mainViewModel.setCallingFragments(mainViewModel.getCallingFragments() + ", " + PARENT_TAG)
        mainViewModel.setAccountWithType(curAccount)
        mainViewModel.setBudgetRuleDetailed(null)
        it.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionAverageFragment()
        )
    }

    private fun gotoAverage(curAccount: AccountWithType, it: View) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setBudgetRuleDetailed(null)
        mainViewModel.setAccountWithType(curAccount)
        it.findNavController().navigate(
            AccountsFragmentDirections
                .actionAccountsFragmentToTransactionAverageFragment()
        )
    }

    private fun deleteAccount(curAccount: AccountWithType) {
        accountViewModel.deleteAccount(
            curAccount.account.accountId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoUpdateAccount(
        curAccount: AccountWithType,
        it: View
    ) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + PARENT_TAG
        )
        mainViewModel.setAccountWithType(
            curAccount
        )
        val direction = AccountsFragmentDirections
            .actionAccountsFragmentToAccountUpdateFragment()
        it.findNavController().navigate(direction)
    }

    private fun gotoCallingFragment(it: View) {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments()!!
                .replace(", $FRAG_ACCOUNTS", "")
        )
        if (mainViewModel.getCallingFragments()!!.contains(FRAG_TRANSACTION_SPLIT)) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionSplitFragment()
            )
        }
        if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_RULE_ADD)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToBudgetRuleAddFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_RULE_UPDATE)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToBudgetRuleUpdateFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANS_ADD)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionAddFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANS_UPDATE)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionUpdateFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_ADD)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToBudgetItemAddFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_BUDGET_ITEM_UPDATE)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToBudgetItemUpdateFragment()
            )
        } else if (mainViewModel.getCallingFragments()!!
                .contains(FRAG_TRANS_PERFORM)
        ) {
            it.findNavController().navigate(
                AccountsFragmentDirections
                    .actionAccountsFragmentToTransactionPerformFragment()
            )
        }
    }
}