package ms.mattschlenkrich.billsprojectionv2.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

@Composable
fun SettingsScreenWrapper(
    mainActivity: MainActivity
) {
    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.setTitle(R.string.settings)
    }
    val settingsManager = remember { SettingsManager(mainActivity) }
    val settings = remember { settingsManager.getSettings() }
    var selectedFontSize by remember { mutableStateOf(settings.fontSize ?: "medium") }
    var selectedThemeMode by remember { mutableStateOf(settings.themeMode ?: "system") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.settings),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = R.string.theme_mode),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            ThemeOption("system", R.string.system_default, selectedThemeMode) {
                updateThemeMode("system", settingsManager, mainActivity)
                selectedThemeMode = "system"
            }
            ThemeOption("light", R.string.light, selectedThemeMode) {
                updateThemeMode("light", settingsManager, mainActivity)
                selectedThemeMode = "light"
            }
            ThemeOption("dark", R.string.dark, selectedThemeMode) {
                updateThemeMode("dark", settingsManager, mainActivity)
                selectedThemeMode = "dark"
            }
        }

        Text(
            text = stringResource(id = R.string.font_size),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            FontSizeOption("small", R.string.small, selectedFontSize) {
                updateFontSize("small", settingsManager, mainActivity)
                selectedFontSize = "small"
            }
            FontSizeOption("medium", R.string.medium, selectedFontSize) {
                updateFontSize("medium", settingsManager, mainActivity)
                selectedFontSize = "medium"
            }
            FontSizeOption("large", R.string.large, selectedFontSize) {
                updateFontSize("large", settingsManager, mainActivity)
                selectedFontSize = "large"
            }
            FontSizeOption("extra_large", R.string.extra_large, selectedFontSize) {
                updateFontSize("extra_large", settingsManager, mainActivity)
                selectedFontSize = "extra_large"
            }
        }
    }
}

@Composable
fun ThemeOption(
    value: String,
    labelRes: Int,
    selectedThemeMode: String,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = (selectedThemeMode == value),
            onClick = onSelect
        )
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun FontSizeOption(
    value: String,
    labelRes: Int,
    selectedFontSize: String,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = (selectedFontSize == value),
            onClick = onSelect
        )
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun updateFontSize(
    fontSize: String,
    settingsManager: SettingsManager,
    mainActivity: MainActivity
) {
    val settings = settingsManager.getSettings()
    settingsManager.saveSettings(settings.copy(fontSize = fontSize))
    mainActivity.recreate()
}

private fun updateThemeMode(
    themeMode: String,
    settingsManager: SettingsManager,
    mainActivity: MainActivity
) {
    val settings = settingsManager.getSettings()
    settingsManager.saveSettings(settings.copy(themeMode = themeMode))
    mainActivity.recreate()
}