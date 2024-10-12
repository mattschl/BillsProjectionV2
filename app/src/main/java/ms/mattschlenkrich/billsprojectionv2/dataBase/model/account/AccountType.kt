package ms.mattschlenkrich.billsprojectionv2.dataBase.model.account

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_TYPE
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_DISPLAY_AS_ASSET
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_IS_DELETED
import ms.mattschlenkrich.billsprojectionv2.common.IS_ASSET
import ms.mattschlenkrich.billsprojectionv2.common.KEEP_BALANCE
import ms.mattschlenkrich.billsprojectionv2.common.KEEP_MILEAGE
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TALLY_OWING


@Entity(
    tableName = TABLE_ACCOUNT_TYPES,
    indices = [Index(
        value = [ACCOUNT_TYPE], unique = true
    ),
        Index(value = [KEEP_BALANCE]),
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
    val typeId: Long,
    val accountType: String,
    val keepTotals: Boolean,
    val isAsset: Boolean,
    val tallyOwing: Boolean,
    val keepMileage: Boolean,
    val displayAsAsset: Boolean,
    @ColumnInfo(defaultValue = "0")
    val allowPending: Boolean,
    @ColumnInfo(defaultValue = "0")
    val acctIsDeleted: Boolean,
    val acctUpdateTime: String,
) : Parcelable
