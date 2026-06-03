package org.listenbrainz.shared.util

import android.os.Build
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.getDefault


class AndroidFileLogWriter(
    logDirectory: String,
    private val buildConfig: BuildInfo,
) : LogWriter(), SharedFileLogWriter {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss",Locale.getDefault())

    private val loggerScope = CoroutineScope(Dispatchers.IO)
    private val loggerQueue = Channel<String>(capacity = Channel.UNLIMITED)
    private var logFile: File

    private val sharedLog = platformLogWriter()

    init {
        val timestamp = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", getDefault()).format(Date())
        logFile = File(logDirectory, "$timestamp.txt")

        try {
            logFile.parentFile?.mkdirs()

           loggerScope.launch {
                try {
                    FileWriter(logFile, true).use { writer ->
                        for (entry in loggerQueue) {
                            writer.write(entry)
                            writer.flush()
                        }
                    }
                } catch (e: Exception) {
                    sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to write to file", throwable =  e)
                }
            }
            collectStartupData()
        }catch (e:Exception){
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to initialize file logging", throwable = e)
        }
    }

    private fun collectStartupData() {
        val startupData = mapOf(
            "App Version" to System.currentTimeMillis().toString(),
            "Device Application Id" to buildConfig.applicationId,
            "Device Version Code" to buildConfig.versionCode.toString(),
            "Device Version Name" to buildConfig.versionName,
            "Device Build Type" to buildConfig.buildType,
            "Device" to Build.DEVICE,
            "Device SDK" to Build.VERSION.SDK_INT.toString(),
            "Device Manufacturer" to Build.MANUFACTURER
        )

        try {
            val timestamp = dateFormat.format(Date())
            val buildLog = buildString {
                append("Logger Started at $timestamp\n")
                startupData.forEach { (key, value) ->
                    append(" $key: $value\n")
                }
            }
            loggerQueue.trySend(buildLog)
        } catch (e: Exception) {
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to write startup data", throwable =  e)
        }
    }

    private fun writeToFile(
        message: String,
        tag: String = "ListenBrainz",
        severity: String = "INFO",
    ) {
        try {
            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [$severity] [$tag] $message\n"
            loggerQueue.trySend(logEntry)
        } catch (e: Exception) {
            sharedLog.log(Severity.Error,tag= "LogFileWriter", message = "Failed to write to file", throwable =  e)
        }
    }

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        val severityString = when(severity){
            Severity.Verbose -> "VERBOSE"
            Severity.Debug -> "DEBUG"
            Severity.Info -> "INFO"
            Severity.Warn -> "WARNING"
            Severity.Error,
            Severity.Assert -> "ERROR"
        }

        writeToFile(message,tag,severityString)
        throwable?.let {
            writeToFile(it.stackTraceToString(),tag,severityString)
        }
    }

}