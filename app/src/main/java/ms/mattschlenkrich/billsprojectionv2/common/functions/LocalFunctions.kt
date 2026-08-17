package ms.mattschlenkrich.billsprojectionv2.common.functions

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNumberFunctions = staticCompositionLocalOf<NumberFunctions> {
    error("No NumberFunctions provided")
}

val LocalDateFunctions = staticCompositionLocalOf<DateFunctions> {
    error("No DateFunctions provided")
}

val LocalVisualsFunctions = staticCompositionLocalOf<VisualsFunctions> {
    error("No VisualsFunctions provided")
}