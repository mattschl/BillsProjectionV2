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
import ms.mattschlenkrich.billsprojectionv2.BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.TRANSACTION_DATE

@Parcelize
@Entity(
    tableName = TABLE_TRANSACTION,
    indices = [
        Index(value = [TRANSACTION_DATE]),
        Index(value = [BUDGET_RULE_ID]),
        Index(value = [TO_ACCOUNT_ID]),
        Index(value = [FROM_ACCOUNT_ID])
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
        entity = BudgetRule::class,
        parentColumns = [RULE_ID],
        childColumns = [BUDGET_RULE_ID]
    )]
)
data class Transactions(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val transId: Long,
    val transDate: String,
    val transName: String,
    val transNote: String,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val bRuleId: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val toAccountId: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val fromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val amount: Double,
    val updateTime: String,
) : Parcelable

@Parcelize
data class TransactionDetailed(
    @Embedded
    val transId: Transactions?,
    @Relation(
        parentColumn = BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
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