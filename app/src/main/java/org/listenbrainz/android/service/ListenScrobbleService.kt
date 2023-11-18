package org.listenbrainz.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Constants.Strings.CHANNEL_ID
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.e
import org.listenbrainz.android.util.Log.w
import java.util.Objects
import javax.inject.Inject

@AndroidEntryPoint
class ListenScrobbleService : NotificationListenerService() {

    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var listensRepository: ListensRepository

    @Inject
    lateinit var workManager: WorkManager
    
    private var sessionManager: MediaSessionManager? = null
    private var sessionListener: ListenSessionListener? = null
    private var listenServiceComponent: ComponentName? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val nm by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        )!!
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_STICKY

    private fun initialize() {
        d("Initializing Listener Service")
        sessionManager = applicationContext.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionListener = ListenSessionListener(appPreferences, workManager, scope)
        listenServiceComponent = ComponentName(this, this.javaClass)
        createNotificationChannel()

        try {
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener!!, listenServiceComponent)
        } catch (e: SecurityException) {
            e(message = "Could not add session listener due to security exception: ${e.message}")
        } catch (e: Exception) {
            e(message = "Could not add session listener: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.clearSessions()
        sessionListener?.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        scrobbleFromNoti(
            sbn,
            removed = false,
            packageNames = Constants.Strings.PACKAGES_PIXEL_NP,
            channelName = Constants.Strings.CHANNEL_PIXEL_NP,
            notiField = Notification.EXTRA_TITLE,
            format = R.string.song_format_string
        )
        scrobbleFromNoti(
            sbn,
            removed = false,
            packageNames = listOf(Constants.Strings.PACKAGE_SHAZAM),
            channelName = Constants.Strings.CHANNEL_SHAZAM,
            notiField = Notification.EXTRA_TEXT,
            format = R.string.auto_shazam_now_playing
        )
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) { //only for >26
        if (reason == REASON_APP_CANCEL || reason == REASON_APP_CANCEL_ALL ||
            reason == REASON_TIMEOUT || reason == REASON_ERROR
        ) {
            scrobbleFromNoti(
                sbn,
                removed = true,
                packageNames = Constants.Strings.PACKAGES_PIXEL_NP,
                channelName = Constants.Strings.CHANNEL_PIXEL_NP,
            )
            scrobbleFromNoti(
                sbn,
                removed = true,
                packageNames = listOf(Constants.Strings.PACKAGE_SHAZAM),
                channelName = Constants.Strings.CHANNEL_SHAZAM,
            )
        }
    }

    private fun scrobbleFromNoti(
        sbn: StatusBarNotification?,
        removed: Boolean,
        packageNames: Collection<String>,
        channelName: String,
        notiField: String = Notification.EXTRA_TITLE,
        @StringRes format: Int = 0
    ) {
        if (appPreferences.submitListens &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            sbn != null &&
            sbn.packageName in packageNames
        ) {
            val n = sbn.notification
            if (n.channelId == channelName) {
                val notiText = n.extras.getString(notiField) ?: return
                val trackInfo = packageTrackMap[sbn.packageName]

                if (removed) {
                    scrobbleHandler.remove(trackInfo?.hash ?: return, trackInfo.packageName)
                    return
                }
                val meta = MetadataUtils.scrobbleFromNotiExtractMeta(
                    notiText,
                    getStringInDeviceLocale(format)
                )
                if (meta != null) {
                    val (artist, title) = meta
                    val hash = Objects.hash(artist, "", title, sbn.packageName)
                    if (trackInfo != null && trackInfo.hash == hash) {
                        val scrobbleTimeReached =
                            SystemClock.elapsedRealtime() >= trackInfo.scrobbleElapsedRealtime
                        if (!scrobbleTimeReached && !scrobbleHandler.has(hash)) { //"resume" scrobbling
                            scrobbleHandler.addScrobble(trackInfo.copy())
                            notifyScrobble(trackInfo)
                            Stuff.log("${this::scrobbleFromNoti.name} rescheduling")
                        } else if (System.currentTimeMillis() - trackInfo.playStartTime < Stuff.NOTI_SCROBBLE_INTERVAL) {
                            Stuff.log("${this::scrobbleFromNoti.name} ignoring possible duplicate")
                        }
                    } else {
                        // different song, scrobble it
                        trackInfo?.let { scrobbleHandler.remove(it.hash) }
                        val newTrackInfo = PlayingTrackInfo(
                            playStartTime = System.currentTimeMillis(),
                            hash = hash,
                            packageName = sbn.packageName,
                        )
                        newTrackInfo.putOriginals(artist, title)

                        packageTrackMap[sbn.packageName] = newTrackInfo
                        scrobbleHandler.nowPlaying(
                            newTrackInfo,
                            prefs.delaySecs.coerceAtMost(3 * 60) * 1000L
                        )
                    }
                } else {
                    w("${this::scrobbleFromNoti.name} parse failed")
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_NAME = "Scrobbling"
        private const val CHANNEL_DESCRIPTION = "Shows notifications when a song is played"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            nm.createNotificationChannel(channel)
        }
    }
}