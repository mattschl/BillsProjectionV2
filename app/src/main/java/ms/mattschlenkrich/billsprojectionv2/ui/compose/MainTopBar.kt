package ms.mattschlenkrich.billsprojectionv2.ui.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.components.ProjectFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onSyncClick: () -> Unit,
    onMenuItemClick: (Int) -> Unit,
) {
    var showMenu by remember { mutableStateOf(value = false) }
    var showManageBudgetSubMenu by remember { mutableStateOf(value = false) }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_go_back),
                        modifier = Modifier.size(ProjectFieldDefaults.iconSize()),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.label_more_options),
                    modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                    showManageBudgetSubMenu = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_manage_budget)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(ProjectFieldDefaults.iconSize())
                        )
                    },
                    onClick = {
                        showManageBudgetSubMenu = !showManageBudgetSubMenu
                    }
                )
                DropdownMenu(
                    expanded = showManageBudgetSubMenu,
                    onDismissRequest = { showManageBudgetSubMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_update_budget_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_update_predictions)
                            showManageBudgetSubMenu = false
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_budget_summary)) },
                        onClick = {
                            onMenuItemClick(R.id.action_view_summary)
                            showManageBudgetSubMenu = false
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_clear_future_predictions)) },
                        onClick = {
                            onMenuItemClick(R.id.action_delete_predictions)
                            showManageBudgetSubMenu = false
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.title_sync)) },
                    onClick = {
                        onSyncClick()
                        showMenu = false
                        showManageBudgetSubMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.nav_settings)) },
                    onClick = {
                        onMenuItemClick(R.id.action_settings)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_help)) },
                    onClick = {
                        onMenuItemClick(R.id.action_help)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_privacy_policy)) },
                    onClick = {
                        onMenuItemClick(R.id.action_privacy_policy)
                        showMenu = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.ic_bills_projection_background),
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black,
            navigationIconContentColor = Color.Black
        )
    )
}