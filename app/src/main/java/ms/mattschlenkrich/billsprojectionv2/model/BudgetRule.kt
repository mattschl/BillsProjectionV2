package ms.mattschlenkrich.billsprojectionv2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_NAME
import ms.mattschlenkrich.billsprojectionv2.DAY_OF_WEEK_ID
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.TO_ACCOUNT_ID


@Parcelize
@Entity(
    tableName = TABLE_BUDGET_RULES,
    indices = [
        Index(value = [BUDGET_RULE_NAME], unique = true),
        Index(value = [TO_ACCOUNT_ID]),
        Index(value = [FROM_ACCOUNT_ID]),
        Index(value = [DAY_OF_WEEK_ID]),
        Index(value = [FREQUENCY_TYPE_ID])
    ],
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [TO_ACCOUNT_ID]
    ), ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [FROM_ACCOUNT_ID]
    )]
)
data class BudgetRule(
    @PrimaryKey(autoGenerate = true)
    val RuleId: Long,
    val budgetRuleName: String,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val toAccountId: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val fromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val amount: Double,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val fixedAmount: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isPayDay: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isAutoPay: Boolean,
    val startDate: String,
    val endDate: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val dayOfWeekId: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val frequencyTypeId: Long,
    @ColumnInfo(defaultValue = "1")
    val frequencyCount: Int,
    @ColumnInfo(defaultValue = "1")
    val leadDays: Int,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String
) : Parcelable

@Parcelize
data class BudgetRuleDetailed(
    @Embedded
    val budgetRule: BudgetRule?,
    @Relation(
        parentColumn = TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        parentColumn = FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable