package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ActionOption(
    val text: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

class ActionSheetState {
    var title by mutableStateOf("")
    var options by mutableStateOf(emptyList<ActionOption>())

    fun show(title: String, options: List<ActionOption>) {
        this.title = title
        this.options = options
    }

    fun dismiss() {
        this.options = emptyList()
        this.title = ""
    }

    fun isVisible(): Boolean = options.isNotEmpty()
}

@Composable
fun rememberActionSheetState(): ActionSheetState {
    return remember { ActionSheetState() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedActionBottomSheet(
    state: ActionSheetState,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    if (state.isVisible()) {
        ActionBottomSheet(
            title = state.title,
            options = state.options,
            sheetState = sheetState,
            onDismissRequest = { state.dismiss() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    title: String,
    options: List<ActionOption>,
    sheetState: SheetState,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Extra padding for navigation bar
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()
            options.filter { it.text.isNotEmpty() }.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            option.onClick()
                            onDismissRequest()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (option.icon != null) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )
                    }
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}