package ms.mattschlenkrich.billsprojectionv2.dataBase.model.account

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TYPE_ID

@Entity(
    tableName = TABLE_ACCOUNTS,
    indices = [
        Index(value = [ACCOUNT_NAME], unique = true),
        Index(value = [ACCOUNT_TYPE_ID])
    ],
    foreignKeys = [ForeignKey(
        entity = AccountType::class,
        parentColumns = [TYPE_ID],
        childColumns = [ACCOUNT_TYPE_ID]
    )]
)
@Parcelize
data class Account(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val accountId: Long,
    val accountName: String,
    val accountNumber: String,
    val accountTypeId: Long,
    @ColumnInfo(defaultValue = "0.0")
    val accBudgetedAmount: Double,
    @ColumnInfo(defaultValue = "0.0")
    val accountBalance: Double,
    @ColumnInfo(defaultValue = "0.0")
    val accountOwing: Double,
    @ColumnInfo(defaultValue = "0.0")
    val accountCreditLimit: Double,
    @ColumnInfo(defaultValue = "0")
    val accIsDeleted: Boolean,
    val accUpdateTime: String,
) : Parcelable
