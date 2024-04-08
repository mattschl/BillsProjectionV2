package ms.mattschlenkrich.billsprojectionv2.model.budgetRule

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountAndType

@Parcelize
data class BudgetRuleComplete(
    @Embedded
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = BUD_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: AccountAndType?,
    @Relation(
        parentColumn = BUD_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: AccountAndType?
) : Parcelable

@Parcelize
data class BudgetRuleDetailed(
    @Embedded
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = BUD_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        parentColumn = BUD_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable

