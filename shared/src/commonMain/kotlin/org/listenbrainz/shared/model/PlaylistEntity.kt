package org.listenbrainz.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "PLAYLISTS")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val items: List<SongEntity> = emptyList(),
    val art: String = "ic_queue_music"
)