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
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_NAME
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_TYPE
import ms.mattschlenkrich.billsprojectionv2.ACCOUNT_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.ACCT_DISPLAY_AS_ASSET
import ms.mattschlenkrich.billsprojectionv2.ACCT_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.IS_ASSET
import ms.mattschlenkrich.billsprojectionv2.KEEP_MILEAGE
import ms.mattschlenkrich.billsprojectionv2.KEEP_TOTALS
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.TALLY_OWING
import ms.mattschlenkrich.billsprojectionv2.TYPE_ID

@Entity(
    tableName = TABLE_ACCOUNT_TYPES,
    indices = [Index(
        value = [ACCOUNT_TYPE], unique = true
    ),
        Index(value = [KEEP_TOTALS]),
        Index(value = [IS_ASSET]),
        Index(value = [TALLY_OWING]),
        Index(value = [KEEP_MILEAGE]),
        Index(value = [ACCT_DISPLAY_AS_ASSET]),
        Index(value = [ACCT_IS_DELETED])
    ]
)
@Parcelize
data class AccountType(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val typeId: Long,
    val accountType: String,
    val keepTotals: Boolean,
    val isAsset: Boolean,
    val tallyOwing: Boolean,
    val keepMileage: Boolean,
    val displayAsAsset: Boolean,
    @ColumnInfo(defaultValue = "0")
    val acctIsDeleted: Boolean,
    val acctUpdateTime: String,
) : Parcelable

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
    @ColumnInfo(defaultValue = "0")
    val accIsDeleted: Boolean,
    val accUpdateTime: String,
) : Parcelable

@Parcelize
data class AccountWithType(
    @Embedded
    val account: Account,
    @Relation(
        entity = AccountType::class,
        parentColumn = ACCOUNT_TYPE_ID,
        entityColumn = TYPE_ID
    )
    val accountType: AccountType
) : Parcelable

