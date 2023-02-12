package org.listenbrainz.android.util

import android.Manifest
import android.app.Activity
import android.provider.Settings
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import org.listenbrainz.android.presentation.features.dashboard.DashboardActivity
import kotlin.math.log

class AppUsage: AppCompatActivity() {


    fun getAppUsageTime(
        context: Context,
        packageName: String,
        startTime: Long,
        endTime: Long
    ): AppUsageTime {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
        var foregroundTime = 0L
        var backgroundTime = 0L
        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                foregroundTime += usageStats.totalTimeInForeground
                backgroundTime += usageStats.lastTimeUsed - usageStats.totalTimeInForeground
            }
        }
        Log.d("AppUsage $foregroundTime")
        return AppUsageTime(foregroundTime, backgroundTime)
    }

    data class AppUsageTime(val foregroundTime: Long, val backgroundTime: Long)

}