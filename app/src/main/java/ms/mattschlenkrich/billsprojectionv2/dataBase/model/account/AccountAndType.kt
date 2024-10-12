package ms.mattschlenkrich.billsprojectionv2.dataBase.model.account

import android.os.Parcelable
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.ACCOUNT_TYPE_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TYPE_ID


@DatabaseView(
    "SELECT $TABLE_ACCOUNTS.*," +
            "$TABLE_ACCOUNT_TYPES.* " +
            "FROM $TABLE_ACCOUNTS " +
            "LEFT JOIN $TABLE_ACCOUNT_TYPES on " +
            "$TABLE_ACCOUNTS.accountTypeId =" +
            "$TABLE_ACCOUNT_TYPES.typeId;"
)
@Parcelize
data class AccountAndType(
    @Embedded
    val account: Account,
    @Relation(
        entity = AccountType::class,
        parentColumn = ACCOUNT_TYPE_ID,
        entityColumn = TYPE_ID,
    )
    val accountType: AccountType?,
) : Parcelable

@Parcelize
data class AccountWithType(
    @Embedded
    val account: Account,
    @Relation(
        entity = AccountType::class,
        parentColumn = ACCOUNT_TYPE_ID,
        entityColumn = TYPE_ID,
    )
    val accountType: AccountType?,
) : Parcelable
