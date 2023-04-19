package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w

class ListenSessionListener(private val handler: ListenHandler, private val appPreferences: AppPreferences) : OnActiveSessionsChangedListener {
    
    private val registeredControllers: MutableSet<String> = mutableSetOf()        // TODO: Use shared preferences.
    private val activeSessions: MutableMap<MediaController, ListenCallback?> = HashMap()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        println("onActiveSessionsChanged: EXECUTED")
        if (controllers == null) return
        for (controller in controllers) {
    
            // TODO: Wall here to block registering of unwanted controllers.
            
            // Avoids registering multiple callbacks.
            if (controller.packageName in registeredControllers) {
                continue
            }
            
            // Enable listens from spotify option.
            if (!appPreferences.preferenceListeningSpotifyEnabled && controller.packageName == Constants.SPOTIFY_PACKAGE_NAME) {
                print("Spotify listens blocked from Listens Service.")
                continue
            }
            
            val callback = ListenCallback()
            controller.registerCallback(callback)
            registeredControllers.add(controller.packageName)
            activeSessions[controller] = callback
            println("### REGISTERED MediaController callback for ${controller.packageName}.")
        }
    }

    fun clearSessions() {
        for ((controller, callback) in activeSessions) {
            controller.unregisterCallback(callback!!)
            println("### UNREGISTERED MediaController Callback for ${controller.packageName}.")
        }
        activeSessions.clear()
        registeredControllers.clear()
    }

    private inner class ListenCallback : MediaController.Callback() {
        var artist: String? = null
        var title: String? = null
        var timestamp: Long = 0
        var state: PlaybackState? = null
        var submitted = true
        
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            
            if (metadata == null) return
            
            when {
                state != null -> d("Listen Metadata " + state!!.state)
                else -> d("Listen Metadata")
            }
            
            artist = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_ARTIST).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                else -> null
            }
            
            title = when {
                !metadata.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                !metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE).isNullOrEmpty() -> metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                else -> null
            }
            
            if (artist == null || title == null || artist!!.isEmpty() || title!!.isEmpty()){
                w("${if (artist == null) "Artist" else "Title"} is null, listen cancelled.")
                return
            }
            
            // If the difference between the timestamp of this listen and previously
            // submitted listen is less that 1 second, listen should not be submitted.
            if ( (System.currentTimeMillis() / 1000 - timestamp) >= 1000) {
                submitted = false
            }
            
            timestamp = System.currentTimeMillis() / 1000
            if (state != null && state!!.state == PlaybackState.STATE_PLAYING && !submitted) {
                handler.submitListen(artist, title, timestamp)
                submitted = true
            }
        }
        // FIXME : Listens are only submitted when song is paused once, then played and skipped.
        
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (state == null) return
            this.state = state
            d("Listen PlaybackState " + state.state)
            if (state.state == PlaybackState.STATE_PLAYING && !submitted) {
                handler.submitListen(artist, title, timestamp)
                submitted = true
            }
            if (state.state == PlaybackState.STATE_PAUSED ||
                    state.state == PlaybackState.STATE_STOPPED) {
                handler.cancelListen(timestamp)
                d("Listen Cancelled.")
                artist = ""
                title = ""
                timestamp = 0
            }
        }
    }
}