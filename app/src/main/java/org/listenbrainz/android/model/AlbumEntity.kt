package org.listenbrainz.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "ALBUMS")
data class AlbumEntity(
    @PrimaryKey
    val albumId: Long = 0L,
    val title: String = "",
    val artist: String = "",
    val albumArt: String = ""
)