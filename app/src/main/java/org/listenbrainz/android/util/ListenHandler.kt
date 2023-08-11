package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants.Strings.TIMESTAMP
import org.listenbrainz.android.util.Log.d

class ListenHandler(val appPreferences: AppPreferences, val repository: ListensRepository) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        CoroutineScope(Dispatchers.Default).launch {
            val token = appPreferences.getLbAccessToken()
            if (token.isEmpty()) {
                d("ListenBrainz User token has not been set!")
                return@launch
            }
            if(msg.data.getInt(MediaMetadata.METADATA_KEY_DURATION) <= 30000) {
                d("Track is too short to submit")
                return@launch
            }
            val metadata = ListenTrackMetadata(
                artist = msg.data.getString(MediaMetadata.METADATA_KEY_ARTIST),
                track = msg.data.getString(MediaMetadata.METADATA_KEY_TITLE),
                release = msg.data.getString(MediaMetadata.METADATA_KEY_ALBUM),
                additionalInfo = AdditionalInfo(
                    durationMs = msg.data.getInt(MediaMetadata.METADATA_KEY_DURATION),
                    mediaPlayer = msg.data.getString(MediaMetadata.METADATA_KEY_WRITER)
                        ?.let { repository.getPackageLabel(it) },
                    submissionClient = "ListenBrainz Android",
                    submissionClientVersion = BuildConfig.VERSION_NAME
                )
            )
    
            val body = ListenSubmitBody()
            body.addListen(
                timestamp = if(msg.data.getString("TYPE") == "single") msg.data.getLong(TIMESTAMP) else null,
                metadata = metadata
            )
            
            body.listenType = msg.data.getString("TYPE")
            
            withContext(Dispatchers.IO){
                repository.submitListen(token, body)
            }
            
        }
        
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
