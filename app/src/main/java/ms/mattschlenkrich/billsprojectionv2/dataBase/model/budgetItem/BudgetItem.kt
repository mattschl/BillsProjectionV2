package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_COMPLETED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BI_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.common.BI_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BI_PROJECTED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.BI_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BI_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

@Parcelize
@Entity(
    tableName = TABLE_BUDGET_ITEMS,
    indices = [
        Index(value = [BI_ACTUAL_DATE]),
        Index(value = [BI_PAY_DAY]),
        Index(value = [BI_IS_PAY_DAY_ITEM]),
        Index(value = [BI_PROJECTED_AMOUNT]),
        Index(value = [BI_TO_ACCOUNT_ID]),
        Index(value = [BI_FROM_ACCOUNT_ID]),
        Index(value = [BI_IS_DELETED]),
        Index(value = [BI_IS_CANCELLED]),
        Index(value = [BI_IS_COMPLETED])
    ],
    primaryKeys = [BI_BUDGET_RULE_ID, BI_PROJECTED_DATE],
    foreignKeys = [ForeignKey(
        entity = BudgetRule::class,
        parentColumns = [RULE_ID],
        childColumns = [BI_BUDGET_RULE_ID]
    ),
        ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BI_TO_ACCOUNT_ID]
        ), ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BI_FROM_ACCOUNT_ID]
        )]
)
data class BudgetItem(
    val biRuleId: Long,
    val biProjectedDate: String,
    val biActualDate: String,
    val biPayDay: String,
    val biBudgetName: String,
    @ColumnInfo(defaultValue = "0")
    val biIsPayDayItem: Boolean,
    val biToAccountId: Long,
    val biFromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0")
    var biProjectedAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val biIsPending: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biIsFixed: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biIsAutomatic: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biManuallyEntered: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biIsCompleted: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biIsCancelled: Boolean,
    @ColumnInfo(defaultValue = "0")
    val biIsDeleted: Boolean,
    val biUpdateTime: String,
    @ColumnInfo(defaultValue = "0")
    val biLocked: Boolean,
) : Parcelable
