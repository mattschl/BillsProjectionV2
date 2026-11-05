package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalDateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalVisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun PendingItem(
    pending: TransactionDetailed,
    selectedAsset: String,
    assetList: List<String>,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onLongClick: () -> Unit = {},
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

    val df = LocalDateFunctions.current
    val nf = LocalNumberFunctions.current
    val vf = LocalVisualsFunctions.current
    val haptic = LocalHapticFeedback.current
    val color = remember { Color(vf.getRandomColorInt()) }
    val trans = pending.transaction ?: return

    val isCredit = when {
        pending.toAccount?.accountName == selectedAsset -> true
        pending.fromAccount?.accountName == selectedAsset -> false
        selectedAsset == ALL_ITEMS -> assetList.contains(pending.toAccount?.accountName)
        else -> false
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
                onClick = { onTransactionClick(pending) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 24.dp)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = df.getDisplayDate(trans.transDate),
                modifier = Modifier.padding(end = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = nf.displayDollars(trans.transAmount),
                modifier = Modifier.padding(end = 8.dp),
                fontWeight = FontWeight.Bold,
                color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trans.transName + if (trans.transNote.isNotBlank()) " - ${trans.transNote}" else "",
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                val fromText =
                    if (trans.transFromAccountPending) pending.fromAccount?.accountName
                        ?: "Unknown" else ""
                val toText =
                    if (trans.transToAccountPending) pending.toAccount?.accountName
                        ?: "Unknown" else ""

                val accountDisplay = if (fromText.isNotBlank() && toText.isNotBlank()) {
                    "$fromText -> $toText"
                } else if (fromText.isNotBlank()) {
                    "From: $fromText"
                } else if (toText.isNotBlank()) {
                    "To: $toText"
                } else {
                    ""
                }

                if (accountDisplay.isNotBlank()) {
                    Text(
                        text = accountDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}