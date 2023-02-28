package org.listenbrainz.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SONGS")
data class SongEntity(
    @PrimaryKey
    val mediaID : Long,
    val title : String,
    val artist: String ,
    val uri : String ,
    val albumID: Long ,
    val album: String ,
    val albumArt: String,
    val trackNumber : Int,
    val year : Int,
    val duration : Long,
    val dateModified : Long,
    val artistId : Long,
    val discNumber : Long
)