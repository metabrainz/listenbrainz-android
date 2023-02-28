package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.AlbumDao
import org.listenbrainz.android.util.Transformer.toAlbum
import org.listenbrainz.android.util.Transformer.toAlbumEntity
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.util.AlbumsData
import org.listenbrainz.android.util.SongData
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao
): AlbumRepository {
    override fun getAlbums(): Flow<List<Album>> =
        albumDao.getAlbumEntities()
            .map { it ->
                it.map {
                    it.toAlbum()
                }
            }

    override fun getAlbum(albumId: Long): Flow<Album> =
        albumDao.getAlbumEntity(albumId)
            .map { it.toAlbum() }


    override suspend fun addAlbums(): Boolean {
        val albums = AlbumsData().fetchAlbums().map {
            it.toAlbumEntity()
        }
        albumDao.addAlbums(albums)
        return albums.isNotEmpty()
    }

    override fun getAllSongsOfAlbum(albumId: Long): Flow<List<Song>> {
        val songs = SongData().fetchSongs().filter { song ->
            song.albumID == albumId
        }
        return flowOf(songs)
    }
}