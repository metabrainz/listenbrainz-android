package org.listenbrainz.android.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.MediaItem

interface MusicSource<T> {
    var songs: List<T>
    fun asMediaSource() : MutableList<MediaItem>
    fun asMediaItem(): MutableList<MediaBrowserCompat.MediaItem>
    fun whenReady(action: (Boolean) -> Unit): Boolean
    suspend fun setMediaSource(newMediaSource: MutableList<MediaMetadataCompat>)
}