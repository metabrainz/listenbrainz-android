package org.listenbrainz.shared.util

import android.os.Build
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class AndroidFileLogWriter(
    logDirectory: String,
    buildConfig: BuildInfo,
) : SharedFileLogWriter(buildConfig) {
    private var logFile: File

    private val sharedLog = platformLogWriter()

    init {
        val timestamp = provideFormattedTime().replace(":","-")
        logFile = File(logDirectory, "$timestamp.txt")

        try {
            logFile.parentFile?.mkdirs()
            initBlock()
            collectStartupData()
        }catch (e:Exception){
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to initialize file logging", throwable = e)
        }
    }

    override fun collectStartupData() {

        val sharedStartupData = mapOf(
            "App Version" to System.currentTimeMillis().toString(),
        ) + startupData()

        val modifiedStartupData = sharedStartupData + mapOf(
            "Device" to Build.DEVICE,
            "Device SDK" to Build.VERSION.SDK_INT.toString(),
            "Device Manufacturer" to Build.MANUFACTURER
        )

        formatStartupData(modifiedStartupData)
    }

    override suspend fun writeLineToFile(entry: String) = withContext(Dispatchers.IO) {
        try {
            FileWriter(logFile,true).use { writer->
                writer.write(entry)
                writer.flush()
            }
        } catch(e:Exception){
            sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to write to file", throwable =  e)
        }
    }

}