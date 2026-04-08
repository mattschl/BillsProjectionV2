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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

private const val TAG: String = "NewActivity"

class NewActivity : ComponentActivity() {

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
            delay(500) // Small delay to ensure Credential Manager is ready
            signInWithCredentialManager()
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
                    onSync = { viewModel.sync(::handleError) }
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
            viewModel.driveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
        } catch (e: Exception) {
            Log.e(TAG, "Drive init failed", e)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSync: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.docContent,
                    onValueChange = { viewModel.docContent = it },
                    label = { Text(stringResource(R.string.document_content)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.driveServiceHelper == null) {
                        Button(onClick = onConnect) { Text(stringResource(R.string.connect_to_drive)) }
                    } else {
                        Button(onClick = onSync) { Text(stringResource(R.string.sync)) }
                    }
                    Button(onClick = onBack) { Text(stringResource(R.string.return_button)) }
                }
            }

            if (viewModel.progressMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = viewModel.progressMessage ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            viewModel.showConflictDialog?.let { info ->
                ConflictDialog(
                    info = info,
                    onChoice = { viewModel.onConflictChoice(it) }
                )
            }
        }
    }
}

@Composable
fun ConflictDialog(
    info: SyncViewModel.ConflictInfo,
    onChoice: (SyncViewModel.ConflictChoice) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text("Sync Conflict") },
        text = {
            Text(
                stringResource(
                    R.string.sync_conflict_message,
                    info.tableName,
                    info.name,
                    info.localId,
                    info.localTime,
                    info.driveId,
                    info.driveTime
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onChoice(SyncViewModel.ConflictChoice.KEEP_LOCAL) }) {
                Text("Keep Local")
            }
        },
        dismissButton = {
            TextButton(onClick = { onChoice(SyncViewModel.ConflictChoice.KEEP_DRIVE) }) {
                Text("Keep Drive")
            }
        }
    )
}