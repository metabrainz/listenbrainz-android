package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import okhttp3.ResponseBody
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.util.Constants.Strings.TIMESTAMP
import org.listenbrainz.android.util.Log.d
import retrofit2.Call
import retrofit2.Response

class ListenHandler(val appPreferences: AppPreferences, val service: ListensService, val repository: ListensRepository) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val token = appPreferences.lbAccessToken
        if (token.isNullOrEmpty()) {
            d("ListenBrainz User token has not been set!")
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
        body.addListen(timestamp = msg.data.getLong(TIMESTAMP), metadata = metadata, insertedAt = System.currentTimeMillis().toInt())
        body.listenType = "single"

        service.submitListen("Token $token", body)?.enqueue(object : retrofit2.Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                d(response.message())
            }
            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                d("Something went wrong: ${t.message}")
            }
        })
    }

    fun submitListen(
        artist: String?,
        title: String?,
        timestamp: Long,
        duration: Long,
        player: String,
        releaseName: String?
    ) {
        val message = obtainMessage()
        val data = Bundle()
        data.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
        data.putString(MediaMetadata.METADATA_KEY_TITLE, title)
        data.putInt(MediaMetadata.METADATA_KEY_DURATION, duration.toInt())
        data.putString(MediaMetadata.METADATA_KEY_WRITER, player)
        data.putString(MediaMetadata.METADATA_KEY_ALBUM, releaseName)
        data.putLong(TIMESTAMP, timestamp)
        message.what = timestamp.toInt()
        message.data = data
        sendMessageDelayed(message, 0)
    }
}
