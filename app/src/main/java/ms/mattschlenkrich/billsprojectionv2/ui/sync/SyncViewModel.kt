package ms.mattschlenkrich.billsprojectionv2.ui.sync

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.BuildConfig
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions

private const val TAG = "SyncViewModel"

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    var driveServiceHelper by mutableStateOf<DriveServiceHelper?>(null)
    var deviceId by mutableLongStateOf(0L)
    var progressMessage by mutableStateOf<String?>(null)
    var docContent by mutableStateOf(
        "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
                application.getString(R.string.sync_help_text),
    )

    private val df = DateFunctions()

    private var applyToAllChoice: ConflictChoice? = null

    var showConflictDialog by mutableStateOf<ConflictInfo?>(null)
    var showTransactionWarning by mutableStateOf(false)
    private var conflictDeferred: CompletableDeferred<ConflictChoice>? = null

    fun onConflictChoice(choice: ConflictChoice, applyToAll: Boolean) {
        if (applyToAll) {
            applyToAllChoice = choice
        }
        conflictDeferred?.complete(choice)
        showConflictDialog = null
    }

    fun queryDriveFiles() {
        progressMessage = "Querying Drive..."
        viewModelScope.launch {
            try {
                val helper = driveServiceHelper ?: return@launch
                val fileList = helper.queryFiles()
                val files = fileList.files ?: emptyList()

                val report = StringBuilder("Files in App Data Folder:\n")
                if (files.isEmpty()) {
                    report.append("No files found.")
                } else {
                    files.asSequence()
                        .filter {
                            (it.name.startsWith("bills2_") && it.name.endsWith(".db")) ||
                                    it.name.endsWith("-wal") ||
                                    it.name.endsWith("-shm")
                        }
                        .sortedByDescending { it.name }
                        .forEach { file ->
                            report.append("- ${file.name} (${file.size} bytes)\n")
                        }
                }
                docContent = report.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Query failed", e)
                docContent = "Query failed: ${e.message}"
            } finally {
                progressMessage = null
            }
        }
    }

    fun sync(onError: (String, Exception, (() -> Unit)) -> Unit) {
        progressMessage = "Synchronizing..."
        val helper = driveServiceHelper ?: return
        applyToAllChoice = null

        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = NumberFunctions(),
            onProgressUpdate = { progressMessage = it },
            onConflict = { info -> showConflictDialogWrapper(info) },
            onTransactionWarning = { showTransactionWarning = true }
        )

        viewModelScope.launch {
            try {
                val result = manager.performSync()
                docContent = result.second
                if (result.first == "Busy") {
                    android.widget.Toast.makeText(
                        getApplication(),
                        R.string.msg_sync_in_progress,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                onError("Sync failed", e) { sync(onError) }
            } finally {
                progressMessage = null
            }
        }
    }

    private suspend fun showConflictDialogWrapper(info: ConflictInfo): ConflictChoice {
        applyToAllChoice?.let { return it }
        val deferred = CompletableDeferred<ConflictChoice>()
        conflictDeferred = deferred
        showConflictDialog = info
        return deferred.await()
    }
}