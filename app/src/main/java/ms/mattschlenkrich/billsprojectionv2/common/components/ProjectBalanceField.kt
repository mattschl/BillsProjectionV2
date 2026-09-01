package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun ProjectBalanceField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    isHighlighted: Boolean = false
) {
    ProjectTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        textStyle = if (isHighlighted) {
            MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        } else {
            ProjectFieldDefaults.titleStyle().copy(
                textAlign = TextAlign.Center,
            )
        },
        colors = if (isHighlighted) {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            )
        } else {
            ProjectFieldDefaults.colors()
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        trailingIcon = if (onIconClick != null) {
            {
                IconButton(onClick = onIconClick) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = stringResource(R.string.title_calculator),
                        modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                    )
                }
            }
        } else null
    )
}

/*
@Composable
fun ProjectBalanceField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onIconClick: (() -> Unit)? = null
) {
    ProjectTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        textStyle = ProjectFieldDefaults.titleStyle().copy(
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        trailingIcon = if (onIconClick != null) {
            {
                IconButton(onClick = onIconClick) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = stringResource(R.string.calculator)
                    )
                }
            }
        } else null
    )
}*/