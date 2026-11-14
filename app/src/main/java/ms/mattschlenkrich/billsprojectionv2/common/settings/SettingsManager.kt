package ms.mattschlenkrich.billsprojectionv2.common.settings

import android.content.Context
import com.google.gson.Gson
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import java.io.File

class SettingsManager(context: Context) {
    private val appContext = context.applicationContext ?: context
    private val fileName = "settings.json"
    private val gson = Gson()

    companion object {
        @Volatile
        private var cachedSettings: AppSettings? = null
        private val lock = Any()
    }

    fun getSettings(): AppSettings {
        synchronized(lock) {
            cachedSettings?.let { return it }

            if (appContext.javaClass.name.contains("BridgeContext")) {
                val previewSettings = AppSettings(deviceId = 1L)
                cachedSettings = previewSettings
                return previewSettings
            }

            val file = File(appContext.filesDir, fileName)
            if (!file.exists()) {
                val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
                saveSettingsInternal(newSettings, file)
                cachedSettings = newSettings
                return newSettings
            }

            return try {
                val jsonText = file.readText()
                val settings = gson.fromJson(jsonText, AppSettings::class.java)
                if (settings != null) {
                    var updated = settings
                    var needsSave = false
                    if (!jsonText.contains("syncWarningDays")) {
                        updated = updated.copy(syncWarningDays = 3)
                        needsSave = true
                    }
                    if (!jsonText.contains("budgetUpdateWarningWeeks")) {
                        updated = updated.copy(budgetUpdateWarningWeeks = 3)
                        needsSave = true
                    }
                    if (!jsonText.contains("columnPreference")) {
                        updated = updated.copy(columnPreference = "medium")
                        needsSave = true
                    }
                    if (!jsonText.contains("listDensityPreference")) {
                        updated = updated.copy(listDensityPreference = "medium")
                        needsSave = true
                    }
                    if (needsSave) {
                        saveSettingsInternal(updated, file)
                    }
                    cachedSettings = updated
                    updated
                } else {
                    val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
                    saveSettingsInternal(newSettings, file)
                    cachedSettings = newSettings
                    newSettings
                }
            } catch (_: Exception) {
                val newSettings = AppSettings(deviceId = NumberFunctions().generateId())
                saveSettingsInternal(newSettings, file)
                cachedSettings = newSettings
                newSettings
            }
        }
    }

    fun saveSettings(settings: AppSettings) {
        synchronized(lock) {
            cachedSettings = settings
            if (appContext.javaClass.name.contains("BridgeContext")) {
                return
            }
            val file = File(appContext.filesDir, fileName)
            saveSettingsInternal(settings, file)
        }
    }

    private fun saveSettingsInternal(settings: AppSettings, targetFile: File) {
        try {
            val json = gson.toJson(settings)
            val tempFile = File(appContext.filesDir, "$fileName.tmp")
            tempFile.writeText(json)
            if (tempFile.exists()) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            }
        } catch (_: Exception) {
            try {
                targetFile.writeText(gson.toJson(settings))
            } catch (_: Exception) {
            }
        }
    }

    fun getColumnCountForList(widthFactor: Int, isLandscape: Boolean = false): Int {
        val preference = getSettings().columnPreference
        return ColumnSettingsHelper.calculateColumnCount(
            widthFactor = widthFactor,
            columnPreference = preference,
            isLandscape = isLandscape
        )
    }
}