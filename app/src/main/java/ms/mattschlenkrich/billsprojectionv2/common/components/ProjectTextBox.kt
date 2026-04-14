package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectTextBox(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = ProjectFieldDefaults.textStyle(),
    isError: Boolean = false,
    shape: Shape = ProjectFieldDefaults.shape(),
    readOnly: Boolean = true,
) {
    Box(
        modifier = modifier
    ) {
        ProjectTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            textStyle = textStyle,
            isError = isError,
            shape = shape,
            readOnly = readOnly
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        )
    }
}