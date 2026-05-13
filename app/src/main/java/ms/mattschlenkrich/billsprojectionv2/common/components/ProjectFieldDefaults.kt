package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ProjectFieldDefaults {
    @Composable
    fun minHeight(): Dp {
        val fontSize = MaterialTheme.typography.bodyMedium.fontSize
        return with(LocalDensity.current) {
            fontSize.toDp() * 2f
        }
    }

    @Composable
    fun shape(): Shape = OutlinedTextFieldDefaults.shape

    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        errorTextColor = MaterialTheme.colorScheme.onSurface,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorBorderColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun contentPadding(): PaddingValues = OutlinedTextFieldDefaults.contentPadding(
        start = 8.dp,
        top = 4.dp,
        end = 8.dp,
        bottom = 4.dp,
    )

    @Composable
    fun textStyle(): TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun labelStyle(): TextStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun titleStyle(): TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}