package org.listenbrainz.android.data.sources.brainzplayer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ARTISTS")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val artistID: Long = 0,
    val name: String,
    val songs: MutableList<SongEntity>,
    val albums: MutableList<AlbumEntity>
)