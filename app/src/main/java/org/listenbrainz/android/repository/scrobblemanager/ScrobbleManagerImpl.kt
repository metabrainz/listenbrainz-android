package org.listenbrainz.android.repository.scrobblemanager

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.ListenSubmissionState
import org.listenbrainz.android.util.Log
import javax.inject.Inject

class ScrobbleManagerImpl @Inject constructor(
    workManager: WorkManager,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
): ScrobbleManager {
    
    private val listenSubmissionState = ListenSubmissionState(workManager, context)
    private val scope = MainScope()
    
    /** This mutex is used to ensure that [ListenSubmissionState.playingTrack] is not changed in a
     * non thread-safe manner. Only one of the metadata alerts (ListenCallback or onNotificationPosted)
     * can change [ListenSubmissionState] at a time.*/
    private val mutex = Mutex()
    
    override fun onMetadataChanged(metadata: MediaMetadata?, player: String) {
        scope.launch {
            mutex.withLock {
                if (metadata == null) return@withLock
                listenSubmissionState.onControllerCallback(metadata, player)
            }
        }
    }
    
    override fun onPlaybackStateChanged(state: PlaybackState?) {
        /*scope.launch {
            timerMutex.withLock {
                listenSubmissionState.toggleTimer(state?.state)
            }
        }*/
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        scope.launch {
            mutex.withLock {
                
                // Avoid repetitive submissions
                if (sbn != null &&
                    sbn.notification.`when` == listenSubmissionState.playingTrack.timestamp &&
                    sbn.packageName == listenSubmissionState.playingTrack.pkgName) return@launch
                
                // Alert submission state
                if (sbn!!.notification.category == Notification.CATEGORY_TRANSPORT
                    && sbn.packageName !in appPreferences.getListeningBlacklist()) {
                    val playingTrack = PlayingTrack(
                        title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
                            ?: return@launch,
                        artist = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
                            ?: return@launch,
                        pkgName = sbn.packageName,
                        timestamp = sbn.notification.`when`
                    )
                    
                    Log.d(playingTrack)
                    listenSubmissionState.alertMediaNotificationActive(playingTrack)
                }
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        scope.launch {
            if (sbn?.notification?.category == Notification.CATEGORY_TRANSPORT
                && sbn.packageName !in appPreferences.getListeningBlacklist()
            ) {
                listenSubmissionState.alertMediaPlayerRemoved(sbn)
            }
        }
    }
    
}