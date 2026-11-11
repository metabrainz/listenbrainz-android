package org.listenbrainz.shared.util

import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL

class IosLogSubmitter(
    private val buildConfig: BuildInfo
) : LogSubmitter {
    private val sharedLog = platformLogWriter()

    override suspend fun submitLogs() {
        val zip = withContext(Dispatchers.IO) { zipLogs() } ?: return
        withContext(Dispatchers.Main) {
            presentShareSheet(listOf("Please find the attached log files for ${buildConfig.applicationId}.", zip))
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun zipLogs(): NSURL? {
        val logDirectory = iosLogDirectory() ?: return null
        val fileManager = NSFileManager.defaultManager
        val hasLogs = fileManager.contentsOfDirectoryAtPath(logDirectory, error = null)
            ?.any { (it as? String)?.endsWith(".txt") == true } == true
        if (!hasLogs) {
            sharedLog.log(Severity.Warn, tag = "IosLogSubmitter", message = "No log files found in $logDirectory", throwable = null)
            return null
        }

        val zip = NSURL.fileURLWithPath(NSTemporaryDirectory() + "Log.zip")
        var isCopied = false
        // Reading a directory "for uploading" hands over a zip of it, valid only inside the block
        NSFileCoordinator(filePresenter = null).coordinateReadingItemAtURL(
            url = NSURL.fileURLWithPath(logDirectory, isDirectory = true),
            options = NSFileCoordinatorReadingForUploading,
            error = null
        ) { zipped ->
            if (zipped == null) return@coordinateReadingItemAtURL
            fileManager.removeItemAtURL(zip, error = null)
            isCopied = fileManager.copyItemAtURL(zipped, toURL = zip, error = null)
        }
        if (!isCopied) {
            sharedLog.log(Severity.Error, tag = "IosLogSubmitter", message = "Failed to zip logs", throwable = null)
        }
        return zip.takeIf { isCopied }
    }
}
