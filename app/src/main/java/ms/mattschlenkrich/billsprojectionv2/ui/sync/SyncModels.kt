package ms.mattschlenkrich.billsprojectionv2.ui.sync

data class ConflictInfo(
    val tableName: String,
    val name: String,
    val localId: Long,
    val localTime: String,
    val driveId: Long,
    val driveTime: String,
    val messageResId: Int? = null,
)

enum class ConflictChoice { KEEP_LOCAL, KEEP_DRIVE }