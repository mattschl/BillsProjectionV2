package ms.mattschlenkrich.billsprojectionv2.common.settings

import android.content.Context
import com.google.gson.Gson
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import java.io.File

class SettingsManager(private val context: Context) {
    private val fileName = "settings.json"
    private val gson = Gson()

    fun getSettings(): AppSettings {
        val file = File(context.filesDir, fileName)
        if (context.javaClass.name.contains("BridgeContext") || !file.exists()) {
            return AppSettings(deviceId = NumberFunctions().generateId())
        }
        return try {
            val jsonText = file.readText()
            val settings = gson.fromJson(jsonText, AppSettings::class.java)
            if (settings != null) {
                var updated = settings
                if (!jsonText.contains("syncWarningDays")) {
                    updated = updated.copy(syncWarningDays = 3)
                }
                if (!jsonText.contains("budgetUpdateWarningWeeks")) {
                    updated = updated.copy(budgetUpdateWarningWeeks = 3)
                }
                if (!jsonText.contains("columnPreference")) {
                    updated = updated.copy(columnPreference = "medium")
                }
                updated
            } else {
                AppSettings(deviceId = NumberFunctions().generateId())
            }
        } catch (_: Exception) {
            val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
            saveSettings(newSettings)
            newSettings
        }
    }

    fun saveSettings(settings: AppSettings) {
        if (context.javaClass.name.contains("BridgeContext")) {
            return
        }
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(settings))
    }
}