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
                if (!jsonText.contains("syncWarningDays")) {
                    settings.copy(syncWarningDays = 3)
                } else {
                    settings
                }
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