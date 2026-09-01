package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_ACTUAL_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_CANCELLED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_COMPLETED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_IS_PAY_DAY_ITEM
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PROJECTED_AMOUNT
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_PROJECTED_DATE
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_ITEM_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

@Parcelize
@Entity(
    tableName = TABLE_BUDGET_ITEMS,
    indices = [
        Index(value = [BUDGET_ITEM_ACTUAL_DATE]),
        Index(value = [BUDGET_ITEM_PAY_DAY]),
        Index(value = [BUDGET_ITEM_IS_PAY_DAY_ITEM]),
        Index(value = [BUDGET_ITEM_PROJECTED_AMOUNT]),
        Index(value = [BUDGET_ITEM_TO_ACCOUNT_ID]),
        Index(value = [BUDGET_ITEM_FROM_ACCOUNT_ID]),
        Index(value = [BUDGET_ITEM_IS_DELETED]),
        Index(value = [BUDGET_ITEM_IS_CANCELLED]),
        Index(value = [BUDGET_ITEM_IS_COMPLETED])
    ],
    primaryKeys = [BUDGET_ITEM_RULE_ID, BUDGET_ITEM_PROJECTED_DATE],
    foreignKeys = [ForeignKey(
        entity = BudgetRule::class,
        parentColumns = [BUDGET_RULE_ID],
        childColumns = [BUDGET_ITEM_RULE_ID]
    ),
        ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BUDGET_ITEM_TO_ACCOUNT_ID]
        ), ForeignKey(
            entity = Account::class,
            parentColumns = [ACCOUNT_ID],
            childColumns = [BUDGET_ITEM_FROM_ACCOUNT_ID]
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