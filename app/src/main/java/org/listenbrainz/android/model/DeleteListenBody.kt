package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class DeleteListenBody {

    @SerializedName("listens")
    var listens: MutableList<ListenToDelete> = ArrayList()

    // Helper to easily add a listen to the list
    fun addListen(timestamp: Long, recordingMsid: String): DeleteListenBody {
        listens.add(ListenToDelete(timestamp, recordingMsid))
        return this
    }

    override fun toString(): String {
        return "DeleteListenBody{" +
                "listens=" + listens +
                '}'
    }

    class ListenToDelete(
        @SerializedName("listened_at")
        var listenedAt: Long,

        @SerializedName("recording_msid")
        var recordingMsid: String
    ) {
        override fun toString(): String {
            return "ListenToDelete{" +
                    "listenedAt=" + listenedAt +
                    ", recordingMsid='" + recordingMsid + '\'' +
                    '}'
        }
    }
}
