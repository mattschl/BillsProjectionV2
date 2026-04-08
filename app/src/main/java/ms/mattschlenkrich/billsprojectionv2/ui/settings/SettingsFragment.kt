package ms.mattschlenkrich.billsprojectionv2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

class SettingsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsManager = SettingsManager(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    SettingsScreen()
                }
            }
        }
    }

    @Composable
    fun SettingsScreen() {
        val settings = remember { settingsManager.getSettings() }
        var selectedFontSize by remember { mutableStateOf(settings.fontSize ?: "medium") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(id = R.string.font_size),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp)
            )

            Column(modifier = Modifier.padding(top = 8.dp)) {
                FontSizeOption("small", R.string.small, selectedFontSize) {
                    updateFontSize("small")
                    selectedFontSize = "small"
                }
                FontSizeOption("medium", R.string.medium, selectedFontSize) {
                    updateFontSize("medium")
                    selectedFontSize = "medium"
                }
                FontSizeOption("large", R.string.large, selectedFontSize) {
                    updateFontSize("large")
                    selectedFontSize = "large"
                }
                FontSizeOption("extra_large", R.string.extra_large, selectedFontSize) {
                    updateFontSize("extra_large")
                    selectedFontSize = "extra_large"
                }
            }
        }
    }

    @Composable
    fun FontSizeOption(
        value: String,
        labelRes: Int,
        selectedFontSize: String,
        onSelect: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RadioButton(
                selected = (selectedFontSize == value),
                onClick = onSelect
            )
            Text(
                text = stringResource(id = labelRes),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    private fun updateFontSize(fontSize: String) {
        val settings = settingsManager.getSettings()
        settingsManager.saveSettings(settings.copy(fontSize = fontSize))
        requireActivity().recreate()
    }
}