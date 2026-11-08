package ms.mattschlenkrich.billsprojectionv2.common.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ColumnSettingsHelper {

    fun getThresholdDp(
        columnPreference: String?,
        widthFactor: Int = 4,
        isLandscape: Boolean = false
    ): Dp {
        val baseDp = when (columnPreference) {
            "very_few" -> 600.dp
            "few" -> 520.dp
            "many" -> 360.dp
            "very_many" -> 280.dp
            else -> 440.dp
        }
        val factorMultiplier = when (widthFactor) {
            1 -> 0.75f
            2 -> 0.85f
            3 -> 0.95f
            4 -> 1.05f
            5 -> 1.15f
            else -> 1.25f
        }
        val adjustedDp = (baseDp.value * factorMultiplier).dp
        return if (isLandscape) {
            (adjustedDp.value * 0.8f).dp
        } else {
            adjustedDp
        }
    }

    /**
     * Calculates the column count for a grid list based on the list item's content width factor,
     * the user's column preference setting in SettingsManager, and screen orientation.
     *
     * The width factor determines how wide an item card must be to display its content
     * (considering number of internal columns and concatenated text strings).
     * Items with high width factors (4, 5, 6+) containing lots of detailed information
     * require wider cards (1 column in portrait, 2 in landscape), while simple items
     * with low width factors (1, 2) fit in narrower cards (2-3 columns in portrait, 4-5 in landscape).
     */
    fun calculateColumnCount(
        widthFactor: Int = 3,
        columnPreference: String? = "medium",
        isLandscape: Boolean = false
    ): Int {
        val (baseColumns, maxColumnsAllowed) = if (isLandscape) {
            when (widthFactor) {
                1 -> Pair(5, 6)
                2 -> Pair(4, 5)
                3 -> Pair(3, 4)
                4 -> Pair(2, 3)
                5 -> Pair(2, 2)
                else -> Pair(2, 2)
            }
        } else {
            when (widthFactor) {
                1 -> Pair(3, 4)
                2 -> Pair(2, 3)
                3 -> Pair(2, 3)
                4 -> Pair(1, 2)
                5 -> Pair(1, 2)
                else -> Pair(1, 1)
            }
        }

        val settingDelta = when (columnPreference) {
            "very_few" -> -1
            "few" -> 0
            "many" -> +1
            "very_many" -> +2
            else -> 0
        }

        return (baseColumns + settingDelta).coerceIn(1, maxColumnsAllowed)
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