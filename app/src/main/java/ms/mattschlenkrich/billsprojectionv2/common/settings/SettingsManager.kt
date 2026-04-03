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
            file.readText().let { gson.fromJson(it, AppSettings::class.java) }
        } catch (e: Exception) {
            val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
            saveSettings(newSettings)
            newSettings
        }
    }

    private fun saveSettings(settings: AppSettings) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(settings))
    }
}