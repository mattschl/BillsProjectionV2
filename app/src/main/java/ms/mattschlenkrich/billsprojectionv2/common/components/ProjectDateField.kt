package ms.mattschlenkrich.billsprojectionv2.common.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions

@Composable
fun ProjectDateField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }

    val showDatePicker = {
        val curDate = if (value.contains("-") && value.split("-").size == 3) {
            value
        } else {
            df.getCurrentDateAsString()
        }
        val curDateAll = curDate.split("-")
        val year = curDateAll.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = (curDateAll.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        val day = curDateAll.getOrNull(2)?.toIntOrNull() ?: 1

        DatePickerDialog(
            context,
            { _, pickedYear, monthOfYear, dayOfMonth ->
                val pickedMonth = monthOfYear + 1
                onValueChange(
                    "$pickedYear-${pickedMonth.toString().padStart(2, '0')}-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                )
            },
            year,
            month,
            day
        ).apply {
            setTitle(label)
            show()
        }
    }

    ProjectTextField(
        value = value,
        onValueChange = { input ->
            val formatted = formatDateInput(input)
            onValueChange(formatted)
        },
        modifier = modifier,
        label = label,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(onClick = { showDatePicker() }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = label,
                    modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                )
            }
        }
    )
}

private fun formatDateInput(input: String): String {
    val trimmed = input.trim()
    if (trimmed == "0" || trimmed == "0000-00-00") return "0"

    val digits = input.filter { it.isDigit() }.take(8)
    return when {
        digits.length <= 4 -> digits
        digits.length <= 6 -> "${digits.substring(0, 4)}-${digits.substring(4)}"
        else -> "${digits.substring(0, 4)}-${digits.substring(4, 6)}-${digits.substring(6)}"
    }
}