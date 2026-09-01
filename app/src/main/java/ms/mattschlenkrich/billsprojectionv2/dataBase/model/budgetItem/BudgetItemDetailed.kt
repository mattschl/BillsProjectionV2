package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule


@Parcelize
data class BudgetItemDetailed(
    @Embedded
    var budgetItem: BudgetItem?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BUDGET_ITEM_RULE_ID,
        entityColumn = BUDGET_RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = Account::class,
        parentColumn = BUDGET_ITEM_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        entity = Account::class,
        parentColumn = BUDGET_ITEM_FROM_ACCOUNT_ID,
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
        parentColumn = BUDGET_ITEM_RULE_ID,
        entityColumn = BUDGET_RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BUDGET_ITEM_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountAndType: AccountAndType?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BUDGET_ITEM_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountAndType: AccountAndType?
) : Parcelable