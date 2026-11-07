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

    /**
     * Calculates dynamic column count based on element count, column preference setting, and landscape mode.
     * Capped at max 2 columns in portrait and max 3 columns in landscape to ensure detailed cards remain wide,
     * spacious, and readable without aggressive text wrapping.
     */
    fun calculateColumnCount(
        elementCount: Int,
        columnPreference: String?,
        isLandscape: Boolean = false
    ): Int {
        if (elementCount <= 1) return 1

        val maxAllowedInPortrait = when (columnPreference) {
            "very_few" -> 1
            "few" -> 1
            "many" -> 2
            "very_many" -> 2
            else -> 2
        }

        val maxAllowedInLandscape = when (columnPreference) {
            "very_few" -> 1
            "few" -> 2
            "many" -> 3
            "very_many" -> 4
            else -> 3
        }

        val baseColumns = if (isLandscape) {
            when {
                elementCount <= 2 -> 2
                elementCount <= 6 -> 2
                else -> 3
            }
        } else {
            when {
                elementCount <= 2 -> 1
                else -> 2
            }
        }

        val maxCap = if (isLandscape) maxAllowedInLandscape else maxAllowedInPortrait
        return baseColumns.coerceIn(1, maxCap)
    }

    fun getGridMinSizeDp(
        columnPreference: String?,
        isLandscape: Boolean = false
    ): Dp {
        val baseMinSize = when (columnPreference) {
            "very_few" -> 380.dp
            "few" -> 300.dp
            "many" -> 200.dp
            "very_many" -> 160.dp
            else -> 240.dp
        }
        return if (isLandscape) {
            (baseMinSize.value * 0.95f).dp
        } else {
            baseMinSize
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