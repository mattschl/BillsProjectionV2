package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalVisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed

@Composable
fun BudgetItemDisplay(
    budgetItemDetailed: BudgetItemDetailed,
    isCredit: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onLockClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val densityPref = remember {
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

    val nf = LocalNumberFunctions.current
    val df = LocalDateFunctions.current
    val vf = LocalVisualsFunctions.current
    val budgetItem = budgetItemDetailed.budgetItem ?: return
    val color = remember(budgetItem.biRuleId, budgetItem.biProjectedDate) {
        Color(vf.getRandomColorInt())
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = cardSpacing)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = verticalPadding)
                .alpha(
                    if (budgetItem.biIsCancelled || budgetItem.biIsCompleted) 0.5f
                    else 1.0f
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(color)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = df.getDisplayDate(budgetItem.biActualDate),
                    modifier = Modifier.weight(1.25f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = nf.displayDollars(budgetItem.biProjectedAmount),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Column(
                    modifier = Modifier.weight(2.5f)
                ) {
                    Text(
                        text = budgetItem.biBudgetName,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (budgetItem.biIsAutomatic) TextDecoration.Underline else null
                    )
                    Text(
                        text = "${budgetItemDetailed.fromAccount?.accountName ?: "Unknown"} -> ${budgetItemDetailed.toAccount?.accountName ?: "Unknown"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (budgetItem.biIsFixed || budgetItem.biIsAutomatic) {
                        Text(
                            text = buildAnnotatedString {
                                if (budgetItem.biIsFixed) {
                                    withStyle(style = SpanStyle(color = Color.Red)) {
                                        append("Fixed")
                                    }
                                } else {
                                    append("Variable")
                                }
                                if (budgetItem.biIsAutomatic) {
                                    append(", Automatic")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                if (onLockClick != null) {
                    Icon(
                        painter = painterResource(
                            id = if (budgetItem.biLocked) R.drawable.ic_locked_foreground
                            else R.drawable.ic_unlocked_foreground
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(ProjectFieldDefaults.iconSize() * 2f)
                            .clickable { onLockClick() }
                            .padding(2.dp),
                        tint = if (budgetItem.biLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}