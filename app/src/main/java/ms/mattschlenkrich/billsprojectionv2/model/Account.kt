package ms.mattschlenkrich.billsprojectionv2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "accounts",
    indices = [
        Index(name = "idxAccountName", value = ["accountName"], unique = true),
        Index(value = ["accountTypeId"]),
        Index(value = ["accountCategoryId"])
    ],
    /*foreignKeys = [ForeignKey(
        entity = AccountType::class,
        parentColumns = ["accountTypeId"],
        childColumns = ["accountTypeId"]
    ),
        ForeignKey(
            entity = AccountCategory::class,
            parentColumns = ["accountCategoryId"],
            childColumns = ["accountCategoryId"]
        )
    ]*/
)
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val accountId: Long,
    val accountName: String,
    val accountNumber: String,
    val accountCategoryId: Long,
    val accountTypeId: Long,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val budgetAmount: Double,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val accountBalance: Double,
    @ColumnInfo(defaultValue = "0.0", typeAffinity = ColumnInfo.REAL)
    val accountOwing: Double,
    @ColumnInfo(defaultValue = "1", typeAffinity = ColumnInfo.INTEGER)
    val keepTotals: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isAsset: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val tallyOwing: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val keepMileage: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val displayAsAsset: Boolean,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String,
) : Parcelable


@Entity(
    tableName = "accountTypes",
    indices = [Index(name = "indexAccountType", value = ["accountType"], unique = true),
        Index(name = "indexAccountTypId", value = ["accountTypeId"])
    ]
)
@Parcelize
data class AccountType(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val accountTypeId: Long,
    val accountType: String,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String,
): Parcelable


@Entity(
    tableName = "accountCategory",
    indices = [Index(name = "indexAccountCategory", value = ["accountCategory"], unique = true),
        Index(name = "indexAccountCategoryId", value = ["accountCategoryId"])
    ]
)
@Parcelize
data class AccountCategory(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val accountCategoryId: Long,
    val accountCategory: String,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val isDeleted: Boolean,
    val updateTime: String,
) : Parcelable

//