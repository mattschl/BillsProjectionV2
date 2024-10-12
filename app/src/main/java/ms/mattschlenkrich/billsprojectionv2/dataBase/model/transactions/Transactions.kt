package ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule

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
    var transAmount: Double,
    @ColumnInfo(defaultValue = "0")
    val transIsDeleted: Boolean,
    val transUpdateTime: String,
) : Parcelable

