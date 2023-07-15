package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import com.dariobrux.kotimer.Timer
import com.dariobrux.kotimer.interfaces.OnTimerListener
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w
import org.listenbrainz.android.util.Utils.roundDuration

class ListenSessionListener(private val handler: ListenHandler, val appPreferences: AppPreferences) : OnActiveSessionsChangedListener {
    
    private val activeSessions: MutableMap<MediaController, ListenCallback?> = HashMap()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        d("onActiveSessionsChanged: EXECUTED")
        if (controllers == null) return
        clearSessions()
        registerControllers(controllers)
    }

    private fun registerControllers(controllers: List<MediaController>) {
        for (controller in controllers) {
            // BlackList
            if (controller.packageName in appPreferences.listeningBlacklist)
                continue

            val callback = ListenCallback(controller.packageName)
            activeSessions[controller] = callback
            controller.registerCallback(callback)
            d("### REGISTERED MediaController callback for ${controller.packageName}.")
        }

        // Adding any new app packages found in the notification.
        controllers.forEach { controller ->
            val appList = appPreferences.listeningApps
            if (controller.packageName !in appList){
                appPreferences.listeningApps = appList.plus(controller.packageName)
            }
        }
        // println(appPreferences.listeningApps)
    }

    fun clearSessions() {
        for ((controller, callback) in activeSessions) {
            controller.unregisterCallback(callback!!)
            d("### UNREGISTERED MediaController Callback for ${controller.packageName}.")
        }
        activeSessions.clear()
    }

    private inner class ListenCallback(private val player: String) : MediaController.Callback() {
        var artist: String? = null
        var title: String? = null
        var releaseName: String? = null
        var timestamp: Long = 0
        var duration: Long = 0
        val timer: Timer = Timer()
        var state: PlaybackState? = null
        var submitted = false
        
        // FIXME:
        //  1) First ever session song isn't recorded. This is because onMetadataChanged
        //      isn't called by callback itself.
        
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            
            if (metadata == null) return
            
            // Stop timer and reset metadata.
            resetMetadata()     // Do not perform this action in timer's onTimerStop due to concurrency issues.
            timer.stop()
    
            when {
                state != null -> d("onMetadataChanged: Listen Metadata " + state!!.state)
                else -> d("onMetadataChanged: Listen Metadata")
            }
            
            setArtist(metadata)
            setTitle(metadata)
            
            if (isMetadataFaulty()){
                w("${if (artist == null) "Artist" else "Title"} is null, listen cancelled.")
                return
            }
            
            setMiscellaneousDetails(metadata)
            setDurationAndCallbacks(metadata)
            
        }
    
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (state == null) return
            
            this.state = state
            d("onPlaybackStateChanged: Listen PlaybackState " + state.state)
    
            if (isDurationUndefined() || submitted) return
            
            if (state.state == PlaybackState.STATE_PLAYING){
                timer.start()
                // d("Timer started")
            }
            
            if (state.state == PlaybackState.STATE_PAUSED){
                timer.pause()
                // d("Timer paused")
            }
            
        }
        
        // UTILITY FUNCTIONS
        
        private fun setTitle(metadata: MediaMetadata) {
            title = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                else -> null
            }
        }
    
        private fun setArtist(metadata: MediaMetadata) {
            artist = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_ARTIST).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                else -> null
            }
        }
        
        /** Sets releaseName*/
        private fun setMiscellaneousDetails(metadata: MediaMetadata) {
            releaseName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        }
        
        private fun isMetadataFaulty() : Boolean
            = artist.isNullOrEmpty() || title.isNullOrEmpty()
        
        /** Run [artist] and [title] value-check before invoking this function.*/
        private fun setDurationAndCallbacks(metadata: MediaMetadata) {
            duration = roundDuration(duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION) / 2L)
                .coerceAtMost(240000)   // Since maximum time required to validate a listen as submittable listen is 4 minutes.
            timestamp = System.currentTimeMillis() / 1000
            
            // d(duration.toString())
            timer.setDuration(duration)
            
            // Setting listener
            timer.setOnTimerListener(listener = object : OnTimerListener {
                override fun onTimerEnded() {
                    handler.submitListen(
                        artist,
                        title,
                        timestamp,
                        metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                        player,
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
                    handler.submitListen(
                        artist,
                        title,
                        null,
                        metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                        player,
                        releaseName,
                        ListenType.PLAYING_NOW
                    )
                }
                override fun onTimerStopped() {}
                
            }, callbacksOnMainThread = true)
            d("Listener Set")
        }
    
        private fun isDurationUndefined() : Boolean
            = duration <= 0
        
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
