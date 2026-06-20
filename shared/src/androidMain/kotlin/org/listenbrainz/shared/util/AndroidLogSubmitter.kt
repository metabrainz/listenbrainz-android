package org.listenbrainz.shared.util

import android.content.Intent
import androidx.core.content.FileProvider
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.repository.PlatformContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal const val ANDROID_LOG_DIR_NAME = "logs"

class AndroidLogSubmitter(
    private val context: PlatformContext,
    private val buildConfig: BuildInfo
) : LogSubmitter {
    private val sharedLog = platformLogWriter()

    override suspend fun submitLogs() = withContext(Dispatchers.IO) {
        val externalFilesDir = context.getExternalFilesDir(null) ?: return@withContext
        val logDir = File(externalFilesDir, ANDROID_LOG_DIR_NAME)

        val logFiles = logDir.listFiles { file -> file.isFile && file.extension == "txt" }
            ?.toList()
            .orEmpty()

        if (logFiles.isEmpty()) {
            sharedLog.log(
                Severity.Warn,
                tag = "AndroidLogSubmitter",
                message = "No log files found in ${logDir.absolutePath}",
                throwable = null
            )
            return@withContext
        }

        val downloadDir = File(externalFilesDir, "Download")
        downloadDir.mkdirs()
        val zipFile = File(downloadDir, "Log.zip")

        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                logFiles.forEach { file ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }

            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_SUBJECT, "Log Files")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("mobile@metabrainz.org"))
                    putExtra(Intent.EXTRA_TEXT, "Please find the attached log files.")

                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            context,
                            "${buildConfig.applicationId}.provider",
                            zipFile
                        )
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooserIntent = Intent.createChooser(intent,"Email logs...").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            sharedLog.log(Severity.Error, tag = "Error submitting logs", message =  "Utils", throwable =  e)
        }
    }

}