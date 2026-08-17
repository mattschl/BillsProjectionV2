package ms.mattschlenkrich.billsprojectionv2.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.settings.AppSettings

class SettingsEditState(initialSettings: AppSettings) {
    var fontSize by mutableStateOf(initialSettings.fontSize ?: "medium")
    var themeMode by mutableStateOf(initialSettings.themeMode ?: "system")
    var usePasswordProtection by mutableStateOf(initialSettings.usePasswordProtection)
    var isPasswordSet by mutableStateOf(initialSettings.passwordHash != null)
    var defaultAccount by mutableStateOf(initialSettings.defaultAccount ?: ALL_ITEMS)

    fun updateFrom(settings: AppSettings) {
        fontSize = settings.fontSize ?: "medium"
        themeMode = settings.themeMode ?: "system"
        usePasswordProtection = settings.usePasswordProtection
        isPasswordSet = settings.passwordHash != null
        defaultAccount = settings.defaultAccount ?: ALL_ITEMS
    }
}

@Composable
fun rememberSettingsEditState(initialSettings: AppSettings): SettingsEditState {
    return remember { SettingsEditState(initialSettings) }
}