package ms.mattschlenkrich.billsprojectionv2.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager

@Composable
fun BillsProjectionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    fontScale: Float? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val settingsManager = remember { SettingsManager(context) }
    val settings = if (isPreview) null else settingsManager.getSettings()

    val actualFontScale = fontScale ?: if (isPreview) {
        1.0f
    } else {
        val fontSize = try {
            settings?.fontSize
        } catch (e: Exception) {
            Log.d("BillsProjectionTheme", "Using default font size", e)
            "normal"
        }
        when (fontSize) {
            "small" -> 0.8f
            "large" -> 1.2f
            "extra_large" -> 1.5f
            else -> 1.0f
        }
    }

    val actualDarkTheme = if (isPreview) {
        darkTheme
    } else {
        when (settings?.themeMode) {
            "light" -> false
            "dark" -> true
            else -> isSystemInDarkTheme()
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (actualDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        actualDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val currentDensity = LocalDensity.current
    val scaledDensity = Density(
        density = currentDensity.density * actualFontScale,
        fontScale = currentDensity.fontScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(1.0f),
            content = content
        )
    }
}