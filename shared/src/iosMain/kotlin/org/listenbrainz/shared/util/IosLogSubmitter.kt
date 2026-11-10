package org.listenbrainz.shared.util

import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

class IosLogSubmitter(
    private val buildConfig: BuildInfo
) : LogSubmitter {
    private val sharedLog = platformLogWriter()

    override suspend fun submitLogs() {
        val zip = withContext(Dispatchers.IO) { zipLogs() } ?: return
        withContext(Dispatchers.Main) { share(zip) }
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

    @OptIn(ExperimentalForeignApi::class)
    private fun share(zip: NSURL) {
        val presenter = topViewController() ?: return
        val shareSheet = UIActivityViewController(
            activityItems = listOf("Please find the attached log files for ${buildConfig.applicationId}.", zip),
            applicationActivities = null
        )
        // iPad shows the sheet as a popover, which needs an anchor
        shareSheet.popoverPresentationController?.apply {
            sourceView = presenter.view
            presenter.view.bounds.useContents {
                sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
            }
        }
        presenter.presentViewController(shareSheet, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
        var controller = window?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}
