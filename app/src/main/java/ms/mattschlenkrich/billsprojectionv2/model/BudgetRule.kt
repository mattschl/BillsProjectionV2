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
import ms.mattschlenkrich.billsprojectionv2.DAY_ID
import ms.mattschlenkrich.billsprojectionv2.DAY_OF_WEEK_ID
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_ID
import ms.mattschlenkrich.billsprojectionv2.FREQUENCY_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.TABLE_DAYS_OF_WEEK
import ms.mattschlenkrich.billsprojectionv2.TABLE_FREQUENCY_TYPES
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
    ), ForeignKey(
        entity = DaysOfWeek::class,
        parentColumns = [DAY_ID],
        childColumns = [DAY_OF_WEEK_ID]
    ), ForeignKey(
        entity = FrequencyTypes::class,
        parentColumns = [FREQUENCY_ID],
        childColumns = [FREQUENCY_TYPE_ID]
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
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String
) : Parcelable

@Entity(tableName = TABLE_DAYS_OF_WEEK)
@Parcelize
data class DaysOfWeek(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val dayId: Long,
    val dayOfWeek: String,
) : Parcelable

@Entity(tableName = TABLE_FREQUENCY_TYPES)
@Parcelize
data class FrequencyTypes(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val frequencyId: Long,
    val frequencyType: String
) : Parcelable

data class BudgetRuleDetailed(
    @Embedded
    val budgetRule: BudgetRule,
    @Relation(
        parentColumn = TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    val toAccount: Account,
    @Relation(
        parentColumn = FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    val fromAccount: Account,
    @Relation(
        parentColumn = DAY_OF_WEEK_ID,
        entityColumn = DAY_ID
    )
    val daysOfWeek: DaysOfWeek,
    @Relation(
        parentColumn = FREQUENCY_TYPE_ID,
        entityColumn = FREQUENCY_ID
    )
    val frequencyTypes: FrequencyTypes
)