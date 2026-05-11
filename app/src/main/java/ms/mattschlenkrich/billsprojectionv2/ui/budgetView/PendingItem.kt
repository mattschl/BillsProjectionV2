package ms.mattschlenkrich.billsprojectionv2.ui.budgetView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.common.ALL_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun PendingItem(
    pending: TransactionDetailed,
    selectedAsset: String,
    assetList: List<String>,
    onTransactionClick: (TransactionDetailed) -> Unit,
    df: DateFunctions,
    nf: NumberFunctions,
) {
    val vf = VisualsFunctions()
    val color = remember { Color(vf.getRandomColorInt()) }
    val isCredit = if (pending.toAccount?.accountName == selectedAsset) true
    else if (pending.fromAccount?.accountName == selectedAsset) false
    else if (selectedAsset == ALL_ITEMS) {
        assetList.contains(pending.toAccount?.accountName)
    } else false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTransactionClick(pending) }
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 5.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = df.getDisplayDate(pending.transaction!!.transDate),
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = nf.displayDollars(pending.transaction.transAmount),
            modifier = Modifier.width(90.dp),
            fontWeight = FontWeight.Bold,
            color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = pending.transaction.transName + if (pending.transaction.transNote.isNotBlank()) " - ${pending.transaction.transNote}" else "",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = if (isCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}