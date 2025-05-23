package ms.mattschlenkrich.billsprojectionv2.dataBase.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "sync")
@Parcelize
data class Sync(
    @PrimaryKey(autoGenerate = true)
    val syncID: Int,
    val syncStart: String,
    val syncComplete: String,
    @ColumnInfo( defaultValue = "0")
    val syncSuccess: Boolean,
    @ColumnInfo(defaultValue = "0")
    val syncDeleted: Boolean,
) : Parcelable


