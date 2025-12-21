package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.listenbrainz.android.R

@Serializable
@Entity(tableName = "PLAYLISTS")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val items: List<SongEntity> = emptyList(),
    @DrawableRes val art: Int = R.drawable.ic_queue_music
)