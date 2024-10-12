package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUDGET_RULE_NAME
import ms.mattschlenkrich.billsprojectionv2.common.BUD_DAY_OF_WEEK_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_FREQUENCY_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.BUD_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.BUD_IS_PAY_DAY
import ms.mattschlenkrich.billsprojectionv2.common.BUD_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account


@Parcelize
@Entity(
    tableName = TABLE_BUDGET_RULES,
    indices = [
        Index(value = [BUDGET_RULE_NAME], unique = true),
        Index(value = [BUD_TO_ACCOUNT_ID]),
        Index(value = [BUD_FROM_ACCOUNT_ID]),
        Index(value = [BUD_DAY_OF_WEEK_ID]),
        Index(value = [BUD_FREQUENCY_TYPE_ID]),
        Index(value = [BUD_IS_PAY_DAY]),
        Index(value = [BUD_IS_DELETED])
    ],
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [BUD_TO_ACCOUNT_ID]
    ), ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [BUD_FROM_ACCOUNT_ID]
    )]
)
data class BudgetRule(
    @PrimaryKey
    val ruleId: Long,
    val budgetRuleName: String,
    val budToAccountId: Long,
    val budFromAccountId: Long,
    val budgetAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val budFixedAmount: Boolean,
    @ColumnInfo(defaultValue = "0")
    val budIsPayDay: Boolean,
    @ColumnInfo(defaultValue = "0")
    val budIsAutoPay: Boolean,
    val budStartDate: String,
    val budEndDate: String?,
    val budDayOfWeekId: Int,
    val budFrequencyTypeId: Int,
    @ColumnInfo(defaultValue = "1")
    val budFrequencyCount: Int,
    @ColumnInfo(defaultValue = "1")
    val budLeadDays: Int,
    @ColumnInfo(defaultValue = "0")
    val budIsDeleted: Boolean,
    val budUpdateTime: String
) : Parcelable
