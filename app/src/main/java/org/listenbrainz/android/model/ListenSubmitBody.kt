package org.listenbrainz.android.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class ListenSubmitBody {
    @SerialName("listen_type")
    var listenType: String? = "single"
    var payload: MutableList<Payload> = ArrayList()
        private set
    
    fun addListens(vararg listens: Payload, listensList: List<Payload> = emptyList()): ListenSubmitBody {
        listensList.forEach { payload.add(it) }
        listens.forEach { payload.add(it) }
        return this
    }
    
    
    override fun toString(): String {
        return "ListenSubmitBody{" +
                "listenType='" + listenType + '\'' +
                ", payload=" + payload +
                '}'
    }

    @Serializable
    @Entity(tableName = "PENDING_LISTENS")
    class Payload(
        
        @SerialName("listened_at")
        @ColumnInfo(name = "listened_at")
        @PrimaryKey
        var timestamp: Long? = null,
        
        @SerialName("track_metadata")
        @Embedded
        var metadata: ListenTrackMetadata = ListenTrackMetadata()
    ) {
        override fun toString(): String {
            return "Payload{" +
                    "timestamp=" + timestamp +
                    ", metadata=" + metadata +
                    '}'
        }
        
    }
}