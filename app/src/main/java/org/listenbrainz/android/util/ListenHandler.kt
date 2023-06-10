package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.util.Constants.Strings.TIMESTAMP
import org.listenbrainz.android.util.Log.d

class ListenHandler(val appPreferences: AppPreferences, val repository: ListensRepository) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val token = appPreferences.lbAccessToken
        if (token.isNullOrEmpty()) {
            d("ListenBrainz User token has not been set!")
            return
        }
        if(msg.data.getInt(MediaMetadata.METADATA_KEY_DURATION) <= 30000) {
            d("Track is too short to submit")
            return
        }
        val metadata = ListenTrackMetadata()
        
        // Main metadata
        metadata.artist = msg.data.getString(MediaMetadata.METADATA_KEY_ARTIST)
        metadata.track = msg.data.getString(MediaMetadata.METADATA_KEY_TITLE)
        metadata.release = msg.data.getString(MediaMetadata.METADATA_KEY_ALBUM)
        
        // Duration
        metadata.additionalInfo.duration_ms = msg.data.getInt(MediaMetadata.METADATA_KEY_DURATION)
        
        // Setting player
        val player = msg.data.getString(MediaMetadata.METADATA_KEY_WRITER)
        if (player != null)
            metadata.additionalInfo.media_player = repository.getPackageLabel(player)
        
        val body = ListenSubmitBody()
        body.addListen(
            timestamp = if(msg.data.getString("TYPE") == "single") msg.data.getLong(TIMESTAMP) else null,
            metadata = metadata,
            insertedAt = System.currentTimeMillis().toInt()
        )
        body.listenType = msg.data.getString("TYPE")

        repository.submitListen(token, body)
    }

    fun submitListen(
        artist: String?,
        title: String?,
        timestamp: Long?,
        duration: Long,
        player: String,
        releaseName: String?,
        type: ListenType
    ) {
        val message = obtainMessage()
        val data = Bundle()
        
        data.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
        data.putString(MediaMetadata.METADATA_KEY_TITLE, title)
        data.putInt(MediaMetadata.METADATA_KEY_DURATION, duration.toInt())
        data.putString(MediaMetadata.METADATA_KEY_WRITER, player)
        data.putString(MediaMetadata.METADATA_KEY_ALBUM, releaseName)
        data.putString("TYPE", type.code)
        if (timestamp != null) {
            data.putLong(TIMESTAMP, timestamp)
            message.what = timestamp.toInt()
        }
        
        message.data = data
        sendMessageDelayed(message, 0)
    }
}
