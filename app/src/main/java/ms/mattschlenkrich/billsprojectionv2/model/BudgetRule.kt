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

@Entity(tableName = "daysOfWeek")
@Parcelize
data class DaysOfWeek(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val dayId: Long,
    val dayOfWeek: String,
) : Parcelable

@Entity(tableName = "frequencyType")
@Parcelize
data class FrequencyType(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val frequencyId: Long,
    val frequencyType: String
) : Parcelable

@Entity(
    tableName = "'budgetRules",
    indices = [
        Index(value = ["budgetRule"], unique = true),
        Index(value = ["toAccountId"]),
        Index(value = ["fromAccountId"])
    ],
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = ["accountId"],
        childColumns = ["toAccountId", "fromAccountId"]
    ), ForeignKey(
        entity = DaysOfWeek::class,
        parentColumns = ["dayId"],
        childColumns = ["dayOfWeekId"]
    ), ForeignKey(
        entity = FrequencyType::class,
        parentColumns = ["frequencyId"],
        childColumns = ["frequencyTypId"]
    )]
)
@Parcelize
data class BudgetRule(
    @PrimaryKey(autoGenerate = true)
    val budgetRuleId: Long,
    val bRuleName: String,
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
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String
) : Parcelable


data class BudgetRuleDetailed(
    @Embedded
    val budgetRule: BudgetRule,
    @Relation(
        parentColumn = "toAccountId",
        entityColumn = "accountId"
    )
    val toAccount: Account,
    @Relation(
        parentColumn = "fromAccountId",
        entityColumn = "accountId"
    )
    val fromAccount: Account,
    @Relation(
        parentColumn = "dayOfWeekId",
        entityColumn = "dayId"
    )
    val daysOfWeek: DaysOfWeek,
    @Relation(
        parentColumn = "frequencyTypeId",
        entityColumn = "frequencyId"
    )
    val frequencyType: FrequencyType
)