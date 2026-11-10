package org.listenbrainz.shared.util

import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.writeData
import platform.UIKit.UIDevice

internal const val IOS_LOG_DIR_NAME = "logs"

/** `Documents/logs`, created if missing. */
@OptIn(ExperimentalForeignApi::class)
internal fun iosLogDirectory(): String? {
    val documents = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).firstOrNull() as? String ?: return null
    val path = "$documents/$IOS_LOG_DIR_NAME"
    val isCreated = NSFileManager.defaultManager.createDirectoryAtPath(
        path,
        withIntermediateDirectories = true,
        attributes = null,
        error = null
    )
    return path.takeIf { isCreated }
}

class IosFileLogWriter(
    logDirectory: String,
    buildConfig: BuildInfo
) : SharedFileLogWriter(buildConfig) {
    private val logFilePath: String

    private val sharedLog = platformLogWriter()

    init {
        val timestamp = provideFormattedTime().replace(":", "-")
        logFilePath = "$logDirectory/$timestamp.txt"

        try {
            NSFileManager.defaultManager.createFileAtPath(logFilePath, contents = null, attributes = null)
            initBlock()
            collectStartupData()
        } catch (e: Exception) {
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message = "Failed to initialize file logging", throwable = e)
        }
    }

    override fun collectStartupData() {
        val device = UIDevice.currentDevice
        formatStartupData(
            startupData() + mapOf(
                "Device" to device.model,
                "Device OS" to "${device.systemName} ${device.systemVersion}"
            )
        )
    }

    @OptIn(BetaInteropApi::class)
    override suspend fun writeLineToFile(entry: String) = withContext(Dispatchers.IO) {
        try {
            val data = NSString.create(string = entry).dataUsingEncoding(NSUTF8StringEncoding)
                ?: return@withContext
            val handle = NSFileHandle.fileHandleForWritingAtPath(logFilePath) ?: return@withContext
            handle.seekToEndOfFile()
            handle.writeData(data)
            handle.closeFile()
        } catch (e: Exception) {
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message = "Failed to write to file", throwable = e)
        }
    }
}
