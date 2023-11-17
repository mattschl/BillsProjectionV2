package ms.mattschlenkrich.billsprojectionv2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_WORK_EXTRAS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_WORK_EXTRA_FREQUENCIES
import ms.mattschlenkrich.billsprojectionv2.common.WORK_EXTRA_FREQUENCY
import ms.mattschlenkrich.billsprojectionv2.common.WORK_EXTRA_FREQUENCY_NAME

@Entity(
    tableName = TABLE_WORK_EXTRAS,
    foreignKeys = [ForeignKey(
        entity = WorkExtraFrequencies::class,
        parentColumns = [WORK_EXTRA_FREQUENCY_NAME],
        childColumns = [WORK_EXTRA_FREQUENCY]
    )]
)
@Parcelize
data class WorkExtras(
    @PrimaryKey
    val workExtraId: Long,
    val weEmployerId: Long,
    val weName: String,
    val weFrequency: String,
    val weValue: Double,
    @ColumnInfo(defaultValue = "1")
    val weIsCredit: Boolean,
    @ColumnInfo(defaultValue = "0")
    val weIsDeleted: Boolean,
    val weUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_EXTRA_FREQUENCIES
)
@Parcelize
data class WorkExtraFrequencies(
    @PrimaryKey
    val workExtraFrequencyName: String
) : Parcelable