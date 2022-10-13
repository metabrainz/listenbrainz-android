package org.listenbrainz.android.presentation.features.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.dashboard.DashboardActivity

class NotificationHelper(base: Context?,val icon: Bitmap) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null
    private var intentActivity: Class<*>

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager? get() {
        if (mManager == null) {
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mManager
    }

    val channelNotification: NotificationCompat.Builder get() = NotificationCompat.Builder(applicationContext, channelID)
        .setContentTitle("NewsBrainz")
        .setContentText("Here are your updates for the day!")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setColor(ContextCompat.getColor(this,R.color.blue_diff))
        .setLargeIcon(icon)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setTimeoutAfter(1800000)
        .setAutoCancel(true)
        .setContentIntent(PendingIntent.getActivity(baseContext, 0, Intent(baseContext, intentActivity), 0))

    companion object {
        const val channelID = "channelId"
        const val channelName = "channelName"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        intentActivity = DashboardActivity::class.java
    }
}