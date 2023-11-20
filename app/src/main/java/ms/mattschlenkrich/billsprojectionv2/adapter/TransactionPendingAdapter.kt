package ms.mattschlenkrich.billsprojectionv2.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.common.ADAPTER_PENDING
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.common.WAIT_250
import ms.mattschlenkrich.billsprojectionv2.databinding.PendingTransactionItemBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetViewFragment
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetView.BudgetViewFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = ADAPTER_PENDING
private const val PARENT_TAG = FRAG_BUDGET_VIEW

class TransactionPendingAdapter(
    private val curAccount: String,
    private val mainViewModel: MainViewModel,
    val mainActivity: MainActivity,
    private val budgetViewFragment: BudgetViewFragment,
    private val context: Context
) : RecyclerView.Adapter<TransactionPendingAdapter.TransactionPendingHolder>() {

    val cf = CommonFunctions()
    val df = DateFunctions()
    val accountViewModel =
        mainActivity.accountViewModel
    private val transactionViewModel =
        mainActivity.transactionViewModel

    class TransactionPendingHolder(val itemBinding: PendingTransactionItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TransactionDetailed>() {
            override fun areItemsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem.transaction!!.transId == newItem.transaction!!.transId &&
                        oldItem.transaction.transName == newItem.transaction.transName
            }

            override fun areContentsTheSame(
                oldItem: TransactionDetailed,
                newItem: TransactionDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionPendingHolder {
        Log.d(TAG, "creating ViewHolder")
        return TransactionPendingHolder(
            PendingTransactionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: TransactionPendingHolder, position: Int
    ) {
        val pendingTransaction = differ.currentList[position]
        holder.itemBinding.tvPendingDate.text =
            df.getDisplayDate(pendingTransaction.transaction!!.transDate)
        holder.itemBinding.tvPendingDate.setTextColor(Color.BLACK)
        holder.itemBinding.tvPendingAmount.text =
            cf.displayDollars(pendingTransaction.transaction.transAmount)
        if (pendingTransaction.toAccount!!.accountName ==
            curAccount
        ) {
            holder.itemBinding.tvPendingAmount.setTextColor(Color.BLACK)
            holder.itemBinding.tvPendingDescription.setTextColor(Color.BLACK)
        } else {
            holder.itemBinding.tvPendingAmount.setTextColor(Color.RED)
            holder.itemBinding.tvPendingDescription.setTextColor(Color.RED)
        }
        var display =
            pendingTransaction.transaction.transName
        display += if (pendingTransaction.transaction.transNote.isNotBlank()) {
            " - " + pendingTransaction.transaction.transNote
        } else {
            ""
        }
        holder.itemBinding.tvPendingDescription.text = display
        holder.itemView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(
                    "Choose an Actions for" +
                            pendingTransaction.transaction.transName
                )
                .setItems(
                    arrayOf(
                        "Complete this pending transaction",
                        "Open the transaction to edit it"
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> {
                            completeTransaction(
                                pendingTransaction,
                                curAccount,
                            )
                        }

                        1 -> {
                            editTransaction(
                                pendingTransaction,
                                it
                            )
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun editTransaction(
        transaction: TransactionDetailed,
        it: View,
    ) {
        mainViewModel.setCallingFragments(
            PARENT_TAG
        )
        mainViewModel.setTransactionDetailed(null)
        CoroutineScope(Dispatchers.IO).launch {
            val transactionFull = async {
                transactionViewModel.getTransactionFull(
                    transaction.transaction!!.transId,
                    transaction.transaction.transToAccountId,
                    transaction.transaction.transFromAccountId
                )
            }
            mainViewModel.setOldTransaction(transactionFull.await())

        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            it.findNavController().navigate(
                BudgetViewFragmentDirections
                    .actionBudgetViewFragmentToTransactionUpdateFragment()
            )
        }
    }

    private fun completeTransaction(
        transaction: TransactionDetailed?,
        curAccount: String,
    ) {
        if (transaction!!.toAccount!!.accountName ==
            curAccount
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                transactionViewModel.updateTransaction(
                    Transactions(
                        transaction.transaction!!.transId,
                        transaction.transaction.transDate,
                        transaction.transaction.transName,
                        transaction.transaction.transNote,
                        transaction.transaction.transRuleId,
                        transaction.transaction.transToAccountId,
                        false,
                        transaction.transaction.transFromAccountId,
                        transaction.transaction.transFromAccountPending,
                        transaction.transaction.transAmount,
                        transaction.transaction.transIsDeleted,
                        df.getCurrentTimeAsString()
                    )
                )
                val account =
                    async {
                        accountViewModel.getAccountWithType(
                            curAccount
                        )
                    }
                val accountDetailed =
                    account.await()
                if (accountDetailed.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        accountDetailed.account.accountBalance +
                                transaction.transaction.transAmount,
                        accountDetailed.account.accountId,
                        df.getCurrentTimeAsString()
                    )
                } else if (accountDetailed.accountType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        accountDetailed.account.accountOwing -
                                transaction.transaction.transAmount,
                        accountDetailed.account.accountId,
                        df.getCurrentTimeAsString()

                    )
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                transactionViewModel.updateTransaction(
                    Transactions(
                        transaction.transaction!!.transId,
                        transaction.transaction.transDate,
                        transaction.transaction.transName,
                        transaction.transaction.transNote,
                        transaction.transaction.transRuleId,
                        transaction.transaction.transToAccountId,
                        transaction.transaction.transToAccountPending,
                        transaction.transaction.transFromAccountId,
                        false,
                        transaction.transaction.transAmount,
                        transaction.transaction.transIsDeleted,
                        df.getCurrentTimeAsString()
                    )
                )
                val account =
                    async {
                        accountViewModel.getAccountWithType(
                            curAccount
                        )
                    }
                val accountDetailed =
                    account.await()
                if (accountDetailed.accountType!!.keepTotals) {
                    transactionViewModel.updateAccountBalance(
                        accountDetailed.account.accountBalance -
                                transaction.transaction.transAmount,
                        accountDetailed.account.accountId,
                        df.getCurrentTimeAsString()
                    )
                } else if (accountDetailed.accountType.tallyOwing) {
                    transactionViewModel.updateAccountOwing(
                        accountDetailed.account.accountOwing +
                                transaction.transaction.transAmount,
                        accountDetailed.account.accountId,
                        df.getCurrentTimeAsString()

                    )
                }
            }
        }
        budgetViewFragment.fillBudgetTotals()
        budgetViewFragment.setupPendingList()
    }

}