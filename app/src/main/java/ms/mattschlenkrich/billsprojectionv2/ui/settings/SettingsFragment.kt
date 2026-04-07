package ms.mattschlenkrich.billsprojectionv2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        settingsManager = SettingsManager(requireContext())
        setupFontSizeSelection()
        return binding.root
    }

    private fun setupFontSizeSelection() {
        val settings = settingsManager.getSettings()
        val savedFontSize = settings.fontSize ?: "medium"

        when (savedFontSize) {
            "small" -> binding.rbSmall.isChecked = true
            "large" -> binding.rbLarge.isChecked = true
            else -> binding.rbMedium.isChecked = true
        }

        binding.rgFontSize.setOnCheckedChangeListener { _, checkedId ->
            val fontSize = when (checkedId) {
                R.id.rbSmall -> "small"
                R.id.rbLarge -> "large"
                else -> "medium"
            }
            settingsManager.saveSettings(settings.copy(fontSize = fontSize))
            requireActivity().recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}