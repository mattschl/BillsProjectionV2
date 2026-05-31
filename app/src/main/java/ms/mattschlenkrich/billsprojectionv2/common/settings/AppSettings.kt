package ms.mattschlenkrich.billsprojectionv2.common.settings

data class AppSettings(
    val deviceId: Long,
    val fontSize: String? = "medium",
    val driveAccount: String? = null,
    val isFirstRun: Boolean = true,
    val themeMode: String? = "system",
    val passwordHash: String? = null,
    val usePasswordProtection: Boolean = false,
    val defaultAccount: String? = null
)