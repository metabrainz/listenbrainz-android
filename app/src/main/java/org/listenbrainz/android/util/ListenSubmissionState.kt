package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.PlaybackState
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dariobrux.kotimer.Timer
import com.dariobrux.kotimer.interfaces.OnTimerListener
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.service.ListenSubmissionWorker
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w

class ListenSubmissionState(
    private var artist: String? = null,
    private var title: String? = null,
    private var releaseName: String? = null,
    private var timestamp: Long = 0,
    private var duration: Long = 0,
    private var state: PlaybackState? = null
) {
    
    private val timer: Timer = Timer()
    private var submitted: Boolean = false
    private var playingNowSubmitted = false
    
    /** Initialize listen metadata and timer.
     * @param metadata Metadata to set the state's data.
     * @param submitListen Function to submit a listen. Use [ListenSubmissionWorker] and pass it into [WorkManager]
     * to execute work request. Use [buildWorkRequest] function to build a work request.*/
    fun initSubmissionFlow(metadata: MediaMetadata?, submitListen: (ListenType) -> Unit){
        
        if (metadata == null) return
        
        // Stop timer and reset metadata.
        resetMetadata()     // Do not perform this action in timer's onTimerStop due to concurrency issues.
        timer.stop()
        
        when {
            state != null -> d("onMetadataChanged: Listen Metadata $state")
            else -> d("onMetadataChanged: Listen Metadata")
        }
    
        setArtist(metadata)
        setTitle(metadata)
    
        if (isMetadataFaulty()) {
            w("${if (artist == null) "Artist" else "Title"} is null, listen cancelled.")
            return
        }
    
        setMiscellaneousDetails(metadata)
        setDurationAndCallbacks(metadata){
            submitListen(it)
        }
    }
    
    /** Toggle timer based on state. */
    fun toggleTimer(state: PlaybackState?) {
    
        if (state == null) return
        
        this.state = state
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
    
    // Metadata setter functions
    
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
    
    /** Run [artist] and [title] value-check before invoking this function.*/
    private fun setDurationAndCallbacks(metadata: MediaMetadata, onSubmit: (ListenType) -> Unit) {
        duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        timestamp = System.currentTimeMillis() / 1000
        
        // d(duration.toString())
        timer.setDuration(
            roundDuration(duration = duration / 2L)     // Since maximum time required to validate a listen as submittable listen is 4 minutes.
                .coerceAtMost(240000)
        )
        
        // Setting listener
        timer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                onSubmit(ListenType.SINGLE)
                submitted = true
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                d("${remainingMillis / 1000} seconds left to submit listen.")
            }
            
            override fun onTimerRun(milliseconds: Long) {}
            override fun onTimerStarted() {
                if (!playingNowSubmitted){
                    onSubmit(ListenType.PLAYING_NOW)
                    playingNowSubmitted = true
                }
            }
            
            override fun onTimerStopped() {}
            
        }, callbacksOnMainThread = true)
        d("Listener Set")
    }
    
    private fun resetMetadata() {
        d("Metadata Reset")
        artist = null
        title = null
        timestamp = 0
        duration = 0
        submitted = false
        releaseName = null
        playingNowSubmitted = false
    }
    
    // Utility functions
    
    /** Build a one time work request to submit a listen.
     * @param listenType Type of listen to submit.
     * @param player Media player that the song is being submitted from.*/
    fun buildWorkRequest(listenType: ListenType, player: String): OneTimeWorkRequest {
        
        val data = Data.Builder()
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putInt(MediaMetadata.METADATA_KEY_DURATION, duration.toInt())
            .putString(MediaMetadata.METADATA_KEY_WRITER, player)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, releaseName)
            .putString("TYPE", listenType.code)
            .putLong(Constants.Strings.TIMESTAMP, timestamp)
            .build()
        
        /** We are not going to set network constraints as we want to minimize API calls
         * by bulk submitting listens.*/
        /*val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()*/
        
        return OneTimeWorkRequestBuilder<ListenSubmissionWorker>()
            .setInputData(data)
            //.setConstraints(constraints)
            .build()
        
    }
    
    private fun roundDuration(duration: Long): Long {
        return (duration / 1000) * 1000
    }
    
    // Metadata checker functions
    
    private fun isMetadataFaulty(): Boolean = artist.isNullOrEmpty() || title.isNullOrEmpty()
    
    private fun isDurationUndefined(): Boolean = duration <= 0
}
