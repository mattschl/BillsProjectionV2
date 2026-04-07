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
        if (!file.exists()) {
            val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
            saveSettings(newSettings)
            return newSettings
        }
        return try {
            val settings = file.readText().let { gson.fromJson(it, AppSettings::class.java) }
            settings ?: AppSettings(deviceId = NumberFunctions().generateId())
        } catch (e: Exception) {
            val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
            saveSettings(newSettings)
            newSettings
        }
    }

    fun saveSettings(settings: AppSettings) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(settings))
    }
}