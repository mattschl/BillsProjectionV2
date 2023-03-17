package ms.mattschlenkrich.billsprojectionv2.dataModel

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity (tableName = "accounts",
    foreignKeys = [ForeignKey(
        entity = AccountType::class,
        parentColumns = arrayOf("accountTypeId"),
        childColumns = arrayOf("accountTypeId")
    ),
    ForeignKey(
        entity = AccountCategory::class,
        parentColumns = arrayOf("accountCategoryId"),
        childColumns = arrayOf("'accountCategoryId")
    )]
)
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
    val account_ID: Int,
    val AccountName: String,
    val accountNumber: String?,
    val accountCategoryId: Int,
    val accountTypeId: Int,
    @ColumnInfo(defaultValue = "0.0" , typeAffinity = ColumnInfo.REAL)
    val budgetAmount: Double,
    @ColumnInfo(defaultValue = "0.0" , typeAffinity = ColumnInfo.REAL)
    val accountBalance: Double,
    @ColumnInfo(defaultValue = "0.0" , typeAffinity = ColumnInfo.REAL)
    val accountOwing: Double,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val accountDeleted: Boolean,
    val updateTime: String,
) : Parcelable


@Entity(tableName = "accountTypes")
@Parcelize
data class AccountType(
    @PrimaryKey(autoGenerate = true)
    val accountTypeId: Int,
    val accountType: String,
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
    val accountTypeDelete: Boolean,
    val updateTime: String,
): Parcelable


@Entity(tableName = "accountCategories")
@Parcelize
data class AccountCategory(
    @PrimaryKey(autoGenerate = true)
    val accountCategoryId: Int,
    val accountCategory: String,
    @ColumnInfo(defaultValue = "0", typeAffinity = ColumnInfo.INTEGER)
    val categoryDelete: Boolean,
    val updateTime: String,
): Parcelable