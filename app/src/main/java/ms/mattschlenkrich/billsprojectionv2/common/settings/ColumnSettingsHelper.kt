package ms.mattschlenkrich.billsprojectionv2.common.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ColumnSettingsHelper {

    fun getThresholdDp(columnPreference: String?, isLandscape: Boolean = false): Dp {
        val baseDp = when (columnPreference) {
            "very_few" -> 600.dp
            "few" -> 520.dp
            "many" -> 360.dp
            "very_many" -> 280.dp
            else -> 440.dp
        }
        return if (isLandscape) {
            (baseDp.value * 0.8f).dp
        } else {
            baseDp
        }
    }

    fun getGridColumnCount(
        columnPreference: String?,
        defaultCount: Int = 2,
        isLandscape: Boolean = false
    ): Int {
        val landscapeBonus = if (isLandscape) 1 else 0
        val delta = when (columnPreference) {
            "very_few" -> -1
            "few" -> 0
            "many" -> +1
            "very_many" -> +2
            else -> 0
        }
        return (defaultCount + delta + landscapeBonus).coerceIn(1, 6)
    }
}