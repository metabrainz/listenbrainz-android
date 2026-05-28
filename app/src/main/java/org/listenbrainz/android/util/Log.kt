package org.listenbrainz.android.util

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.shared.util.Log as sharedLog
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.getDefault


object Log {

    private var isInitialized = false
    private lateinit var logFile: File
    private val dateFormat by lazy { SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", getDefault()) }

    private val loggerScope = CoroutineScope(Dispatchers.IO)

    private val loggerQueue = Channel<String>(capacity = Channel.UNLIMITED)

    fun init(context: Context, logDirectory: String) {
        if (isInitialized) return

        try {

            val timestamp = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", getDefault()).format(Date())
            logFile = File(logDirectory, "$timestamp.txt")

            logFile.parentFile?.mkdirs()


            isInitialized = true

            loggerScope.launch {
                try {
                    FileWriter(logFile,true).use { writer->
                        for(entry in loggerQueue){
                            writer.write(entry)
                            writer.flush()
                        }
                    }
                }catch (e: Exception){
                    sharedLog.e("LogFileWriter","Failed to write to file", e)
                }
            }
            collectStartupData()

        } catch (e: Exception) {
            sharedLog.e("LogFileWriter", "Failed to initialize file logging", e)
        }
    }

    private fun collectStartupData() {
        val startupData = mapOf(
            "App Version" to System.currentTimeMillis().toString(),
            "Device Application Id" to BuildConfig.APPLICATION_ID,
            "Device Version Code" to BuildConfig.VERSION_CODE.toString(),
            "Device Version Name" to BuildConfig.VERSION_NAME,
            "Device Build Type" to BuildConfig.BUILD_TYPE,
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
            sharedLog.e("LogFileWriter", "Failed to write startup data", e)
        }
    }

    private fun writeToFile(
        message: String,
        tag: String = "ListenBrainz",
        severity: String = "INFO",
    ) {
        if (!isInitialized) return

        try {
            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [$severity] [$tag] $message\n"
            loggerQueue.trySend(logEntry)
        } catch (e: Exception) {
            sharedLog.e("LogFileWriter", "Failed to write to file", e)
        }
    }

    fun e(message: Any?, tag: String? = null, throwable: Throwable? = null) {
        sharedLog.e(message, tag, throwable)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "ERROR")
        throwable?.let {
            writeToFile("Stack trace: ${it.stackTraceToString()}", logTag, "ERROR")
        }
    }

    fun d(message: Any?, tag: String? = null) {
        sharedLog.d(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "DEBUG")
    }

    fun i(message: Any?, tag: String? = null) {
        sharedLog.i(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "INFO")
    }

    fun w(message: Any?, tag: String? = null) {
        sharedLog.w(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "WARNING")
    }

    fun v(message: Any?, tag: String? = null) {
        sharedLog.v(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "VERBOSE")
    }


}