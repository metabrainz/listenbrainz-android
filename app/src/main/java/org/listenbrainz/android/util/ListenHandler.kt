package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import okhttp3.ResponseBody
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.service.ListenSubmitService
import org.listenbrainz.android.util.ListenBrainzServiceGenerator.createListensService
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.UserPreferences.preferenceListenBrainzToken
import retrofit2.Call
import retrofit2.Response

class ListenHandler : Handler(Looper.getMainLooper()) {
    private val delay = 30000
    private val timestamp = "timestamp"

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val token = preferenceListenBrainzToken
        if (token.isNullOrEmpty()) {
            d("ListenBrainz User token has not been set!")
            return
        }
        val service = createListensService(ListenSubmitService::class.java, true)
        val metadata = ListenTrackMetadata()
        metadata.artist = msg.data.getString(MediaMetadata.METADATA_KEY_ARTIST)
        metadata.track = msg.data.getString(MediaMetadata.METADATA_KEY_TITLE)
        val body = ListenSubmitBody()
        body.addListen(msg.data.getLong(timestamp), metadata)
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

    fun submitListen(artist: String?, title: String?, timestamp: Long) {
        val message = obtainMessage()
        val data = Bundle()
        data.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
        data.putString(MediaMetadata.METADATA_KEY_TITLE, title)
        data.putLong(this.timestamp, timestamp)
        message.what = timestamp.toInt()
        message.data = data
        sendMessageDelayed(message, delay.toLong())
    }

    fun cancelListen(timestamp: Long) {
        removeMessages(timestamp.toInt())
    }
}