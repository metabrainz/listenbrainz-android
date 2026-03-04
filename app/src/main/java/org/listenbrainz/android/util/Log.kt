package org.listenbrainz.android.util

import android.content.Context
import android.os.Build
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.shared.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.getDefault


object Log {

    private var isInitialized = false
    private lateinit var logFile: File
    private val dateFormat by lazy { SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", getDefault()) }

    fun init(context: Context, logDirectory: String) {
        if (isInitialized) return

        try {

            val timestamp = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", getDefault()).format(Date())
            logFile = File(logDirectory, "$timestamp.txt")

            logFile.parentFile?.mkdirs()

            collectStartupData()

            isInitialized = true

        } catch (e: Exception) {
            Log.e("LogFileWriter", "Failed to initialize file logging", e)
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
            FileWriter(logFile, true).use { writer ->
                val timestamp = dateFormat.format(Date())
                writer.write("Logger Started at $timestamp\n")
                startupData.forEach { (key, value) ->
                    writer.write(" $key: $value\n")
                }
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e("LogFileWriter", "Failed to write startup data", e)
        }
    }

    private fun writeToFile(
        message: String,
        tag: String = "ListenBrainz",
        severity: String = "INFO",
    ) {
        if (!isInitialized) return

        try {
            FileWriter(logFile, true).use { writer ->
                val timestamp = dateFormat.format(Date())
                val logEntry = "[$timestamp] [$severity] [$tag] $message\n"
                writer.write(logEntry)
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e("LogFileWriter", "Failed to write to file", e)
        }
    }

    fun e(message: Any?, tag: String? = null, throwable: Throwable? = null) {
        Log.e(message, tag, throwable)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "ERROR")
        throwable?.let {
            writeToFile("Stack trace: ${it.stackTraceToString()}", logTag, "ERROR")
        }
    }

    fun d(message: Any?, tag: String? = null) {
        Log.d(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "DEBUG")
    }

    fun i(message: Any?, tag: String? = null) {
        Log.i(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "INFO")
    }

    fun w(message: Any?, tag: String? = null) {
        Log.w(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "WARNING")
    }

    fun v(message: Any?, tag: String? = null) {
        Log.v(message, tag)

        val logTag = tag ?: "ListenBrainz"
        writeToFile(message.toString(), logTag, "VERBOSE")
    }

    fun log(message: String, tag: String = Constants.TAG, severity: String = "INFO") {
        if (!isInitialized) return

        try {

            FileWriter(logFile, true).use { writer ->
                val timestamp = dateFormat.format(Date())
                val logEntry = "[$timestamp] [$severity] [$tag] $message\n"
                writer.write(logEntry)
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e("LogFileWriter", "Failed to write to file", e)
        }
    }

}