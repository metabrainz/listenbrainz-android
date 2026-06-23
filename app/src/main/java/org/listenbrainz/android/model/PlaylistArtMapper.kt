package org.listenbrainz.android.model

import org.listenbrainz.android.R


fun getPlaylistArtMapper(art: String): Int{
    return when(art){
        "ic_queue_music" -> R.drawable.ic_queue_music
        "ic_queue_music_playing" -> R.drawable.ic_queue_music_playing
        "ic_liked" -> R.drawable.ic_liked
        else -> R.drawable.ic_queue_music
    }
}