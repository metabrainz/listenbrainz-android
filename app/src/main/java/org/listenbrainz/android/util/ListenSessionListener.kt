package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w
import org.listenbrainz.android.util.UserPreferences.preferenceListeningSpotifyEnabled

class ListenSessionListener(private val handler: ListenHandler) : OnActiveSessionsChangedListener {
    private val controllers: MutableList<MediaController> = ArrayList()
    private val activeSessions: MutableMap<MediaSession.Token, ListenCallback?> = HashMap()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        if (controllers == null) return
        clearSessions()
        this.controllers.addAll(controllers)
        for (controller in controllers) {
            if (!preferenceListeningSpotifyEnabled && controller.packageName == Constants.SPOTIFY_PACKAGE_NAME){
                continue
            }
            val callback = ListenCallback()
            controller.registerCallback(callback)
        }
    }

    fun clearSessions() {
        for ((key, value) in activeSessions) {
            for (controller in controllers) {
                if (controller.sessionToken == key){
                    controller.unregisterCallback(value!!)
                }
            }
        }
        activeSessions.clear()
        controllers.clear()
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
        //  the next song is recorded then.
        
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