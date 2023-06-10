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
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.TYPE_ID


@Entity(
    tableName = TABLE_ACCOUNT_TYPES,
    indices = [Index(
        value = [ACCOUNT_TYPE], unique = true
    )
    ]
)
@Parcelize
data class AccountType(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val typeId: Long,
    val accountType: String,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val keepTotals: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val isAsset: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val tallyOwing: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val keepMileage: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val displayAsAsset: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String,
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
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val budgetAmount: Double,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val accountBalance: Double,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val accountOwing: Double,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String,
) : Parcelable

@Parcelize
data class AccountWithType(
    @Embedded
    val account: Account,
    @Relation(
        parentColumn = ACCOUNT_TYPE_ID,
        entityColumn = TYPE_ID
    )
    val accountType: AccountType
) : Parcelable