package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.BuildConfig
import java.util.*

class ListenSubmitBody {
    @SerializedName("listen_type")
    var listenType: String? = "single"
    @JvmField
    var payload: MutableList<Payload> = ArrayList()
    
    fun addListen(timestamp: Long?, metadata: ListenTrackMetadata) {
        payload.add(Payload(timestamp = timestamp, metadata = metadata).setClientDetails())
    }

    private fun Payload.setClientDetails(): Payload{
        this.metadata.additionalInfo.submission_client = "ListenBrainz Android"
        this.metadata.additionalInfo.submission_client_version = BuildConfig.VERSION_NAME
        return this
    }
    
    override fun toString(): String {
        return "ListenSubmitBody{" +
                "listenType='" + listenType + '\'' +
                ", payload=" + payload +
                '}'
    }

    class Payload(
            @SerializedName("listened_at") var timestamp: Long?,
            @SerializedName("track_metadata") var metadata: ListenTrackMetadata
        ) {

        override fun toString(): String {
            return "Payload{" +
                    "timestamp=" + timestamp +
                    ", metadata=" + metadata +
                    '}'
        }
    }
}