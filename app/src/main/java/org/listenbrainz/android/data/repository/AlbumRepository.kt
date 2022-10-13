package org.listenbrainz.android.data.repository

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Song

interface AlbumRepository {
    fun getAlbums() : Flow<List<Album>>
    fun getAlbum(albumId: Long) : Flow<Album>
    fun getAllSongsOfAlbum(albumId: Long): Flow<List<Song>>
    suspend fun addAlbums() : Boolean
}