package ms.mattschlenkrich.billsprojectionv2.common.sync

import android.accounts.Account
import android.database.sqlite.SQLiteDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNTS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_ACCOUNT_TYPES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_RULES
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_TRANSACTION
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.settings.SettingsManager
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions
import ms.mattschlenkrich.billsprojectionv2.databinding.ActivityNewBinding
import java.io.File
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account as AccountModel

private const val TAG: String = "NewActivity"

class NewActivity : AppCompatActivity() {

    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var mCurrentAccount: Account? = null
    private var pendingAction: (() -> Unit)? = null
    private var mDeviceId: Long = 0L

    private lateinit var binding: ActivityNewBinding
    private lateinit var credentialManager: CredentialManager
    private val nf = NumberFunctions()

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
        binding.syncButton.setOnClickListener { sync() }

        binding.returnButton.setOnClickListener {
            finish()
        }

        assignDeviceId()
        signInWithCredentialManager()
    }

    private fun assignDeviceId() {
        val settings = SettingsManager(this).getSettings()
        mDeviceId = settings.deviceId
        Log.d(TAG, "Assigned Device ID: $mDeviceId")
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

    fun sync() {
        showProgress("Synchronizing...")
        lifecycleScope.launch {
            var status = "Failed"
            var recordsProcessed = 0
            val syncTime = SimpleDateFormat(SQLITE_TIME, Locale.getDefault()).format(Date())
            try {
                val helper = mDriveServiceHelper ?: return@launch
                val targetFolderId = getTargetFolderId(helper)
                val fileList: FileList = helper.queryFiles(targetFolderId)

                val latestFile = fileList.files
                    ?.filter { it.name.startsWith("bills2_") && it.name.endsWith(".db") }
                    ?.maxByOrNull { it.name }

                if (latestFile == null) {
                    Toast.makeText(this@NewActivity, "No backups found", Toast.LENGTH_SHORT).show()
                    status = "No Backups"
                    return@launch
                }

                val tempFile = File(cacheDir, "temp_sync.db")
                showProgress("Downloading ${latestFile.name}...")
                helper.downloadBinaryFile(latestFile.name, tempFile, targetFolderId)

                showProgress("Comparing and updating records...")
                recordsProcessed = processSync(tempFile)
                tempFile.delete()

                status = "Success"
                Toast.makeText(
                    this@NewActivity,
                    "Sync complete: $recordsProcessed records updated",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                status = "Error: ${e.message}"
                handleError("Sync failed", e) { sync() }
            } finally {
                logSyncHistory(syncTime, status, recordsProcessed.toString())
                hideProgress()
            }
        }
    }

    private suspend fun processSync(backupFile: File): Int {
        return withContext(Dispatchers.IO) {
            var count = 0
            val backupDb = SQLiteDatabase.openDatabase(
                backupFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val appDb = BillsDatabase(this@NewActivity)

            // Sync Account Types
            backupDb.query(TABLE_ACCOUNT_TYPES, null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val item = AccountType(
                        typeId = cursor.getLong(cursor.getColumnIndexOrThrow("typeId")),
                        accountType = cursor.getString(cursor.getColumnIndexOrThrow("accountType")),
                        keepTotals = cursor.getInt(cursor.getColumnIndexOrThrow("keepTotals")) != 0,
                        isAsset = cursor.getInt(cursor.getColumnIndexOrThrow("isAsset")) != 0,
                        tallyOwing = cursor.getInt(cursor.getColumnIndexOrThrow("tallyOwing")) != 0,
                        keepMileage = cursor.getInt(cursor.getColumnIndexOrThrow("keepMileage")) != 0,
                        allowPending = cursor.getInt(cursor.getColumnIndexOrThrow("allowPending")) != 0,
                        displayAsAsset = cursor.getInt(cursor.getColumnIndexOrThrow("displayAsAsset")) != 0,
                        acctIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("acctIsDeleted")) != 0,
                        acctUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("acctUpdateTime"))
                    )
                    val existing = appDb.getAccountTypesDao().findAccountType(item.typeId)
                    if (existing.isEmpty() || existing[0].acctUpdateTime < item.acctUpdateTime) {
                        if (existing.isEmpty()) appDb.getAccountTypesDao().insertAccountType(item)
                        else appDb.getAccountTypesDao().updateAccountType(item)
                        count++
                    }
                }
            }

            // Sync Accounts
            backupDb.query(TABLE_ACCOUNTS, null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val item = AccountModel(
                        accountId = cursor.getLong(cursor.getColumnIndexOrThrow("accountId")),
                        accountName = cursor.getString(cursor.getColumnIndexOrThrow("accountName")),
                        accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("accountNumber")),
                        accountTypeId = cursor.getLong(cursor.getColumnIndexOrThrow("accountTypeId")),
                        accBudgetedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("accBudgetedAmount")),
                        accountBalance = cursor.getDouble(cursor.getColumnIndexOrThrow("accountBalance")),
                        accountOwing = cursor.getDouble(cursor.getColumnIndexOrThrow("accountOwing")),
                        accountCreditLimit = cursor.getDouble(cursor.getColumnIndexOrThrow("accountCreditLimit")),
                        accIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("accIsDeleted")) != 0,
                        accUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("accUpdateTime"))
                    )
                    val existing = appDb.getAccountDao().findAccount(item.accountId)
                    if (existing.isEmpty() || existing[0].accUpdateTime < item.accUpdateTime) {
                        if (existing.isEmpty()) appDb.getAccountDao().insertAccount(item)
                        else appDb.getAccountDao().updateAccount(item)
                        count++
                    }
                }
            }

            // Sync Budget Rules
            backupDb.query(TABLE_BUDGET_RULES, null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val item = BudgetRule(
                        ruleId = cursor.getLong(cursor.getColumnIndexOrThrow("ruleId")),
                        budgetRuleName = cursor.getString(cursor.getColumnIndexOrThrow("budgetRuleName")),
                        budToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("budToAccountId")),
                        budFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("budFromAccountId")),
                        budgetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("budgetAmount")),
                        budFixedAmount = cursor.getInt(cursor.getColumnIndexOrThrow("budFixedAmount")) != 0,
                        budIsPayDay = cursor.getInt(cursor.getColumnIndexOrThrow("budIsPayDay")) != 0,
                        budIsAutoPay = cursor.getInt(cursor.getColumnIndexOrThrow("budIsAutoPay")) != 0,
                        budStartDate = cursor.getString(cursor.getColumnIndexOrThrow("budStartDate")),
                        budEndDate = cursor.getString(cursor.getColumnIndexOrThrow("budEndDate")),
                        budDayOfWeekId = cursor.getInt(cursor.getColumnIndexOrThrow("budDayOfWeekId")),
                        budFrequencyTypeId = cursor.getInt(cursor.getColumnIndexOrThrow("budFrequencyTypeId")),
                        budFrequencyCount = cursor.getInt(cursor.getColumnIndexOrThrow("budFrequencyCount")),
                        budLeadDays = cursor.getInt(cursor.getColumnIndexOrThrow("budLeadDays")),
                        budIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("budIsDeleted")) != 0,
                        budUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("budUpdateTime"))
                    )
                    val existing = appDb.getBudgetRuleDao().getBudgetRule(item.ruleId)
                    if (existing == null || existing.budUpdateTime < item.budUpdateTime) {
                        if (existing == null) appDb.getBudgetRuleDao().insertBudgetRule(item)
                        else appDb.getBudgetRuleDao().updateBudgetRule(item)
                        count++
                    }
                }
            }

            // Sync Transactions
            backupDb.query(TABLE_TRANSACTION, null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val item = Transactions(
                        transId = cursor.getLong(cursor.getColumnIndexOrThrow("transId")),
                        transDate = cursor.getString(cursor.getColumnIndexOrThrow("transDate")),
                        transName = cursor.getString(cursor.getColumnIndexOrThrow("transName")),
                        transNote = cursor.getString(cursor.getColumnIndexOrThrow("transNote")),
                        transRuleId = cursor.getLong(cursor.getColumnIndexOrThrow("transRuleId")),
                        transToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("transToAccountId")),
                        transToAccountPending = cursor.getInt(cursor.getColumnIndexOrThrow("transToAccountPending")) != 0,
                        transFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("transFromAccountId")),
                        transFromAccountPending = cursor.getInt(cursor.getColumnIndexOrThrow("transFromAccountPending")) != 0,
                        transAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("transAmount")),
                        transIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("transIsDeleted")) != 0,
                        transUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("transUpdateTime"))
                    )
                    val existing = appDb.getTransactionDao().getTransaction(item.transId)
                    if (existing == null || existing.transUpdateTime < item.transUpdateTime) {
                        if (existing == null) appDb.getTransactionDao().insertTransaction(item)
                        else appDb.getTransactionDao().updateTransaction(item)
                        count++
                    }
                }
            }

            // Sync Budget Items
            backupDb.query(TABLE_BUDGET_ITEMS, null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val item = BudgetItem(
                        biRuleId = cursor.getLong(cursor.getColumnIndexOrThrow("biRuleId")),
                        biProjectedDate = cursor.getString(cursor.getColumnIndexOrThrow("biProjectedDate")),
                        biActualDate = cursor.getString(cursor.getColumnIndexOrThrow("biActualDate")),
                        biPayDay = cursor.getString(cursor.getColumnIndexOrThrow("biPayDay")),
                        biBudgetName = cursor.getString(cursor.getColumnIndexOrThrow("biBudgetName")),
                        biIsPayDayItem = cursor.getInt(cursor.getColumnIndexOrThrow("biIsPayDayItem")) != 0,
                        biToAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("biToAccountId")),
                        biFromAccountId = cursor.getLong(cursor.getColumnIndexOrThrow("biFromAccountId")),
                        biProjectedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("biProjectedAmount")),
                        biIsPending = cursor.getInt(cursor.getColumnIndexOrThrow("biIsPending")) != 0,
                        biIsFixed = cursor.getInt(cursor.getColumnIndexOrThrow("biIsFixed")) != 0,
                        biIsAutomatic = cursor.getInt(cursor.getColumnIndexOrThrow("biIsAutomatic")) != 0,
                        biManuallyEntered = cursor.getInt(cursor.getColumnIndexOrThrow("biManuallyEntered")) != 0,
                        biIsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("biIsCompleted")) != 0,
                        biIsCancelled = cursor.getInt(cursor.getColumnIndexOrThrow("biIsCancelled")) != 0,
                        biIsDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("biIsDeleted")) != 0,
                        biUpdateTime = cursor.getString(cursor.getColumnIndexOrThrow("biUpdateTime")),
                        biLocked = cursor.getInt(cursor.getColumnIndexOrThrow("biLocked")) != 0
                    )
                    val existing =
                        appDb.getBudgetItemDao().getBudgetItem(item.biRuleId, item.biProjectedDate)
                    if (existing == null || existing.biUpdateTime < item.biUpdateTime) {
                        if (existing == null) appDb.getBudgetItemDao().insertBudgetItem(item)
                        else appDb.getBudgetItemDao().updateBudgetItem(item)
                        count++
                    }
                }
            }

            backupDb.close()
            count
        }
    }

    private suspend fun logSyncHistory(time: String, status: String, records: String) {
        withContext(Dispatchers.IO) {
            try {
                val db = BillsDatabase(this@NewActivity)
                val syncHistory = SyncHistory(
                    syncId = nf.generateId(),
                    syncTime = time,
                    syncSourceName = "Google Drive",
                    syncDeviceId = mDeviceId,
                    syncStatus = status,
                    syncRecordsProcessed = records
                )
                db.getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log sync history", e)
            }
        }
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