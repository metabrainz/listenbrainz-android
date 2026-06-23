package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.Song
import kotlin.random.Random

@Serializable
data class Playlist(
    val id: Long = Random.nextLong(),
    val title: String = "",
    val items: List<Song> = listOf(),
    val art: String = "ic_queue_music"
) {
    companion object {
        val currentlyPlaying = Playlist(
            id = -1,
            title = "Currently Playing",
            items = emptyList(),
            art = "ic_queue_music_playing"
        )

        val favourite = Playlist(
            id = 0,
            title = "Favourite",
            items = emptyList(),
            art = "ic_liked"
        )

        val recentlyPlayed = Playlist(
            id = 1,
            title = "Recently Played",
            items = emptyList()
        )
    }
}