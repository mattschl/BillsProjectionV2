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
import ms.mattschlenkrich.billsprojectionv2.ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.PROJECTED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_VIEW
import ms.mattschlenkrich.billsprojectionv2.TO_ACCOUNT_ID

@Parcelize
@Entity(
    tableName = TABLE_BUDGET_VIEW,
    indices = [
        Index(value = [ACTUAL_DATE]),
        Index(value = [PAY_DAY]),
        Index(value = [IS_PAY_DAY_ITEM]),
        Index(value = [PROJECTED_AMOUNT])
    ],
    primaryKeys = [BUDGET_RULE_ID, PROJECTED_DATE],
    foreignKeys = [ForeignKey(
        entity = BudgetRule::class,
        parentColumns = [RULE_ID],
        childColumns = [BUDGET_RULE_ID]
    ),
        ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [TO_ACCOUNT_ID]
        ), ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [FROM_ACCOUNT_ID]
        )]
)
data class BudgetView(
    val bRuleId: Long,
    val projectedDate: String,
    val actualDate: String,
    val payDay: String,
    val budgetName: String,
    @ColumnInfo(defaultValue = "0")
    val isPayDayItem: Boolean,
    val toAccountId: Long,
    val fromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0")
    val projectedAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val isPending: Boolean,
    @ColumnInfo(defaultValue = "0")
    val isFixed: Boolean,
    @ColumnInfo(defaultValue = "0")
    val isAutomatic: Boolean,
    @ColumnInfo(defaultValue = "0")
    val manuallyEntered: Boolean,
    @ColumnInfo(defaultValue = "0")
    val isCompleted: Boolean,
    @ColumnInfo(defaultValue = "0")
    val isCancelled: Boolean,
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean,
    val updateTime: String
) : Parcelable

@Parcelize
data class BudgetDetailed(
    @Embedded
    val budgetView: BudgetView?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = Account::class,
        parentColumn = TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        entity = Account::class,
        parentColumn = FROM_ACCOUNT_ID,
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
        parentColumn = BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountWithType: AccountWithType?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountWithType: AccountWithType?
) : Parcelable
