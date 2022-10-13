package org.listenbrainz.android.presentation.features.listens.submitter

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import org.listenbrainz.android.presentation.UserPreferences.preferenceListeningSpotifyEnabled
import org.listenbrainz.android.util.Log.d

class ListenSessionListener(handler: ListenHandler) : OnActiveSessionsChangedListener {
    private val controllers: MutableList<MediaController>
    private val handler: ListenHandler
    private val activeSessions: MutableMap<MediaSession.Token, ListenCallback?>

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        if (controllers == null) return
        clearSessions()
        this.controllers.addAll(controllers)
        for (controller in controllers) {
            if (!preferenceListeningSpotifyEnabled && controller.packageName == SPOTIFY_PACKAGE_NAME){
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
            artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            if (artist == null || artist!!.isEmpty()) {
                artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            }
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            if (artist == null || title == null || artist!!.isEmpty() || title!!.isEmpty()){
                return
            }
            if (System.currentTimeMillis() / 1000 - timestamp >= 1000) {
                submitted = false
            }
            timestamp = System.currentTimeMillis() / 1000
            if (state != null && state!!.state == PlaybackState.STATE_PLAYING && !submitted) {
                handler.submitListen(artist, title, timestamp)
                submitted = true
            }
        }

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
                artist = ""
                title = ""
                timestamp = 0
            }
        }
    }

    companion object {
        private const val SPOTIFY_PACKAGE_NAME = "com.spotify.music"
    }

    init {
        controllers = ArrayList()
        this.handler = handler
        activeSessions = HashMap()
    }
}