package ms.mattschlenkrich.billsprojectionv2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.BV_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.BV_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.BV_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.BV_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.BV_IS_COMPLETED
import ms.mattschlenkrich.billsprojectionv2.BV_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.BV_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.BV_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.BV_PROJECTED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.BV_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.BV_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_VIEW

@Parcelize
@Entity(
    tableName = TABLE_BUDGET_VIEW,
    indices = [
        Index(value = [BV_ACTUAL_DATE]),
        Index(value = [BV_PAY_DAY]),
        Index(value = [BV_IS_PAY_DAY_ITEM]),
        Index(value = [BV_PROJECTED_AMOUNT]),
        Index(value = [BV_TO_ACCOUNT_ID]),
        Index(value = [BV_FROM_ACCOUNT_ID]),
        Index(value = [BV_IS_DELETED]),
        Index(value = [BV_IS_CANCELLED]),
        Index(value = [BV_IS_COMPLETED])
    ],
    primaryKeys = [BV_BUDGET_RULE_ID, BV_PROJECTED_DATE],
    foreignKeys = [ForeignKey(
        entity = BudgetRule::class,
        parentColumns = [RULE_ID],
        childColumns = [BV_BUDGET_RULE_ID]
    ),
        ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BV_TO_ACCOUNT_ID]
        ), ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BV_FROM_ACCOUNT_ID]
        )]
)
data class BudgetView(
    val bvRuleId: Long,
    val bvProjectedDate: String,
    val bvActualDate: String,
    val bvPayDay: String,
    val bvBudgetName: String,
    @ColumnInfo(defaultValue = "0")
    val bvIsPayDayItem: Boolean,
    val bvToAccountId: Long,
    val bvFromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0")
    val bvProjectedAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val bvIsPending: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvIsFixed: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvIsAutomatic: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvManuallyEntered: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvIsCompleted: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvIsCancelled: Boolean,
    @ColumnInfo(defaultValue = "0")
    val bvIsDeleted: Boolean,
    val bvUpdateTime: String
) : Parcelable

@Parcelize
data class BudgetDetailed(
    @Embedded
    val budgetView: BudgetView?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BV_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = Account::class,
        parentColumn = BV_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        entity = Account::class,
        parentColumn = BV_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable

@Parcelize
data class BudgetFullView(
    @Embedded
    val budgetView: BudgetView?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BV_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BV_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountWithType: AccountWithType?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = BV_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountWithType: AccountWithType?
) : Parcelable
