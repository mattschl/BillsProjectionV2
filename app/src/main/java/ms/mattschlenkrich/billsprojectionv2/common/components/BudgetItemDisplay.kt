package ms.mattschlenkrich.billsprojectionv2.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItemDetailed

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetItemDisplay(
    budgetItemDetailed: BudgetItemDetailed,
    isCredit: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onLockClick: (() -> Unit)? = null,
) {
    val nf = NumberFunctions()
    val df = DateFunctions()
    val vf = VisualsFunctions()
    val budgetItem = budgetItemDetailed.budgetItem!!
    val color = remember(budgetItem.biRuleId, budgetItem.biProjectedDate) {
        Color(vf.getRandomColorInt())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center

    ) {
        Box(
            modifier = Modifier
                .size(width = 250.dp, height = 2.dp)
                .background(color)
                .padding(vertical = 1.dp),
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 1.dp),
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
            color = if (isCredit) Color.Black else Color.Red,
            style = MaterialTheme.typography.bodySmall
        )
        Column(
            modifier = Modifier.weight(2.5f)
        ) {
            Text(
                text = budgetItem.biBudgetName,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) Color.Black else Color.Red,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Text(
                text = "${budgetItemDetailed.fromAccount?.accountName ?: "Unknown"} -> ${budgetItemDetailed.toAccount?.accountName ?: "Unknown"}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 2
            )
            if (budgetItem.biIsFixed || budgetItem.biIsAutomatic) {
                Text(
                    text = (if (budgetItem.biIsFixed) "Fixed" else "Variable") +
                            (if (budgetItem.biIsAutomatic) ", Automatic" else ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    maxLines = 1
                )
            }

        }
        if (onLockClick != null) {
            Icon(
                painter = painterResource(
                    id = if (budgetItem.biLocked) R.drawable.ic_liocked_foreground
                    else R.drawable.ic_unlocked_foreground
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onLockClick() }
                    .padding(4.dp),
                tint = if (budgetItem.biLocked) Color.Red else Color.Black
            )
        }
    }
}