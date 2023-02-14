package org.listenbrainz.android.data.di

import android.graphics.Bitmap
import org.listenbrainz.android.data.sources.brainzplayer.Song

data class Card(val heading: String, val content: String, val image: Int)

data class user_profile(val name: String, val time: Long, val image: Int?)

data class TotalListens(val lastListen: Song, val total: Int){
    companion object {
        val emptyListen = TotalListens(Song.emptySong,0)
    }
}