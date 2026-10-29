package ms.mattschlenkrich.billsprojectionv2.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.components.DropdownSelector
import ms.mattschlenkrich.billsprojectionv2.common.functions.SecurityUtils
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

@Composable
fun SettingsScreenWrapper(
    mainActivity: MainActivity
) {
    val settingsManager = remember { SettingsManager(mainActivity) }
    val initialSettings = remember { settingsManager.getSettings() }
    val state = rememberSettingsEditState(initialSettings)

    val rawAssetList by mainActivity.budgetItemViewModel.getAssetsForBudget()
        .observeAsState(initial = emptyList())
    val assetList = remember(rawAssetList) {
        if (rawAssetList.isEmpty()) listOf(ALL_ITEMS)
        else listOf(ALL_ITEMS) + rawAssetList
    }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showConfirmRemovalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.nav_settings),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = R.string.label_theme_mode),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            ThemeOption("system", R.string.label_system_default, state.themeMode) {
                updateThemeMode("system", settingsManager, mainActivity)
                state.themeMode = "system"
            }
            ThemeOption("light", R.string.label_theme_light, state.themeMode) {
                updateThemeMode("light", settingsManager, mainActivity)
                state.themeMode = "light"
            }
            ThemeOption("dark", R.string.label_theme_dark, state.themeMode) {
                updateThemeMode("dark", settingsManager, mainActivity)
                state.themeMode = "dark"
            }
        }

        Text(
            text = stringResource(id = R.string.label_font_size),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            FontSizeOption("small", R.string.label_font_small, state.fontSize) {
                updateFontSize("small", settingsManager, mainActivity)
                state.fontSize = "small"
            }
            FontSizeOption("medium", R.string.label_font_medium, state.fontSize) {
                updateFontSize("medium", settingsManager, mainActivity)
                state.fontSize = "medium"
            }
            FontSizeOption("large", R.string.label_font_large, state.fontSize) {
                updateFontSize("large", settingsManager, mainActivity)
                state.fontSize = "large"
            }
            FontSizeOption("extra_large", R.string.label_font_extra_large, state.fontSize) {
                updateFontSize("extra_large", settingsManager, mainActivity)
                state.fontSize = "extra_large"
            }
        }

        Text(
            text = stringResource(id = R.string.label_column_count),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            FontSizeOption("very_few", R.string.label_columns_very_few, state.columnPreference) {
                updateColumnPreference("very_few", settingsManager)
                state.columnPreference = "very_few"
            }
            FontSizeOption("few", R.string.label_columns_few, state.columnPreference) {
                updateColumnPreference("few", settingsManager)
                state.columnPreference = "few"
            }
            FontSizeOption("medium", R.string.label_columns_medium, state.columnPreference) {
                updateColumnPreference("medium", settingsManager)
                state.columnPreference = "medium"
            }
            FontSizeOption("many", R.string.label_columns_many, state.columnPreference) {
                updateColumnPreference("many", settingsManager)
                state.columnPreference = "many"
            }
            FontSizeOption("very_many", R.string.label_columns_very_many, state.columnPreference) {
                updateColumnPreference("very_many", settingsManager)
                state.columnPreference = "very_many"
            }
        }

        Text(
            text = stringResource(id = R.string.title_general),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            DropdownSelector(
                label = stringResource(R.string.label_default_startup_account),
                options = assetList,
                selectedOption = if (assetList.contains(state.defaultAccount)) state.defaultAccount else ALL_ITEMS,
                onOptionSelected = { selected ->
                    state.defaultAccount = selected
                    val currentSettings = settingsManager.getSettings()
                    settingsManager.saveSettings(currentSettings.copy(defaultAccount = selected))
                }
            )

            val syncOptionsMap = mapOf(
                "Disabled" to 0,
                "1 Day" to 1,
                "2 Days" to 2,
                "3 Days (Default)" to 3,
                "5 Days" to 5,
                "7 Days (1 Week)" to 7,
                "14 Days (2 Weeks)" to 14,
                "30 Days (1 Month)" to 30,
            )
            val syncOptions = syncOptionsMap.keys.toList()
            val currentSyncSelectedOption =
                syncOptionsMap.entries.find { it.value == state.syncWarningDays }?.key
                    ?: "3 Days (Default)"

            Spacer(modifier = Modifier.height(16.dp))

            DropdownSelector(
                label = stringResource(R.string.label_sync_reminder_interval),
                options = syncOptions,
                selectedOption = currentSyncSelectedOption,
                onOptionSelected = { selected ->
                    val days = syncOptionsMap[selected] ?: 3
                    state.syncWarningDays = days
                    val currentSettings = settingsManager.getSettings()
                    settingsManager.saveSettings(currentSettings.copy(syncWarningDays = days))
                }
            )

            val budgetUpdateOptionsMap = mapOf(
                "Disabled" to 0,
                "1 Week" to 1,
                "2 Weeks" to 2,
                "3 Weeks (Default)" to 3,
                "4 Weeks (1 Month)" to 4,
                "6 Weeks" to 6,
                "8 Weeks (2 Months)" to 8,
            )
            val budgetUpdateOptions = budgetUpdateOptionsMap.keys.toList()
            val currentBudgetUpdateSelectedOption =
                budgetUpdateOptionsMap.entries.find { it.value == state.budgetUpdateWarningWeeks }?.key
                    ?: "3 Weeks (Default)"

            Spacer(modifier = Modifier.height(16.dp))

            DropdownSelector(
                label = stringResource(R.string.label_budget_update_reminder_interval),
                options = budgetUpdateOptions,
                selectedOption = currentBudgetUpdateSelectedOption,
                onOptionSelected = { selected ->
                    val weeks = budgetUpdateOptionsMap[selected] ?: 3
                    state.budgetUpdateWarningWeeks = weeks
                    val currentSettings = settingsManager.getSettings()
                    settingsManager.saveSettings(currentSettings.copy(budgetUpdateWarningWeeks = weeks))
                }
            )
        }

        Text(
            text = stringResource(id = R.string.title_security),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_password_protection),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.usePasswordProtection,
                    onCheckedChange = { checked ->
                        if (checked && !state.isPasswordSet) {
                            showPasswordDialog = true
                        } else if (!checked && state.isPasswordSet) {
                            showConfirmRemovalDialog = true
                        } else {
                            state.usePasswordProtection = checked
                            val currentSettings = settingsManager.getSettings()
                            settingsManager.saveSettings(currentSettings.copy(usePasswordProtection = checked))
                        }
                    }
                )
            }

            if (state.isPasswordSet) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.title_change_password))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        showConfirmRemovalDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_remove_password))
                }
            }
        }
    }

    if (showPasswordDialog) {
        SetPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordSet = { newPassword ->
                val hash = SecurityUtils().hashPassword(newPassword)
                val currentSettings = settingsManager.getSettings()
                settingsManager.saveSettings(
                    currentSettings.copy(
                        passwordHash = hash,
                        usePasswordProtection = true
                    )
                )
                state.isPasswordSet = true
                state.usePasswordProtection = true
                showPasswordDialog = false
            }
        )
    }

    if (showConfirmRemovalDialog) {
        ConfirmPasswordDialog(
            passwordHash = initialSettings.passwordHash ?: "",
            onDismiss = { showConfirmRemovalDialog = false },
            onConfirmed = {
                val currentSettings = settingsManager.getSettings()
                settingsManager.saveSettings(
                    currentSettings.copy(
                        passwordHash = null,
                        usePasswordProtection = false
                    )
                )
                state.isPasswordSet = false
                state.usePasswordProtection = false
                showConfirmRemovalDialog = false
            }
        )
    }
}

private fun updateColumnPreference(
    columnPref: String,
    settingsManager: SettingsManager
) {
    val settings = settingsManager.getSettings()
    settingsManager.saveSettings(settings.copy(columnPreference = columnPref))
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