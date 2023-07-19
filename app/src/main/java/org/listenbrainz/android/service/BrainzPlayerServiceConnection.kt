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
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dariobrux.kotimer.Timer
import com.dariobrux.kotimer.interfaces.OnTimerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlaying
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils

class BrainzPlayerServiceConnection(
    context: Context,
    val appPreferences: AppPreferences,
    val listensRepository: ListensRepository
) {

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
    private val workManager = WorkManager.getInstance(context)

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
        var artist: String? = null
        var title: String? = null
        var releaseName: String? = null
        var timestamp: Long = 0
        var duration: Long = 0
        val timer: Timer = Timer()
        var state: PlaybackState? = null
        var submitted = false
        
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.value = state ?: EMPTY_PLAYBACK_STATE
            _playButtonState.value = if (state?.isPlaying == true) Icons.Rounded.Pause
            else Icons.Rounded.PlayArrow
            if (state?.isPlaying != previousPlaybackState) _isPlaying.value =
                state?.isPlaying == true
            previousPlaybackState = state?.isPlaying == true
    
            if (state == null) return
    
            this.state = state.playbackState as PlaybackState
            d("onPlaybackStateChanged: Listen PlaybackState " + state.state)
    
            if (isDurationUndefined() || submitted) return
    
            if (state.state == PlaybackState.STATE_PLAYING) {
                timer.start()
                // d("Timer started")
            }
    
            if (state.state == PlaybackState.STATE_PAUSED) {
                timer.pause()
                // d("Timer paused")
            }
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
        
            val metadata = metadataCompat?.mediaMetadata as MediaMetadata
    
            // Stop timer and reset metadata.
            resetMetadata()     // Do not perform this action in timer's onTimerStop due to concurrency issues.
            timer.stop()
        
            when {
                state != null -> Log.d("onMetadataChanged: Listen Metadata " + state!!.state)
                else -> Log.d("onMetadataChanged: Listen Metadata")
            }
        
            setArtist(metadata)
            setTitle(metadata)
        
            if (isMetadataFaulty()) {
                Log.w("${if (artist == null) "Artist" else "Title"} is null, listen cancelled.")
                return
            }
        
            setMiscellaneousDetails(metadata)
            setDurationAndCallbacks(metadata)
            
        }
        
        // UTILITY FUNCTIONS
    
        private fun setTitle(metadata: MediaMetadata) {
            title = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_TITLE
                )
            
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_TITLE
                )
            
                else -> null
            }
        }
    
        private fun setArtist(metadata: MediaMetadata) {
            artist = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_ARTIST
                )
            
                !metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_ALBUM_ARTIST
                )
            
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE
                )
            
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                    .isNullOrEmpty() -> metadata.getString(
                    MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION
                )
            
                else -> null
            }
        }
    
        /** Sets releaseName*/
        private fun setMiscellaneousDetails(metadata: MediaMetadata) {
            releaseName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        }
    
        private fun isMetadataFaulty(): Boolean = artist.isNullOrEmpty() || title.isNullOrEmpty()
    
        /** Run [artist] and [title] value-check before invoking this function.*/
        private fun setDurationAndCallbacks(metadata: MediaMetadata) {
            duration =
                roundDuration(duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION) / 2L)
                    .coerceAtMost(240000)   // Since maximum time required to validate a listen as submittable listen is 4 minutes.
            timestamp = System.currentTimeMillis() / 1000
        
            // d(duration.toString())
            timer.setDuration(duration)
        
            // Setting listener
            timer.setOnTimerListener(listener = object : OnTimerListener {
                override fun onTimerEnded() {
                    val data = Data.Builder()
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                        .putInt(MediaMetadata.METADATA_KEY_DURATION, duration.toInt())
                        .putString(MediaMetadata.METADATA_KEY_WRITER, "ListenBrainz Android")
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, releaseName)
                        .putString("TYPE", ListenType.SINGLE.code)
                        .putLong(Constants.Strings.TIMESTAMP, timestamp)
                        .build()
    
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    
                    val listenSubmissionWork = OneTimeWorkRequestBuilder<ListenSubmissionWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    
                    workManager.enqueue(listenSubmissionWork)
                    submitted = true
                }
            
                override fun onTimerPaused(remainingMillis: Long) {
                    Log.d("${remainingMillis / 1000} seconds left to submit listen.")
                }
            
                override fun onTimerRun(milliseconds: Long) {}
                override fun onTimerStarted() {
                    val data = Data.Builder()
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                        .putInt(MediaMetadata.METADATA_KEY_DURATION, duration.toInt())
                        .putString(MediaMetadata.METADATA_KEY_WRITER, "ListenBrainz Android")
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, releaseName)
                        .putString("TYPE", ListenType.PLAYING_NOW.code)
                        .putLong(Constants.Strings.TIMESTAMP, timestamp)
                        .build()
    
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
    
                    val listenSubmissionWork = OneTimeWorkRequestBuilder<ListenSubmissionWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
    
                    workManager.enqueue(listenSubmissionWork)
                }
            
                override fun onTimerStopped() {}
            
            }, callbacksOnMainThread = true)
            Log.d("Listener Set")
        }
    
        private fun isDurationUndefined(): Boolean = duration <= 0
    
        private fun resetMetadata() {
            Log.d("Metadata Reset")
            artist = null
            title = null
            timestamp = 0
            duration = 0
            submitted = false
            releaseName = null
        }
    
        private fun roundDuration(duration: Long): Long {
            return (duration / 1000) * 1000
        }
    

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
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