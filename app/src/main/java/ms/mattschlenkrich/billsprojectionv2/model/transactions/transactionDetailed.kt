package ms.mattschlenkrich.billsprojectionv2.model.transactions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule


@Parcelize
data class TransactionDetailed(
    @Embedded
    val transaction: Transactions?,
    @Relation(
        parentColumn = TRANS_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = TRANSACTION_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        parentColumn = TRANSACTION_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable

@Parcelize
data class TransactionFull(
    @Embedded
    val transaction: Transactions,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = TRANS_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule,
    @Relation(
        entity = AccountAndType::class,
        parentColumn = TRANSACTION_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountAndType: AccountAndType,
    @Relation(
        entity = AccountAndType::class,
        parentColumn = TRANSACTION_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountAndType: AccountAndType
) : Parcelable
