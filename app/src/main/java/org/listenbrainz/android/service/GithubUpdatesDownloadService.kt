package org.listenbrainz.android.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

const val TAG = "GHAppUpdatesDownload"

class GithubUpdatesDownloadService(
    private val context: Context
) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    var downloadId: Long? = null
    private var downloadBroadcastReceiver: BroadcastReceiver? = null

    fun downloadUpdate(
        fileName: String,
        downloadUrl: String,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ): Long? {
        try {
            val request = DownloadManager.Request(downloadUrl.toUri()).apply {
                setTitle("ListenBrainz App Update")
                setDescription("Downloading latest version...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(false)
                setMimeType("application/vnd.android.package-archive")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )
                } else {
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )
                }
            }
            downloadId = downloadManager.enqueue(request)
            downloadId?.let {
                registerDownloadBroadcastReceiver(
                    it,
                    onCompletedDownload,
                    onDownloadError
                )
            }
            Log.d(TAG, "Download started with ID: $downloadId")
            return downloadId
        } catch (e: Exception) {
            Log.d(TAG, "Error in starting download ${e.message}", e)
            onDownloadError("Failed to start download: ${e.message}")
            return null
        }
    }

    fun registerDownloadBroadcastReceiver(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ) {
        try {
            // Clean up any existing receiver first
            cleanUp()

            downloadBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d(TAG, "Broadcast received: ${intent?.action}")
                    Log.d(TAG, "Intent extras: ${intent?.extras}")

                    val receivedDownloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    Log.d(TAG, "Expected download ID: $downloadId, Received: $receivedDownloadId")

                    if (receivedDownloadId == downloadId) {
                        queryDownloadStatus(downloadId, onCompletedDownload, onDownloadError)
                    }
                }
            }

            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

            // Use application context and correct receiver flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    context.applicationContext,
                    downloadBroadcastReceiver,
                    intentFilter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                context.applicationContext.registerReceiver(downloadBroadcastReceiver, intentFilter)
            }

            Log.d(TAG, "Broadcast receiver registered for download ID: $downloadId")

        } catch (e: Exception) {
            Log.e(TAG, "Error registering receiver", e)
            onDownloadError("Error registering receiver: ${e.message}")
        }
    }

    private fun queryDownloadStatus(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        try {
            if (cursor.moveToFirst()) {
                val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusColumnIndex)

                Log.d(TAG, "Download status: $status")

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val uriColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val localUri = cursor.getString(uriColumnIndex)
                        Log.d(TAG, "Download completed: $localUri")

                        val fileUri = getFileUri(localUri)
                        onCompletedDownload(fileUri)
                        cleanUp()
                    }

                    DownloadManager.STATUS_FAILED -> {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(reasonIndex)
                        val errorMessage = getDownloadErrorMessage(reason)
                        Log.e(TAG, "Download failed: $errorMessage")
                        onDownloadError(errorMessage)
                        cleanUp()
                    }

                    else -> {
                        Log.d(TAG, "Download still in progress, status: $status")
                    }
                }
            } else {
                Log.w(TAG, "No data found for download ID: $downloadId")
            }
        } finally {
            cursor.close()
        }
    }

    fun getFileUri(localUri: String?): Uri? {
        return localUri?.let {
            val filePath = if (it.startsWith("file://")) {
                it.substring(7)
            } else {
                it
            }

            val file = File(filePath)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        }
    }

    fun isDownloadInProgress(downloadId: Long): Boolean {
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusColumn)
                cursor.close()
                return status == DownloadManager.STATUS_RUNNING ||
                        status == DownloadManager.STATUS_PENDING ||
                        status == DownloadManager.STATUS_PAUSED
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't fetch progress of the download $e", e)
        }
        return false
    }

    fun getDownloadProgress(downloadId: Long): Float {
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val downloadProgressColumnIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalDownloadSizeColumnIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val downloadProgress = cursor.getLong(downloadProgressColumnIndex)
                val totalDownloadSize = cursor.getLong(totalDownloadSizeColumnIndex)
                cursor.close()
                return if (totalDownloadSize > 0) (downloadProgress * 1f / totalDownloadSize) else 0f
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't fetch progress of the download $e", e)
        }
        return 0f
    }

    fun cancelDownload(downloadId: Long) {
        try {
            downloadManager.remove(downloadId)
            Log.d(TAG, "Download canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't cancel download")
        }
        cleanUp()
    }

    private fun cleanUp() {
        downloadBroadcastReceiver?.let {
            try {
                context.unregisterReceiver(it)
                Log.d(TAG, "Unregistered receiver")
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't unregister receiver", e)
            }
        }
    }

    private fun getDownloadErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error occurred"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed with error code: $reason"
        }
    }
}