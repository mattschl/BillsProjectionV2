package ms.mattschlenkrich.billsprojectionv2.common.settings

data class AppSettings(
    val deviceId: Long,
    val fontSize: String? = "medium",
    val driveAccount: String? = null,
    val isFirstRun: Boolean = true
)