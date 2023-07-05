package ms.mattschlenkrich.billsprojectionv2.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.billsprojectionv2.ADAPTER_BUDGET_RULE
import ms.mattschlenkrich.billsprojectionv2.FRAG_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.BudgetRuleLayoutBinding
import ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules.BudgetRuleFragmentDirections
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.model.TransactionDetailed
import java.text.NumberFormat
import java.util.Locale

private const val TAG = ADAPTER_BUDGET_RULE

class BudgetRuleAdapter(
    private val transaction: TransactionDetailed?,
    private val context: Context,
    private val callingFragments: String,
) : RecyclerView.Adapter<BudgetRuleAdapter.BudgetRuleViewHolder>() {

    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)

    class BudgetRuleViewHolder(
        val itemBinding: BudgetRuleLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<BudgetRuleDetailed>() {
            override fun areItemsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem.budgetRule!!.ruleId ==
                        newItem.budgetRule!!.ruleId
            }

            override fun areContentsTheSame(
                oldItem: BudgetRuleDetailed,
                newItem: BudgetRuleDetailed
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BudgetRuleViewHolder {
        return BudgetRuleViewHolder(
            BudgetRuleLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: BudgetRuleViewHolder, position: Int
    ) {
        val budgetRuleDetailed =
            differ.currentList[position]
        holder.itemBinding.tvBudgetRule.text =
            budgetRuleDetailed.budgetRule!!.budgetRuleName
        var info = "To: " + budgetRuleDetailed.toAccount!!.accountName
        holder.itemBinding.tvToAccount.text = info
        info = "\nFrom: " + budgetRuleDetailed.fromAccount!!.accountName
        holder.itemBinding.tvFromAccount.text = info
        val amount =
            dollarFormat.format(budgetRuleDetailed.budgetRule.budgetAmount)
        Log.d(TAG, "Amount is ${budgetRuleDetailed.budgetRule.budgetAmount}")
        val frequencyTypes =
            context.resources.getStringArray(R.array.frequency_types)
        val frequencyType =
            frequencyTypes[budgetRuleDetailed.budgetRule.budFrequencyTypeId]
        val daysOfWeek =
            context.resources.getStringArray(R.array.days_of_week)
        val dayOfWeek =
            daysOfWeek[budgetRuleDetailed.budgetRule.budDayOfWeekId]
        info = "$amount " + frequencyType +
                " X " + budgetRuleDetailed.budgetRule.budFrequencyCount +
                "\nOn " + dayOfWeek
        holder.itemBinding.tvInfo.text = info

        holder.itemView.setOnClickListener {
            if (transaction != null) {
                Log.d(
                    TAG,
                    "transaction name is" +
                            " ${transaction.transaction!!.transName}"
                )
                Log.d(TAG, "callingFragments is $callingFragments")
                if (callingFragments.contains(FRAG_TRANS_ADD)) {
                    val fragmentChain = "$callingFragments, $FRAG_BUDGET_RULES"
                    val mTransaction = transaction
                    mTransaction.budgetRule =
                        budgetRuleDetailed.budgetRule
                    mTransaction.toAccount =
                        budgetRuleDetailed.toAccount
                    mTransaction.fromAccount =
                        budgetRuleDetailed.fromAccount
                    val direction =
                        BudgetRuleFragmentDirections
                            .actionBudgetRuleFragmentToTransactionAddFragment(
                                mTransaction,
                                fragmentChain
                            )
                    it.findNavController().navigate(direction)
                } else if (callingFragments.contains(FRAG_TRANS_UPDATE)) {
                    val fragmentChain = "$callingFragments, $FRAG_BUDGET_RULES"
                    val mTransaction = transaction
                    mTransaction.budgetRule =
                        budgetRuleDetailed.budgetRule
                    mTransaction.toAccount =
                        budgetRuleDetailed.toAccount
                    mTransaction.fromAccount =
                        budgetRuleDetailed.fromAccount
                    val direction =
                        BudgetRuleFragmentDirections
                            .actionBudgetRuleFragmentToTransactionUpdateFragment(
                                mTransaction,
                                fragmentChain
                            )
                    it.findNavController().navigate(direction)
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            val direction = BudgetRuleFragmentDirections
                .actionBudgetRuleFragmentToBudgetRuleUpdateFragment(
                    transaction,
                    budgetRuleDetailed,
                    callingFragments
                )
            it.findNavController().navigate(direction)
            false
        }

    }


}