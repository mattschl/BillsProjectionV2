package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun ProjectBalanceField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onIconClick: (() -> Unit)? = null
) {
    ProjectTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
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
}

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
        textStyle = MaterialTheme.typography.titleLarge.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
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
}