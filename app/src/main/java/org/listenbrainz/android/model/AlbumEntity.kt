package org.listenbrainz.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ALBUMS")
data class AlbumEntity(
    @PrimaryKey
    val albumId: Long,
    val title: String ,
    val artist: String ,
    val albumArt: String
)