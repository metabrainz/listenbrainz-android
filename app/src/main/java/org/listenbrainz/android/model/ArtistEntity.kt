package org.listenbrainz.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ARTISTS")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val artistID: Long = 0,
    val name: String,
    var songs: List<SongEntity>,
    var albums: List<AlbumEntity>
)