package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ProjectIntField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    ProjectTextField(
        value = value,
        onValueChange = {
            if (it.all { char -> char.isDigit() }) {
                onValueChange(it)
            }
        },
        label = label,
        modifier = modifier,
        textStyle = ProjectFieldDefaults.titleStyle().copy(
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}