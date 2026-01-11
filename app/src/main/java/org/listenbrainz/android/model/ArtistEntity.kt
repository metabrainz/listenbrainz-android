package org.listenbrainz.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "ARTISTS")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val artistID: Long = 0,
    val name: String = "",
    var songs: List<SongEntity> = emptyList(),
    var albums: List<AlbumEntity> = emptyList()
)