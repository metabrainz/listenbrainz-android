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

actual interface LogSubmitter {
    actual suspend fun submitLogs(context: PlatformContext)
}

class AndroidLogSubmitter(
    private val buildConfig: BuildInfo
) : LogSubmitter {
    private val sharedLog = platformLogWriter()

    override suspend fun submitLogs(context: PlatformContext) = withContext(Dispatchers.IO) {
        val logDir = context.getExternalFilesDir(null) ?: return@withContext
        val downloadDir = File(context.getExternalFilesDir(null), "Download")
        downloadDir.mkdirs()
        val zipFile = File(downloadDir, "Log.zip")

        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                logDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension == "txt") {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
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
                context.startActivity(Intent.createChooser(intent, "Email logs..."))
            }
        } catch (e: Exception) {
            sharedLog.log(Severity.Error, tag = "Error submitting logs", message =  "Utils", throwable =  e)
        }
    }

}