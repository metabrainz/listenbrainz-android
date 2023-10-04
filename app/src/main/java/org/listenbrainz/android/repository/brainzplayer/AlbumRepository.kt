package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Song

interface AlbumRepository {
    fun getAlbums() : Flow<List<Album>>
    fun getAlbum(albumId: Long) : Flow<Album>
    fun getAllSongsOfAlbum(albumId: Long): Flow<List<Song>>
    suspend fun addAlbums(userRequestedRefresh: Boolean = false) : Boolean
}