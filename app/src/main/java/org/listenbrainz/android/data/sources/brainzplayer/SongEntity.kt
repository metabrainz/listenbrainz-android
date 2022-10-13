package org.listenbrainz.android.data.sources.brainzplayer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SONGS")
data class SongEntity(
    @PrimaryKey
    val mediaID : String,
    val title : String,
    val artist: String ,
    val uri : String ,
    val albumID: String ,
    val album: String ,
    val albumArt: String
)