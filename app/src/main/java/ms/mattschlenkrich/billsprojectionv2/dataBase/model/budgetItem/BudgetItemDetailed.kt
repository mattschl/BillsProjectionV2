package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule


@Parcelize
data class BudgetItemDetailed(
    @Embedded
    val budgetItem: BudgetItem?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BI_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = Account::class,
        parentColumn = BI_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        entity = Account::class,
        parentColumn = BI_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable

@Parcelize
data class BudgetFullView(
    @Embedded
    val budgetItem: BudgetItem?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BI_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BI_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountAndType: AccountAndType?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BI_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountAndType: AccountAndType?
) : Parcelable
