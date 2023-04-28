package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.BuildConfig
import java.util.*

class ListenSubmitBody {
    @SerializedName("listen_type")
    var listenType: String? = null
    @JvmField
    var payload: MutableList<Payload> = ArrayList()
    fun getPayload(): List<Payload> {
        return payload
    }

    fun setPayload(payload: MutableList<Payload>) {
        this.payload = payload
    }

    fun addListen(payload: Payload) {
        this.payload.add(payload)
    }

    fun addListen(timestamp: Long, metadata: ListenTrackMetadata) {
        payload.add(Payload(timestamp, metadata).setClientDetails())
    }

    private fun Payload.setClientDetails(): Payload{
        this.metadata.additionalInfo.submission_client = BuildConfig.APPLICATION_ID
        this.metadata.additionalInfo.submission_client_version = BuildConfig.VERSION_NAME
        return this
    }
    
    override fun toString(): String {
        return "ListenSubmitBody{" +
                "listenType='" + listenType + '\'' +
                ", payload=" + payload +
                '}'
    }

    class Payload(@field:SerializedName("listened_at") var timestamp: Long, @field:SerializedName("track_metadata") var metadata: ListenTrackMetadata) {

        override fun toString(): String {
            return "Payload{" +
                    "timestamp=" + timestamp +
                    ", metadata=" + metadata +
                    '}'
        }
    }
}