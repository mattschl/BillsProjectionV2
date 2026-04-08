package ms.mattschlenkrich.billsprojectionv2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager

@Composable
fun BillsProjectionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    fontScale: Float? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = SettingsManager(context)
    val actualFontScale = fontScale ?: when (settingsManager.getSettings().fontSize) {
        "small" -> 0.8f
        "large" -> 1.2f
        "extra_large" -> 1.5f
        else -> 1.0f
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(actualFontScale),
        content = content
    )
}