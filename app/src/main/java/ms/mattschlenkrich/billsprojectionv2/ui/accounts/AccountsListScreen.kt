package ms.mattschlenkrich.billsprojectionv2.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectTextField
import ms.mattschlenkrich.billsprojectionv2.common.functions.VisualsFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType

@Composable
fun AccountsListScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    accountsWithType: List<AccountWithType>,
    onAddAccountClick: () -> Unit,
    onAccountClick: (AccountWithType) -> Unit,
    getAccountInfoText: (AccountWithType) -> String,
    showType: Boolean = true,
    vf: VisualsFunctions = VisualsFunctions()
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_a_new_account),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            ProjectTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = stringResource(R.string.search),
                placeholder = { Text(stringResource(R.string.enter_criteria)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (accountsWithType.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_accounts_to_view),
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(accountsWithType) { account ->
                            AccountListItem(
                                accountWithType = account,
                                onClick = onAccountClick,
                                getAccountInfoText = getAccountInfoText,
                                showType = showType,
                                vf = vf
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountChooseScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    accountsWithType: List<AccountWithType>,
    onAccountClick: (AccountWithType) -> Unit,
    onAddAccountClick: () -> Unit,
    vf: VisualsFunctions = VisualsFunctions()
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_a_new_account),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            ProjectTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = stringResource(R.string.search),
                placeholder = { Text(stringResource(R.string.enter_criteria)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (accountsWithType.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_accounts_to_view),
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalItemSpacing = 4.dp
                    ) {
                        items(accountsWithType) { account ->
                            AccountChooseItem(
                                accountWithType = account,
                                onClick = onAccountClick,
                                vf = vf
                            )
                        }
                    }
                }
            }
        }
    }
}