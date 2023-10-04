package org.listenbrainz.android.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.State
import org.listenbrainz.android.repository.brainzplayer.AlbumRepository
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import javax.inject.Inject

class LocalMusicSource @Inject constructor(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: AlbumRepository,
    private val playlistRepository: PlaylistRepository
) :
    MusicSource<MediaMetadataCompat> {

    override var songs = emptyList<MediaMetadataCompat>()

    override fun asMediaSource(): MutableList<MediaItem> {
        val listOfMediaItem = mutableListOf<MediaItem>()
        songs.forEach { song ->
            val mediaItem =
                MediaItem.fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
            listOfMediaItem.add(mediaItem)
        }
        return listOfMediaItem
    }

    override fun asMediaItem() = songs.map { song ->
        val mediaUri = song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI) ?: "null"
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(mediaUri.toUri())
            .setTitle(song.description.title)
            .setMediaId(song.description.mediaId)
            .setSubtitle(song.description.subtitle)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    override fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == State.STATE_INITIALIZED)
            true
        }
    }

    override suspend fun setMediaSource(newMediaSource: MutableList<MediaMetadataCompat>) {
        fetchMediaData(newMediaSource)
    }

    private suspend fun fetchMediaData(newMediaSource: MutableList<MediaMetadataCompat>) = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING
        songs = newMediaSource
        state = State.STATE_INITIALIZED
    }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }
}