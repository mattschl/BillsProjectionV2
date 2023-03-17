package ms.mattschlenkrich.billsprojectionv2.dataModel

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "accounts",
    foreignKeys = [ForeignKey(
        entity = AccountType::class,
        parentColumns = arrayOf("accountTypeId"),
        childColumns = arrayOf("accountTypeId")
    ),
        ForeignKey(
            entity = AccountCategory::class,
            parentColumns = arrayOf("accountCategoryId"),
            childColumns = arrayOf("'accountCategoryId")
        )],
    indices = [
        Index(name = "indexAccountName", value = ["accountName"], unique = true)
    ]
)
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val accountId: Long,
    val accountName: String,
    val accountNumber: String?,
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
    indices = [Index(name = "indexAccountType", value = ["accountType"], unique = true)]
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
    indices = [Index(name = "indexAccountCategory", value = ["accountCategory"], unique = true)]
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
): Parcelable