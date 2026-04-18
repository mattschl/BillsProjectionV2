package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object ProjectFieldDefaults {
    val MinHeight = 36.dp

    @Composable
    fun shape(): Shape = OutlinedTextFieldDefaults.shape

    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors()

    @Composable
    fun contentPadding(): PaddingValues = OutlinedTextFieldDefaults.contentPadding(
        start = 8.dp,
        top = 4.dp,
        end = 8.dp,
        bottom = 4.dp,
    )

    @Composable
    fun textStyle(): TextStyle = MaterialTheme.typography.bodyMedium

    @Composable
    fun labelStyle(): TextStyle = MaterialTheme.typography.labelMedium

    @Composable
    fun titleStyle(): TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold
    )
}