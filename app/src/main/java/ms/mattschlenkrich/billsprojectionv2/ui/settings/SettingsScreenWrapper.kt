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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.components.DropdownSelector
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
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

    LaunchedEffect(Unit) {
        mainActivity.topMenuBar.setTitle(R.string.nav_settings)
    }

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

@Composable
fun ConfirmPasswordDialog(
    passwordHash: String,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val incorrectPasswordMsg = stringResource(id = R.string.msg_error_incorrect_password)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_enter_password)) },
        text = {
            Column {
                ProjectTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = stringResource(R.string.label_enter_password),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val inputHash = SecurityUtils().hashPassword(password)
                    val failsafeHash = SecurityUtils().hashPassword(BuildConfig.FAILSAFE_PASSWORD)
                    if (inputHash == passwordHash || inputHash == failsafeHash) {
                        onConfirmed()
                    } else {
                        error = incorrectPasswordMsg
                    }
                }
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}


@Composable
fun SetPasswordDialog(
    onDismiss: () -> Unit,
    onPasswordSet: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val emptyError = stringResource(id = R.string.msg_error_password_empty)
    val mismatchError = stringResource(id = R.string.msg_error_passwords_mismatch)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_set_password)) },
        text = {
            Column {
                ProjectTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = stringResource(R.string.label_enter_new_password),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProjectTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        error = null
                    },
                    label = stringResource(R.string.label_confirm_new_password),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isEmpty()) {
                        error = emptyError
                    } else if (password != confirmPassword) {
                        error = mismatchError
                    } else {
                        onPasswordSet(password)
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
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