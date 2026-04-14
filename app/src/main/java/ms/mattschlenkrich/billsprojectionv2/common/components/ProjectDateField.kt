package ms.mattschlenkrich.billsprojectionv2.common.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions

@Composable
fun ProjectDateField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val df = DateFunctions()

    Box(modifier = modifier) {
        ProjectTextField(
            value = value,
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = label
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    val curDate = if (value.contains("-")) value else df.getCurrentDateAsString()
                    val curDateAll = curDate.split("-")
                    DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val month = monthOfYear + 1
                            onValueChange(
                                "$year-${month.toString().padStart(2, '0')}-${
                                    dayOfMonth.toString().padStart(2, '0')
                                }"
                            )
                        },
                        curDateAll[0].toInt(),
                        curDateAll[1].toInt() - 1,
                        curDateAll[2].toInt()
                    ).apply {
                        setTitle(label)
                        show()
                    }
                }
        )
    }
}