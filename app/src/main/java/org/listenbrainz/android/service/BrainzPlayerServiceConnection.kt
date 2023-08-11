package org.listenbrainz.android.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlaying
import org.listenbrainz.android.util.ListenSubmissionState
import org.listenbrainz.android.util.Resource

class BrainzPlayerServiceConnection(
    context: Context,
    val appPreferences: AppPreferences,
    val workManager: WorkManager
) {
    val scope = CoroutineScope(Dispatchers.Main)

    private val _isConnected = MutableStateFlow(Resource(Resource.Status.LOADING, false))
    val isConnected = _isConnected.asStateFlow()

    private val _playbackState = MutableStateFlow(EMPTY_PLAYBACK_STATE)
    val playbackState = _playbackState.asStateFlow()

    private val _currentlyPlayingSong = MutableStateFlow(NOTHING_PLAYING)
    val currentPlayingSong = _currentlyPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playButtonState = MutableStateFlow(Icons.Rounded.PlayArrow)
    val playButtonState = _playButtonState.asStateFlow()

    private val _shuffleState = MutableStateFlow(false)
    val shuffleState = _shuffleState.asStateFlow()

    private val _repeatModeState = MutableStateFlow(RepeatMode.REPEAT_MODE_OFF)
    val repeatModeState = _repeatModeState.asStateFlow()

    private var previousPlaybackState: Boolean = false
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            BrainzPlayerService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    //To skip, pause, resume etc in player
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    //subscribe and unsubscribe will be called from ViewModel to subscribe and unsubscribe from a mediaID to get access of mediaItems from local
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.value = Resource(Resource.Status.SUCCESS, true)
        }

        override fun onConnectionSuspended() {
            _isConnected.value = Resource(Resource.Status.FAILED,false)
        }

        override fun onConnectionFailed() {
            _isConnected.value = Resource(Resource.Status.FAILED, false)
        }
    }
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        val listen: ListenSubmissionState = ListenSubmissionState()
        
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.value = state ?: EMPTY_PLAYBACK_STATE
            _playButtonState.value =
                if (state?.isPlaying == true)
                    Icons.Rounded.Pause
                else
                    Icons.Rounded.PlayArrow
            
            if (state?.isPlaying != previousPlaybackState) _isPlaying.value =
                state?.isPlaying == true
            previousPlaybackState = state?.isPlaying == true
    
            // Cutout point for normal bp and bp submitter
            if (appPreferences.isNotificationServiceAllowed) return
    
            listen.toggleTimer(state?.playbackState as PlaybackState)
            
        }
    
        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
            when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_NONE -> _repeatModeState.value =
                    RepeatMode.REPEAT_MODE_OFF
            
                PlaybackStateCompat.REPEAT_MODE_ONE -> _repeatModeState.value =
                    RepeatMode.REPEAT_MODE_ONE
            
                PlaybackStateCompat.REPEAT_MODE_ALL -> _repeatModeState.value =
                    RepeatMode.REPEAT_MODE_ALL
            
                else -> _repeatModeState.value = RepeatMode.REPEAT_MODE_OFF
            }
        }
    
        override fun onShuffleModeChanged(shuffleMode: Int) {
            super.onShuffleModeChanged(shuffleMode)
            _shuffleState.value = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
        }
        
        override fun onMetadataChanged(metadataCompat: MediaMetadataCompat?) {
            _currentlyPlayingSong.value =
                if (metadataCompat?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) == null)
                    NOTHING_PLAYING
                else metadataCompat
        
            // Cutoff point for bp and bp submitter.
            if (appPreferences.isNotificationServiceAllowed) return
            
            val metadata = metadataCompat?.mediaMetadata as MediaMetadata
    
            listen.initSubmissionFlow(metadata){
                // Submit a listen.
                workManager.enqueue(
                    listen.buildWorkRequest(listenType = it, player = "ListenBrainz Android")
                )
            }
            
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }

        // UTILITY FUNCTIONS

        private fun setTitle(metadata: MediaMetadataCompat) {
            title = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_TITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                else -> null
            }
        }

        private fun setArtist(metadata: MediaMetadataCompat) {
            artist = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_ARTIST).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION).isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                else -> null
            }
        }

        /** Sets releaseName*/
        private fun setMiscellaneousDetails(metadata: MediaMetadataCompat) {
            releaseName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        }

        private fun isMetadataFaulty() : Boolean
                = artist.isNullOrEmpty() || title.isNullOrEmpty()

        /** Run [artist] and [title] value-check before invoking this function.*/
        private fun setDurationAndCallbacks(metadata: MediaMetadataCompat) {
            duration = Utils.roundDuration(duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION) / 2L)
                .coerceAtMost(240000)   // Since maximum time required to validate a listen as submittable listen is 4 minutes.
            timestamp = System.currentTimeMillis() / 1000

            timer.setDuration(duration)

            // Setting listener
            timer.setOnTimerListener(listener = object : OnTimerListener {
                override fun onTimerEnded() {
                    submitListenFromBP(
                        artist,
                        title,
                        timestamp,
                        metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                        releaseName,
                        ListenType.SINGLE
                    )
                    submitted = true
                }

                override fun onTimerPaused(remainingMillis: Long) {
                    d("${remainingMillis / 1000} seconds left to submit listen.")
                }
                override fun onTimerRun(milliseconds: Long) {}
                override fun onTimerStarted() {
                    d("Timer started")
                    submitListenFromBP(
                        artist,
                        title,
                        null,
                        metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                        releaseName,
                        ListenType.PLAYING_NOW
                    )
                }
                override fun onTimerStopped() {}

            }, callbacksOnMainThread = true)
            d("Listener Set")
        }

        private fun submitListenFromBP(artist: String?, title: String?, timestamp: Long?, duration: Long, releaseName: String?, listenType: ListenType) {
            scope.launch {
                val token = appPreferences.getLbAccessToken()
                if(token.isNotEmpty() && !appPreferences.isNotificationServiceAllowed) {
                    
                    if(duration <= 30000) {
                        d("Track is too short to submit")
                        return@launch
                    }
                    
                    val metadata = ListenTrackMetadata(
                        artist = artist,
                        track = title,
                        release = releaseName,
                        additionalInfo = AdditionalInfo(
                            durationMs = duration.toInt(),
                            mediaPlayer = "BrainzPlayer",
                            submissionClient = "ListenBrainz Android",
                            submissionClientVersion = BuildConfig.VERSION_NAME
                        )
                    )
        
                    val body = ListenSubmitBody()
                    body.addListen(
                        timestamp = if(listenType == ListenType.SINGLE) timestamp else null,
                        metadata = metadata
                    )
                    
                    body.listenType = listenType.code
        
                    d("Submitting Listen: $body")
        
                    listensRepository.submitListen(token = token, body)
                }
            }
            
        }

        private fun resetMetadata() {
            d("Metadata Reset")
            artist = null
            title = null
            timestamp = 0
            duration = 0
            submitted = false
            releaseName = null
        }
    }
}

val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()