package ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "precitionUpdateHistory")
data class PredictionUpdateHistory(
    @PrimaryKey
    val predictionUpdateId: Long,
    val puhTime: String, //the time this update was completed, created during the update pr
    val puhLastPredictionDate: String, //the last predicted budget item that was created
    val puhPredictionLength: Int, //the number of months predicted ahead in this update
    val puhDeviceId: Long,
) : Parcelable