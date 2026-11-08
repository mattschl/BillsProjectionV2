package ms.mattschlenkrich.billsprojectionv2.ui.budgetRules.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalVisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRuleDetailed

@Composable
fun BudgetRuleChooseItem(
    budgetRuleDetailed: BudgetRuleDetailed,
    onClick: () -> Unit
) {
    val rule = budgetRuleDetailed.budgetRule!!
    val isDeleted = rule.budIsDeleted
    val containerColor = if (isDeleted) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isDeleted) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val vf = LocalVisualsFunctions.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val densityPref = androidx.compose.runtime.remember {
        ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager(context)
            .getSettings().listDensityPreference
    }
    val verticalPadding =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ListItemDensityHelper.getVerticalPadding(
            densityPref
        )
    val cardSpacing =
        ms.mattschlenkrich.billsprojectionv2.common.settings.ListItemDensityHelper.getCardVerticalSpacing(
            densityPref
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = cardSpacing)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = verticalPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rule.budgetRuleName + if (isDeleted) " " + stringResource(R.string.label_deleted_suffix) else "",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(10.dp, 8.dp)
                    .background(Color(vf.getRandomColorInt()))
            )
        }
    }
}

object BudgetRuleChooseItemDefaults {
    const val WIDTH_FACTOR = 2
}