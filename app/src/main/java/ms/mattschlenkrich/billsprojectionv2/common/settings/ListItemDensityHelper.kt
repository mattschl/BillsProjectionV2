package ms.mattschlenkrich.billsprojectionv2.common.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ListItemDensityHelper {

    fun getVerticalPadding(densityPreference: String?): Dp {
        return when (densityPreference) {
            "very_tight" -> 2.dp
            "tight" -> 4.dp
            "loose" -> 10.dp
            "very_loose" -> 14.dp
            else -> 6.dp
        }
    }

    fun getCardVerticalSpacing(densityPreference: String?): Dp {
        return when (densityPreference) {
            "very_tight" -> 1.dp
            "tight" -> 2.dp
            "loose" -> 6.dp
            "very_loose" -> 10.dp
            else -> 3.dp
        }
    }
}