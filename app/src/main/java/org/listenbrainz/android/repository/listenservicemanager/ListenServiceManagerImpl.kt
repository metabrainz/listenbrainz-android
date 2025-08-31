package org.listenbrainz.android.repository.listenservicemanager

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.model.PlayingTrack.Companion.toPlayingTrack
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.ListenSessionListener.Companion.isPlaying
import org.listenbrainz.android.util.ListenSubmissionState
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractTitle
import org.listenbrainz.android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/** The sole responsibility of this layer is to maintain mutual exclusion between [onMetadataChanged] and
 * [onNotificationPosted], filter out repetitive submissions and handle changes in settings which concern
 * listening.
 *
 * FUTURE: Call notification popups here as well.*/
@Singleton
class ListenServiceManagerImpl @Inject constructor(
    workManager: WorkManager,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ListenServiceManager {

    private val handler: Handler = Handler(Looper.getMainLooper())
    override val listenSubmissionState = ListenSubmissionState(handler, workManager, context)

    //private val jobQueue: JobQueue by lazy { JobQueue(defaultDispatcher) }
    //private val listenSubmissionState = ListenSubmissionState(jobQueue, workManager, context)
    private val scope = MainScope()

    /** Used to avoid repetitive submissions.*/
    private var lastCallbackTs = System.currentTimeMillis()

    /** Used to avoid repetitive submissions.*/
    private var lastNotificationPostTs = System.currentTimeMillis()

    private val whitelist = appPreferences
        .listeningWhitelist
        .getFlow()
        .onEach { whitelist ->
            // Discard current listen if the controller/package has been removed from whitelist.
            if (listenSubmissionState.playingTrack.pkgName !in whitelist) {
                listenSubmissionState.discardCurrentListen()
            }
        }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val isListeningAllowed = appPreferences.isListeningAllowed
        .getFlow()
        .onEach { isListeningAllowed ->
            if (!isListeningAllowed) {
                // Immediately discard current listen if "Send Listens" option has been turned off.
                listenSubmissionState.discardCurrentListen()
            }
        }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            false,
        )

    override fun onMetadataChanged(metadata: MediaMetadata?, packageName: String, isMediaPlaying: Boolean) {
        handler.post {
            if (!isListeningAllowed.value) return@post
            if (metadata == null) return@post

            val newTimestamp = System.currentTimeMillis()
            with(listenSubmissionState) {
                // Repetitive submissions blocker
                if (playingTrack.isDurationPresent()
                    && newTimestamp in lastCallbackTs..lastCallbackTs + CALLBACK_SUBMISSION_TIMEOUT_INTERVAL
                    && metadata.extractTitle() == playingTrack.title
                ) return@post

                lastCallbackTs = newTimestamp

                val newTrack =
                    metadata.toPlayingTrack(packageName).apply { timestamp = newTimestamp }

                onNewMetadata(
                    newTrack = newTrack,
                    isMediaPlaying = isMediaPlaying
                )
            }
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        state?.isPlaying?.let {
            listenSubmissionState.alertPlaybackStateChanged(it)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, mediaPlaying: Boolean) {
        handler.post {
            if (!isListeningAllowed.value) return@post

            val mediaSessionToken = sbn
                ?.notification
                ?.extras
                ?.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
                ?: return@post

            val controller = MediaController(context, mediaSessionToken)
            val newTrack = controller
                .metadata
                ?.toPlayingTrack(sbn.packageName)
                ?: PlayingTrack(pkgName = sbn.packageName)

            newTrack.apply {
                timestamp = sbn.timestamp
                title = title ?: sbn.title ?: return@post
                artist = artist ?: sbn.subtitle ?: return@post
                releaseName = releaseName ?: sbn.title
                if (isDurationAbsent()) {
                    duration = sbn.notification.extras
                        .getInt(
                            Notification.EXTRA_PROGRESS_MAX,
                            0
                        ).toLong()
                }
            }

            // Avoid repetitive submissions
            with(listenSubmissionState) {
                /*if (
                    newTrack.pkgName == playingTrack.pkgName
                    && newTrack.timestamp in lastNotificationPostTs..lastNotificationPostTs + NOTI_SUBMISSION_TIMEOUT_INTERVAL
                    && newTrack.title == playingTrack.title
                ) {
                    Log.d("Repetitive listen, dismissing.")
                    return@post
                }*/

                // Check for whitelisted apps
                if (sbn.packageName !in whitelist.value) {
                    Log.d("Package ${sbn.packageName} not in whitelist, dismissing.")
                    return@post
                }

                lastNotificationPostTs = newTrack.timestamp

                // Alert submission state
                onNewMetadata(
                    newTrack = newTrack,
                    isMediaPlaying = controller.playbackState?.isPlaying == true || mediaPlaying
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        scope.launch {
            if (!isListeningAllowed.value) return@launch

            if (sbn?.notification?.category == Notification.CATEGORY_TRANSPORT
                && sbn.packageName in appPreferences.listeningWhitelist.get()
            ) {
                listenSubmissionState.alertMediaPlayerRemoved(sbn)
            }
        }
    }

    override fun close() {
        scope.cancel()
    }

    companion object {
        /** Because some notification posts are repetitive and in close proximity to each other, these variables
         * are used to mitigate those cases.*/
        const val NOTI_SUBMISSION_TIMEOUT_INTERVAL = 300

        /** Because some callbacks are repetitive and in close proximity to each other, these variables
         * are used to mitigate those cases.*/
        const val CALLBACK_SUBMISSION_TIMEOUT_INTERVAL = 500

        val StatusBarNotification.title: String?
            get() = notification.extras.getCharSequence(Notification.EXTRA_TITLE)
                ?.toString()
                ?: notification.extras.getString(Notification.EXTRA_TITLE)
                ?: (notification.extras.get(Notification.EXTRA_TITLE) as? SpannableString)?.toString()
                    .also { Log.d("Notification title for $packageName is null") }

        val StatusBarNotification.subtitle: String?
            get() = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
                ?.toString()
                ?: notification.extras.getString(Notification.EXTRA_TEXT)
                ?: (notification.extras.get(Notification.EXTRA_TEXT) as? SpannableString)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    .also { Log.d("Notification subtitle for $packageName is null") }

        val StatusBarNotification.timestamp
            get() = notification.`when`.let {
                if (it == 0L) System.currentTimeMillis() else it
            }
    }
}