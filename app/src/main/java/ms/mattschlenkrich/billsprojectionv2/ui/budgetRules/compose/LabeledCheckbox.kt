package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults

@Composable
fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = ProjectFieldDefaults.labelStyle(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}