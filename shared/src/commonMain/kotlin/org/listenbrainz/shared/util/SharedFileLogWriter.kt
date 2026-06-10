package org.listenbrainz.shared.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@OptIn(DelicateCoroutinesApi::class)
abstract class SharedFileLogWriter(
    private val buildConfig: BuildInfo
): LogWriter() {

    @OptIn(DelicateCoroutinesApi::class)
    private val loggerScope = GlobalScope
    private val loggerQueue = Channel<String>(capacity = Channel.UNLIMITED)

    private val sharedLog = platformLogWriter()

    protected fun provideFormattedTime():String{
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return now.format(dateTimeFormatter)
    }

    private val dateTimeFormatter = LocalDateTime.Format {
        dayOfMonth(Padding.ZERO)
        char('-')
        monthNumber(Padding.ZERO)
        char('-')
        year(Padding.NONE)
        char('-')
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
    }

    protected fun initBlock(){
        loggerScope.launch(Dispatchers.IO) {
            try {
                for(entry in loggerQueue){
                    writeLineToFile(entry)
                }
            } catch (e: Exception){
                sharedLog.log(Severity.Error, tag = "LogFileWriter", message =  "Failed to write startup data", throwable =  e)
            }
        }
    }

    open fun collectStartupData(){
        val sharedData = startupData()
        formatStartupData(sharedData)
    }

    protected fun startupData(): Map<String, String>{
        return mapOf(
            "Device Application Id" to buildConfig.applicationId,
            "Device Version Code" to buildConfig.versionCode.toString(),
            "Device Version Name" to buildConfig.versionName,
            "Device Build Type" to buildConfig.buildType,
        )
    }

    protected fun formatStartupData(data: Map<String,String>){
        try {
            val timestamp = provideFormattedTime()
            val buildLog = buildString {
                append("Logger Started at $timestamp\n")
                data.forEach { (key, value) ->
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
        severity: String = "INFO"
    ){
      try {
          val timestamp = provideFormattedTime()
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

    protected abstract suspend fun writeLineToFile(entry:String)

}