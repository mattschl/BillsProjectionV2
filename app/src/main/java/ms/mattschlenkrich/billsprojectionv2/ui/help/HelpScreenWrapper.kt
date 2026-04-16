package ms.mattschlenkrich.billsprojectionv2.ui.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R

@Composable
fun HelpScreenWrapper() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.help_text),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        HelpSection(
            titleRes = R.string.general,
            contentRes = R.string.instructions_general
        )
        HelpSection(
            titleRes = R.string.budget_view_help,
            contentRes = R.string.instructions_budget_view
        )
        HelpSection(
            titleRes = R.string.transaction_view_help,
            contentRes = R.string.instructions_transaction_view
        )
        HelpSection(
            titleRes = R.string.analysis_view_help,
            contentRes = R.string.instructions_transactions_analysis_view
        )
        HelpSection(
            titleRes = R.string.accounts_help,
            contentRes = R.string.instruction_accounts
        )
        HelpSection(
            titleRes = R.string.budget_rule_help,
            contentRes = R.string.instructions_budget_rules
        )
        HelpSection(
            titleRes = R.string.current_budget_summary,
            contentRes = R.string.instructions_budget_list
        )
    }
}

@Composable
fun HelpSection(titleRes: Int, contentRes: Int) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            text = stringResource(id = contentRes),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Start
        )
    }
}