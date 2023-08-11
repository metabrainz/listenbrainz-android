package org.listenbrainz.android.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

class ListenSubmitBody {
    @SerializedName("listen_type")
    var listenType: String? = "single"
    @JvmField
    var payload: MutableList<Payload> = ArrayList()
    
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

    @Entity(tableName = "PENDING_LISTENS")
    class Payload(
        
        @SerializedName("listened_at")
        @ColumnInfo(name = "listened_at")
        @PrimaryKey
        var timestamp: Long?,
        
        @SerializedName("track_metadata")
        @Embedded
        var metadata: ListenTrackMetadata
    ) {
        override fun toString(): String {
            return "Payload{" +
                    "timestamp=" + timestamp +
                    ", metadata=" + metadata +
                    '}'
        }
        
    }
}