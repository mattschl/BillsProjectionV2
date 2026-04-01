package ms.mattschlenkrich.billsprojectionv2.common.sync

import android.accounts.Account
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityNewBinding
import java.io.File
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG: String = "NewActivity"

class NewActivity : AppCompatActivity() {

    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var mCurrentAccount: Account? = null
    private var pendingAction: (() -> Unit)? = null

    private lateinit var binding: ActivityNewBinding
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        binding.queryBtn.setOnClickListener { query() }
        binding.upButton.setOnClickListener { testUpload() }
        binding.downButton.setOnClickListener { testDownload() }
        binding.updateButton.setOnClickListener { testUpdate() }

        binding.returnButton.setOnClickListener {
            finish()
        }

        signInWithCredentialManager()
    }

    private fun showProgress(message: String) {
        binding.progressText.text = message
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressOverlay.visibility = View.GONE
    }

    private suspend fun getTargetFolderId(helper: DriveServiceHelper): String {
        val appsFolderId = helper.getOrCreateFolder("Apps")
        val appName = getString(R.string.app_name)
        return helper.getOrCreateFolder(appName, appsFolderId)
    }

    fun testDownload() {
        showProgress("Searching for latest backup...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val fileList: FileList = helper.queryFiles(targetFolderId)

                val latestFile = fileList.files
                    ?.filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    ?.maxByOrNull { it.name }

                if (latestFile == null) {
                    Toast.makeText(
                        this@NewActivity,
                        "No backups found on Drive",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val localFile = File(applicationInfo.dataDir, "databases/${latestFile.name}")
                if (localFile.exists()) {
                    Toast.makeText(
                        this@NewActivity,
                        "File already exists locally.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                localFile.parentFile?.mkdirs()
                showProgress("Downloading ${latestFile.name}...")
                helper.downloadBinaryFile(latestFile.name, localFile, targetFolderId)

                Toast.makeText(
                    this@NewActivity,
                    "Downloaded: ${latestFile.name}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                handleError("Download failed", e) { testDownload() }
            } finally {
                hideProgress()
            }
        }
    }

    fun testUpload() {
        showProgress("Uploading database...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val dbFile = File(applicationInfo.dataDir, "databases/bills2.db")

                if (!dbFile.exists()) throw FileNotFoundException("Database not found")

                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val driveFileName = "bills2_$timestamp.db"

                helper.uploadFile(dbFile, "application/vnd.sqlite3", driveFileName, targetFolderId)
                Toast.makeText(
                    this@NewActivity,
                    "Upload successful: $driveFileName",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                handleError("Upload failed", e) { testUpload() }
            } finally {
                hideProgress()
            }
        }
    }

    fun testUpdate() {
        showProgress("Cleaning up old backups...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)

                val driveFileList = helper.queryFiles(targetFolderId)
                val driveBackups = driveFileList.files
                    ?.filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (driveBackups.size > 3) {
                    driveBackups.drop(3).forEach { helper.deleteFile(it.id) }
                }

                hideProgress()
                testDownload()
            } catch (e: Exception) {
                handleError("Update failed", e) { testUpdate() }
            } finally {
                hideProgress()
            }
        }
    }

    private fun handleError(message: String, e: Exception, action: (() -> Unit)? = null) {
        Log.e(TAG, message, e)
        if (e is UserRecoverableAuthIOException) {
            pendingAction = action
            recoverAuthLauncher.launch(e.intent)
        } else {
            val errMsg = if (e.message?.contains("verification process") == true) {
                "Access Blocked: Add your email as a 'Test User' in Google Cloud Console."
            } else {
                "$message: ${e.message}"
            }
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
        }
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mCurrentAccount?.name?.let {
                mDriveServiceHelper = null
                initializeDriveService(it)
                pendingAction?.invoke()
            }
        }
    }

    private fun signInWithCredentialManager() {
        lifecycleScope.launch {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            try {
                val result = credentialManager.getCredential(this@NewActivity, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                initializeDriveService(googleIdTokenCredential.id)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
            }
        }
    }

    private fun initializeDriveService(email: String) {
        val account = Account(email, "com.google")
        try {
            val credential = GoogleAccountCredential.usingOAuth2(this, DRIVE_SCOPES)
            credential.selectedAccount = account
            val googleDriveService = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
            mDriveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
        } catch (e: Exception) {
            Log.e(TAG, "Drive init failed", e)
        }
    }

    private fun query() {
        val helper = mDriveServiceHelper ?: return
        showProgress("Querying...")
        lifecycleScope.launch {
            try {
                val targetFolderId = getTargetFolderId(helper)
                val fileList = helper.queryFiles(targetFolderId)
                val names = fileList.files.joinToString("\n") { it.name }
                binding.docContentEdittext.setText(names)
            } catch (e: Exception) {
                handleError("Query failed", e) { query() }
            } finally {
                hideProgress()
            }
        }
    }

    private fun generateNonce() = Base64.encodeToString(
        ByteArray(16).apply { SecureRandom().nextBytes(this) },
        Base64.URL_SAFE
    )

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_FILE)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}