package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import org.listenbrainz.android.R
import kotlin.random.Random

data class Playlist(
    var id: Long = Random.nextLong(),
    var title: String = "",
    var items: List<Song> = listOf(),
    @DrawableRes val art: Int = R.drawable.ic_queue_music
) {
    companion object {
        val currentlyPlaying = Playlist(
            id = -1,
            title = "Currently Playing",
            items = emptyList(),
            art = R.drawable.ic_queue_music_playing
        )

        val favourite = Playlist(
            id = 0,
            title = "Favourite",
            items = emptyList(),
            art = R.drawable.ic_liked
        )

        val recentlyPlayed = Playlist(
            id = 1,
            title = "Recently Played",
            items = emptyList()
        )
    }
}