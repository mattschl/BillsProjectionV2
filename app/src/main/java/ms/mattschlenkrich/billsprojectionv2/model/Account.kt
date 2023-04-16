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


@Entity(
    tableName = "accountTypes",
    indices = [Index(
        name = "idxAccountType",
        value = ["accountType"], unique = true
    )
    ]
)
@Parcelize
data class AccountType(
    @PrimaryKey(autoGenerate = true)
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
    tableName = "accounts",
    indices = [
        Index(value = ["accountName"], unique = true),
        Index(value = ["accountTypeId"])
    ],
    foreignKeys = [ForeignKey(
        entity = AccountType::class,
        parentColumns = ["typeId"],
        childColumns = ["accountTypeId"]
    )]
)
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
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

data class AccountWithType(
    @Embedded
    val account: Account,
    @Relation(
        parentColumn = "accountTypeId",
        entityColumn = "typeId"
    )
    val accountType: AccountType
)