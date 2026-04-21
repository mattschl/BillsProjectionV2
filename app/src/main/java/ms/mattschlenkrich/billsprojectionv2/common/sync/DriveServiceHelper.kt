package ms.mattschlenkrich.billsprojectionv2.common.sync

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import com.google.api.services.drive.model.File as DriveFile

/**
 * A utility for performing read/write operations on Drive files via the REST API.
 */
class DriveServiceHelper(private val mDriveService: Drive) {

    /**
     * Uploads a local file to Google Drive's app-specific (hidden) storage.
     */
    suspend fun uploadFile(
        localFile: File,
        mimeType: String,
        driveFileName: String
    ): String = withContext(Dispatchers.IO) {
        val metadata = DriveFile()
            .setName(driveFileName)
            .setMimeType(mimeType)
            .setParents(Collections.singletonList("appDataFolder"))

        val mediaContent = FileContent(mimeType, localFile)

        val googleFile = mDriveService.files().create(metadata, mediaContent)
            .set("spaces", "appDataFolder")
            .setFields("id")
            .execute() ?: throw IOException("Null result when uploading file.")

        googleFile.id
    }

    /**
     * Finds a file ID on Google Drive by its name.
     */
    suspend fun findFileIdByName(fileName: String): String? =
        withContext(Dispatchers.IO) {
            val query = "name = '$fileName' and 'appDataFolder' in parents and trashed = false"

            val result = mDriveService.files().list()
                .setQ(query)
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (files.isNullOrEmpty()) null else files[0].id
        }

    /**
     * Downloads a file from Google Drive to a local file.
     */
    suspend fun downloadBinaryFile(fileName: String, targetFile: File) =
        withContext(Dispatchers.IO) {
            val fileId = findFileIdByName(fileName)
                ?: throw IOException("File not found on Drive: $fileName")

            FileOutputStream(targetFile).use { outputStream ->
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }
        }

    /**
     * Deletes a file from Google Drive.
     */
    suspend fun deleteFile(fileId: String) = withContext(Dispatchers.IO) {
        mDriveService.files().delete(fileId).execute()
    }

    /**
     * Returns a [FileList] containing files in the appDataFolder.
     * Explicitly requests 'id', 'name', 'modifiedTime', and 'size' fields to ensure they are available in the result.
     */
    suspend fun queryFiles(): FileList = withContext(Dispatchers.IO) {
        val listRequest = mDriveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("'appDataFolder' in parents and trashed = false")
            .setFields("files(id, name, modifiedTime, size)")

        listRequest.execute()
    }

}