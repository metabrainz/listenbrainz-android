package org.listenbrainz.android.presentation.features.brainzplayer.musicsource

import android.support.v4.media.MediaBrowserCompat
import com.google.android.exoplayer2.MediaItem

interface MusicSource<T> {
    var songs: List<T>
    fun asMediaSource() : MutableList<MediaItem>
    fun asMediaItem(): MutableList<MediaBrowserCompat.MediaItem>
    fun whenReady(action: (Boolean) -> Unit): Boolean
}