package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType

@Parcelize
data class BudgetRuleComplete(
    @Embedded
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = BUDGET_RULE_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: AccountAndType?,
    @Relation(
        parentColumn = BUDGET_RULE_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: AccountAndType?
) : Parcelable

@Parcelize
data class BudgetRuleDetailed(
    @Embedded
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = BUDGET_RULE_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        parentColumn = BUDGET_RULE_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable