package ms.mattschlenkrich.billsprojectionv2.common.sync

import android.accounts.Account
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme
import java.security.SecureRandom

private const val TAG: String = "SyncActivity"

class SyncActivity : ComponentActivity() {

    private val viewModel: SyncViewModel by viewModels()
    private var mCurrentAccount: Account? = null
    private var pendingAction: (() -> Unit)? = null

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)

        val settings = SettingsManager(this).getSettings()
        viewModel.deviceId = settings.deviceId

        lifecycleScope.launch {
            delay(200) // Small delay to ensure Credential Manager is ready
            settings.driveAccount?.let {
                initializeDriveService(it)
            } ?: run {
                signInWithCredentialManager()
            }
        }

        setContent {
            val settings = remember { SettingsManager(this).getSettings() }
            val fontScale = when (settings.fontSize) {
                "small" -> 0.8f
                "large" -> 1.2f
                "extra_large" -> 1.5f
                else -> 1.0f
            }
            BillsProjectionTheme(fontScale = fontScale) {
                SyncScreen(
                    viewModel = viewModel,
                    onBack = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onConnect = { signInWithCredentialManager() },
                    onSync = { viewModel.sync(::handleError) },
                    onQuery = { viewModel.queryDriveFiles() }
                )
            }
        }
    }

    private fun handleError(message: String, e: Exception, action: (() -> Unit)) {
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
                viewModel.driveServiceHelper = null
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
                val result = credentialManager.getCredential(this@SyncActivity, request)
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
            viewModel.driveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account

            // Save the successful account
            val settingsManager = SettingsManager(this)
            val settings = settingsManager.getSettings()
            if (settings.driveAccount != email) {
                settingsManager.saveSettings(settings.copy(driveAccount = email))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Drive init failed", e)
        }
    }

    private fun generateNonce() = Base64.encodeToString(
        ByteArray(16).apply { SecureRandom().nextBytes(this) },
        Base64.URL_SAFE
    )

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}