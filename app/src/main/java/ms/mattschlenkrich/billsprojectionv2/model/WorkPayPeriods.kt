package ms.mattschlenkrich.billsprojectionv2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.billsprojectionv2.common.EMPLOYER_ID
import ms.mattschlenkrich.billsprojectionv2.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.billsprojectionv2.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_WORK_DATES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_WORK_PAY_PERIODS
import ms.mattschlenkrich.billsprojectionv2.common.WORK_DATES_CUTOFF_DATE
import ms.mattschlenkrich.billsprojectionv2.common.WORK_DATES_DATE
import ms.mattschlenkrich.billsprojectionv2.common.WORK_DATES_EMPLOYER_ID

@Entity(
    tableName = TABLE_EMPLOYERS
)
@Parcelize
data class Employers(
    @PrimaryKey
    val employerId: Long,
    val employerName: String,
    val employerIsDeleted: Boolean,
    val employerUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_PAY_PERIODS,
    primaryKeys = [PAY_PERIOD_CUTOFF_DATE, PAY_PERIOD_EMPLOYER_ID],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [PAY_PERIOD_EMPLOYER_ID]
    )]
)
@Parcelize
data class WorkPayPeriods(
    val ppCutoffDate: String,
    val ppEmployerId: Long,
    @ColumnInfo(defaultValue = "0")
    val ppIsDeleted: Boolean,
    val ppUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_DATES,
    primaryKeys = [WORK_DATES_EMPLOYER_ID, WORK_DATES_CUTOFF_DATE, WORK_DATES_DATE],
    foreignKeys = [
        ForeignKey(
            entity = WorkPayPeriods::class,
            parentColumns = [PAY_PERIOD_EMPLOYER_ID],
            childColumns = [WORK_DATES_EMPLOYER_ID]
        ),
        ForeignKey(
            entity = WorkPayPeriods::class,
            parentColumns = [PAY_PERIOD_CUTOFF_DATE],
            childColumns = [WORK_DATES_CUTOFF_DATE]
        )
    ]
)
@Parcelize
data class WorkDates(
    val wdEmployerId: Long,
    val wdCutoffDate: String,
    val wdDate: String,
    val wdRegHours: Double,
    val wdOtHours: Double,
    val wdDblOtHours: Double,
    val wdStatHours: Double,
    @ColumnInfo(defaultValue = "0")
    val wdIsDeleted: Boolean,
    val wdUpdateTime: String,
) : Parcelable

