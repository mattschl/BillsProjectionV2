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
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_DATE
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_FROM_ACCOUNT_PENDING
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANSACTION_TO_ACCOUNT_PENDING
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_BUDGET_RULE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TRANS_IS_DELETED

@Parcelize
@Entity(
    tableName = TABLE_TRANSACTION,
    indices = [
        Index(value = [TRANSACTION_DATE]),
        Index(value = [TRANS_BUDGET_RULE_ID]),
        Index(value = [TRANSACTION_TO_ACCOUNT_ID]),
        Index(value = [TRANSACTION_FROM_ACCOUNT_ID]),
        Index(value = [TRANS_IS_DELETED]),
        Index(value = [TRANSACTION_TO_ACCOUNT_PENDING]),
        Index(value = [TRANSACTION_FROM_ACCOUNT_PENDING])
    ],
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [TRANSACTION_TO_ACCOUNT_ID]
    ), ForeignKey(
        entity = Account::class,
        parentColumns = [ACCOUNT_ID],
        childColumns = [TRANSACTION_FROM_ACCOUNT_ID]
    ), ForeignKey(
        entity = BudgetRule::class,
        parentColumns = [RULE_ID],
        childColumns = [TRANS_BUDGET_RULE_ID]
    )]
)
data class Transactions(
    @PrimaryKey(autoGenerate = true)
    val transId: Long,
    val transDate: String,
    val transName: String,
    val transNote: String,
    val transRuleId: Long,
    var transToAccountId: Long,
    @ColumnInfo(defaultValue = "0.0")
    var transToAccountPending: Boolean,
    var transFromAccountId: Long,
    @ColumnInfo(defaultValue = "0.0")
    var transFromAccountPending: Boolean,
    @ColumnInfo(defaultValue = "0.0")
    val transAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val transIsDeleted: Boolean,
    val transUpdateTime: String,
) : Parcelable

@Parcelize
data class TransactionDetailed(
    @Embedded
    val transaction: Transactions?,
    @Relation(
        parentColumn = TRANS_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        parentColumn = TRANSACTION_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccount: Account?,
    @Relation(
        parentColumn = TRANSACTION_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccount: Account?
) : Parcelable

@Parcelize
data class TransactionFull(
    @Embedded
    val transaction: Transactions?,
    @Relation(
        entity = BudgetRule::class,
        parentColumn = TRANS_BUDGET_RULE_ID,
        entityColumn = RULE_ID
    )
    var budgetRule: BudgetRule?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = TRANSACTION_TO_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var toAccountWithType: AccountWithType?,
    @Relation(
        entity = AccountWithType::class,
        parentColumn = TRANSACTION_FROM_ACCOUNT_ID,
        entityColumn = ACCOUNT_ID
    )
    var fromAccountWithType: AccountWithType?
) : Parcelable

