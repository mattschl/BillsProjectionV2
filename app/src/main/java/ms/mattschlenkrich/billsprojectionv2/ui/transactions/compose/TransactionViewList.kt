package ms.mattschlenkrich.billsprojectionv2.ui.transactions.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.common.settings.ColumnSettingsHelper
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.TransactionDetailed

@Composable
fun TransactionViewList(
    transactions: List<TransactionDetailed>,
    onTransactionClick: (TransactionDetailed) -> Unit,
    onTransactionLongClick: (TransactionDetailed) -> Unit = {},
    selectedItems: Set<Long> = emptySet(),
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnPreference = remember { SettingsManager(context).getSettings().columnPreference }
    val columnCount = ColumnSettingsHelper.calculateColumnCount(
        transactions.size,
        columnPreference,
        isLandscape
    )
    val haptic = LocalHapticFeedback.current

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 2.dp
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
                    onTransactionLongClick(transactionDetailed)
                },
                isSelected = selectedItems.contains(transactionDetailed.transaction?.transId)
            )
        }
    }
}