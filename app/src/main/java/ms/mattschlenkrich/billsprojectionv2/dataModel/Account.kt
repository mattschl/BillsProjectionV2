package ms.mattschlenkrich.billsprojectionv2.dataModel

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity (tableName = "tblAccounts", )
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
    val account_ID: Int,
    val AccountName: String,
    val accountNumber: String,
    val budgetCat: String,
    val accountTypeId: Int,
    val budgetAmount: Double,
    val accountBalance: Double,
    val accountOwing: Double,
    val keepTotals: Boolean,
    val isAsset: Boolean,
    val tallyOwing: Boolean,
    val keepMileage: Boolean,
    val displayAsAsset: Boolean,
    val updateTime: String,
) : Parcelable
