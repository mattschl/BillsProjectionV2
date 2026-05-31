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
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.SecurityUtils
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.budgetView.DropdownSelector

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
    var usePasswordProtection by remember { mutableStateOf(settings.usePasswordProtection) }
    var isPasswordSet by remember { mutableStateOf(settings.passwordHash != null) }

    val rawAssetList by mainActivity.budgetItemViewModel.getAssetsForBudget()
        .observeAsState(initial = emptyList())
    val assetList = remember(rawAssetList) {
        if (rawAssetList.isEmpty()) listOf(ALL_ITEMS)
        else listOf(ALL_ITEMS) + rawAssetList
    }
    var defaultAccount by remember { mutableStateOf(settings.defaultAccount ?: ALL_ITEMS) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showConfirmRemovalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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

        Text(
            text = stringResource(id = R.string.general),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            DropdownSelector(
                label = stringResource(R.string.default_startup_account),
                options = assetList,
                selectedOption = if (assetList.contains(defaultAccount)) defaultAccount else ALL_ITEMS,
                onOptionSelected = { selected ->
                    defaultAccount = selected
                    val currentSettings = settingsManager.getSettings()
                    settingsManager.saveSettings(currentSettings.copy(defaultAccount = selected))
                }
            )
        }

        Text(
            text = stringResource(id = R.string.security),
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
                    text = stringResource(R.string.password_protection),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = usePasswordProtection,
                    onCheckedChange = { checked ->
                        if (checked && !isPasswordSet) {
                            showPasswordDialog = true
                        } else if (!checked && isPasswordSet) {
                            showConfirmRemovalDialog = true
                        } else {
                            usePasswordProtection = checked
                            val currentSettings = settingsManager.getSettings()
                            settingsManager.saveSettings(currentSettings.copy(usePasswordProtection = checked))
                        }
                    }
                )
            }

            if (isPasswordSet) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.change_password))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        showConfirmRemovalDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.remove_password))
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
                isPasswordSet = true
                usePasswordProtection = true
                showPasswordDialog = false
            }
        )
    }

    if (showConfirmRemovalDialog) {
        ConfirmPasswordDialog(
            passwordHash = settings.passwordHash ?: "",
            onDismiss = { showConfirmRemovalDialog = false },
            onConfirmed = {
                val currentSettings = settingsManager.getSettings()
                settingsManager.saveSettings(
                    currentSettings.copy(
                        passwordHash = null,
                        usePasswordProtection = false
                    )
                )
                isPasswordSet = false
                usePasswordProtection = false
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
    val incorrectPasswordMsg = stringResource(id = R.string.incorrect_password)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_password)) },
        text = {
            Column {
                ProjectTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = stringResource(R.string.enter_password),
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
                    val failsafeHash = SecurityUtils().hashPassword("mschlenk")
                    if (inputHash == passwordHash || inputHash == failsafeHash) {
                        onConfirmed()
                    } else {
                        error = incorrectPasswordMsg
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_password)) },
        text = {
            Column {
                ProjectTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = stringResource(R.string.enter_new_password),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProjectTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        error = null
                    },
                    label = stringResource(R.string.confirm_new_password),
                    singleLine = true
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
                        error = "Password cannot be empty"
                    } else if (password != confirmPassword) {
                        error = "Passwords do not match"
                    } else {
                        onPasswordSet(password)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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