package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun TransactionViewList(
    transactions: List<TransactionDetailed>,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onTransactionLongClick: (TransactionDetailed) -> Unit = {},
    selectedItems: Set<Long> = emptySet(),
) {
    val haptic = LocalHapticFeedback.current
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            transactions,
            key = { it.transaction?.transId ?: it.hashCode() }
        ) { transactionDetailed ->
            TransactionHistoryItem(
                transactionDetailed = transactionDetailed,
                onClick = onTransactionClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTransactionLongClick(it)
                },
                isSelected = selectedItems.contains(transactionDetailed.transaction?.transId)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}